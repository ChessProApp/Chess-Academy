package com.chessacademy.ai.chess

/**
 * Plattformunabhängige Grundtypen (keine Android-Abhängigkeit, JVM-testbar).
 * Felder sind Ints: a1 = 0, b1 = 1, ... h1 = 7, a2 = 8, ... h8 = 63.
 */
enum class PieceKind(val letter: Char, val value: Int, val germanName: String) {
    PAWN('P', 1, "Bauer"),
    KNIGHT('N', 3, "Springer"),
    BISHOP('B', 3, "Läufer"),
    ROOK('R', 5, "Turm"),
    QUEEN('Q', 9, "Dame"),
    KING('K', 0, "König");

    companion object {
        fun fromLetter(c: Char): PieceKind? = entries.firstOrNull { it.letter == c.uppercaseChar() }
    }
}

data class Pc(val kind: PieceKind, val white: Boolean) {
    val fenChar: Char get() = if (white) kind.letter else kind.letter.lowercaseChar()

    companion object {
        fun fromFen(c: Char): Pc? {
            val k = PieceKind.fromLetter(c) ?: return null
            return Pc(k, c.isUpperCase())
        }
    }
}

object Sq {
    fun of(file: Int, rank: Int): Int = rank * 8 + file
    fun file(sq: Int): Int = sq and 7
    fun rank(sq: Int): Int = sq shr 3
    fun name(sq: Int): String = "${'a' + file(sq)}${'1' + rank(sq)}"
    fun parse(s: String): Int {
        if (s.length != 2) return -1
        val f = s[0] - 'a'
        val r = s[1] - '1'
        return if (f in 0..7 && r in 0..7) of(f, r) else -1
    }
    /** a1 ist ein dunkles Feld. */
    fun isLight(sq: Int): Boolean = (file(sq) + rank(sq)) % 2 == 1
}

const val START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

/** Unveränderliche Stellungsansicht (für UI und Lektionen). */
class Position(
    val squares: Array<Pc?>,
    val whiteToMove: Boolean,
    val castling: String,
    val epSquare: Int,
    val halfmove: Int,
    val fullmove: Int
) {
    fun pieceAt(sq: Int): Pc? = squares[sq]

    fun kingSquare(white: Boolean): Int =
        squares.indices.firstOrNull { squares[it] == Pc(PieceKind.KING, white) } ?: -1

    fun toFen(): String {
        val sb = StringBuilder()
        for (rank in 7 downTo 0) {
            var empty = 0
            for (file in 0..7) {
                val p = squares[Sq.of(file, rank)]
                if (p == null) empty++ else {
                    if (empty > 0) { sb.append(empty); empty = 0 }
                    sb.append(p.fenChar)
                }
            }
            if (empty > 0) sb.append(empty)
            if (rank > 0) sb.append('/')
        }
        sb.append(' ').append(if (whiteToMove) 'w' else 'b')
        sb.append(' ').append(if (castling.isEmpty()) "-" else castling)
        sb.append(' ').append(if (epSquare < 0) "-" else Sq.name(epSquare))
        sb.append(' ').append(halfmove).append(' ').append(fullmove)
        return sb.toString()
    }

    /** Material-Summe (ohne König) je Seite. */
    fun material(white: Boolean): Int =
        squares.sumOf { p -> if (p != null && p.white == white) p.kind.value else 0 }

    companion object {
        /** Liest ein FEN (tolerant bei fehlenden Zählern). Wirft IllegalArgumentException bei Syntaxfehlern. */
        fun fromFen(fen: String): Position {
            val parts = fen.trim().split(Regex("\\s+"))
            require(parts.size >= 2) { "FEN unvollständig" }
            val rows = parts[0].split('/')
            require(rows.size == 8) { "FEN braucht 8 Reihen" }
            val squares = arrayOfNulls<Pc>(64)
            for ((i, row) in rows.withIndex()) {
                val rank = 7 - i
                var file = 0
                for (c in row) {
                    if (c.isDigit()) file += c - '0'
                    else {
                        val p = Pc.fromFen(c) ?: throw IllegalArgumentException("Ungültige Figur '$c'")
                        require(file in 0..7) { "Reihe zu lang" }
                        squares[Sq.of(file, rank)] = p
                        file++
                    }
                }
                require(file == 8) { "Reihe hat nicht 8 Felder" }
            }
            val white = when (parts[1]) { "w" -> true; "b" -> false; else -> throw IllegalArgumentException("Seite am Zug fehlt") }
            val castling = parts.getOrNull(2)?.takeIf { it != "-" } ?: ""
            val ep = parts.getOrNull(3)?.takeIf { it != "-" }?.let { Sq.parse(it) } ?: -1
            val half = parts.getOrNull(4)?.toIntOrNull() ?: 0
            val full = parts.getOrNull(5)?.toIntOrNull() ?: 1
            return Position(squares, white, castling, ep, half, full)
        }
    }
}

enum class CastleSide { KING_SIDE, QUEEN_SIDE }

data class PlayedMove(
    val from: Int,
    val to: Int,
    val promotion: PieceKind?,
    val piece: Pc,
    val captured: Pc?,
    val isEnPassant: Boolean,
    val castle: CastleSide?,
    val san: String,
    val givesCheck: Boolean,
    val givesMate: Boolean
) {
    val uci: String get() = Sq.name(from) + Sq.name(to) + (promotion?.letter?.lowercaseChar()?.toString() ?: "")
    val isCapture: Boolean get() = captured != null
    /** Feld des geschlagenen Bauern bei En passant, sonst das Zielfeld. */
    val captureSquare: Int get() = if (isEnPassant) Sq.of(Sq.file(to), Sq.rank(from)) else to
}

enum class DrawReason(val automatic: Boolean, val label: String) {
    STALEMATE(true, "Patt"),
    INSUFFICIENT_MATERIAL(true, "Unzureichendes Material"),
    FIVEFOLD_REPETITION(true, "Fünffache Stellungswiederholung"),
    SEVENTYFIVE_MOVE(true, "75-Züge-Regel"),
    THREEFOLD_REPETITION(false, "Dreifache Stellungswiederholung"),
    FIFTY_MOVE(false, "50-Züge-Regel"),
    AGREEMENT(false, "Remis vereinbart")
}

sealed class GameResult {
    data class Checkmate(val whiteWins: Boolean) : GameResult()
    data class Draw(val reason: DrawReason, val claimed: Boolean = false) : GameResult()
    data class Resignation(val whiteWins: Boolean) : GameResult()

    val pgnToken: String
        get() = when (this) {
            is Checkmate -> if (whiteWins) "1-0" else "0-1"
            is Resignation -> if (whiteWins) "1-0" else "0-1"
            is Draw -> "1/2-1/2"
        }
}

/** Zustand der aktuellen Stellung. [claimable] sind Remis-Arten, die ein Spieler reklamieren dürfte. */
data class GameStatus(
    val inCheck: Boolean,
    val result: GameResult?,
    val claimable: List<DrawReason>
) {
    val isOver: Boolean get() = result != null
}

/** Begründung, warum ein Zug nicht erlaubt ist (für verständliche Fehlermeldungen). */
enum class IllegalReason(val message: String) {
    NOT_YOUR_PIECE("Das ist keine Figur der Seite, die am Zug ist."),
    NO_PIECE("Auf diesem Feld steht keine Figur."),
    GEOMETRY("So kann sich diese Figur nicht bewegen."),
    BLOCKED("Der Weg ist durch eine andere Figur versperrt."),
    OWN_PIECE("Das Zielfeld ist von einer eigenen Figur besetzt."),
    KING_ATTACKED_SQUARE("Der König darf nicht auf ein bedrohtes Feld ziehen."),
    PINNED("Diese Figur ist gefesselt – ihr König stünde sonst im Schach."),
    STILL_IN_CHECK("Dein König steht im Schach. Dieser Zug beendet das Schach nicht."),
    CASTLE_THROUGH_CHECK("Rochade nicht möglich: Der König würde über ein bedrohtes Feld ziehen oder stünde im Schach."),
    CASTLE_RIGHTS("Rochade nicht mehr erlaubt: König oder Turm haben sich schon bewegt."),
    CASTLE_BLOCKED("Rochade nicht möglich: Zwischen König und Turm steht noch eine Figur."),
    GAME_OVER("Die Partie ist beendet.")
}
