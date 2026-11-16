# Reparaturbericht der 60 Academy-Level

Alle 60 Level wurden mit python-chess und Stockfish 17 (Tiefe 14) geprüft: gültige Stellung, legale Lösung, keine doppelte Stellung+Lösung, Lösung nicht klar schwächer als der beste Zug.
Level, die nicht bestanden, wurden korrigiert. Alle anderen blieben unverändert (Text, Stellung, Lösung).

Prüfung in der App-Logik: `tests/jvm` spielt jede Lösung (und Alternativen/Folgezüge) mit der Regelschicht nach.

Geänderte Level: 15 von 60

## Level 6 – Schach abwehren
- **Theorie** alt: `Schütze deinen König sofort.`  
  neu: `Steht dein König im Schach, musst du es sofort beenden: wegziehen, schlagen oder dazwischenstellen.`
- **Aufgabe** alt: `Weiche dem Turm aus.`  
  neu: `Dein König steht im Schach. Weiche dem Turm aus.`
- **FEN** alt: `4k3/8/8/8/8/8/4r3/4K3 w - - 0 1`  
  neu: `4r1k1/8/8/8/8/8/8/4K3 w - - 0 1`
- **Tipp** alt: `e1 nach d1`  
  neu: `Der Turm kontrolliert die e-Linie – ziehe seitlich weg, z. B. e1 nach d1.`
- Zusätzlich akzeptierte Lösungen: e1f1, e1d2, e1f2

## Level 16 – Springergabel
- **Aufgabe** alt: `Gable auf c4.`  
  neu: `Gabel auf e7: Greife König und Dame gleichzeitig an.`
- **FEN** alt: `4k3/8/8/3q4/8/4N3/8/4K3 w - - 0 1`  
  neu: `6k1/8/2q5/3N4/8/8/8/6K1 w - - 0 1`
- **Lösung** alt: `e3c4`  
  neu: `d5e7`
- **Tipp** alt: `e3 nach c4`  
  neu: `d5 nach e7 – der Springer greift g8 und c6 an.`

## Level 19 – Der Spieß
- **Theorie** alt: `Schach auf a8 gewinnt die Dame.`  
  neu: `Beim Spieß muss die wertvolle Figur vorn ausweichen, dann fällt die Figur dahinter.`
- **Aufgabe** alt: `Ziehe nach a8.`  
  neu: `Gib Schach mit dem Turm – die Dame dahinter fällt.`
- **FEN** alt: `4k3/8/8/8/8/8/q7/R3K3 w - - 0 1`  
  neu: `3q4/8/8/3k4/8/8/8/R5K1 w - - 0 1`
- **Lösung** alt: `a1a8`  
  neu: `a1d1`
- **Tipp** alt: `a1 nach a8`  
  neu: `a1 nach d1 – Schach auf der d-Linie, hinter dem König steht die Dame.`

## Level 20 – Doppelschach
- **Theorie** alt: `Zwei Figuren geben Schach.`  
  neu: `Doppelschach: Zwei Figuren geben gleichzeitig Schach – nur der König kann antworten.`
- **Aufgabe** alt: `Springe nach f7.`  
  neu: `Springe so, dass der Springer und der Turm gleichzeitig Schach geben.`
- **FEN** alt: `4k3/8/8/4N3/8/8/4R3/4K3 w - - 0 1`  
  neu: `4k3/8/8/8/4N3/8/4R3/4K3 w - - 0 1`
- **Lösung** alt: `e5f7`  
  neu: `e4d6`
- **Tipp** alt: `e5 nach f7`  
  neu: `Der Springer räumt die e-Linie (Turm gibt Schach) und schlägt selbst zu: e4 nach d6.`
- Zusätzlich akzeptierte Lösungen: e4f6

## Level 21 – Ersticktes Matt
- **FEN** alt: `6rk/6pp/8/5N2/8/8/8/6K1 w - - 0 1`  
  neu: `6rk/6pp/8/6N1/8/8/8/6K1 w - - 0 1`
- **Lösung** alt: `f5h6`  
  neu: `g5f7`
- **Tipp** alt: `f5 nach h6`  
  neu: `g5 nach f7 – der König kann nirgends hin.`

## Level 22 – Arabisches Matt
- **Theorie** alt: `Turm und Springer am Rand.`  
  neu: `Arabisches Matt: Turm und Springer arbeiten zusammen, der Springer deckt das Mattfeld.`
- **Aufgabe** alt: `Ziehe nach h7.`  
  neu: `Setze mit dem Turm auf h7 matt.`
- **FEN** alt: `7k/8/5N2/8/8/8/7R/6K1 w - - 0 1`  
  neu: `7k/R7/5N2/8/8/8/8/6K1 w - - 0 1`
- **Lösung** alt: `h2h7`  
  neu: `a7h7`
- **Tipp** alt: `h2 nach h7`  
  neu: `a7 nach h7 – der Springer auf f6 deckt h7 und g8.`

## Level 24 – Griechenopfer
- **Theorie** alt: `Reiße die Königsstellung auf.`  
  neu: `Das Läuferopfer auf h7 reißt die Königsstellung auf; danach folgen Springerschach und Damenangriff.`
- **Aufgabe** alt: `Schlage auf h7.`  
  neu: `Opfere den Läufer: Schlage auf h7 mit Schach.`
- **FEN** alt: `r1bq1rk1/ppp2ppp/2n2n2/3p4/3P4/2NB1N2/PPP2PPP/R1BQ1RK1 w - - 0 8`  
  neu: `r4rk1/pp3ppp/2p5/8/8/3B1N2/PP3PPP/3Q1RK1 w - - 0 1`
- **Tipp** alt: `d3 nach h7`  
  neu: `d3 nach h7 – nach Kxh7 folgen Sg5+ und Dh5.`

## Level 26 – Die 7. Reihe
- **Theorie** alt: `Greife Bauern von der Seite an.`  
  neu: `Ein Turm auf der 7. Reihe greift Bauern von der Seite an.`
- **Aufgabe** alt: `Dringe nach d7 ein.`  
  neu: `Dringe mit dem Turm nach d7 ein.`
- **FEN** alt: `4k3/pp3ppp/8/8/8/8/PP3PPP/3R2K1 w - - 0 1`  
  neu: `6k1/pp3p1p/6p1/8/8/8/PP3PPP/3R2K1 w - - 0 1`
- **Tipp** alt: `d1 nach d7`  
  neu: `d1 nach d7 – der Turm greift b7 und f7 an.`
- Zusätzlich akzeptierte Lösungen: d1d8

## Level 45 – Zugzwang
- **Theorie** alt: `Matt im Zugzwang.`  
  neu: `Zugzwang und Opposition: Mit dem König die Schlüsselfelder vor dem Bauern besetzen und den Gegner zum Ausweichen zwingen.`
- **Aufgabe** alt: `Ziehe h1 nach h8.`  
  neu: `Besetze mit dem König das Schlüsselfeld d6.`
- **FEN** alt: `7k/8/6K1/8/8/8/8/7R w - - 0 1`  
  neu: `4k3/8/8/2K1P3/8/8/8/8 w - - 0 1`
- **Lösung** alt: `h1h8`  
  neu: `c5d6`
- **Tipp** alt: `h1 nach h8`  
  neu: `c5 nach d6 – der König steht vor dem Bauern.`

## Level 46 – Räumungsopfer
- **Theorie** alt: `Setze auf e7 matt.`  
  neu: `Räumungsopfer: Die Dame beseitigt den einzigen Verteidiger und der König hat keine Fluchtfelder mehr.`
- **Aufgabe** alt: `Ziehe e2 nach e7.`  
  neu: `Schlage auf e8 und setze matt.`
- **FEN** alt: `4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1`  
  neu: `4r1k1/5ppp/8/4Q3/8/8/5PPP/4R1K1 w - - 0 1`
- **Lösung** alt: `e2e7`  
  neu: `e5e8`
- **Tipp** alt: `e2 nach e7`  
  neu: `e5 nach e8 – der Turm e1 deckt die Dame.`

## Level 47 – Linienöffnung
- **Theorie** alt: `Matt auf der d-Linie.`  
  neu: `Linienöffnung: Der Läufer macht die e-Linie frei, der Turm gibt Abzugsschach und der Läufer deckt die Fluchtfelder.`
- **Aufgabe** alt: `Ziehe d1 nach d8.`  
  neu: `Räume die Linie und setze matt.`
- **FEN** alt: `3k4/8/3K4/8/8/8/8/3R4 w - - 0 1`  
  neu: `3rk3/3p1p2/8/4B3/8/8/8/4R1K1 w - - 0 1`
- **Lösung** alt: `d1d8`  
  neu: `e5g7`
- **Tipp** alt: `d1 nach d8`  
  neu: `Der Läufer zieht von e5 weg und deckt zugleich f8.`
- Zusätzlich akzeptierte Lösungen: e5d6

## Level 57 – Zugzwang II
- **Theorie** alt: `Zwinge den schwarzen König zum Aufgeben der Deckung.`  
  neu: `Zugzwang II: Gewinne Raum mit dem König, bevor der Bauer weiterzieht.`
- **Aufgabe** alt: `Ziehe den Turm nach h8.`  
  neu: `Ziehe den König vor den Bauern.`
- **FEN** alt: `7k/8/6K1/8/8/8/8/7R w - - 0 1`  
  neu: `5k2/8/8/8/3KP3/8/8/8 w - - 0 1`
- **Lösung** alt: `h1h8`  
  neu: `d4d5`
- **Tipp** alt: `h1 nach h8`  
  neu: `d4 nach d5 – der König steht neben dem Bauern und erobert Schlüsselfelder.`

## Level 58 – Räumungsopfer II
- **Theorie** alt: `Räume die Diagonale oder Linie für das Matt.`  
  neu: `Räumungsopfer II: Der Turm schlägt den Verteidiger und setzt auf der Grundreihe matt.`
- **Aufgabe** alt: `Setze auf e7 matt.`  
  neu: `Schlage auf c8 und setze matt.`
- **FEN** alt: `4k3/8/8/8/8/8/4Q3/4K3 w - - 0 1`  
  neu: `2r3k1/5ppp/8/8/8/8/5PPP/2R1R1K1 w - - 0 1`
- **Lösung** alt: `e2e7`  
  neu: `c1c8`
- **Tipp** alt: `e2 nach e7`  
  neu: `c1 nach c8 – die zweite Turmlinie deckt das Feld.`

## Level 59 – Linienöffnung II
- **Theorie** alt: `Öffne die d-Linie zum entscheidenden Matt.`  
  neu: `Linienöffnung II: Schlage auf der d-Linie – die Grundreihe ist ohne Luftloch verloren.`
- **Aufgabe** alt: `Ziehe d1 nach d8.`  
  neu: `Schlage auf d8 und setze matt.`
- **FEN** alt: `3k4/8/3K4/8/8/8/8/3R4 w - - 0 1`  
  neu: `3r2k1/5ppp/8/8/8/8/5PPP/3RR1K1 w - - 0 1`
- **Tipp** alt: `d1 nach d8`  
  neu: `d1 nach d8 – der zweite Turm auf e1 sichert die Grundreihe.`

## Level 60 – Großmeister-Finale II
- **Theorie** alt: `Der krönende Abschlusszug der Akademie.`  
  neu: `Der krönende Abschluss: Ein Grundreihenmatt nach Turmtausch.`
- **Aufgabe** alt: `Ziehe d1 nach d8.`  
  neu: `Schlage auf a8 und setze matt.`
- **FEN** alt: `6k1/5ppp/8/8/8/8/5PPP/3R2K1 w - - 0 1`  
  neu: `r5k1/5ppp/8/8/8/8/5PPP/R3R1K1 w - - 0 1`
- **Lösung** alt: `d1d8`  
  neu: `a1a8`
- **Tipp** alt: `d1 nach d8`  
  neu: `a1 nach a8.`
