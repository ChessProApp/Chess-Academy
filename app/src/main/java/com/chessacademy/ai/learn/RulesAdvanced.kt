package com.chessacademy.ai.learn

/** Regelkurs Teil 2: Taktikmotive, Remisregeln, Mattmaterial. Alle Stellungen/Züge sind testgeprüft (tests/jvm). */
object RulesAdvanced {
    val lessons: List<RuleLesson> = listOf(
    RuleLesson(
        id = "double_check", order = 11, level = RuleLevel.ADVANCED,
        title = "Doppelschach",
        summary = "Zwei Figuren geben gleichzeitig Schach – nur der König kann antworten.",
        prerequisites = listOf("check"),
        steps = listOf(
            ExplainStep("Beim Doppelschach greifen zwei Figuren den König gleichzeitig an. Schlagen oder Blocken kann nur eine davon stoppen – also bleibt nur der Königszug.", "4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", listOf(Marker("e8", MarkerKind.ATTACK)), listOf(Arrow("e4", "d6", MarkerKind.ATTACK))),
            ExplainStep("Meist entsteht es durch ein Abzugsschach: Der Springer räumt die e-Linie (Turm gibt Schach) und schlägt selbst Schach.", "4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", listOf(Marker("d6", MarkerKind.LEGAL)), emptyList(), moveUci = "e4d6"),
            ExplainStep("Nach Sd6+ kann Schwarz nur den König bewegen.", "4k3/8/3N4/8/8/8/8/4R1K1 b - - 0 1", listOf(Marker("d8", MarkerKind.SAFE), Marker("d7", MarkerKind.SAFE), Marker("f8", MarkerKind.SAFE)), emptyList()),
        ),
        allowed = listOf(
            RuleExample("4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", "e4d6", "Doppelschach durch Abzug.", emptyList(), emptyList()),
            RuleExample("4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", "e4f6", "Auch f6 gibt Doppelschach.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4kb2/8/3N4/8/8/8/8/4R1K1 b - - 0 1", "f8d6", "Läufer schlägt den Springer – aber der Turm gibt weiter Schach.", emptyList(), emptyList()),
            RuleExample("4kb2/8/3N4/8/8/8/8/4R1K1 b - - 0 1", "f8e7", "Der Läufer blockt den Turm, der Springer gibt aber weiter Schach.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Bei Doppelschach versuchen zu schlagen oder zu blocken.", "Eine Figur nur auf ein Schach reagieren lassen."),
        exercises = listOf(
            RuleExercise("Gib Doppelschach.", "4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", listOf("e4d6", "e4f6"), "Der Springer räumt die e-Linie.", "Turm und Springer geben zugleich Schach.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wer kann auf ein Doppelschach antworten?", listOf("Jede Figur", "Nur der König", "Nur die Dame", "Niemand"), 1, "Nur ein Königszug beendet beide Schachs."),
            QuizQuestion("Wie entsteht Doppelschach oft?", listOf("Durch Rochade", "Durch Abzugsschach", "Durch Patt", "Durch En passant"), 1, "Die ziehende Figur gibt frei und selbst Schach."),
            QuizQuestion("Kann man ein Doppelschach durch Schlagen beenden?", listOf("Ja, immer", "Nein, außer der König schlägt", "Nur mit der Dame", "Nur mit Bauern"), 1, "Der König schlägt ggf. selbst – sonst hilft nur Wegziehen."),
        )
    ),
    RuleLesson(
        id = "pin", order = 12, level = RuleLevel.ADVANCED,
        title = "Fesselung",
        summary = "Eine Figur ist gebunden, weil dahinter etwas Wertvolleres steht.",
        prerequisites = listOf("check"),
        steps = listOf(
            ExplainStep("Absolute Fesselung: Zieht die Figur weg, stünde der eigene König im Schach. Sie darf dann nicht ziehen.", "4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1", listOf(Marker("e2", MarkerKind.FOCUS), Marker("e8", MarkerKind.ATTACK)), listOf(Arrow("e8", "e1", MarkerKind.ATTACK))),
            ExplainStep("Eine gefesselte Figur darf noch entlang der Fesselungslinie ziehen.", "4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1", listOf(Marker("e3", MarkerKind.LEGAL), Marker("e4", MarkerKind.LEGAL), Marker("e5", MarkerKind.LEGAL), Marker("e6", MarkerKind.LEGAL), Marker("e7", MarkerKind.LEGAL), Marker("e8", MarkerKind.ATTACK)), emptyList()),
            ExplainStep("Relative Fesselung: Hinter der Figur steht etwas Wertvolles (z. B. Dame), nicht der König. Die Figur darf ziehen – verliert aber das Dahinterstehende.", "4k3/4p3/8/8/1b6/8/3N4/4QK2 w - - 0 1", listOf(Marker("d2", MarkerKind.FOCUS), Marker("e1", MarkerKind.ATTACK)), listOf(Arrow("b4", "e1", MarkerKind.ATTACK))),
            ExplainStep("Gefesselte Figuren sind Angriffsziele: Man greift sie mehrfach an, ohne dass sie ausweichen können.", "4k3/4n3/8/3P4/8/8/8/4R1K1 w - - 0 1", listOf(Marker("e7", MarkerKind.FOCUS)), listOf(Arrow("d5", "d6", MarkerKind.LEGAL)), moveUci = "d5d6"),
        ),
        allowed = listOf(
            RuleExample("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1", "e2e5", "Entlang der Fesselungslinie bleibt der König gedeckt.", emptyList(), emptyList()),
            RuleExample("4k3/4p3/8/8/1b6/8/3N4/4QK2 w - - 0 1", "d2f3", "Erlaubt, aber nach Lxe1 wäre die Dame weg.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1", "e2c3", "Der Springer ist absolut gefesselt: Der König stünde im Schach.", emptyList(), emptyList()),
            RuleExample("4r1k1/8/8/8/8/8/4N3/4K3 w - - 0 1", "e2g3", "Auch g3 ist verboten – gefesselte Springer können nie ziehen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Eine absolute Fesselung vergessen und die Figur ziehen wollen.", "Relative Fesselung wie absolute behandeln (sie ist erlaubt, aber teuer).", "Meinen, gefesselte Figuren dürften nicht schlagen – sie dürfen entlang der Linie."),
        exercises = listOf(
            RuleExercise("Gewinne die gefesselte Figur: Greife den Springer mit einem Bauern an.", "4k3/4n3/8/3P4/8/8/8/4R1K1 w - - 0 1", listOf("d5d6"), "Ein Bauernzug greift e7 an.", "Der Springer kann nicht ausweichen.", emptyMap()),
            RuleExercise("Ziehe den gefesselten Turm entlang der Linie.", "4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1", listOf("e2e3", "e2e4", "e2e5", "e2e6", "e2e7", "e2e8"), "Bleibe auf der e-Linie.", "Nur entlang der Linie ist erlaubt.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was bedeutet absolute Fesselung?", listOf("Figur darf nicht ziehen, weil der König sonst im Schach steht", "Figur darf nur schlagen", "Figur verliert ein Tempo", "Figur darf nur rückwärts"), 0, "Sonst stünde der König im Schach."),
            QuizQuestion("Darf eine absolut gefesselte Figur entlang der Fesselungslinie ziehen?", listOf("Ja", "Nein", "Nur schlagen", "Nur der Turm"), 0, "Der König bleibt gedeckt."),
            QuizQuestion("Was unterscheidet relative von absoluter Fesselung?", listOf("Dahinter steht kein König", "Sie gilt nur für Läufer", "Sie ist verboten", "Sie gibt Schach"), 0, "Dahinter steht eine andere Figur; ziehen ist erlaubt."),
        )
    ),
    RuleLesson(
        id = "skewer", order = 13, level = RuleLevel.ADVANCED,
        title = "Spieß",
        summary = "Die wertvolle Figur muss weichen – dahinter fällt die andere.",
        prerequisites = listOf("pin"),
        steps = listOf(
            ExplainStep("Beim Spieß steht vorne die wertvollere Figur. Sie muss ausweichen, dann wird die dahinter geschlagen.", "3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", listOf(Marker("d5", MarkerKind.FOCUS), Marker("d8", MarkerKind.ATTACK)), listOf(Arrow("a1", "d1", MarkerKind.ATTACK)), moveUci = "a1d1"),
            ExplainStep("Unterschied zur Fesselung: Bei der Fesselung steht das Wertvollere hinten, beim Spieß vorne.", "3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", emptyList(), emptyList()),
            ExplainStep("Nach Td1+ weicht der König aus, danach nimmt der Turm die Dame.", "3q4/8/8/8/3k4/8/8/3R2K1 b - - 0 1", listOf(Marker("d8", MarkerKind.ATTACK)), emptyList()),
        ),
        allowed = listOf(
            RuleExample("3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", "a1d1", "Schach auf der d-Linie spießt König und Dame.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", "a1d2", "Ein Turm zieht auf d2 nur, wenn er über die d-Linie kommt – von a1 aus ist das kein Turmzug.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Spieß und Fesselung verwechseln.", "Eine Dame hinter einem König nicht bemerken."),
        exercises = listOf(
            RuleExercise("Gib Schach und gewinne die Dame.", "3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", listOf("a1d1"), "Turm auf die d-Linie.", "König und Dame stehen hintereinander.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Wo steht beim Spieß die wertvollere Figur?", listOf("Vorn", "Hinten", "Daneben", "Egal"), 0, "Vorne – sie muss weichen."),
            QuizQuestion("Wie nennt man das Gegenstück (wertvolle Figur hinten)?", listOf("Gabel", "Fesselung", "Opfer", "Rochade"), 1, "Fesselung."),
            QuizQuestion("Welche Figuren spießen am besten?", listOf("Turm, Läufer, Dame", "Springer", "Bauern", "König"), 0, "Langstreckenfiguren."),
        )
    ),
    RuleLesson(
        id = "fork", order = 14, level = RuleLevel.ADVANCED,
        title = "Gabel",
        summary = "Eine Figur greift zwei Ziele gleichzeitig an.",
        prerequisites = listOf("double_check"),
        steps = listOf(
            ExplainStep("Bei der Gabel greift eine Figur zwei oder mehr Ziele an. Der Gegner kann nur eines retten.", "6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", listOf(Marker("g8", MarkerKind.ATTACK), Marker("c6", MarkerKind.ATTACK)), listOf(Arrow("d5", "e7", MarkerKind.ATTACK)), moveUci = "d5e7"),
            ExplainStep("Am bekanntesten ist die Springergabel. Auch Dame, Bauer und König können gabeln.", "r3k3/8/8/3N4/8/8/8/4K3 w - - 0 1", listOf(Marker("c7", MarkerKind.FOCUS), Marker("a8", MarkerKind.ATTACK), Marker("e8", MarkerKind.ATTACK)), listOf(Arrow("d5", "c7", MarkerKind.ATTACK)), moveUci = "d5c7"),
            ExplainStep("Gabel gegen König: Das Schach muss beantwortet werden, danach wird das zweite Ziel geschlagen.", "6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
            RuleExample("6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", "d5e7", "Springergabel auf König und Dame.", emptyList(), emptyList()),
            RuleExample("r3k3/8/8/3N4/8/8/8/4K3 w - - 0 1", "d5c7", "Königsgabel mit Turmgewinn.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", "d5d7", "Ein Springer zieht nicht gerade.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Nur auf eigene Angriffe achten – auch Gegner gabeln.", "Gabelfeld nicht auf Deckung prüfen."),
        exercises = listOf(
            RuleExercise("Gabel auf König und Dame.", "6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", listOf("d5e7"), "Springer nach e7.", "Se7+ greift g8 und c6 an.", emptyMap()),
            RuleExercise("Gabel auf König und Turm.", "r3k3/8/8/3N4/8/8/8/4K3 w - - 0 1", listOf("d5c7"), "Springer nach c7.", "Sc7+ greift e8 und a8.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was ist eine Gabel?", listOf("Zwei Ziele gleichzeitig angreifen", "Eine Figur fesseln", "Rochade", "Matt mit zwei Türmen"), 0, "Ein Zug, zwei Ziele."),
            QuizQuestion("Welche Figur gabelt am häufigsten?", listOf("Springer", "Läufer", "König", "Turm"), 0, "Der Springer – schwer zu blockieren."),
            QuizQuestion("Hilft Blocken gegen Springergabel?", listOf("Ja", "Nein", "Immer", "Nur Dame"), 1, "Der Springer springt."),
        )
    ),
    RuleLesson(
        id = "discovered_check", order = 15, level = RuleLevel.ADVANCED,
        title = "Abzugsschach",
        summary = "Eine Figur gibt den Weg frei – dahinter gibt eine andere Schach.",
        prerequisites = listOf("double_check"),
        steps = listOf(
            ExplainStep("Beim Abzugsschach zieht eine Figur weg und gibt dadurch die Linie einer anderen Figur frei.", "4k3/7q/8/8/4B3/8/8/4R1K1 w - - 0 1", listOf(Marker("e4", MarkerKind.FOCUS), Marker("e8", MarkerKind.ATTACK)), listOf(Arrow("e1", "e8", MarkerKind.ATTACK))),
            ExplainStep("Die ziehende Figur kann gleichzeitig etwas schlagen oder bedrohen.", "4k3/7q/8/8/4B3/8/8/4R1K1 w - - 0 1", listOf(Marker("h7", MarkerKind.ATTACK)), listOf(Arrow("e4", "h7", MarkerKind.ATTACK)), moveUci = "e4h7"),
            ExplainStep("Ist die ziehende Figur selbst Schach gebend, entsteht ein Doppelschach.", "4k3/8/8/8/4N3/8/8/4R1K1 w - - 0 1", listOf(Marker("d6", MarkerKind.LEGAL)), emptyList(), moveUci = "e4d6"),
        ),
        allowed = listOf(
            RuleExample("4k3/7q/8/8/4B3/8/8/4R1K1 w - - 0 1", "e4h7", "Läufer schlägt die Dame mit Abzugsschach.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4k3/7q/8/8/4B3/8/8/4R1K1 w - - 0 1", "e4e5", "Ein Läufer zieht nicht gerade.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Abzugsmöglichkeiten vergessen.", "Den eigenen König auf dieselbe Linie stellen (dann gibt der Gegner Abzugsschach)."),
        exercises = listOf(
            RuleExercise("Gewinne die Dame durch Abzugsschach.", "4k3/7q/8/8/4B3/8/8/4R1K1 w - - 0 1", listOf("e4h7"), "Läufer schlägt auf h7.", "Der Gegner muss das Schach beantworten – die Dame ist schon weg.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was ist ein Abzugsschach?", listOf("Schach durch Freigabe einer Linie", "Schach durch den König", "Schach durch Rochade", "Schach durch En passant"), 0, "Die ziehende Figur macht die Linie frei."),
            QuizQuestion("Wann ist es Doppelschach?", listOf("Wenn die ziehende Figur auch Schach gibt", "Nie", "Nur mit Bauern", "Wenn der König zieht"), 0, "Zwei gleichzeitige Schachs."),
            QuizQuestion("Warum ist Abzugsschach stark?", listOf("Der Gegner muss auf das Schach reagieren", "Es ist immer Matt", "Die Figur kann nicht geschlagen werden", "Es zählt doppelt"), 0, "Die Dame bleibt währenddessen ungedeckt."),
        )
    ),
    RuleLesson(
        id = "perpetual_check", order = 16, level = RuleLevel.EXPERT,
        title = "Dauerschach",
        summary = "Wer schlechter steht, kann mit endlosem Schach Remis erzwingen.",
        prerequisites = listOf("draw_rules"),
        steps = listOf(
            ExplainStep("Weiß steht materiell schlechter, hat aber ein Dauerschach: Qe8+ zwingt Kh7.", "6k1/6p1/5p2/8/8/rr6/5PPP/4Q1K1 w - - 0 1", listOf(Marker("g8", MarkerKind.ATTACK)), listOf(Arrow("e1", "e8", MarkerKind.ATTACK)), moveUci = "e1e8"),
            ExplainStep("Danach geht Dh5+ und der König muss zurück nach g8. Qe8+ und das Spiel beginnt von vorn.", "4Q3/6pk/5p2/8/8/rr6/5PPP/6K1 w - - 0 1", listOf(Marker("h7", MarkerKind.ATTACK)), listOf(Arrow("e8", "h5", MarkerKind.ATTACK)), moveUci = "e8h5"),
            ExplainStep("Die Stellung wiederholt sich: Beim dritten Mal darf Weiß Remis reklamieren, bei fünf Wiederholungen ist es automatisch Remis.", "6k1/6p1/5p2/8/8/rr6/5PPP/4Q1K1 w - - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
            RuleExample("6k1/6p1/5p2/8/8/rr6/5PPP/4Q1K1 w - - 0 1", "e1e8", "Qe8+ – Schwarz hat nur Kh7.", emptyList(), emptyList()),
        ),
        forbidden = listOf(
            RuleExample("4Q1k1/6p1/5p2/8/8/rr6/5PPP/6K1 b - - 0 1", "g8f7", "f7 wird von der Dame auf e8 angegriffen.", emptyList(), emptyList()),
        ),
        mistakes = listOf("Ein Dauerschach nicht rechtzeitig bemerken.", "Nicht darauf achten, wie oft sich die Stellung wiederholt."),
        exercises = listOf(
            RuleExercise("Beginne das Dauerschach.", "6k1/6p1/5p2/8/8/rr6/5PPP/4Q1K1 w - - 0 1", listOf("e1e8"), "Dame auf die 8. Reihe.", "Qe8+ Kh7 Qh5+ Kg8 …", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was ist Dauerschach?", listOf("Endlose Schachs, die zum Remis führen", "Ein langes Matt", "Ein Remisangebot", "Ein Patt"), 0, "Der Gegner kann dem Schach nicht entkommen."),
            QuizQuestion("Wann kann man bei Dauerschach Remis reklamieren?", listOf("Nach der dritten Wiederholung der Stellung", "Nach dem ersten Schach", "Nie", "Nach 10 Schachs"), 0, "Dreifache Wiederholung."),
            QuizQuestion("Wann ist es automatisch Remis?", listOf("Nach fünf Wiederholungen", "Nach drei", "Nach zwei", "Nie"), 0, "Fünffache Stellungswiederholung."),
        )
    ),
    RuleLesson(
        id = "draw_rules", order = 17, level = RuleLevel.EXPERT,
        title = "Remisregeln im Überblick",
        summary = "Automatisch oder auf Antrag: Wann endet eine Partie unentschieden?",
        prerequisites = listOf("stalemate"),
        steps = listOf(
            ExplainStep("Automatisch Remis: Patt, unzureichendes Material, fünffache Stellungswiederholung, 75-Züge-Regel.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Auf Reklamation: dreifache Stellungswiederholung und 50-Züge-Regel.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Zusätzlich: Remis durch Einigung. In Chess Academy beenden automatische Fälle die Partie sofort; bei reklamierbaren erscheint „Remis reklamieren“.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("„Gleiche Stellung“ heißt: gleiche Figuren, gleicher Spieler am Zug, gleiche Rochade- und En-passant-Rechte.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
        ),
        forbidden = listOf(
        ),
        mistakes = listOf("Meinen, dreifache Wiederholung beendet die Partie automatisch.", "Den Zähler der 50-Züge-Regel nicht im Blick haben (Bauernzug/Schlag setzt zurück)."),
        exercises = listOf(
            RuleExercise("Beende die Partie: Setze matt, bevor die 75-Züge-Regel greift.", "6k1/5ppp/8/8/8/8/8/R5K1 w - - 149 90", listOf("a1a8"), "Auf die 8. Reihe.", "Matt im letzten Zug geht vor.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Was ist automatisch Remis?", listOf("Dreifache Wiederholung", "50-Züge-Regel", "Fünffache Wiederholung", "Alle Wiederholungen"), 2, "Fünffache Wiederholung (und 75 Züge, Patt, totes Material)."),
            QuizQuestion("Was muss man reklamieren?", listOf("Patt", "Dreifache Wiederholung", "Zugzwang", "Matt"), 1, "Dreifache Wiederholung und 50-Züge-Regel."),
            QuizQuestion("Was zählt als „gleiche Stellung“?", listOf("Nur die Figurenstellung", "Figuren, Zugrecht, Rochade-/En-passant-Rechte", "Nur der Spieler am Zug", "Nur die Dame"), 1, "Alle drei Faktoren."),
        )
    ),
    RuleLesson(
        id = "repetition_fifty", order = 18, level = RuleLevel.EXPERT,
        title = "Wiederholung & 50-Züge",
        summary = "Zähler, Reset und Zugumstellung im Detail.",
        prerequisites = listOf("draw_rules"),
        steps = listOf(
            ExplainStep("Die 50-Züge-Regel zählt Halbzüge ohne Bauernzug und Schlag. Bei 100 Halbzügen darf man reklamieren, bei 150 ist es automatisch Remis.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Ein Bauernzug oder Schlag setzt den Zähler auf 0 zurück.", "4k3/8/8/8/8/8/P7/4K3 w - - 100 80", listOf(Marker("a3", MarkerKind.LEGAL)), listOf(Arrow("a2", "a3", MarkerKind.LEGAL)), moveUci = "a2a3"),
            ExplainStep("Für die Wiederholung zählt nur die Stellung – nicht die Zugfolge (Zugumstellung).", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
            ExplainStep("Verliert ein Spieler ein Rochaderecht, ist die Stellung eine andere – auch bei gleicher Figurenstellung.", "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
        ),
        forbidden = listOf(
        ),
        mistakes = listOf("Meinen, die Zugfolge sei für Wiederholung wichtig.", "Nicht bemerken, dass ein Bauernzug den Zähler zurücksetzt."),
        exercises = listOf(
            RuleExercise("Setze den Zähler der 50-Züge-Regel zurück.", "4k3/8/8/8/8/8/P7/4K3 w - - 100 80", listOf("a2a3", "a2a4"), "Ein Bauernzug hilft.", "Bauernzug = Reset.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Welcher Zug setzt den 50-Züge-Zähler zurück?", listOf("König zieht", "Bauernzug oder Schlag", "Rochade", "Türme tauschen"), 1, "Bauernzug oder Schlag."),
            QuizQuestion("Wie viele Halbzüge für den 50-Züge-Anspruch?", listOf("50", "75", "100", "150"), 2, "50 Züge je Spieler = 100 Halbzüge."),
            QuizQuestion("Bei wie vielen Halbzügen ist automatisch Remis?", listOf("100", "125", "150", "200"), 2, "75-Züge-Regel = 150 Halbzüge."),
        )
    ),
    RuleLesson(
        id = "insufficient_material", order = 19, level = RuleLevel.EXPERT,
        title = "Unzureichendes Mattmaterial",
        summary = "Wenn kein Matt mehr möglich ist, endet die Partie remis.",
        prerequisites = listOf("draw_rules"),
        steps = listOf(
            ExplainStep("König gegen König: kein Matt möglich – sofort Remis.", "4k3/8/8/8/8/8/8/4K3 w - - 0 1", emptyList(), emptyList()),
            ExplainStep("König + Läufer oder König + Springer gegen König genügt nicht für ein Matt.", "4k3/8/8/8/8/8/8/3BK3 w - - 0 1", emptyList(), emptyList()),
            ExplainStep("Läufer gegen Läufer auf gleichfarbigen Feldern: Matt unmöglich – tote Stellung.", "4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1", emptyList(), emptyList()),
            ExplainStep("Nicht tot: zwei Springer, Läufer gegen Springer oder jede Stellung mit Bauer, Turm oder Dame.", "4k3/8/8/8/8/8/8/2NNK3 w - - 0 1", emptyList(), emptyList()),
        ),
        allowed = listOf(
        ),
        forbidden = listOf(
        ),
        mistakes = listOf("Meinen, zwei Springer erzwingen Matt (nur mit Fehlern des Gegners).", "Läufer verschiedener Feldfarben für tot halten."),
        exercises = listOf(
            RuleExercise("Schlage den letzten Turm – und die Partie endet remis.", "4k3/8/8/8/8/8/4r3/4K3 w - - 0 1", listOf("e1e2"), "Der König schlägt.", "Danach bleibt König gegen König.", emptyMap()),
        ),
        quiz = listOf(
            QuizQuestion("Ist König+Läufer gegen König genug für Matt?", listOf("Ja", "Nein", "Nur mit Hilfe", "Nur auf dunklen Feldern"), 1, "Es ist keine Mattführung möglich."),
            QuizQuestion("Sind zwei Springer gegen König eine tote Stellung?", listOf("Ja", "Nein, Matt ist theoretisch möglich", "Nur im Endspiel", "Immer"), 1, "Matt ist nur bei Fehlern möglich, daher nicht tot."),
            QuizQuestion("Wann endet das Spiel bei totem Material?", listOf("Auf Antrag", "Automatisch", "Nach 50 Zügen", "Nach 3 Wiederholungen"), 1, "Automatisch."),
        )
    )
    )
}
