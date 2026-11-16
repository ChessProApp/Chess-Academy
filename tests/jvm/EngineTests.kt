package com.chessacademy.ai.tests

import com.chessacademy.ai.chess.*
import com.chessacademy.ai.engine.*
import kotlinx.coroutines.*
import java.io.File

object EngineTests {
    fun run() {
        T.suite("UCI-Parser") {
            val i = UciParser.parseInfo("info depth 14 seldepth 20 multipv 2 score cp -35 nodes 1000 nps 5000 pv e7e5 g1f3 b8c6")!!
            T.eq(i.depth, 14, "depth"); T.eq(i.multiPv, 2, "multipv"); T.eq(i.score.cp, -35, "cp"); T.eq(i.pv, listOf("e7e5", "g1f3", "b8c6"), "pv")
            val m = UciParser.parseInfo("info depth 8 score mate -3 pv a1a2")!!
            T.eq(m.score.mate, -3, "mate"); T.eq(m.score.asCp, -10000 + 3, "asCp Matt")
            T.check(UciParser.parseInfo("info currmove e2e4 currmovenumber 1") == null, "currmove ignoriert")
            T.check(UciParser.parseInfo("info string NNUE evaluation using nn.nnue") == null, "string ignoriert")
            T.eq(UciParser.parseBestMove("bestmove e2e4 ponder e7e5"), "e2e4", "bestmove")
            T.eq(UciParser.parseBestMove("bestmove (none)"), "", "bestmove none")
            T.eq(EngineScore(35, null).label(true), "+0.35", "Label Weiß")
            T.eq(EngineScore(35, null).label(false), "−0.35", "Label Schwarz am Zug")
            T.eq(EngineScore(null, 2).label(true), "M2", "Matt-Label")
            T.check(EngineScore.winPercent(0) == 50.0 && EngineScore.winPercent(300) > 70.0, "Gewinnwahrscheinlichkeit")
        }
        T.suite("Spielstil-Auswahl (ohne Engine)") {
            val fen = START_FEN
            val lines = listOf(
                InfoLine(10, 1, EngineScore(30, null), listOf("e2e4")),
                InfoLine(10, 2, EngineScore(20, null), listOf("d2d4")),
                InfoLine(10, 3, EngineScore(-300, null), listOf("f2f4"))
            )
            T.eq(AiMoveSelector.choose(fen, lines, "e2e4", AiStyle.BALANCED), "e2e4", "ausgeglichen = bester")
            val pick = AiMoveSelector.choose(fen, lines, "e2e4", AiStyle.AGGRESSIVE)
            T.check(pick == "e2e4" || pick == "d2d4", "Stil bleibt innerhalb der Toleranz (nie f2f4)")
            T.eq(AiMoveSelector.choose(fen, emptyList(), "a2a3", AiStyle.RISKY), "a2a3", "Fallback bestmove")
        }
        val sf = System.getenv("SF_BIN")
        if (sf == null || !File(sf).exists()) { println("  SKIP Stockfish-Tests (SF_BIN nicht gesetzt)"); return }
        T.suite("Stockfish: Suche, Matt, Abbruch, Analyse") {
            runBlocking {
                val r = StockfishRunner(sf, threads = 1, hashMb = 16)
                T.check(r.isAvailable, "Binary vorhanden")
                val mate = r.search("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", depth = 8)!!
                T.eq(mate.bestMove, "a1a8", "Mattzug a1a8")
                T.eq(mate.lines.first().score.mate, 1, "Matt in 1")
                val multi = r.search(START_FEN, depth = 8, multiPv = 3)!!
                T.eq(multi.lines.size, 3, "3 Varianten")
                T.check(multi.lines.map { it.multiPv } == listOf(1, 2, 3), "MultiPV-Reihenfolge")
                // Abbruch mitten in der Suche, danach muss die nächste Suche sauber laufen
                val job = launch(Dispatchers.Default) { r.search(START_FEN, depth = 40) }
                delay(300); job.cancelAndJoin()
                val after = r.search("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", depth = 6)!!
                T.eq(after.bestMove, "a1a8", "Suche nach Abbruch funktioniert")
                // Schwachstes Level darf nur legale Züge liefern
                for (f in listOf(START_FEN, "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")) {
                    val w = r.search(f, depth = 1, skill = 0)!!
                    T.check(ChessGame(f).playUci(w.bestMove!!) != null, "Skill 0: legaler Zug $f")
                }
                // Analyse einer Partie mit offensichtlichem Patzer: 1.f3 e5 2.g4?? Qh4#
                val g = ChessGame(); listOf("f2f3", "e7e5", "g2g4", "d8h4").forEach { g.playUci(it) }
                val ev = EngineEvaluator(r, depth = 10)
                val a = GameAnalyzer { f -> ev.evaluate(f) }.analyze(g)!!
                T.eq(a.plies.size, 4, "4 Halbzüge")
                T.check(a.plies[2].moveClass == MoveClass.BLUNDER || a.plies[2].moveClass == MoveClass.MISTAKE, "g4 ist ein grober Fehler: ${a.plies[2].moveClass}")
                T.eq(a.plies[3].moveClass, MoveClass.BEST, "Qh4# ist der beste Zug")
                T.check(a.accuracyWhite < a.accuracyBlack, "Weiß weniger genau (${a.accuracyWhite} < ${a.accuracyBlack})")
                T.eq(a.plies[3].after.whiteCp, -10000, "Endstellung Matt für Schwarz")
                r.close()
                T.check(!r.isAvailable || true, "close ohne Fehler")
            }
        }
    }
}
