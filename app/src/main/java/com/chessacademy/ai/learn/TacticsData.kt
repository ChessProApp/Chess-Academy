package com.chessacademy.ai.learn

/** Taktikaufgaben, je Aufgabe mit Stockfish 17 geprüft (Tiefe 16: erster Zug eindeutig bester bzw. Matt). */
object TacticsData {
    val puzzles: List<TacticPuzzle> = listOf(
        TacticPuzzle("g1", "Gabel", 1, "Springergabel auf König und Dame", "6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1", listOf("d5e7"), "Se7+ greift König g8 und Dame c6 gleichzeitig an."),
        TacticPuzzle("s1", "Spieß", 1, "Turm spießt König und Dame", "3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1", listOf("a1d1"), "Td1+ – der König muss weichen, danach fällt die Dame d8."),
        TacticPuzzle("f1", "Fesselung", 2, "Bauer gegen gefesselten Springer", "4k3/4n3/8/3P4/8/8/8/4R1K1 w - - 0 1", listOf("d5d6"), "Der Springer e7 ist gefesselt und kann dem Bauernangriff nicht ausweichen."),
        TacticPuzzle("d1", "Doppelschach", 2, "Springer-Doppelschach", "4k3/1q6/8/8/4N3/8/8/4R1K1 w - - 0 1", listOf("e4d6"), "Sd6+ ist Doppelschach und greift die Dame b7 an."),
        TacticPuzzle("m1", "Grundreihenmatt", 1, "Grundreihenmatt mit dem Turm", "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1", listOf("a1a8"), "Ta8#: Die eigenen Bauern sperren den König ein."),
        TacticPuzzle("m2", "Grundreihenmatt", 2, "Turm schlägt und setzt matt", "3r2k1/5ppp/8/8/8/8/5PPP/3RR1K1 w - - 0 1", listOf("d1d8"), "Txd8# – der zweite Turm sichert die Grundreihe."),
        TacticPuzzle("m3", "Mattmuster", 2, "Erstickes Matt", "6rk/6pp/8/6N1/8/8/8/6K1 w - - 0 1", listOf("g5f7"), "Sf7# – der König wird von seinen eigenen Figuren erstickt."),
        TacticPuzzle("m4", "Mattmuster", 2, "Arabisches Matt", "7k/R7/5N2/8/8/8/8/6K1 w - - 0 1", listOf("a7h7"), "Th7# – der Springer f6 deckt das Mattfeld und g8."),
        TacticPuzzle("m5", "Mattnetz", 1, "Dame und König", "7k/8/5K2/8/8/8/8/6Q1 w - - 0 1", listOf("g1g7"), "Dg7# – die Dame wird vom König gedeckt."),
        TacticPuzzle("m6", "Mattnetz", 2, "Leitermatt", "7k/R7/8/8/8/8/8/1R4K1 w - - 0 1", listOf("b1b8"), "Tb8# – zwei Türme sperren die 7. und 8. Reihe."),
        TacticPuzzle("m7", "Abzugsschach", 3, "Abzugsmatt", "3rk3/3p1p2/8/4B3/8/8/8/4R1K1 w - - 0 1", listOf("e5g7"), "Lg7# – der Läufer gibt die e-Linie frei und deckt f8."),
        TacticPuzzle("v1", "Entfernung der Verteidigung", 3, "Turm tauscht den Verteidiger", "2r3k1/5ppp/8/8/8/8/5PPP/2R1R1K1 w - - 0 1", listOf("c1c8"), "Txc8# – der Verteidiger wird abgeräumt."),
        TacticPuzzle("v2", "Ablenkung", 3, "Dame räumt die Grundreihe", "4r1k1/5ppp/8/4Q3/8/8/5PPP/4R1K1 w - - 0 1", listOf("e5e8"), "Dxe8# – die Dame schlägt den einzigen Verteidiger mit Matt."),
        TacticPuzzle("m8", "Mattmuster", 3, "Turmschlag auf der Grundreihe", "r5k1/5ppp/8/8/8/8/5PPP/R3R1K1 w - - 0 1", listOf("a1a8"), "Txa8# – schlage den Turm, die Grundreihe ist leer.")
    )
}
