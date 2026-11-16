package com.chessacademy.ai.tests

import com.chessacademy.ai.chess.*
import com.chessacademy.ai.learn.*

object ContentTests {
    private fun legalWithPromo(fen: String, uci: String): Boolean {
        return ChessGame(fen, autoDraws = false).playUci(uci) != null
    }
    private fun squareOk(s: String) = Sq.parse(s) >= 0

    fun run() {
        T.suite("Academy: 60 Level lückenlos und lösbar") {
            val ls = AcademyData.allLessons
            T.eq(ls.map { it.level }, (1..60).toList(), "Level 1..60")
            for (l in ls) {
                T.eq(ChessGame.validate(l.fen), null, "Level ${l.level}: FEN gültig")
                val whiteToMove = !l.fen.contains(" b ")
                T.eq(Position.fromFen(l.fen).whiteToMove, whiteToMove, "Level ${l.level}: Seite am Zug")
                T.check(l.chapter.isNotBlank() && l.title.isNotBlank() && l.task.isNotBlank(), "Level ${l.level}: Texte")
                for (s in listOf(l.solutionUci) + l.alternativeSolutions) T.check(legalWithPromo(l.fen, s), "Level ${l.level}: Lösung $s legal")
                if (l.followUp.isNotEmpty()) {
                    val g = ChessGame(l.fen, autoDraws = false); g.playUci(l.solutionUci)
                    for (m in l.followUp) T.check(g.playUci(m) != null, "Level ${l.level}: followUp $m legal")
                }
            }
        }
        T.suite("Regelkurs: Struktur") {
            val all = RulesCurriculum.all
            T.eq(all.map { it.order }, (1..all.size).toList(), "order lückenlos")
            T.eq(all.map { it.id }.toSet().size, all.size, "ids eindeutig")
            T.check(all.size >= 19, "mindestens 19 Lektionen")
            for (l in all) {
                for (p in l.prerequisites) T.check(RulesCurriculum.byId(p) != null, "${l.id}: Voraussetzung $p existiert")
                T.check(l.steps.size >= 3 && l.exercises.isNotEmpty() && l.quiz.size >= 3 && l.mistakes.isNotEmpty(), "${l.id}: Mindestinhalt")
                T.check(l.summary.isNotBlank() && l.title.isNotBlank(), "${l.id}: Titel")
            }
            T.check(RulesCurriculum.byLevel(RuleLevel.BEGINNER).isNotEmpty() && RulesCurriculum.byLevel(RuleLevel.ADVANCED).isNotEmpty() && RulesCurriculum.byLevel(RuleLevel.EXPERT).isNotEmpty(), "alle drei Level besetzt")
            val ids = all.map { it.id }.toSet()
            for (must in listOf("en_passant", "castling", "promotion", "double_check", "pin", "skewer", "fork", "discovered_check", "perpetual_check", "stalemate", "draw_rules", "insufficient_material"))
                T.check(must in ids, "Pflichtthema $must vorhanden")
        }
        T.suite("Regelkurs: alle Stellungen und Züge stimmen mit der Engine") {
            for (l in RulesCurriculum.all) {
                for ((i, s) in l.steps.withIndex()) {
                    T.eq(ChessGame.validate(s.fen), null, "${l.id} Schritt $i: FEN")
                    s.moveUci?.let { T.check(legalWithPromo(s.fen, it), "${l.id} Schritt $i: Demozug $it legal") }
                    s.markers.forEach { T.check(squareOk(it.square), "${l.id}: Marker ${it.square}") }
                    s.arrows.forEach { T.check(squareOk(it.from) && squareOk(it.to), "${l.id}: Pfeil") }
                }
                for (e in l.allowed) {
                    T.eq(ChessGame.validate(e.fen), null, "${l.id}: erlaubt FEN")
                    T.check(legalWithPromo(e.fen, e.moveUci), "${l.id}: erlaubter Zug ${e.moveUci} ist legal")
                }
                for (e in l.forbidden) {
                    T.eq(ChessGame.validate(e.fen), null, "${l.id}: verboten FEN")
                    val g = ChessGame(e.fen, autoDraws = false)
                    val from = Sq.parse(e.moveUci.substring(0, 2)); val to = Sq.parse(e.moveUci.substring(2, 4))
                    T.check(!g.isLegal(from, to), "${l.id}: verbotener Zug ${e.moveUci} ist wirklich illegal")
                    T.check(g.explainIllegal(from, to) != IllegalReason.GAME_OVER || g.isOver, "${l.id}: Begründung vorhanden")
                }
                for (x in l.exercises) {
                    T.eq(ChessGame.validate(x.fen), null, "${l.id}: Übung FEN")
                    T.check(x.solutions.isNotEmpty(), "${l.id}: Lösungen vorhanden")
                    x.solutions.forEach { T.check(legalWithPromo(x.fen, it), "${l.id}: Lösung $it legal") }
                    x.wrongFeedback.keys.forEach { T.check(legalWithPromo(x.fen, it), "${l.id}: Fehler-Feedback-Zug $it legal") }
                    T.check(x.wrongFeedback.keys.none { it in x.solutions }, "${l.id}: falscher Zug ist nicht zugleich Lösung")
                }
                for (q in l.quiz) {
                    T.eq(q.options.size, 4, "${l.id}: 4 Antworten")
                    T.check(q.correctIndex in 0..3 && q.explanation.isNotBlank(), "${l.id}: Index/Erklärung")
                    q.fen?.let { T.eq(ChessGame.validate(it), null, "${l.id}: Quiz-FEN") }
                }
            }
        }
        T.suite("Regelkurs: Matt-Übungen haben genau die erwarteten Lösungen") {
            for (l in RulesCurriculum.all) for (x in l.exercises) {
                if (x.prompt.startsWith("Setze matt")) {
                    val g0 = ChessGame(x.fen, autoDraws = false)
                    for (s in x.solutions) { val g = ChessGame(x.fen, autoDraws = false); g.playUci(s); T.check(g.status().result is GameResult.Checkmate, "${l.id}: $s ist Matt") }
                    val allMates = g0.movableSquares().flatMap { f -> g0.legalTargets(f).map { f to it } }.filter { (f, t) ->
                        val g = ChessGame(x.fen, autoDraws = false); g.play(f, t, PieceKind.QUEEN); g.status().result is GameResult.Checkmate }
                    T.eq(allMates.size, x.solutions.size, "${l.id}: alle Mattzüge sind als Lösung erfasst")
                }
            }
        }
    }
}

object TrainingTests {
    fun run() {
        T.suite("Taktikaufgaben: legale Lösungsketten") {
            val ps = TacticsData.puzzles
            T.check(ps.size >= 12, "genug Aufgaben")
            T.eq(ps.map { it.id }.toSet().size, ps.size, "ids eindeutig")
            T.eq(ps.map { it.fen }.toSet().size, ps.size, "keine doppelten FENs")
            for (p in ps) {
                T.eq(ChessGame.validate(p.fen), null, "${p.id}: FEN")
                val g = ChessGame(p.fen, autoDraws = false)
                for (m in p.solution) T.check(g.playUci(m) != null, "${p.id}: $m legal")
                T.check(p.difficulty in 1..3 && p.explanation.isNotBlank(), "${p.id}: Felder")
            }
        }
        T.suite("Endspiel-Drills") {
            for (d in EndgamesData.drills) {
                T.eq(ChessGame.validate(d.fen), null, "${d.id}: FEN")
                T.eq(Position.fromFen(d.fen).whiteToMove, d.playerWhite, "${d.id}: Spieler am Zug")
                T.check(d.tips.isNotEmpty(), "${d.id}: Tipps")
            }
            T.check(EndgamesData.drills.any { it.goal == EndgameGoal.DRAW } && EndgamesData.drills.any { it.goal == EndgameGoal.WIN }, "Ziele WIN und DRAW")
        }
        T.suite("Eröffnungsbibliothek") {
            val es = OpeningBook.entries
            T.check(es.size >= 60, "mindestens 60 Einträge")
            for (e in es) {
                val g = ChessGame(autoDraws = false)
                for (m in e.moves.split(" ")) T.check(g.playSan(m) != null, "${e.name}: $m legal")
                T.check(OpeningBook.detect(g.moves.map { it.san })!!.moves.split(" ").size >= e.moves.split(" ").size, "${e.name}: wird erkannt")
            }
            T.eq(OpeningBook.detect(emptyList()), null, "leer = keine Eröffnung")
            val g = ChessGame(); listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6").forEach { g.playUci(it) }
            T.eq(OpeningBook.detect(g.moves.map { it.san })!!.name, "Spanische Partie", "Spanisch erkannt (längster Präfix)")
            T.check(OpeningBook.continuations(listOf("e4", "e5")).isNotEmpty(), "Fortsetzungen vorhanden")
        }
        T.suite("Tagesaufgabe") {
            var prev: String? = null
            for (d in 20000L until 20400L) {
                val p = DailyPuzzle.forEpochDay(d)
                T.check(prev == null || prev != p.theme, "Tag $d: Thema wechselt")
                prev = p.theme
            }
            T.eq(DailyPuzzle.forEpochDay(20000L).id, DailyPuzzle.forEpochDay(20000L).id, "deterministisch")
        }
    }
}
