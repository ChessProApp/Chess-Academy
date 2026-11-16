package com.chessacademy.ai.chess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.abs

/**
 * Regelschicht über chesslib.
 *
 * chesslib liefert die Zuggenerierung (legale Züge inkl. Rochade, En passant, Umwandlung, Fesselung).
 * Alles, was chesslib nicht zuverlässig kann oder was in der App eigene Semantik braucht, liegt hier:
 *  - Stellungshistorie und Wiederholungszählung (3x reklamierbar, 5x automatisch)
 *  - 50-Züge-Regel (reklamierbar) und 75-Züge-Regel (automatisch)
 *  - unzureichendes Material (tote Stellung)
 *  - Matt/Patt-Vorrang, Aufgabe, Remis-Reklamation
 *  - SAN-Erzeugung / -Parsing, Zugrücknahme, verständliche Fehlerbegründungen
 *
 * [autoDraws] = false schaltet die automatischen Remisfälle (totes Material, 5x, 75 Züge) ab –
 * nötig für Lehr- und Sandbox-Stellungen mit sehr wenig Material.
 *
 * Nicht thread-safe: Zugriff nur von einem Thread (ViewModel/Main).
 */
class ChessGame(val startFen: String = START_FEN, private val autoDraws: Boolean = true) {

    private val board = Board()
    private val played = ArrayList<PlayedMove>()
    private val positions = ArrayList<Position>()
    private val keys = ArrayList<String>()
    private var legalCache: List<Move>? = null
    private var resultOverride: GameResult? = null
    private var statusCache: GameStatus? = null

    init {
        val problem = validate(startFen)
        require(problem == null) { problem ?: "" }
        board.loadFromFen(startFen)
        positions.add(Position.fromFen(startFen))
        legalCache = null
        keys.add(positionKey())
    }

    // ---------------------------------------------------------------- Zustand

    val moves: List<PlayedMove> get() = played
    val ply: Int get() = played.size
    val position: Position get() = positions.last()
    fun positionAt(ply: Int): Position = positions[ply.coerceIn(0, positions.lastIndex)]
    val whiteToMove: Boolean get() = position.whiteToMove
    val fen: String get() = position.toFen()
    val inCheck: Boolean get() = board.isKingAttacked
    val lastMove: PlayedMove? get() = played.lastOrNull()
    val isOver: Boolean get() = status().isOver

    private fun legal(): List<Move> {
        legalCache?.let { return it }
        val fresh: List<Move> = board.legalMoves()
        legalCache = fresh
        return fresh
    }

    fun legalMoveCount(): Int = legal().size

    private fun finished(): Boolean = status().isOver

    /** Alle Zielfelder der Figur auf [from] (Umwandlungen nur einmal). */
    fun legalTargets(from: Int): List<Int> {
        if (finished()) return emptyList()
        val fromSq = Square.squareAt(from)
        return legal().filter { it.from == fromSq }.map { idx(it.to) }.distinct()
    }

    /** Alle Felder, von denen aus die Seite am Zug ziehen kann. */
    fun movableSquares(): Set<Int> =
        if (finished()) emptySet() else legal().map { idx(it.from) }.toSet()

    fun isLegal(from: Int, to: Int): Boolean = findLegal(from, to, null, anyPromotion = true) != null

    fun needsPromotion(from: Int, to: Int): Boolean {
        if (finished()) return false
        val fromSq = Square.squareAt(from)
        val toSq = Square.squareAt(to)
        return legal().any { it.from == fromSq && it.to == toSq && it.promotion != Piece.NONE }
    }

    private fun findLegal(from: Int, to: Int, promo: PieceKind?, anyPromotion: Boolean = false): Move? {
        if (from !in 0..63 || to !in 0..63 || finished()) return null
        val fromSq = Square.squareAt(from)
        val toSq = Square.squareAt(to)
        for (m in legal()) {
            if (m.from != fromSq || m.to != toSq) continue
            val mp = m.promotion
            if (mp == Piece.NONE) return m
            if (anyPromotion) return m
            if (promo != null && kindOf(mp) == promo) return m
        }
        return null
    }

    // ---------------------------------------------------------------- Züge ausführen

    /**
     * Führt einen Zug aus. Gibt null zurück, wenn er illegal ist (oder bei Umwandlung die Figur fehlt –
     * dann vorher [needsPromotion] fragen).
     */
    fun play(from: Int, to: Int, promotion: PieceKind? = null): PlayedMove? {
        val m = findLegal(from, to, promotion) ?: return null
        return apply(m)
    }

    /** UCI-Zug wie "e2e4" oder "e7e8q". Fehlt bei einer Umwandlung der Buchstabe, wird Dame gewählt. */
    fun playUci(uci: String): PlayedMove? {
        if (uci.length < 4) return null
        val from = Sq.parse(uci.substring(0, 2))
        val to = Sq.parse(uci.substring(2, 4))
        if (from < 0 || to < 0) return null
        val promo = uci.getOrNull(4)?.let { PieceKind.fromLetter(it) }
        val effective = promo ?: if (needsPromotion(from, to)) PieceKind.QUEEN else null
        return play(from, to, effective)
    }

    /** SAN-Zug wie "Nf3", "exd5", "O-O", "e8=Q+". */
    fun playSan(text: String): PlayedMove? {
        val wanted = normalizeSan(text)
        if (wanted.isEmpty()) return null
        val pos = position
        val list = legal()
        var loose: Move? = null
        for (m in list) {
            val base = sanBase(pos, list, m)
            if (base == wanted) return apply(m)
            if (loose == null && looseSan(base) == looseSan(wanted)) loose = m
        }
        return loose?.let { apply(it) }
    }

    private fun apply(m: Move): PlayedMove {
        val pos = position
        val list = legal()
        val from = idx(m.from)
        val to = idx(m.to)
        val piece = pos.squares[from]!!
        var captured = pos.squares[to]
        val isEp = piece.kind == PieceKind.PAWN && Sq.file(from) != Sq.file(to) && captured == null
        if (isEp) captured = pos.squares[Sq.of(Sq.file(to), Sq.rank(from))]
        val castle = castleOf(piece, from, to)
        val promo = if (m.promotion == Piece.NONE) null else kindOf(m.promotion)
        val base = sanBase(pos, list, m)

        board.doMove(m)
        legalCache = null
        statusCache = null
        val check = board.isKingAttacked
        val noMoves = legal().isEmpty()
        val mate = check && noMoves

        val san = base + if (mate) "#" else if (check) "+" else ""
        val pm = PlayedMove(from, to, promo, piece, captured, isEp, castle, san, check, mate)
        played.add(pm)

        val reset = piece.kind == PieceKind.PAWN || captured != null
        val half = if (reset) 0 else pos.halfmove + 1
        val full = if (pos.whiteToMove) pos.fullmove else pos.fullmove + 1
        val parsed = Position.fromFen(board.fen)
        val next = Position(parsed.squares, parsed.whiteToMove, parsed.castling, parsed.epSquare, half, full)
        positions.add(next)
        keys.add(positionKey())
        statusCache = null
        return pm
    }

    /** Nimmt den letzten Halbzug zurück. */
    fun undo(): PlayedMove? {
        if (played.isEmpty()) return null
        val pm = played.removeAt(played.lastIndex)
        positions.removeAt(positions.lastIndex)
        keys.removeAt(keys.lastIndex)
        board.loadFromFen(positions.last().toFen())
        legalCache = null
        statusCache = null
        resultOverride = null
        return pm
    }

    /** Schneidet die Partie auf [targetPly] Halbzüge zurück. */
    fun truncate(targetPly: Int) {
        while (played.size > targetPly.coerceAtLeast(0)) undo()
    }

    // ---------------------------------------------------------------- Ergebnis

    fun resign(whiteResigns: Boolean) {
        if (!status().isOver) { resultOverride = GameResult.Resignation(whiteWins = !whiteResigns); statusCache = null }
    }

    fun agreeDraw() {
        if (!status().isOver) { resultOverride = GameResult.Draw(DrawReason.AGREEMENT, claimed = true); statusCache = null }
    }

    /** Reklamiert ein Remis (3x Wiederholung oder 50 Züge). Gibt false zurück, wenn kein Anspruch besteht. */
    fun claimDraw(): Boolean {
        val s = status()
        if (s.isOver) return false
        val reason = s.claimable.firstOrNull() ?: return false
        resultOverride = GameResult.Draw(reason, claimed = true)
        statusCache = null
        return true
    }

    /** Wie oft die aktuelle Stellung (Regelsinn: gleiche Figuren, Zugrecht, Rochade-/En-passant-Rechte) vorkam. */
    fun repetitionCount(): Int {
        val k = keys.last()
        var n = 0
        for (x in keys) if (x == k) n++
        return n
    }

    fun status(): GameStatus {
        statusCache?.let { return it }
        val fresh = computeStatus()
        statusCache = fresh
        return fresh
    }

    private fun computeStatus(): GameStatus {
        resultOverride?.let { return GameStatus(false, it, emptyList()) }
        val pos = position
        val check = board.isKingAttacked
        if (legal().isEmpty()) {
            return if (check) GameStatus(true, GameResult.Checkmate(whiteWins = !pos.whiteToMove), emptyList())
            else GameStatus(false, GameResult.Draw(DrawReason.STALEMATE), emptyList())
        }
        if (autoDraws && isInsufficientMaterial(pos)) return GameStatus(check, GameResult.Draw(DrawReason.INSUFFICIENT_MATERIAL), emptyList())
        val reps = repetitionCount()
        if (autoDraws && reps >= 5) return GameStatus(check, GameResult.Draw(DrawReason.FIVEFOLD_REPETITION), emptyList())
        if (autoDraws && pos.halfmove >= 150) return GameStatus(check, GameResult.Draw(DrawReason.SEVENTYFIVE_MOVE), emptyList())
        val claim = ArrayList<DrawReason>(2)
        if (reps >= 3) claim.add(DrawReason.THREEFOLD_REPETITION)
        if (pos.halfmove >= 100) claim.add(DrawReason.FIFTY_MOVE)
        return GameStatus(check, null, claim)
    }

    // ---------------------------------------------------------------- Felder / Angriffe

    /** Anzahl Figuren der Farbe [byWhite], die [sq] angreifen bzw. (bei eigener Figur) verteidigen. */
    fun attackers(sq: Int, byWhite: Boolean): Int =
        java.lang.Long.bitCount(board.squareAttackedBy(Square.squareAt(sq), if (byWhite) Side.WHITE else Side.BLACK))

    // ---------------------------------------------------------------- Fehlerbegründung

    fun explainIllegal(from: Int, to: Int): IllegalReason {
        if (status().isOver) return IllegalReason.GAME_OVER
        val pos = position
        val pc = pos.squares[from] ?: return IllegalReason.NO_PIECE
        if (pc.white != pos.whiteToMove) return IllegalReason.NOT_YOUR_PIECE
        val target = pos.squares[to]
        if (target != null && target.white == pc.white) return IllegalReason.OWN_PIECE

        val df = Sq.file(to) - Sq.file(from)
        val dr = Sq.rank(to) - Sq.rank(from)

        if (pc.kind == PieceKind.KING && dr == 0 && abs(df) == 2) {
            val homeRank = if (pc.white) 0 else 7
            if (Sq.rank(from) == homeRank && Sq.file(from) == 4) {
                val kingSide = df > 0
                val right = (if (kingSide) 'K' else 'Q').let { if (pc.white) it else it.lowercaseChar() }
                if (!pos.castling.contains(right)) return IllegalReason.CASTLE_RIGHTS
                val between = if (kingSide) listOf(5, 6) else listOf(1, 2, 3)
                if (between.any { pos.squares[Sq.of(it, homeRank)] != null }) return IllegalReason.CASTLE_BLOCKED
                return IllegalReason.CASTLE_THROUGH_CHECK
            }
        }

        val fromSq = Square.squareAt(from)
        val toSq = Square.squareAt(to)
        val pseudo = board.pseudoLegalMoves().any { it.from == fromSq && it.to == toSq }
        if (pseudo) {
            if (pc.kind == PieceKind.KING) return IllegalReason.KING_ATTACKED_SQUARE
            return if (board.isKingAttacked) IllegalReason.STILL_IN_CHECK else IllegalReason.PINNED
        }
        return if (shapeAllows(pc, from, to)) IllegalReason.BLOCKED else IllegalReason.GEOMETRY
    }

    private fun shapeAllows(pc: Pc, from: Int, to: Int): Boolean {
        val df = abs(Sq.file(to) - Sq.file(from))
        val dr = Sq.rank(to) - Sq.rank(from)
        val adr = abs(dr)
        return when (pc.kind) {
            PieceKind.KNIGHT -> false // Springer sind nie blockiert; passt die Form, wäre der Zug pseudo-legal
            PieceKind.KING -> df <= 1 && adr <= 1
            PieceKind.ROOK -> (df == 0) != (adr == 0)
            PieceKind.BISHOP -> df == adr && df != 0
            PieceKind.QUEEN -> ((df == 0) != (adr == 0)) || (df == adr && df != 0)
            PieceKind.PAWN -> {
                val dir = if (pc.white) 1 else -1
                val startRank = if (pc.white) 1 else 6
                df == 0 && (dr == dir || (dr == 2 * dir && Sq.rank(from) == startRank))
            }
        }
    }

    // ---------------------------------------------------------------- Hilfen

    private fun positionKey(): String {
        val pos = positions.last()
        val sb = StringBuilder(80)
        val fen = pos.toFen()
        sb.append(fen.substringBefore(' ')) // Figurenstellung
        sb.append(if (pos.whiteToMove) 'w' else 'b')
        sb.append(pos.castling.ifEmpty { "-" })
        // En passant zählt nur, wenn tatsächlich ein legaler Schlag möglich ist (FIDE Art. 9.2).
        var ep = "-"
        if (pos.epSquare >= 0) {
            val epSq = Square.squareAt(pos.epSquare)
            val possible = legal().any { m ->
                m.to == epSq && pos.squares[idx(m.from)]?.kind == PieceKind.PAWN &&
                    Sq.file(idx(m.from)) != Sq.file(pos.epSquare) && pos.squares[pos.epSquare] == null
            }
            if (possible) ep = Sq.name(pos.epSquare)
        }
        sb.append(ep)
        return sb.toString()
    }

    private fun sanBase(pos: Position, list: List<Move>, m: Move): String {
        val from = idx(m.from)
        val to = idx(m.to)
        val pc = pos.squares[from]!!
        val castle = castleOf(pc, from, to)
        if (castle == CastleSide.KING_SIDE) return "O-O"
        if (castle == CastleSide.QUEEN_SIDE) return "O-O-O"
        val isEp = pc.kind == PieceKind.PAWN && Sq.file(from) != Sq.file(to) && pos.squares[to] == null
        val capture = pos.squares[to] != null || isEp
        val sb = StringBuilder()
        if (pc.kind == PieceKind.PAWN) {
            if (capture) sb.append('a' + Sq.file(from)).append('x')
        } else {
            sb.append(pc.kind.letter)
            val others = list.filter {
                it !== m && idx(it.to) == to && idx(it.from) != from && pos.squares[idx(it.from)]?.kind == pc.kind
            }
            if (others.isNotEmpty()) {
                val sameFile = others.any { Sq.file(idx(it.from)) == Sq.file(from) }
                val sameRank = others.any { Sq.rank(idx(it.from)) == Sq.rank(from) }
                when {
                    !sameFile -> sb.append('a' + Sq.file(from))
                    !sameRank -> sb.append('1' + Sq.rank(from))
                    else -> sb.append(Sq.name(from))
                }
            }
            if (capture) sb.append('x')
        }
        sb.append(Sq.name(to))
        if (m.promotion != Piece.NONE) sb.append('=').append(kindOf(m.promotion).letter)
        return sb.toString()
    }

    companion object {
        private fun idx(s: Square): Int = s.ordinal

        private fun castleOf(pc: Pc, from: Int, to: Int): CastleSide? =
            if (pc.kind == PieceKind.KING && abs(Sq.file(to) - Sq.file(from)) == 2 && Sq.rank(to) == Sq.rank(from))
                if (Sq.file(to) > Sq.file(from)) CastleSide.KING_SIDE else CastleSide.QUEEN_SIDE
            else null

        private fun kindOf(p: Piece): PieceKind = when (p.pieceType) {
            PieceType.PAWN -> PieceKind.PAWN
            PieceType.KNIGHT -> PieceKind.KNIGHT
            PieceType.BISHOP -> PieceKind.BISHOP
            PieceType.ROOK -> PieceKind.ROOK
            PieceType.QUEEN -> PieceKind.QUEEN
            else -> PieceKind.KING
        }

        private fun normalizeSan(raw: String): String {
            var s = raw.trim()
            s = s.replace("0-0-0", "O-O-O").replace("0-0", "O-O")
            s = s.replace("e.p.", "").replace("ep", "")
            s = s.trimEnd('+', '#', '!', '?', ' ')
            return s.trim()
        }

        private fun looseSan(s: String): String = s.replace("x", "").replace("=", "").replace("-", "")

        /** Gleiche Figur-Prüfung für „tote Stellung“: König gegen König, +1 Leichtfigur, oder nur gleichfarbige Läufer. */
        fun isInsufficientMaterial(pos: Position): Boolean {
            var minors = 0
            var knights = 0
            val bishopSquareColors = HashSet<Boolean>()
            for (i in 0..63) {
                val p = pos.squares[i] ?: continue
                when (p.kind) {
                    PieceKind.PAWN, PieceKind.ROOK, PieceKind.QUEEN -> return false
                    PieceKind.KNIGHT -> { minors++; knights++ }
                    PieceKind.BISHOP -> { minors++; bishopSquareColors.add(Sq.isLight(i)) }
                    PieceKind.KING -> {}
                }
            }
            if (minors <= 1) return true
            return knights == 0 && bishopSquareColors.size == 1
        }

        /** Prüft ein FEN auf Syntax und Spielbarkeit. Gibt eine Fehlermeldung oder null zurück. */
        fun validate(fen: String): String? {
            val pos = try { Position.fromFen(fen) } catch (e: IllegalArgumentException) {
                return e.message ?: "Ungültiges FEN"
            }
            val wk = pos.squares.count { it == Pc(PieceKind.KING, true) }
            val bk = pos.squares.count { it == Pc(PieceKind.KING, false) }
            if (wk != 1 || bk != 1) return "Jede Seite braucht genau einen König."
            for (i in 0..63) {
                val p = pos.squares[i] ?: continue
                if (p.kind == PieceKind.PAWN && (Sq.rank(i) == 0 || Sq.rank(i) == 7)) return "Bauern dürfen nicht auf der 1. oder 8. Reihe stehen."
            }
            val b = Board()
            try { b.loadFromFen(fen) } catch (e: Exception) { return "FEN konnte nicht geladen werden." }
            // Die Seite, die NICHT am Zug ist, darf nicht im Schach stehen.
            val other = Board()
            try {
                val flipped = pos.toFen().split(' ').toMutableList()
                flipped[1] = if (pos.whiteToMove) "b" else "w"
                flipped[3] = "-"
                other.loadFromFen(flipped.joinToString(" "))
                if (other.isKingAttacked) return "Der König der Seite, die nicht am Zug ist, steht im Schach."
            } catch (e: Exception) { return "Stellung ungültig." }
            return null
        }
    }
}
