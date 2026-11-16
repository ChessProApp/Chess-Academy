# Änderungsübersicht (gegenüber Chess-Academy-main)

## Architektur
- `setup_project.py` entfernt: Kotlin-Code liegt jetzt direkt im Repository (vorher: ~780 Zeilen Python-Strings, `rm -rf` des Quellordners bei jedem Build). Workflow regeneriert weder Manifest noch Code.
- Nicht genutzte Abhängigkeiten entfernt (Room, KSP, DataStore, Navigation). Lose Ordner `core/ data/ domain/ ui/` im Wurzelverzeichnis (nicht im Gradle-Build) entfernt.
- `AndroidManifest.xml` und Icon-XML-Namespaces korrigiert (`xmlns:android` war fehlerhaft), `ic_launcher_round` ergänzt.
- Regeln/Engine/Inhalte in reine Kotlin-Pakete ausgelagert und testbar gemacht.

## Regeln
- Eigene Regelschicht über chesslib: Wiederholungen (3x reklamierbar / 5x automatisch, mit korrekter En-passant- und Rochade-Normalisierung), 50 (reklamierbar) / 75 Züge (automatisch), totes Material, Matt vor Patt, Aufgabe, Remisangebot, Undo.
- Verständliche Begründungen für unzulässige Züge (gefesselt, Schach, Rochade durch Schach usw.).
- Umwandlung in Dame/Turm/Läufer/Springer (Auswahl oder Auto-Dame).

## Engine
- Stockfish-Wrapper mit Mutex, sauberem Abbruch (`stop` + Ausgabe leeren), Prozess wird mit dem ViewModel beendet.
- Spielstile wählen unter nahezu gleichwertigen MultiPV-Zügen (vorher: „letzter MultiPV-Zug“, auch bei deutlich schlechteren Zügen).
- Notlösung ohne Stockfish: zufälliger legaler Zug.

## Neue Funktionen
Regel-Lernbereich (19 Lektionen), Taktiktrainer (14), Tagesaufgabe, Endspieltrainer (6), Eröffnungsbibliothek/-erkennung (65), Partieanalyse, Zugliste, Undo, Drag & Drop, PGN-Import/-Export, Speichern/Fortsetzen, Stellungseditor, Lernfortschritt mit Wiederholung, Tipps mit Pfeil.

## Verbesserte Level
15 der 60 Level hatten fehlerhafte Stellungen/Lösungen und wurden repariert (`docs/lessons_repair_report.md`).

## Design/Animation
Einheitliche Typografie und Karten, kompakte Bildschirme, alle bisherigen Design-Optionen erhalten. Animationen wie im README beschrieben; Bewegung läuft über `graphicsLayer` (Zeichenphase, keine Neukomposition pro Bild), Dauer-Animationen nur solange sichtbar. Sounds werden einmal erzeugt und zwischengespeichert (vorher bei jedem Zug neu berechnet).
