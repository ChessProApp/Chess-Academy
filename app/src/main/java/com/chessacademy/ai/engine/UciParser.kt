package com.chessacademy.ai.engine

import kotlin.math.exp

/** Bewertung aus Sicht der Seite am Zug: entweder Centipawns oder Matt in N (negativ = bekommt Matt). */
data class EngineScore(val cp: Int?, val mate: Int?) {
    /** Zahl aus Sicht der Seite am Zug, Matt wird auf ±(10000 − N) abgebildet. */
    val asCp: Int get() = mate?.let { if (it > 0) 10000 - it else -10000 - it } ?: (cp ?: 0)

    fun label(whiteToMove: Boolean): String {
        mate?.let {
            val m = if (whiteToMove) it else -it
            return if (m > 0) "M$m" else "−M${-m}"
        }
        val v = (cp ?: 0) * (if (whiteToMove) 1 else -1) / 100.0
        return String.format(java.util.Locale.ROOT, "%+.2f", v).replace('-', '−')
    }

    companion object {
        /** Gewinnwahrscheinlichkeit (0..100) aus Sicht des Spielers, dessen cp-Wert übergeben wird (Lichess-Formel). */
        fun winPercent(cp: Int): Double = 50.0 + 50.0 * (2.0 / (1.0 + exp(-0.00368208 * cp.coerceIn(-1500, 1500))) - 1.0)
    }
}

data class InfoLine(val depth: Int, val multiPv: Int, val score: EngineScore, val pv: List<String>)

object UciParser {
    /** Liest eine "info ... score ... pv ..."-Zeile; null bei Zeilen ohne Bewertung/Zug (z. B. currmove). */
    fun parseInfo(line: String): InfoLine? {
        if (!line.startsWith("info ")) return null
        val t = line.trim().split(' ')
        var depth = 0
        var mpv = 1
        var cp: Int? = null
        var mate: Int? = null
        var pvStart = -1
        var i = 1
        while (i < t.size) {
            when (t[i]) {
                "depth" -> { depth = t.getOrNull(i + 1)?.toIntOrNull() ?: depth; i++ }
                "multipv" -> { mpv = t.getOrNull(i + 1)?.toIntOrNull() ?: mpv; i++ }
                "score" -> {
                    val kind = t.getOrNull(i + 1)
                    val v = t.getOrNull(i + 2)?.toIntOrNull()
                    if (kind == "cp") cp = v else if (kind == "mate") mate = v
                    i += 2
                }
                "pv" -> { pvStart = i + 1; i = t.size }
            }
            i++
        }
        if (pvStart < 0 || (cp == null && mate == null)) return null
        return InfoLine(depth, mpv, EngineScore(cp, mate), t.subList(pvStart, t.size))
    }

    fun parseBestMove(line: String): String? {
        if (!line.startsWith("bestmove")) return null
        val m = line.trim().split(' ').getOrNull(1)
        return if (m == null || m == "(none)" || m == "0000") "" else m
    }
}
