package com.chessacademy.ai.learn

/** Endspiel-Trainings (Ziel per Stockfish 17, Tiefe 20, bestätigt). */
object EndgamesData {
    val drills: List<EndgameDrill> = listOf(
        EndgameDrill("kq", "Dame + König gegen König", "Setze mit Dame und König matt.", "4k3/8/8/8/8/8/8/3QK3 w - - 0 1", true, EndgameGoal.WIN, "Setze in höchstens 20 Zügen matt – ohne Patt.", listOf("Drängle den König mit der Dame ein Feld Abstand an den Rand.", "Hole den eigenen König heran.", "Prüfe vor jedem Zug: Hat Schwarz noch einen legalen Zug? (Patt-Gefahr)")),
        EndgameDrill("kr", "Turm + König gegen König", "Setze mit Turm und König matt.", "4k3/8/8/8/8/8/8/R3K3 w - - 0 1", true, EndgameGoal.WIN, "Setze in höchstens 30 Zügen matt.", listOf("Schneide den König mit dem Turm von einer Linie ab.", "Nutze die Opposition der Könige.", "Das Matt gelingt am Brettrand.")),
        EndgameDrill("kp1", "Bauernendspiel: Schlüsselfelder", "König und Bauer gegen König.", "4k3/8/8/2K1P3/8/8/8/8 w - - 0 1", true, EndgameGoal.WIN, "Bringe den Bauern zur Umwandlung und gewinne.", listOf("Der König zieht vor dem Bauern.", "Besetze die Schlüsselfelder vor dem Bauern.", "Achte auf Opposition und Patt.")),
        EndgameDrill("lucena", "Lucena-Stellung", "Die wichtigste Gewinnstellung im Turmendspiel.", "1K1k4/1P6/8/8/8/8/r7/2R5 w - - 0 1", true, EndgameGoal.WIN, "Wandle den Bauern um und gewinne.", listOf("Baue eine „Brücke“ mit dem Turm auf der 4. Reihe.", "Der König kommt unter Schutz des Turms heraus.", "Der Turm schirmt Schachs ab.")),
        EndgameDrill("kp2", "König + Bauer: Opposition", "Mit Opposition den Bauern durchbringen.", "5k2/8/8/8/3KP3/8/8/8 w - - 0 1", true, EndgameGoal.WIN, "Wandle den Bauern um und gewinne.", listOf("Bringe den König vor den Bauern.", "Wer die Opposition hat, gewinnt Raum.")),
        EndgameDrill("philidor", "Philidor-Verteidigung", "Mit dem Turm auf der 6. Reihe das Turmendspiel halten.", "3k4/R7/3r4/3PK3/8/8/8/8 b - - 0 1", false, EndgameGoal.DRAW, "Halte das Remis (50-Züge-Regel, Wiederholung oder Tausch).", listOf("Halte den Turm auf der 6. Reihe, bis der Bauer zieht.", "Danach Schach von hinten geben.", "Der König bleibt vor dem Bauern."))
    )
}
