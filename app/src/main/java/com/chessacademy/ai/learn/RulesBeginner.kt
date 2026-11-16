package com.chessacademy.ai.learn

/** Regelkurs Teil 1: Brett, Figuren, Schach, Matt, Patt, Rochade, En passant, Umwandlung. Alle Stellungen/Züge sind testgeprüft (tests/jvm). */
object RulesBeginner {
    val lessons: List<RuleLesson> = listOf(
    RuleLesson(
        id = "board", order = 1, level = RuleLevel.BEGINNER,
        title = "Brett & Aufstellung",
        summary = "64 Felder, 32 Figuren: So steht alles am Anfang.",
        prerequisites = listOf(),
        steps = listOf(
            ExplainStep("Das Brett hat 8×8 = 64 Felder in zwei Farben. Die Spalten heißen a–h, die Reihen 1–8.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf(Marker("a1", MarkerKind.FOCUS), Marker("h1", MarkerKind.FOCUS), Marker("a8", MarkerKind.FOCUS), Marker("h8", MarkerKind.FOCUS)), emptyList()),
            ExplainStep("Unten links (a1) liegt ein dunkles Feld, unten rechts (h1) ein helles. Merke: Weiß hat rechts unten ein helles Feld.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf(Marker("a1", MarkerKind.FOCUS), Marker("h1", MarkerKind.SAFE)), emptyList()),
            ExplainStep("Jeder Spieler hat 16 Figuren: König, Dame, 2 Türme, 2 Läufer, 2 Springer, 8 Bauern. Die Dame steht auf ihrer Farbe: weiße Dame d1, schwarze Dame d8.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf(Marker("d1", MarkerKind.FOCUS), Marker("d8", MarkerKind.FOCUS), Marker("e1", MarkerKind.SAFE), Marker("e8", MarkerKind.SAFE)), emptyList()),
            ExplainStep("Weiß beginnt. Danach ziehen Weiß und Schwarz abwechselnd – immer genau ein Zug.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), listOf(Arrow("e2", "e4", MarkerKind.LEGAL)), moveUci = "e2e4"),
        ),
        allowed = listOf(
            RuleExample("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4", "Weiß beginnt mit einem Bauernzug.", emptyList(), listOf(Arrow("e2", "e4", MarkerKind.LEGAL))),
            RuleExample("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "g1f3", "Auch ein Springer darf als erster Zug ziehen.", emptyList(), listOf(Arrow("g1", "f3", MarkerKind.LEGAL))),
        ),
        forbidden = listOf(
            RuleExample("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e7e5", "Schwarz darf nicht beginnen – Weiß ist am Zug.", emptyList(), emptyList()),
            RuleExample("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e5", "Ein Bauer darf höchstens zwei Felder ziehen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Dame und König vertauschen – die Dame steht immer auf ihrer eigenen Farbe.", "Brett falsch herum aufstellen (rechts unten muss ein helles Feld sein).", "Zwei Züge hintereinander machen."),
        exercises = listOf(
            RuleExercise("Ziehe den e-Bauern zwei Felder vor.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf("e2e4"), "e2 nach e4", "Vom Startfeld darf ein Bauer ein oder zwei Felder ziehen.", mapOf("e2e3" to "Auch erlaubt, aber die Aufgabe verlangt zwei Felder.")),
            RuleExercise("Entwickle den Springer von g1 nach f3.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf("g1f3"), "Der Springer springt in L-Form.", "Springer ziehen zwei Felder in eine Richtung und eins zur Seite.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Welche Farbe hat das Feld a1?", listOf("Hell", "Dunkel", "Mal so, mal so", "Hängt von der Seite ab"), 1, "a1 ist immer dunkel, h1 immer hell."),
            QuizQuestion("Auf welchem Feld steht die weiße Dame am Anfang?", listOf("d1", "e1", "d8", "c1"), 0, "Die Dame steht auf ihrer Farbe: weiß auf d1, schwarz auf d8."),
            QuizQuestion("Wer macht den ersten Zug?", listOf("Schwarz", "Weiß", "Wer will", "Der Jüngere"), 1, "Im Schach beginnt immer Weiß."),
        )
    ),
    RuleLesson(
        id = "rook_bishop", order = 2, level = RuleLevel.BEGINNER,
        title = "Turm, Läufer & Dame",
        summary = "Gerade, diagonal oder beides: so ziehen die Langstreckenfiguren.",
        prerequisites = listOf("board"),
        steps = listOf(
            ExplainStep("Der Turm zieht beliebig weit geradeaus: waagerecht oder senkrecht.", "4k3/8/8/8/3R4/8/8/4K3 w - - 0 1", listOf(Marker("d5", MarkerKind.LEGAL), Marker("d6", MarkerKind.LEGAL), Marker("d7", MarkerKind.LEGAL), Marker("d8", MarkerKind.LEGAL), Marker("a4", MarkerKind.LEGAL), Marker("h4", MarkerKind.LEGAL), Marker("d3", MarkerKind.LEGAL), Marker("d2", MarkerKind.LEGAL), Marker("d1", MarkerKind.LEGAL)), emptyList()),
            ExplainStep("Der Läufer zieht beliebig weit diagonal – und bleibt immer auf seiner Feldfarbe.", "4k3/8/8/8/3B4/8/8/4K3 w - - 0 1", listOf(Marker("e5", MarkerKind.LEGAL), Marker("f6", MarkerKind.LEGAL), Marker("g7", MarkerKind.LEGAL), Marker("h8", MarkerKind.LEGAL), Marker("c5", MarkerKind.LEGAL), Marker("b6", MarkerKind.LEGAL), Marker("a7", MarkerKind.LEGAL), Marker("e3", MarkerKind.LEGAL), Marker("f2", MarkerKind.LEGAL), Marker("c3", MarkerKind.LEGAL), Marker("b2", MarkerKind.LEGAL), Marker("a1", MarkerKind.LEGAL)), emptyList()),
            ExplainStep("Die Dame vereint beides: wie Turm und Läufer zusammen. Sie ist die stärkste Figur.", "4k3/8/8/8/3Q4/8/8/4K3 w - - 0 1", listOf(Marker("d8", MarkerKind.LEGAL), Marker("h8", MarkerKind.LEGAL), Marker("a1", MarkerKind.LEGAL), Marker("h4", MarkerKind.LEGAL), Marker("a4", MarkerKind.LEGAL), Marker("a7", MarkerKind.LEGAL)), emptyList(), moveUci = "d4h8"),
            ExplainStep("Keine dieser Figuren darf über andere Figuren springen. Eigene Figuren blockieren, gegnerische darf man schlagen.", "4k3/8/8/3p4/8/3R4/8/4K3 w - - 0 1", listOf(Marker("d4", MarkerKind.ILLEGAL), Marker("d5", MarkerKind.ATTACK), Marker("d6", MarkerKind.ILLEGAL)), listOf(Arrow("d3", "d5", MarkerKind.ATTACK))),
        ),
        allowed = listOf(
            RuleExample("4k3/8/8/8/3R4/8/8/4K3 w - - 0 1", "d4d7", "Der Turm zieht senkrecht.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/3B4/8/8/4K3 w - - 0 1", "d4a7", "Der Läufer zieht diagonal.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/3Q4/8/8/4K3 w - - 0 1", "d4a1", "Die Dame zieht auch diagonal.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/8/8/8/3R4/8/8/4K3 w - - 0 1", "d4e5", "Ein Turm zieht nie diagonal.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/3B4/8/8/4K3 w - - 0 1", "d4d5", "Ein Läufer zieht nie gerade.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/3p4/8/3R4/8/4K3 w - - 0 1", "d3d6", "Der schwarze Bauer auf d5 versperrt den Weg – man darf nicht überspringen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Mit dem Läufer gerade ziehen.", "Mit Turm oder Läufer über eine Figur springen.", "Vergessen: Der Läufer wechselt nie die Feldfarbe."),
        exercises = listOf(
            RuleExercise("Schlage den Bauern g7 mit dem Läufer.", "4k3/6p1/8/8/8/8/1B6/4K3 w - - 0 1", listOf("b2g7"), "Die Diagonale b2–h8.", "Der Läufer läuft über c3, d4, e5, f6 bis g7.", emptyMap()),
            RuleExercise("Schlage den Bauern a5 mit dem Turm.", "4k3/8/8/p7/8/8/8/R3K3 w - - 0 1", listOf("a1a5"), "Die a-Linie hinauf.", "Der Turm greift entlang der Linie an.", emptyMap()),
            RuleExercise("Schlage den Bauern b3 mit der Dame.", "4k3/8/8/8/8/1p6/8/3QK3 w - - 0 1", listOf("d1b3"), "Diagonale d1–b3.", "Die Dame darf auch diagonal ziehen.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Welche Figur wechselt nie die Feldfarbe?", listOf("Turm", "Springer", "Läufer", "Dame"), 2, "Der Läufer bleibt auf seiner Feldfarbe."),
            QuizQuestion("Darf ein Turm diagonal ziehen?", listOf("Ja", "Nur schlagen", "Nein", "Nur vom Start aus"), 2, "Der Turm zieht nur waagerecht und senkrecht."),
            QuizQuestion("Kann die Dame über eigene Figuren springen?", listOf("Ja", "Nein", "Nur Bauern", "Nur diagonal"), 1, "Nur der Springer darf springen."),
        )
    ),
    RuleLesson(
        id = "knight_king_pawn", order = 3, level = RuleLevel.BEGINNER,
        title = "Springer, König & Bauer",
        summary = "Die drei besonderen Figuren und ihre Eigenheiten.",
        prerequisites = listOf("rook_bishop"),
        steps = listOf(
            ExplainStep("Der Springer zieht in L-Form: zwei Felder gerade und eins zur Seite. Er ist der Einzige, der über andere Figuren springt.", "4k3/8/8/8/3N4/8/8/4K3 w - - 0 1", listOf(Marker("b3", MarkerKind.LEGAL), Marker("b5", MarkerKind.LEGAL), Marker("c2", MarkerKind.LEGAL), Marker("c6", MarkerKind.LEGAL), Marker("e2", MarkerKind.LEGAL), Marker("e6", MarkerKind.LEGAL), Marker("f3", MarkerKind.LEGAL), Marker("f5", MarkerKind.LEGAL)), emptyList()),
            ExplainStep("Der König zieht ein Feld in jede Richtung. Er darf nie auf ein bedrohtes Feld und nie neben den gegnerischen König.", "8/8/8/8/8/4k3/8/4K3 w - - 0 1", listOf(Marker("d1", MarkerKind.LEGAL), Marker("f1", MarkerKind.LEGAL), Marker("e2", MarkerKind.LEGAL), Marker("d2", MarkerKind.ILLEGAL), Marker("f2", MarkerKind.ILLEGAL)), listOf(Arrow("e3", "d2", MarkerKind.ATTACK))),
            ExplainStep("Der Bauer zieht vorwärts: ein Feld, vom Startfeld aus wahlweise zwei. Geschlagen wird nur diagonal vorwärts.", "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1", listOf(Marker("e3", MarkerKind.LEGAL), Marker("e4", MarkerKind.LEGAL)), listOf(Arrow("e2", "e4", MarkerKind.LEGAL))),
            ExplainStep("Ein Bauer kann nie rückwärts ziehen, und eine Figur direkt vor ihm blockiert ihn.", "4k3/8/8/8/8/4p3/4P3/4K3 w - - 0 1", listOf(Marker("e3", MarkerKind.ILLEGAL), Marker("e4", MarkerKind.ILLEGAL)), listOf(Arrow("e2", "e3", MarkerKind.ILLEGAL))),
        ),
        allowed = listOf(
            RuleExample("4k3/8/8/8/3N4/8/8/4K3 w - - 0 1", "d4f5", "Der Springer springt in L-Form.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1", "e2e4", "Vom Startfeld darf der Bauer zwei Felder ziehen.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/3p4/4P3/8/8/4K3 w - - 0 1", "e4d5", "Der Bauer schlägt diagonal.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/8/8/8/3N4/8/8/4K3 w - - 0 1", "d4d6", "Zwei Felder gerade – das ist kein Springerzug.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1", "e2d3", "Ohne gegnerische Figur darf der Bauer nicht diagonal ziehen.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/4p3/4P3/4K3 w - - 0 1", "e2e4", "Der Bauer auf e3 blockiert – auch der Doppelschritt geht nicht.", emptyList(), emptyList()),
            RuleExample("8/8/8/8/8/4k3/8/4K3 w - - 0 1", "e1d2", "Der König darf nicht neben den gegnerischen König.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Den Bauern geradeaus schlagen wollen.", "Mit dem Bauern rückwärts ziehen.", "Den König neben den gegnerischen König ziehen.", "Doppelschritt mitten im Brett versuchen."),
        exercises = listOf(
            RuleExercise("Schlage den Bauern e6 mit dem Springer.", "4k3/8/4p3/8/3N4/8/8/4K3 w - - 0 1", listOf("d4e6"), "L-Form: zwei hoch, eins rechts.", "Der Springer erreicht e6 aus d4.", emptyMap()),
            RuleExercise("Schlage den schwarzen Bauern mit deinem Bauern.", "4k3/8/8/3p4/4P3/8/8/4K3 w - - 0 1", listOf("e4d5"), "Diagonal nach vorn.", "Bauern schlagen diagonal.", mapOf("e4e5" to "Das ist ein normaler Vorwärtszug – geschlagen wird diagonal.")),
            RuleExercise("Schlage den Bauern d2 mit dem König.", "7k/8/8/8/8/8/3p4/4K3 w - - 0 1", listOf("e1d2"), "Der König zieht ein Feld.", "Der Bauer d2 ist ungedeckt – der König schlägt ihn.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wie viele Felder darf ein Bauer vom Startfeld aus ziehen?", listOf("Nur 1", "1 oder 2", "Genau 2", "Beliebig viele"), 1, "Vom Startfeld sind ein oder zwei Felder erlaubt."),
            QuizQuestion("Welche Figur darf über andere springen?", listOf("Läufer", "Turm", "Springer", "König"), 2, "Nur der Springer."),
            QuizQuestion("Wie schlägt ein Bauer?", listOf("Geradeaus", "Diagonal vorwärts", "Seitwärts", "Rückwärts"), 1, "Bauern schlagen diagonal nach vorn."),
        )
    ),
    RuleLesson(
        id = "capture_values", order = 4, level = RuleLevel.BEGINNER,
        title = "Schlagen & Figurenwerte",
        summary = "Wie viel ist welche Figur wert – und wann lohnt sich ein Tausch?",
        prerequisites = listOf("knight_king_pawn"),
        steps = listOf(
            ExplainStep("Schlagen: Du ziehst auf ein Feld mit einer gegnerischen Figur und nimmst sie vom Brett. Geschlagen wird nur gegnerisches Material.", "4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1", listOf(Marker("d5", MarkerKind.ATTACK)), listOf(Arrow("d1", "d5", MarkerKind.ATTACK)), moveUci = "d1d5"),
            ExplainStep("Figurenwerte als Lernhilfe: Bauer 1, Springer 3, Läufer 3, Turm 5, Dame 9. Der König ist unbezahlbar.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", listOf(Marker("d1", MarkerKind.FOCUS), Marker("a1", MarkerKind.FOCUS), Marker("b1", MarkerKind.FOCUS), Marker("c1", MarkerKind.FOCUS), Marker("a2", MarkerKind.FOCUS)), emptyList()),
            ExplainStep("Ein Tausch ist gleichwertig (Springer gegen Läufer), gewinnbringend (Turm schlägt Dame) oder verlustreich (Dame schlägt Bauer, der gedeckt ist).", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Schlagen ist nie Pflicht – außer es ist der einzige Weg, ein Schach zu beenden.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
            RuleExample("4k3/8/8/3q4/8/8/8/3RK3 w - - 0 1", "d1d5", "Turm (5) schlägt Dame (9): ein Gewinn von 4 Punkten.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/8/3p4/3RK3 w - - 0 1", "d1d2", "Der Turm darf den Bauern schlagen, den der König nicht decken kann… hier ist er ungedeckt.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/8/8/8/8/8/3P4/3RK3 w - - 0 1", "d1d2", "Eigene Figuren darf man nicht schlagen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Eigene Figuren schlagen wollen.", "Zu viel für wenig geben (Dame gegen gedeckten Bauern).", "Glauben, Schlagen sei immer Pflicht."),
        exercises = listOf(
            RuleExercise("Schlage die wertvollste ungedeckte Figur.", "4k3/8/8/1q6/8/8/5r2/1R2K3 w - - 0 1", listOf("b1b5"), "Die Dame ist 9 wert.", "Dame (9) ist mehr wert als Turm (5).", mapOf("e1f2" to "Der Turm (5) ist weniger wert als die Dame (9).")),
            RuleExercise("Schlage den Springer mit dem Läufer.", "4k3/8/8/4n3/8/2B5/8/4K3 w - - 0 1", listOf("c3e5"), "Diagonale c3–e5.", "Ein gleichwertiger Tausch… der Springer ist ungedeckt, also Materialgewinn.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wie viele Punkte ist ein Turm wert?", listOf("3", "5", "7", "9"), 1, "Turm = 5."),
            QuizQuestion("Was ist mehr wert: Läufer oder Springer?", listOf("Läufer", "Springer", "Beide etwa gleich (3)", "Springer 4, Läufer 3"), 2, "Beide gelten als etwa 3 Bauern."),
            QuizQuestion("Muss man schlagen, wenn man kann?", listOf("Ja, immer", "Nein, nie", "Nein – es ist freiwillig", "Nur mit der Dame"), 2, "Im Schach gibt es keinen Schlagzwang."),
        )
    ),
    RuleLesson(
        id = "check", order = 5, level = RuleLevel.BEGINNER,
        title = "Schach",
        summary = "Der König wird angegriffen – es gibt genau drei Auswege.",
        prerequisites = listOf("capture_values"),
        steps = listOf(
            ExplainStep("Schach: Der König steht unter Angriff. Der Spieler muss sofort reagieren – andere Züge sind verboten.", "4r1k1/8/8/8/8/8/8/4K3 w - - 0 1", listOf(Marker("e1", MarkerKind.ATTACK)), listOf(Arrow("e8", "e1", MarkerKind.ATTACK))),
            ExplainStep("Ausweg 1: Der König zieht auf ein sicheres Feld.", "4r1k1/8/8/8/8/8/8/4K3 w - - 0 1", listOf(Marker("d1", MarkerKind.SAFE), Marker("f1", MarkerKind.SAFE), Marker("d2", MarkerKind.SAFE), Marker("f2", MarkerKind.SAFE)), emptyList(), moveUci = "e1f1"),
            ExplainStep("Ausweg 2: Eine Figur stellt sich dazwischen.", "4r1k1/8/8/8/8/8/3R4/4K3 w - - 0 1", listOf(Marker("e2", MarkerKind.SAFE)), listOf(Arrow("d2", "e2", MarkerKind.SAFE)), moveUci = "d2e2"),
            ExplainStep("Ausweg 3: Die angreifende Figur wird geschlagen. Gegen ein Springerschach hilft kein Dazwischenstellen.", "4k3/8/8/8/8/3n4/8/3QK3 w - - 0 1", listOf(Marker("d3", MarkerKind.ATTACK)), listOf(Arrow("d1", "d3", MarkerKind.SAFE)), moveUci = "d1d3"),
        ),
        allowed = listOf(
            RuleExample("4r1k1/8/8/8/8/8/3R4/4K3 w - - 0 1", "d2e2", "Der Turm stellt sich dazwischen.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/3n4/8/3QK3 w - - 0 1", "d1d3", "Die Dame schlägt den Springer.", emptyList(), emptyList()),
            RuleExample("4r1k1/8/8/8/8/8/8/4K3 w - - 0 1", "e1f1", "Der König weicht aus.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4r1k1/8/8/8/8/8/8/4K3 w - - 0 1", "e1e2", "Der König bliebe auf der e-Linie im Schach.", emptyList(), emptyList()),
            RuleExample("4r1k1/8/8/8/8/8/3R4/4K3 w - - 0 1", "d2d3", "Dieser Zug beendet das Schach nicht.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Einen anderen Zug machen, obwohl der König im Schach steht.", "In ein Schach hineinziehen.", "Glauben, man müsse „Schach!“ ansagen – das ist nicht nötig."),
        exercises = listOf(
            RuleExercise("Beende das Schach, indem du dazwischenziehst.", "4r1k1/8/8/8/8/8/3R4/4K3 w - - 0 1", listOf("d2e2"), "Auf das Feld zwischen den Türmen.", "Der Turm blockt die e-Linie.", mapOf("e1f1" to "Das beendet das Schach auch, aber die Aufgabe will einen Block.", "e1d1" to "Auch das beendet das Schach, aber die Aufgabe will einen Block.")),
            RuleExercise("Schlage die schachbietende Figur.", "4k3/8/8/8/8/3n4/8/3QK3 w - - 0 1", listOf("d1d3"), "Die Dame schlägt auf d3.", "Gegen den Springer hilft nur Wegziehen oder Schlagen.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wie viele Möglichkeiten gibt es, ein Schach zu beenden?", listOf("Eine", "Zwei", "Drei", "Vier"), 2, "Wegziehen, Dazwischenstellen, Schlagen."),
            QuizQuestion("Kann man ein Springerschach blockieren?", listOf("Ja", "Nein", "Nur mit Bauern", "Nur mit der Dame"), 1, "Der Springer springt – nur Wegziehen oder Schlagen."),
            QuizQuestion("Muss man „Schach“ ansagen?", listOf("Ja", "Nein", "Nur bei der Dame", "Nur im Turnier"), 1, "Ansagen ist nicht Pflicht."),
        )
    ),
    RuleLesson(
        id = "checkmate", order = 6, level = RuleLevel.BEGINNER,
        title = "Schachmatt",
        summary = "Schach ohne Ausweg beendet die Partie.",
        prerequisites = listOf("check"),
        steps = listOf(
            ExplainStep("Schachmatt: Der König steht im Schach und kein legaler Zug kann das Schach beenden. Die Partie ist sofort zu Ende.", "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", listOf(Marker("g8", MarkerKind.ATTACK)), listOf(Arrow("a1", "a8", MarkerKind.ATTACK)), moveUci = "a1a8"),
            ExplainStep("Grundreihenmatt: Eigene Bauern nehmen dem König alle Fluchtfelder.", "R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1", listOf(Marker("a8", MarkerKind.ATTACK), Marker("f8", MarkerKind.ILLEGAL), Marker("h8", MarkerKind.ILLEGAL), Marker("f7", MarkerKind.ILLEGAL), Marker("g7", MarkerKind.ILLEGAL), Marker("h7", MarkerKind.ILLEGAL)), emptyList()),
            ExplainStep("Leitermatt: Zwei Türme schieben den König Reihe für Reihe an den Rand.", "7k/R7/8/8/8/8/8/1R4K1 w - - 0 1", listOf(Marker("b8", MarkerKind.ATTACK)), listOf(Arrow("b1", "b8", MarkerKind.ATTACK)), moveUci = "b1b8"),
            ExplainStep("Dame und König: Die Dame wird vom König gedeckt – matt.", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf(Marker("g7", MarkerKind.ATTACK)), listOf(Arrow("g1", "g7", MarkerKind.ATTACK)), moveUci = "g1g7"),
        ),
        allowed = listOf(
            RuleExample("6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", "a1a8", "Matt auf der Grundreihe.", emptyList(), emptyList()),
            RuleExample("7k/R7/8/8/8/8/8/1R4K1 w - - 0 1", "b1b8", "Leitermatt.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1", "g8f8", "Nach dem Matt ist kein Zug mehr möglich – f8 wird vom Turm angegriffen.", emptyList(), emptyList()),
            RuleExample("R5k1/5ppp/8/8/8/8/8/6K1 b - - 0 1", "g7g6", "Der Bauer darf nicht ziehen – das Schach bleibt bestehen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Schach und Matt verwechseln.", "Matt „übersehen“: erst alle Fluchtfelder prüfen.", "Mit ungedeckter Dame matt setzen wollen – der König schlägt sie."),
        exercises = listOf(
            RuleExercise("Setze matt.", "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", listOf("a1a8"), "Auf die 8. Reihe.", "Turm auf a8: Die eigenen Bauern sperren den König ein.", emptyMap()),
            RuleExercise("Setze matt.", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf("g1g7"), "Die Dame wird vom König gedeckt.", "Dg7 wird von Kf6 gedeckt.", emptyMap()),
            RuleExercise("Setze matt (Leitermatt).", "7k/R7/8/8/8/8/8/1R4K1 w - - 0 1", listOf("b1b8"), "Der zweite Turm geht auf die 8. Reihe.", "Rb8: der König hat keine Felder.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was passiert nach Schachmatt?", listOf("Man darf einen Zug zurück", "Die Partie endet sofort", "Der Gegner zieht noch einmal", "Es wird gelost"), 1, "Die Partie ist beendet; der mattsetzende Spieler gewinnt."),
            QuizQuestion("Wann ist Schach auch Matt?", listOf("Wenn die Dame angreift", "Wenn es keinen legalen Ausweg gibt", "Wenn der Turm angreift", "Wenn der König nicht ziehen kann"), 1, "Matt = Schach ohne Ausweg (Wegziehen, Schlagen, Blocken)."),
            QuizQuestion("Warum ist Dg7 in der Stellung ein Matt?", listOf("Die Dame ist gedeckt", "Die Dame ist stark", "Der König kann ziehen", "Es ist ein Doppelschach"), 0, "Der König kann die gedeckte Dame nicht schlagen.", fen = "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1"),
        )
    ),
    RuleLesson(
        id = "stalemate", order = 7, level = RuleLevel.BEGINNER,
        title = "Patt",
        summary = "Kein legaler Zug und kein Schach: Das ist Remis.",
        prerequisites = listOf("checkmate"),
        steps = listOf(
            ExplainStep("Patt: Der Spieler am Zug hat keinen legalen Zug, steht aber nicht im Schach. Die Partie endet remis (unentschieden).", "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", listOf(Marker("h8", MarkerKind.FOCUS), Marker("g8", MarkerKind.ILLEGAL), Marker("g7", MarkerKind.ILLEGAL), Marker("h7", MarkerKind.ILLEGAL)), emptyList()),
            ExplainStep("Gefährlich, wenn du stark führst: Ein „fast-Matt“-Zug kann Patt sein.", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf(Marker("g6", MarkerKind.ILLEGAL), Marker("g7", MarkerKind.LEGAL)), listOf(Arrow("g1", "g6", MarkerKind.ILLEGAL))),
            ExplainStep("Richtig ist hier das Matt: Dg7 wird vom König gedeckt.", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf(Marker("g7", MarkerKind.LEGAL)), listOf(Arrow("g1", "g7", MarkerKind.LEGAL)), moveUci = "g1g7"),
            ExplainStep("Patt rettet manchmal den Schwächeren – es ist eine echte Verteidigungsidee.", "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
            RuleExample("7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", "g1g7", "Matt statt Patt.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", "h8g8", "Der Dame auf f7 greift g8 an – der König darf dort nicht hin.", emptyList(), emptyList()),
            RuleExample("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1", "h8h7", "h7 wird von der Dame angegriffen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Bei klarem Vorteil die Dame „einsperren“ und Patt geben.", "Glauben, Patt sei ein Sieg.", "Vor dem Zug nicht prüfen: Hat der Gegner noch einen legalen Zug?"),
        exercises = listOf(
            RuleExercise("Setze matt – ohne Patt!", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf("g1g7"), "Dame ins Feld neben dem König.", "Dg7# – Dg6 wäre Patt.", mapOf("g1g6" to "Patt! Der Schwarze hat keinen legalen Zug mehr und steht nicht im Schach.")),
            RuleExercise("Setze matt.", "k7/8/1K6/8/8/8/7Q/8 w - - 0 1", listOf("h2h8"), "Die Dame geht auf die 8. Reihe.", "Dh8# – Matt auf der 8. Reihe.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wie endet eine Partie bei Patt?", listOf("Der Angreifer gewinnt", "Remis", "Der Spieler am Zug gewinnt", "Sie wird fortgesetzt"), 1, "Patt ist Remis."),
            QuizQuestion("Was ist der Unterschied zu Matt?", listOf("Beim Patt steht der König im Schach", "Beim Patt steht der König nicht im Schach", "Kein Unterschied", "Patt gibt es nur mit der Dame"), 1, "Beim Patt steht der König NICHT im Schach."),
            QuizQuestion("Ist die Stellung Patt (Schwarz am Zug)?", listOf("Ja", "Nein, es ist Matt", "Nein, Schwarz kann ziehen", "Nur mit Weiß am Zug"), 0, "Kh8 hat keinen Zug und ist nicht im Schach.", fen = "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"),
        )
    ),
    RuleLesson(
        id = "castling", order = 8, level = RuleLevel.ADVANCED,
        title = "Rochade",
        summary = "König in Sicherheit, Turm ins Spiel – aber nur unter Bedingungen.",
        prerequisites = listOf("check"),
        steps = listOf(
            ExplainStep("Die Rochade ist der einzige Zug, bei dem zwei Figuren ziehen: Der König geht zwei Felder zum Turm, der Turm springt auf die andere Seite des Königs.", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", listOf(Marker("g1", MarkerKind.LEGAL), Marker("f1", MarkerKind.LEGAL), Marker("c1", MarkerKind.LEGAL), Marker("d1", MarkerKind.LEGAL)), listOf(Arrow("e1", "g1", MarkerKind.LEGAL)), moveUci = "e1g1"),
            ExplainStep("Kurz (O-O): König nach g1, Turm h1 nach f1. Lang (O-O-O): König nach c1, Turm a1 nach d1.", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", listOf(Marker("c1", MarkerKind.LEGAL), Marker("d1", MarkerKind.LEGAL)), listOf(Arrow("e1", "c1", MarkerKind.LEGAL)), moveUci = "e1c1"),
            ExplainStep("Voraussetzungen: König und dieser Turm haben sich noch nie bewegt; zwischen ihnen stehen keine Figuren; der König steht nicht im Schach.", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Außerdem darf der König weder über ein bedrohtes Feld ziehen noch auf einem landen. Der Turm darf bedroht sein, und bei der langen Rochade darf b1 bedroht sein.", "1r2k3/8/8/8/8/8/8/R3K2R w KQ - 0 1", listOf(Marker("b1", MarkerKind.ATTACK), Marker("c1", MarkerKind.SAFE), Marker("d1", MarkerKind.SAFE)), emptyList()),
        ),
        allowed = listOf(
            RuleExample("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1g1", "Kurze Rochade.", emptyList(), emptyList()),
            RuleExample("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", "e1c1", "Lange Rochade.", emptyList(), emptyList()),
            RuleExample("1r2k3/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1c1", "b1 darf bedroht sein – nur c1 und d1 zählen.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4kr2/8/8/8/8/8/8/R3K2R w KQ - 0 1", "e1g1", "Der König müsste über f1 ziehen, das vom Turm f8 bedroht wird.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/8/4r3/R3K2R w KQ - 0 1", "e1g1", "Der König steht im Schach – keine Rochade.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/8/8/8/RN2K2R w KQ - 0 1", "e1c1", "Der Springer auf b1 steht im Weg.", emptyList(), emptyList()),
            RuleExample("r3k2r/8/8/8/8/8/8/R3K2R w kq - 0 1", "e1g1", "Das Rochaderecht ist verwirkt – der König oder Turm hat sich schon bewegt.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Rochieren, wenn der König im Schach steht.", "Vergessen: Auch ein zurückgekehrter Turm hat das Recht verwirkt.", "Meinen, der Turm dürfe nicht bedroht sein."),
        exercises = listOf(
            RuleExercise("Rochiere kurz.", "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", listOf("e1g1"), "Der König zieht zwei Felder.", "König g1, Turm f1.", emptyMap()),
            RuleExercise("Rochiere lang – obwohl b1 bedroht ist.", "1r2k3/8/8/8/8/8/8/R3K2R w KQ - 0 1", listOf("e1c1"), "Nur c1 und d1 müssen sicher sein.", "Das b-Feld wird vom König nicht betreten.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wann darf man nicht rochieren?", listOf("Wenn der Turm bedroht ist", "Wenn der König im Schach steht", "Wenn der Gegner am Zug ist", "Wenn man Weiß ist"), 1, "Aus dem Schach heraus ist keine Rochade erlaubt."),
            QuizQuestion("Darf der König über ein bedrohtes Feld rochieren?", listOf("Ja", "Nein", "Nur kurz", "Nur lang"), 1, "Weder über noch auf ein bedrohtes Feld."),
            QuizQuestion("Verliert man das Rochaderecht, wenn der Turm weggezogen und zurückgekehrt ist?", listOf("Nein", "Ja", "Nur den halben Anspruch", "Nur bei Schwarz"), 1, "Einmal bewegt, ist das Recht für diesen Turm weg."),
        )
    ),
    RuleLesson(
        id = "en_passant", order = 9, level = RuleLevel.ADVANCED,
        title = "En passant",
        summary = "Der Bauer darf „im Vorbeigehen“ schlagen – aber nur sofort.",
        prerequisites = listOf("knight_king_pawn"),
        steps = listOf(
            ExplainStep("Schwarz zieht seinen Bauern zwei Felder vor und steht neben einem weißen Bauern auf der 5. Reihe.", "4k3/3p4/8/4P3/8/8/8/4K3 b - - 0 1", listOf(Marker("e5", MarkerKind.FOCUS), Marker("d5", MarkerKind.FOCUS)), listOf(Arrow("d7", "d5", MarkerKind.FOCUS)), moveUci = "d7d5"),
            ExplainStep("Jetzt darf Weiß den Bauern so schlagen, als hätte er nur ein Feld gezogen: e5 schlägt auf d6.", "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", listOf(Marker("d6", MarkerKind.LEGAL)), listOf(Arrow("e5", "d6", MarkerKind.LEGAL)), moveUci = "e5d6"),
            ExplainStep("Der geschlagene Bauer d5 wird vom Brett genommen, der weiße Bauer landet auf d6.", "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", listOf(Marker("d5", MarkerKind.ATTACK), Marker("d6", MarkerKind.LEGAL)), emptyList()),
            ExplainStep("Das Recht gilt nur unmittelbar im nächsten Zug. Wird er nicht sofort genutzt, verfällt es.", "4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1", listOf(Marker("d6", MarkerKind.ILLEGAL)), emptyList()),
        ),
        allowed = listOf(
            RuleExample("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", "e5d6", "En passant – direkt nach dem Doppelschritt.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1", "d4e3", "Auch Schwarz kann en passant schlagen (von der 4. Reihe).", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/8/8/3pP3/8/8/8/4K3 w - - 0 1", "e5d6", "Der Doppelschritt liegt zu weit zurück – das Recht ist verfallen.", emptyList(), emptyList()),
            RuleExample("8/8/8/KPp4r/8/8/8/7k w - c6 0 1", "b5c6", "Verboten, weil danach der eigene König vom Turm h5 im Schach stünde.", emptyList(), emptyList()),
        ),
        mistakes = listOf("En passant auch Wochen später versuchen (nur unmittelbar!).", "Mit Figuren statt Bauern en passant schlagen wollen.", "Das Feld falsch wählen: Man landet hinter dem geschlagenen Bauern."),
        exercises = listOf(
            RuleExercise("Schlage en passant.", "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", listOf("e5d6"), "Diagonal auf das übersprungene Feld.", "Der Bauer d5 verschwindet.", mapOf("e5e6" to "Normaler Vorstoß – die Aufgabe will en passant.")),
            RuleExercise("Schlage als Schwarz en passant.", "4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1", listOf("d4e3"), "Diagonal nach e3.", "Der weiße Bauer e4 wird entfernt.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wann ist En passant möglich?", listOf("Immer", "Nur unmittelbar nach dem Doppelschritt", "Nach 2 Zügen", "Nur mit Weiß"), 1, "Nur im direkt folgenden Zug."),
            QuizQuestion("Auf welcher Reihe steht der schlagende weiße Bauer?", listOf("Der 4.", "Der 5.", "Der 6.", "Der 7."), 1, "Weißer Bauer steht auf der 5. Reihe (Schwarz auf der 4.)."),
            QuizQuestion("Muss man en passant schlagen?", listOf("Ja", "Nein", "Nur als Schwarz", "Nur mit Schach"), 1, "Es ist freiwillig."),
        )
    ),
    RuleLesson(
        id = "promotion", order = 10, level = RuleLevel.ADVANCED,
        title = "Bauernumwandlung",
        summary = "Am Brettrand wird der Bauer zur neuen Figur.",
        prerequisites = listOf("en_passant"),
        steps = listOf(
            ExplainStep("Erreicht ein Bauer die letzte Reihe, muss er sich umwandeln – in Dame, Turm, Läufer oder Springer.", "4k3/P7/8/8/8/8/8/4K3 w - - 0 1", listOf(Marker("a8", MarkerKind.LEGAL)), listOf(Arrow("a7", "a8", MarkerKind.LEGAL)), moveUci = "a7a8q"),
            ExplainStep("Meistens wählt man die Dame. Nie erlaubt: König oder Bauer; und der Bauer darf nicht Bauer bleiben.", "4k3/P7/8/8/8/8/8/4K3 w - - 0 1", emptyList(), emptyList()),
            ExplainStep("Unterumwandlung: Manchmal ist ein Springer besser, z. B. für ein Gabelschach auf König und Dame.", "8/4P1k1/3q4/8/8/8/8/4K3 w - - 0 1", listOf(Marker("e8", MarkerKind.FOCUS), Marker("g7", MarkerKind.ATTACK), Marker("d6", MarkerKind.ATTACK)), listOf(Arrow("e7", "e8", MarkerKind.LEGAL)), moveUci = "e7e8n"),
            ExplainStep("Auch mit Schlag möglich: Der Bauer zieht diagonal auf die letzte Reihe und schlägt dabei.", "3rk3/2P5/8/8/8/8/8/4K3 w - - 0 1", listOf(Marker("d8", MarkerKind.ATTACK)), listOf(Arrow("c7", "d8", MarkerKind.ATTACK)), moveUci = "c7d8q"),
        ),
        allowed = listOf(
            RuleExample("4k3/P7/8/8/8/8/8/4K3 w - - 0 1", "a7a8q", "Umwandlung in eine Dame.", emptyList(), emptyList()),
            RuleExample("4k3/P7/8/8/8/8/8/4K3 w - - 0 1", "a7a8n", "Auch ein Springer ist erlaubt.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/P7/8/8/8/8/8/4K3 w - - 0 1", "a7a6", "Ein Bauer zieht nie rückwärts.", emptyList(), emptyList()),
            RuleExample("n3k3/P7/8/8/8/8/8/4K3 w - - 0 1", "a7a8", "Der Springer auf a8 blockiert den Bauern.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Den Bauern auf der letzten Reihe liegen lassen.", "In einen König umwandeln wollen.", "Immer Dame wählen – manchmal gewinnt der Springer, manchmal verhindert eine Unterumwandlung das Patt."),
        exercises = listOf(
            RuleExercise("Wandle in eine Dame um.", "4k3/P7/8/8/8/8/8/4K3 w - - 0 1", listOf("a7a8q"), "Der Bauer geht auf a8.", "Dame ist meist die stärkste Wahl.", emptyMap()),
            RuleExercise("Gewinne die Dame mit einer Springerumwandlung.", "8/4P1k1/3q4/8/8/8/8/4K3 w - - 0 1", listOf("e7e8n"), "Der Springer gibt Gabelschach.", "Se8+ greift König g7 und Dame d6 an.", mapOf("e7e8q" to "Mit Dame gibt es hier kein Schach, die Dame d6 schlägt dort.")),
        ),
        quiz = listOf(
            QuizQuestion("In welche Figur darf sich ein Bauer NICHT umwandeln?", listOf("Dame", "Turm", "König", "Springer"), 2, "König (und Bauer) sind nicht erlaubt."),
            QuizQuestion("Muss man sich umwandeln?", listOf("Ja, sofort", "Nein", "Nur mit Schach", "Nur in die Dame"), 0, "Der Zug ist unvollständig, ohne dass die Figur gewählt wird."),
            QuizQuestion("Warum eine Springerumwandlung?", listOf("Weil sie stärker ist", "Weil sie manchmal Gabel/Matt gibt oder Patt vermeidet", "Weil Läufer verboten sind", "Das ist nicht erlaubt"), 1, "Der Springer kann Ziele erreichen, die der Dame verwehrt bleiben."),
        )
    )
    )
}
