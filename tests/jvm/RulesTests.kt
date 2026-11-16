package com.chessacademy.ai.tests

import com.chessacademy.ai.chess.*
import com.github.bhlangonijr.chesslib.Square

object RulesTests {

    private fun sq(s: String) = Sq.parse(s)
    private fun ChessGame.mv(uci: String): PlayedMove = playUci(uci) ?: error("Zug $uci illegal in ${this.fen}")
    private fun ChessGame.mvs(s: String) { s.trim().split(" ").forEach { mv(it) } }

    private fun perft(g: ChessGame, depth: Int): Long {
        if (depth == 0) return 1
        var n = 0L
        val pos = g.position
        // alle (from,to,promo)-Kombinationen aus den legalen Zielen
        for (from in g.movableSquares()) {
            for (to in g.legalTargets(from)) {
                val promos: List<PieceKind?> = if (g.needsPromotion(from, to))
                    listOf(PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT) else listOf(null)
                for (p in promos) {
                    val m = g.play(from, to, p) ?: error("legal, aber play() fehlgeschlagen")
                    n += if (depth == 1) 1 else perft(g, depth - 1)
                    g.undo()
                }
            }
        }
        check(pos === g.position) { "Undo hat Position verändert" }
        return n
    }

    private fun check(c: Boolean, m: () -> String) { if (!c) error(m()) }

    fun run() {
        T.suite("Feldzuordnung chesslib <-> Sq") {
            T.eq(Square.squareAt(0), Square.A1, "a1 = 0")
            T.eq(Square.squareAt(7), Square.H1, "h1 = 7")
            T.eq(Square.squareAt(8), Square.A2, "a2 = 8")
            T.eq(Square.squareAt(63), Square.H8, "h8 = 63")
            for (i in 0..63) T.eq(Square.squareAt(i).ordinal, i, "ordinal $i")
            T.eq(Sq.name(sq("e4")), "e4", "Namen")
            T.check(!Sq.isLight(sq("a1")) && Sq.isLight(sq("h1")), "a1 dunkel, h1 hell")
        }

        T.suite("Perft (Zuggenerator + play/undo)") {
            val cases = listOf(
                Triple(START_FEN, 3, 8902L),
                Triple(START_FEN, 4, 197281L),
                Triple("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 3, 97862L),
                Triple("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 4, 43238L),
                Triple("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3, 9467L),
                Triple("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3, 62379L),
                Triple("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3, 89890L)
            )
            for ((fen, d, expected) in cases) {
                val g = ChessGame(fen)
                T.eq(perft(g, d), expected, "perft($d) $fen")
            }
        }

        T.suite("Schachmatt: Narrenmatt") {
            val g = ChessGame()
            g.mvs("f2f3 e7e5 g2g4")
            val last = g.mv("d8h4")
            T.eq(last.san, "Qh4#", "SAN")
            val s = g.status()
            T.eq(s.result, GameResult.Checkmate(whiteWins = false), "Schwarz gewinnt")
            T.check(s.inCheck, "im Schach")
            T.check(g.play(sq("a2"), sq("a3")) == null, "nach Matt kein Zug mehr")
            T.eq(g.explainIllegal(sq("a2"), sq("a3")), IllegalReason.GAME_OVER, "Begründung")
        }

        T.suite("Patt") {
            val g = ChessGame("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
            T.eq(g.status().result, GameResult.Draw(DrawReason.STALEMATE), "Patt")
            T.check(!g.status().inCheck, "nicht im Schach")
            val g2 = ChessGame("7k/5Q2/6K1/8/8/8/8/8 w - - 0 1")
            val m = g2.mv("f7g7")
            T.eq(m.san, "Qg7#", "Matt statt Patt")
            T.eq(g2.status().result, GameResult.Checkmate(true), "Weiß gewinnt")
        }

        T.suite("Rochade: erlaubt") {
            val g = ChessGame("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1")
            T.check(g.isLegal(sq("e1"), sq("g1")), "kurz")
            T.check(g.isLegal(sq("e1"), sq("c1")), "lang")
            val m = g.mv("e1g1")
            T.eq(m.castle, CastleSide.KING_SIDE, "castle-Flag")
            T.eq(m.san, "O-O", "SAN kurz")
            T.eq(g.position.pieceAt(sq("f1")), Pc(PieceKind.ROOK, true), "Turm auf f1")
            T.eq(g.position.pieceAt(sq("h1")), null, "h1 leer")
            val m2 = g.mv("e8c8")
            T.eq(m2.san, "O-O-O", "SAN lang")
            T.eq(g.position.pieceAt(sq("d8")), Pc(PieceKind.ROOK, false), "Turm d8")
        }

        T.suite("Rochade: verboten") {
            // König im Schach
            var g = ChessGame("4k3/8/8/8/8/8/4r3/R3K2R w KQ - 0 1")
            T.check(!g.isLegal(sq("e1"), sq("g1")), "nicht aus dem Schach")
            T.eq(g.explainIllegal(sq("e1"), sq("g1")), IllegalReason.CASTLE_THROUGH_CHECK, "Begründung Schach")
            // Zwischenfeld bedroht (f1 durch Turm auf f8)
            g = ChessGame("4kr2/8/8/8/8/8/8/R3K2R w KQ - 0 1")
            T.check(!g.isLegal(sq("e1"), sq("g1")), "nicht über bedrohtes f1")
            T.check(g.isLegal(sq("e1"), sq("c1")), "lang bleibt erlaubt")
            // Zielfeld bedroht
            g = ChessGame("4k1r1/8/8/8/8/8/8/R3K2R w KQ - 0 1")
            T.check(!g.isLegal(sq("e1"), sq("g1")), "nicht auf bedrohtes g1")
            // Feld b1 bedroht ist bei langer Rochade erlaubt (nur c1,d1 zählen)
            g = ChessGame("1r2k3/8/8/8/8/8/8/R3K2R w KQ - 0 1")
            T.check(g.isLegal(sq("e1"), sq("c1")), "b1 bedroht -> lange Rochade erlaubt")
            // Figur dazwischen
            g = ChessGame("4k3/8/8/8/8/8/8/RN2K2R w KQ - 0 1")
            T.check(!g.isLegal(sq("e1"), sq("c1")), "Springer b1 versperrt")
            T.eq(g.explainIllegal(sq("e1"), sq("c1")), IllegalReason.CASTLE_BLOCKED, "Begründung blockiert")
            // Rechte verloren
            g = ChessGame("4k3/8/8/8/8/8/8/R3K2R w - - 0 1")
            T.check(!g.isLegal(sq("e1"), sq("g1")), "keine Rechte")
            T.eq(g.explainIllegal(sq("e1"), sq("g1")), IllegalReason.CASTLE_RIGHTS, "Begründung Rechte")
            // Turm bewegt -> Recht weg, auch wenn er zurückkehrt
            g = ChessGame("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
            g.mvs("h1h2 a8a7 h2h1 a7a8")
            T.check(!g.isLegal(sq("e1"), sq("g1")), "Turm war weg -> kurze Rochade verwirkt")
            T.check(g.isLegal(sq("e1"), sq("c1")), "lange Rochade noch möglich")
        }

        T.suite("En passant: nur unmittelbar") {
            var g = ChessGame()
            g.mvs("e2e4 a7a6 e4e5 d7d5")
            T.check(g.isLegal(sq("e5"), sq("d6")), "e.p. sofort erlaubt")
            val ep = g.play(sq("e5"), sq("d6"))!!
            T.check(ep.isEnPassant && ep.captured == Pc(PieceKind.PAWN, false), "Flag + geschlagener Bauer")
            T.eq(ep.captureSquare, sq("d5"), "Schlagfeld d5")
            T.eq(ep.san, "exd6", "SAN")
            T.eq(g.position.pieceAt(sq("d5")), null, "Bauer d5 entfernt")
            // verzögert
            g = ChessGame()
            g.mvs("e2e4 a7a6 e4e5 d7d5 g1f3 g8f6")
            T.check(!g.isLegal(sq("e5"), sq("d6")), "e.p. nach Zwischenzug verfallen")
            // Undo
            g = ChessGame()
            g.mvs("e2e4 a7a6 e4e5 d7d5 e5d6")
            g.undo()
            T.eq(g.position.pieceAt(sq("d5")), Pc(PieceKind.PAWN, false), "Undo stellt Bauer wieder her")
            T.check(g.isLegal(sq("e5"), sq("d6")), "e.p. nach Undo wieder möglich")
            // e.p. deckt Schach nicht auf: Fesselung auf der Reihe
            g = ChessGame("8/8/8/KPp4r/8/8/8/7k w - c6 0 1")
            T.check(!g.isLegal(sq("b5"), sq("c6")), "e.p. wäre illegal (Turm h5 würde König a5 treffen)")
        }

        T.suite("Bauernumwandlung") {
            val g = ChessGame("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
            T.check(g.needsPromotion(sq("a7"), sq("a8")), "braucht Wahl")
            T.check(g.play(sq("a7"), sq("a8"), null) == null, "ohne Figur kein Zug")
            for ((k, san) in listOf(
                PieceKind.QUEEN to "a8=Q+", PieceKind.ROOK to "a8=R+", PieceKind.BISHOP to "a8=B", PieceKind.KNIGHT to "a8=N"
            )) {
                val h = ChessGame("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
                val m = h.play(sq("a7"), sq("a8"), k)!!
                T.eq(m.promotion, k, "Figur $k")
                T.eq(h.position.pieceAt(sq("a8")), Pc(k, true), "Figur auf a8 ($k)")
                T.eq(m.san, san, "SAN $k")
            }
            val q = ChessGame("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
            T.eq(q.playUci("a7a8")!!.promotion, PieceKind.QUEEN, "UCI ohne Buchstabe -> Dame")
            val u = ChessGame("4k3/P7/8/8/8/8/8/4K3 w - - 0 1")
            T.eq(u.playUci("a7a8n")!!.promotion, PieceKind.KNIGHT, "UCI mit n")
            // Schlag-Umwandlung
            val c = ChessGame("3rk3/2P5/8/8/8/8/8/4K3 w - - 0 1")
            T.eq(c.mv("c7d8q").san, "cxd8=Q+", "Schlag mit Umwandlung")
            // schwarz
            val b = ChessGame("4k3/8/8/8/8/8/p7/4K3 b - - 0 1")
            T.check(b.needsPromotion(sq("a2"), sq("a1")), "Schwarz: Umwandlung")
        }

        T.suite("Fesselung, Selbstschach, König") {
            val p = ChessGame("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1") // Sc e2 gefesselt durch Te8
            T.check(p.legalTargets(sq("e2")).isEmpty(), "gefesselter Springer hat keine Züge")
            T.eq(p.explainIllegal(sq("e2"), sq("c3")), IllegalReason.PINNED, "PINNED")
            // gefesselter Turm darf entlang der Linie ziehen
            val r = ChessGame("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1")
            T.check(r.legalTargets(sq("e2")).containsAll(listOf(sq("e3"), sq("e8"))), "Turm entlang Fesselung erlaubt")
            T.check(!r.legalTargets(sq("e2")).contains(sq("d2")), "Turm quer verboten")
            // König auf bedrohtes Feld
            val k2 = ChessGame("4k3/8/8/8/8/8/r7/4K3 w - - 0 1")
            T.eq(k2.explainIllegal(sq("e1"), sq("d2")), IllegalReason.KING_ATTACKED_SQUARE, "König auf bedrohtes Feld")
            // Schach ignorieren
            val c = ChessGame("4k3/8/8/8/8/8/4r3/R3K3 w - - 0 1")
            T.eq(c.explainIllegal(sq("a1"), sq("a2")), IllegalReason.STILL_IN_CHECK, "Schach nicht beendet")
            // Weg versperrt / Geometrie
            val s = ChessGame()
            T.eq(s.explainIllegal(sq("a1"), sq("a4")), IllegalReason.BLOCKED, "Turm blockiert")
            T.eq(s.explainIllegal(sq("b1"), sq("b3")), IllegalReason.GEOMETRY, "Springer-Geometrie")
            T.eq(s.explainIllegal(sq("e7"), sq("e5")), IllegalReason.NOT_YOUR_PIECE, "falsche Farbe")
            T.eq(s.explainIllegal(sq("e4"), sq("e5")), IllegalReason.NO_PIECE, "leeres Feld")
            T.eq(s.explainIllegal(sq("a1"), sq("a2")), IllegalReason.OWN_PIECE, "eigene Figur")
            T.eq(s.explainIllegal(sq("e2"), sq("d3")), IllegalReason.GEOMETRY, "Bauer diagonal ohne Schlag")
            T.eq(s.explainIllegal(sq("d1"), sq("d3")), IllegalReason.BLOCKED, "Dame blockiert")
        }

        T.suite("Doppelschach: nur Königszüge") {
            // Springer e4 verdeckt den Turm e1; Sd6+ gibt Springer- und Abzugsschach zugleich.
            val g = ChessGame("4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1")
            g.mv("e4d6")
            val s = g.status()
            T.check(s.inCheck, "Doppelschach ist Schach")
            val from = g.movableSquares()
            T.eq(from, setOf(sq("e8")), "nur der König kann ziehen")
        }

        T.suite("Wiederholung: 3x reklamierbar, 5x automatisch") {
            val g = ChessGame()
            val cycle = "g1f3 g8f6 f3g1 f6g8"
            g.mvs(cycle)
            T.eq(g.repetitionCount(), 2, "2. Auftreten nach einem Zyklus")
            T.check(g.status().claimable.isEmpty(), "noch nicht reklamierbar")
            g.mvs(cycle)
            T.eq(g.repetitionCount(), 3, "3. Auftreten")
            T.eq(g.status().claimable, listOf(DrawReason.THREEFOLD_REPETITION), "reklamierbar")
            T.check(!g.status().isOver, "nicht automatisch beendet")
            // weiterspielen ist erlaubt
            g.mvs(cycle)
            T.eq(g.repetitionCount(), 4, "4. Auftreten")
            T.check(!g.status().isOver, "immer noch offen")
            g.mvs(cycle)
            T.eq(g.repetitionCount(), 5, "5. Auftreten")
            T.eq(g.status().result, GameResult.Draw(DrawReason.FIVEFOLD_REPETITION), "automatisches Remis")
            T.check(g.play(sq("a2"), sq("a3")) == null, "danach kein Zug")
            // Reklamation
            val h = ChessGame()
            h.mvs(cycle + " " + cycle)
            T.check(h.claimDraw(), "Reklamation erfolgreich")
            T.eq(h.status().result, GameResult.Draw(DrawReason.THREEFOLD_REPETITION, claimed = true), "Ergebnis")
            // Reklamation ohne Anspruch
            val i = ChessGame()
            i.mvs("e2e4")
            T.check(!i.claimDraw(), "kein Anspruch")
            // Rochaderecht-Unterschied macht Stellung verschieden
            val r = ChessGame("r3k3/8/8/8/8/8/8/R3K3 w Qq - 0 1")
            r.mvs("e1e2 e8e7 e2e1 e7e8")
            T.eq(r.repetitionCount(), 1, "Rochaderecht verwirkt -> andere Stellung")
            // Undo senkt den Zähler
            val u = ChessGame(); u.mvs(cycle); u.undo(); T.eq(u.repetitionCount(), 1, "Zähler nach Undo")
        }

        T.suite("50- und 75-Züge-Regel") {
            val g = ChessGame("4k3/8/8/8/8/8/8/R3K3 w - - 98 80")
            g.mv("a1a2")
            T.eq(g.position.halfmove, 99, "99")
            T.check(g.status().claimable.isEmpty(), "99 noch nicht")
            g.mv("e8d8")
            T.eq(g.position.halfmove, 100, "100")
            T.eq(g.status().claimable, listOf(DrawReason.FIFTY_MOVE), "50-Züge reklamierbar")
            T.check(!g.status().isOver, "nicht automatisch")
            // Bauernzug setzt zurück
            val p = ChessGame("4k3/8/8/8/8/8/P7/4K3 w - - 100 80")
            p.mv("a2a3")
            T.eq(p.position.halfmove, 0, "Bauernzug resettet")
            // Schlag setzt zurück
            val c = ChessGame("4k3/8/8/8/8/8/r7/R3K3 w - - 120 80")
            c.mv("a1a2")
            T.eq(c.position.halfmove, 0, "Schlag resettet")
            // 75
            val a = ChessGame("4k3/8/8/8/8/8/8/R3K3 w - - 148 90")
            a.mv("a1a2")
            T.check(!a.status().isOver, "149 nicht automatisch")
            a.mv("e8d8")
            T.eq(a.status().result, GameResult.Draw(DrawReason.SEVENTYFIVE_MOVE), "150 -> automatisch")
            // Matt hat Vorrang vor 75-Züge-Regel
            val m = ChessGame("6k1/5ppp/8/8/8/8/8/R5K1 w - - 149 90")
            m.mv("a1a8")
            T.eq(m.status().result, GameResult.Checkmate(true), "Matt auf dem 150. Halbzug zählt")
        }

        T.suite("Unzureichendes Material") {
            fun ins(fen: String) = ChessGame.isInsufficientMaterial(Position.fromFen(fen))
            T.check(ins("4k3/8/8/8/8/8/8/4K3 w - - 0 1"), "K gegen K")
            T.check(ins("4k3/8/8/8/8/8/8/3BK3 w - - 0 1"), "KL gegen K")
            T.check(ins("4k3/8/8/8/8/8/8/3NK3 w - - 0 1"), "KS gegen K")
            T.check(ins("4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1"), "KL gegen KL gleichfarbig (c1 und f8 dunkel)")
            T.check(!ins("4kb2/8/8/8/8/8/8/3BK3 w - - 0 1"), "KL gegen KL verschiedenfarbig (d1 hell, f8 dunkel)")
            T.check(ins("4k3/8/8/8/8/B7/8/2B1K3 w - - 0 1"), "zwei gleichfarbige Läufer gegen König")
            T.check(!ins("4k3/8/8/8/8/B7/8/3BK3 w - - 0 1"), "zwei verschiedenfarbige Läufer: Matt möglich")
            T.check(!ins("4k3/8/8/8/8/8/8/2NNK3 w - - 0 1"), "KSS gegen K ist keine tote Stellung")
            T.check(!ins("4k3/8/8/8/8/8/8/3RK3 w - - 0 1"), "Turm")
            T.check(!ins("4k3/8/8/8/8/8/P7/4K3 w - - 0 1"), "Bauer")
            T.check(!ins("4kn2/8/8/8/8/8/8/3BK3 w - - 0 1"), "KL gegen KS")
            val g = ChessGame("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1")
            g.mv("e1e2")
            T.eq(g.status().result, GameResult.Draw(DrawReason.INSUFFICIENT_MATERIAL), "Dame/Turm geschlagen -> Remis")
        }

        T.suite("Zugrücknahme") {
            val g = ChessGame()
            g.mvs("e2e4 e7e5 g1f3 b8c6 f1b5")
            val fenBefore = g.fen
            g.mvs("a7a6 b5c6 d7c6 e1g1")
            while (g.ply > 5) g.undo()
            T.eq(g.fen, fenBefore, "FEN nach Undo identisch")
            T.eq(g.ply, 5, "Ply")
            T.check(g.undo() != null, "weiter zurück")
            T.eq(g.truncate(0).let { g.ply }, 0, "truncate")
            T.eq(g.fen, START_FEN, "Startstellung")
            T.check(g.undo() == null, "nichts mehr zu tun")
            // Matt per Undo aufheben
            val m = ChessGame(); m.mvs("f2f3 e7e5 g2g4 d8h4")
            T.check(m.isOver, "Matt"); m.undo(); T.check(!m.isOver, "Matt aufgehoben")
        }

        T.suite("Aufgabe / Remisvereinbarung") {
            val g = ChessGame()
            g.mv("e2e4")
            g.resign(whiteResigns = false)
            T.eq(g.status().result, GameResult.Resignation(whiteWins = true), "Aufgabe")
            T.eq(g.status().result?.pgnToken, "1-0", "Token")
            g.undo()
            T.check(!g.isOver, "Undo hebt Aufgabe auf")
            g.agreeDraw()
            T.eq(g.status().result?.pgnToken, "1/2-1/2", "Remis")
        }

        T.suite("SAN: Mehrdeutigkeit und Parsing") {
            var g = ChessGame("4k3/8/8/8/8/8/6K1/R4R2 w - - 0 1")
            T.eq(g.mv("a1d1").san, "Rad1", "Datei-Disambiguierung")
            g = ChessGame("4k3/8/8/R7/8/8/8/R3K3 w - - 0 1")
            T.eq(g.mv("a1a3").san, "R1a3", "Reihen-Disambiguierung")
            g = ChessGame("4k3/8/8/8/8/5N2/8/1N2K3 w - - 0 1")
            T.eq(g.mv("b1d2").san, "Nbd2", "Springer b/d2")
            g = ChessGame("4k3/8/8/8/8/8/8/Q1Q1K1Q1 w - - 0 1")
            T.eq(g.mv("a1c3").san, "Qac3", "Dame mit Zwischenfeld")
            g = ChessGame("4k3/8/8/8/8/Q7/8/Q1Q1K3 w - - 0 1")
            T.eq(g.mv("a1c3").san, "Qa1c3", "volle Feldangabe bei dreifacher Mehrdeutigkeit")
            // Parsing
            val p = ChessGame()
            T.check(p.playSan("e4") != null, "e4")
            T.check(p.playSan("e5") != null, "e5")
            T.check(p.playSan("Nf3") != null, "Nf3")
            T.check(p.playSan("Nc6") != null, "Nc6")
            T.check(p.playSan("0-0") == null, "Rochade noch unmöglich (Läufer f1 im Weg)")
            T.check(p.playSan("Bb5+") != null, "Zusatz + wird ignoriert")
            T.check(p.playSan("a6!?") != null, "Annotation ignoriert")
            T.check(p.playSan("Ke3") == null, "Ke3 ist illegal")
            T.check(p.playSan("0-0") != null, "0-0 jetzt legal")
            val c = ChessGame("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
            T.check(c.playSan("0-0") != null, "0-0 wird akzeptiert")
            T.check(c.playSan("O-O-O") != null, "O-O-O")
        }

        T.suite("PGN: Export, Import, Kommentare, Varianten") {
            val g = ChessGame()
            g.mvs("e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4 g8f6 e1g1 f8e7")
            val pgn = Pgn.export(g, white = "Ich", black = "Stockfish", date = "2026.11.15")
            T.check(pgn.contains("[White \"Ich\"]") && pgn.contains("1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7"), "Export-Format")
            val back = Pgn.parse(pgn)
            T.eq(back.error, null, "kein Importfehler")
            T.eq(back.game.fen, g.fen, "Roundtrip-FEN")
            T.eq(back.headers["White"], "Ich", "Header")
            val messy = """
                [Event "Test"]
                [Result "1-0"]

                1. e4 {Kommentar} e5 $1 2. Nf3 (2. f4 exf4) Nc6 ; Zeilenkommentar
                3. Bb5 a6 1-0
            """.trimIndent()
            val m = Pgn.parse(messy)
            T.eq(m.error, null, "kein Fehler")
            T.eq(m.game.ply, 6, "6 Halbzüge")
            val bad = Pgn.parse("1. e4 e5 2. Ke4 Nc6")
            T.check(bad.error != null && bad.game.ply == 2, "Fehler gemeldet, bis dahin gespielt")
            // Mit FEN
            val f = ChessGame("4k3/P7/8/8/8/8/8/4K3 w - - 0 1"); f.mv("a7a8q")
            val ff = Pgn.parse(Pgn.export(f))
            T.eq(ff.game.fen, f.fen, "FEN-Startstellung im Roundtrip")
            // Schwarz am Zug
            val b = ChessGame("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"); b.mv("e7e5")
            T.check(Pgn.export(b).contains("1... e5"), "Schwarz beginnt: 1... e5")
        }

        T.suite("PieceTracker: Gleiten, Schlagen, Rochade, Umwandlung") {
            val g = ChessGame()
            val t = PieceTracker(); t.reset(g.position)
            fun idAt(s: String) = t.pieces.first { it.square == sq(s) }.id
            val e2 = idAt("e2")
            var m = g.mv("e2e4"); var removed = t.update(g.position, m.from, m.to)
            T.eq(idAt("e4"), e2, "Bauer behält ID"); T.check(removed.isEmpty(), "nichts entfernt")
            m = g.mv("d7d5"); t.update(g.position, m.from, m.to)
            val e4 = idAt("e4"); val d5 = idAt("d5")
            m = g.mv("e4d5"); removed = t.update(g.position, m.from, m.to)
            T.eq(idAt("d5"), e4, "Schläger behält ID"); T.eq(removed.map { it.id }, listOf(d5), "Geschlagener entfernt")
            T.eq(t.pieces.size, 31, "31 Figuren")
            // Rochade
            val c = ChessGame("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
            val tc = PieceTracker(); tc.reset(c.position)
            val kingId = tc.pieces.first { it.square == sq("e1") }.id
            val rookId = tc.pieces.first { it.square == sq("h1") }.id
            m = c.mv("e1g1"); tc.update(c.position, m.from, m.to)
            T.eq(tc.pieces.first { it.square == sq("g1") }.id, kingId, "König-ID")
            T.eq(tc.pieces.first { it.square == sq("f1") }.id, rookId, "Turm gleitet mit")
            // Umwandlung
            val p = ChessGame("1r2k3/P7/8/8/8/8/8/4K3 w - - 0 1")
            val tp = PieceTracker(); tp.reset(p.position)
            val pawnId = tp.pieces.first { it.square == sq("a7") }.id
            val rk = tp.pieces.first { it.square == sq("b8") }.id
            m = p.mv("a7b8q"); removed = tp.update(p.position, m.from, m.to)
            T.eq(tp.pieces.first { it.square == sq("b8") }.id, pawnId, "Bauer wird zur Dame (gleiche ID)")
            T.eq(tp.pieces.first { it.square == sq("b8") }.piece, Pc(PieceKind.QUEEN, true), "neue Figur")
            T.eq(removed.map { it.id }, listOf(rk), "Turm geschlagen")
            // En passant
            val e = ChessGame(); e.mvs("e2e4 a7a6 e4e5")
            val te = PieceTracker(); te.reset(e.position)
            m = e.mv("d7d5"); te.update(e.position, m.from, m.to)
            val dId = te.pieces.first { it.square == sq("d5") }.id
            m = e.mv("e5d6"); removed = te.update(e.position, m.from, m.to)
            T.eq(removed.map { it.id }, listOf(dId), "e.p.: Bauer d5 entfernt")
            // Zugrücknahme: Figur gleitet zurück
            val ids = te.pieces.associate { it.square to it.id }
            e.undo(); te.update(e.position)
            T.eq(te.pieces.size, 32, "alle 32 Figuren nach Undo wieder da")
            T.eq(te.pieces.first { it.square == sq("e5") }.id, ids[sq("d6")], "Undo: gleiche ID auf e5")
        }

        T.suite("Angriffszähler") {
            val g = ChessGame()
            T.eq(g.attackers(sq("f3"), true), 3, "f3 von Weiß: e2,g2 Bauern + Sg1 = 3")
            T.eq(g.attackers(sq("e4"), true), 0, "e4 unangegriffen")
            T.eq(g.attackers(sq("e6"), false), 2, "e6 von Schwarz: d7,f7 Bauern... + Lf8? nein -> Läufer c8 blockiert")
        }
    }
}
