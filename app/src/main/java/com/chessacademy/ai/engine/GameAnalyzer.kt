package com.chessacademy.ai.engine

import com.chessacademy.ai.chess.ChessGame
import kotlin.math.exp
import kotlin.math.max

enum class MoveClass(val label: String, val symbol: String) {
    BEST("Bester Zug", "★"), GOOD("Gut", "✓"), INACCURACY("Ungenauigkeit", "?!"), MISTAKE("Fehler", "?"), BLUNDER("Patzer", "??")
}

/** Bewertung einer Stellung aus Weiß-Sicht plus bester Zug (UCI/SAN). */
data class PositionEval(val whiteCp: Int, val bestUci: String?, val label: String)

data class PlyAnalysis(
    val ply: Int,               // 1-basiert: Halbzug-Nummer
    val san: String,
    val uci: String,
    val whiteMoved: Boolean,
    val before: PositionEval,
    val after: PositionEval,
    val bestSan: String?,
    val winPercentLoss: Double,
    val moveClass: MoveClass,
    /** Chance verpasst: eine klar gewonnene Stellung wurde nicht verwertet. */
    val missedChance: Boolean
)

data class GameAnalysis(val plies: List<PlyAnalysis>, val accuracyWhite: Double, val accuracyBlack: Double) {
    fun count(white: Boolean, c: MoveClass) = plies.count { it.whiteMoved == white && it.moveClass == c }
}

/** Analysiert eine Partie Zug für Zug. [evaluate] liefert Stellungsbewertung (Weiß-Sicht) und besten Zug. */
class GameAnalyzer(private val evaluate: suspend (fen: String) -> PositionEval?) {

    suspend fun analyze(game: ChessGame, onProgress: (Int, Int) -> Unit = { _, _ -> }): GameAnalysis? {
        val n = game.moves.size
        if (n == 0) return GameAnalysis(emptyList(), 100.0, 100.0)
        val evals = ArrayList<PositionEval>(n + 1)
        for (i in 0..n) {
            val pos = game.positionAt(i)
            val e = evaluate(pos.toFen()) ?: return null
            evals.add(e)
            onProgress(i + 1, n + 1)
        }
        val plies = ArrayList<PlyAnalysis>()
        for (i in 0 until n) {
            val m = game.moves[i]
            val white = m.piece.white
            val before = evals[i]
            val after = evals[i + 1]
            // Endstellungen (Matt) haben keinen besten Zug: Bewertung auf ±10000 setzen
            val bCp = if (white) before.whiteCp else -before.whiteCp
            val aCp = if (white) after.whiteCp else -after.whiteCp
            val loss = max(0.0, EngineScore.winPercent(bCp) - EngineScore.winPercent(aCp))
            val cls = when {
                m.uci == before.bestUci -> MoveClass.BEST
                loss < 5.0 -> MoveClass.GOOD
                loss < 10.0 -> MoveClass.INACCURACY
                loss < 15.0 -> MoveClass.MISTAKE
                else -> MoveClass.BLUNDER
            }
            val missed = bCp >= 250 && aCp < 100 && loss >= 10.0
            val bestSan = before.bestUci?.let { sanFor(game, i, it) }
            plies.add(PlyAnalysis(i + 1, m.san, m.uci, white, before, after, bestSan, loss, cls, missed))
        }
        return GameAnalysis(plies, accuracy(plies, true), accuracy(plies, false))
    }

    private fun sanFor(game: ChessGame, ply: Int, uci: String): String? {
        return try {
            val g = ChessGame(game.positionAt(ply).toFen())
            g.playUci(uci)?.san
        } catch (e: IllegalArgumentException) { null }
    }

    private fun accuracy(plies: List<PlyAnalysis>, white: Boolean): Double {
        val mine = plies.filter { it.whiteMoved == white }
        if (mine.isEmpty()) return 100.0
        val avg = mine.map { (103.1668 * exp(-0.04354 * it.winPercentLoss) - 3.1669).coerceIn(0.0, 100.0) }.average()
        return avg
    }
}

/** Verbindet [StockfishRunner] mit dem Analysator; behandelt beendete Partien ohne Engine. */
class EngineEvaluator(private val runner: StockfishRunner, private val depth: Int = 12) {
    suspend fun evaluate(fen: String): PositionEval? {
        val g = try { ChessGame(fen) } catch (e: IllegalArgumentException) { return null }
        val st = g.status()
        val white = g.whiteToMove
        val result = st.result
        if (result != null) {
            val cp = when (result) {
                is com.chessacademy.ai.chess.GameResult.Checkmate -> if (result.whiteWins) 10000 else -10000
                else -> 0
            }
            return PositionEval(cp, null, if (cp == 0) "0.00" else if (cp > 0) "1-0" else "0-1")
        }
        val res = runner.search(fen, depth, 1, 20) ?: return null
        val line = res.lines.firstOrNull() ?: return null
        val cpWhite = if (white) line.score.asCp else -line.score.asCp
        return PositionEval(cpWhite, res.bestMove ?: line.pv.firstOrNull(), line.score.label(white))
    }
}
