package com.chessacademy.ai.engine

import com.chessacademy.ai.chess.ChessGame
import com.chessacademy.ai.chess.Sq

/** Spielstärke: Stockfish-Skill (0..20) und Suchtiefe. Entspricht den bisherigen Stufen der App. */
enum class AiLevel(val label: String, val skill: Int, val depth: Int) {
    BEGINNER("Anfänger", 0, 1), EASY("Leicht", 4, 3), NORMAL("Normal", 8, 6),
    MEDIUM("Mittel", 12, 8), HARD("Schwer", 16, 11), EXPERT("Experte", 19, 14), MAX("Maximum", 20, 18)
}

/** Spielstil: wählt unter nahezu gleichwertigen Engine-Zügen (MultiPV) nach Charakter. */
enum class AiStyle(val label: String, val multiPv: Int, val toleranceCp: Int) {
    BALANCED("Ausgeglichen", 1, 0),
    AGGRESSIVE("Aggressiv", 3, 60),
    DEFENSIVE("Defensiv", 2, 40),
    RISKY("Risikofreudig", 4, 120)
}

object AiMoveSelector {
    /**
     * Wählt aus den MultiPV-Linien einen Zug. Nur Züge innerhalb der Toleranz zum besten Zug kommen infrage.
     * AGGRESSIVE: Schlagen und Schach bevorzugen. DEFENSIVE: ruhige Züge auf sichere Felder.
     * RISKY: Züge auf angegriffene Felder (Opfer/Komplikationen) bevorzugen.
     */
    fun choose(fen: String, lines: List<InfoLine>, bestMove: String?, style: AiStyle): String? {
        val first = lines.firstOrNull()?.pv?.firstOrNull() ?: return bestMove
        if (style == AiStyle.BALANCED || lines.size < 2) return first
        val top = lines.first().score.asCp
        val cands = lines.filter { it.pv.isNotEmpty() && top - it.score.asCp <= style.toleranceCp }
        if (cands.size < 2) return first
        var best = first
        var bestScore = Double.NEGATIVE_INFINITY
        for ((rank, c) in cands.withIndex()) {
            val uci = c.pv.first()
            val g = ChessGame(fen)
            val capture = g.position.pieceAt(Sq.parse(uci.substring(2, 4))) != null
            val pm = g.playUci(uci) ?: continue
            val attackedDest = g.attackers(pm.to, byWhite = !pm.piece.white) > 0
            var s = -rank * 5.0 + (c.score.asCp - top) * 0.1
            when (style) {
                AiStyle.AGGRESSIVE -> { if (capture) s += 30; if (pm.givesCheck) s += 40 }
                AiStyle.DEFENSIVE -> { if (!attackedDest) s += 30; if (!capture) s += 10 }
                AiStyle.RISKY -> { if (attackedDest) s += 40; if (capture) s += 10 }
                AiStyle.BALANCED -> {}
            }
            if (s > bestScore) { bestScore = s; best = uci }
        }
        return best
    }
}
