package com.chessacademy.ai.learn

/** Tagesaufgabe: deterministisch nach Kalendertag; benachbarte Tage haben nie dasselbe Thema. */
object DailyPuzzle {
    private val order: List<TacticPuzzle> by lazy {
        val list = TacticsData.puzzles
        // feste Verteilung: Primzahl-Schrittweite, dann Themenwechsel zwischen Nachbarn erzwingen
        val n = list.size
        var step = 7
        while (gcd(step, n) != 1) step++
        val seq = (0 until n).map { list[(it * step) % n] }.toMutableList()
        for (i in 1 until seq.size) {
            if (seq[i].theme == seq[i - 1].theme) {
                val j = (i + 1 until seq.size).firstOrNull { seq[it].theme != seq[i - 1].theme && (i + 1 >= seq.size || seq[it].theme != seq.getOrNull(i + 1)?.theme) }
                if (j != null) { val t = seq[i]; seq[i] = seq[j]; seq[j] = t }
            }
        }
        seq
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    fun forEpochDay(epochDay: Long): TacticPuzzle = order[Math.floorMod(epochDay, order.size.toLong()).toInt()]
}
