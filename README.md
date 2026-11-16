# Chess Academy

Android-Schach-App (Kotlin, Jetpack Compose, Stockfish 17) zum Spielen, Lernen und Trainieren.
Gebaut wird ausschließlich über **GitHub Actions** – Android Studio ist nicht nötig.

> **Ehrlicher Stand:** Die Regel-, Engine- und Lerninhalts-Logik ist auf dem JVM automatisiert getestet (siehe [Tests](#tests--was-wirklich-geprüft-wurde)).
> Die Compose-Oberfläche und der APK-Build konnten in der Entwicklungsumgebung **nicht** kompiliert oder auf einem Gerät ausgeführt werden.
> Der erste GitHub-Actions-Lauf ist daher der erste echte Build; Kompilierfehler sind möglich (Hinweise unten).

## Funktionen

**Spielen**
- Partie gegen Stockfish (7 Stärkestufen, 4 Spielstile), Tippen **und** Drag & Drop
- Zugliste mit Antippen zum Zurückblättern, Zug zurück (Undo), Tipp (bester Zug als Pfeil + Bewertung)
- Statusanzeige (am Zug / Schach / denkt), verständliche Fehlerbegründung bei ungültigen Zügen (z. B. „Figur ist gefesselt“)
- Eingabesperre: kein Ziehen während die Engine denkt, bei beendeter Partie oder im Zugverlauf – Doppelzüge sind ausgeschlossen
- Aufgeben, Remis anbieten, Remis reklamieren (3-fach / 50 Züge), automatische Remis (Patt, totes Material, 5-fach, 75 Züge)
- PGN teilen / kopieren / einfügen, Partie wird automatisch gespeichert und beim Start fortgesetzt
- Eröffnungserkennung (65 Eröffnungen)
- Partieanalyse: Bewertung je Zug, Genauigkeit, Patzer/Fehler/Ungenauigkeiten, besserer Zug als Pfeil

**Animationen** (abschaltbar unter Design; keine blockiert Eingabe oder Logik)
Figuren gleiten vom Start- zum Zielfeld (auch Rochade-Turm, En passant, Umwandlung), Schlagen: Figur schrumpft/blendet aus + dezente Partikel,
Schach: pulsierendes Leuchten am König, Matt: König kippt + Lichtring + Ergebnis-Karte, Auswahl-Skalierung, Hinweis-Einblendung, Wackeln bei ungültigem Zug,
Umwandlungsauswahl mit Ein-/Ausblenden, Tab-Überblendung. Die Bildrate wird nicht erzwungen; die App bittet nur um den höchsten Anzeigemodus.

**Lernen**
- **Regeln** (neu): 19 aufbauende Lektionen (Anfänger/Fortgeschritten) mit Erklärschritten, erlaubten/verbotenen Beispielen (Verbote werden live von der Regel-Engine begründet), typischen Fehlern, interaktiven Übungen, Quiz, Fortschritt und Wiederholung schwieriger Aufgaben
- **Lektionen**: die bisherigen 60 Level (geprüft, 15 repariert – siehe `docs/lessons_repair_report.md`)
- **Taktik** (14 Aufgaben, mit Stockfish geprüft) und **Tagesaufgabe** mit Serie
- **Endspiele** (6 Drills gegen Stockfish), **Eröffnungen** (Bibliothek mit Schritt-für-Schritt-Ansicht)

**Labor**: freies Brett mit legalen Zügen für beide Seiten, Engine-Bewertung, FEN/PGN kopieren, **Stellungseditor** mit Prüfung (Könige, Bauern, Schach-Regeln) und Start gegen Stockfish.

**Design**: alle bisherigen Optionen bleiben – 4 Akzentfarben, 8 Brett-Themes, eigene Farben (Regler), Rahmen, Koordinaten, Zug-Ringe/Punkte, Serif-Figuren, Ton, Vibration – plus Animationen, Drag & Drop, Auto-Dame, letzten Zug markieren.

## APK bekommen

1. Repository auf GitHub hochladen (siehe [docs/GITHUB_UPLOAD.md](docs/GITHUB_UPLOAD.md)).
2. Reiter **Actions** → „Build Release APK“ läuft bei jedem Push auf `main` (oder „Run workflow“). Das Ergebnis wird zusätzlich unter **Releases** als `Chess Academy.apk` veröffentlicht.
3. Nach ca. 10–20 Minuten beim Lauf unter **Artifacts** `Chess-Academy-AI-APK` herunterladen, entpacken, `app-debug.apk` auf dem Handy installieren (Installation aus unbekannten Quellen erlauben).

Der Workflow baut Stockfish 17 für ARM64 mit dem NDK und legt es als `libstockfish.so` in die App; die App startet es als Prozess aus dem nativen Bibliotheksverzeichnis.
Ohne Stockfish-Datei läuft die App weiter (zufällige Gegnerzüge, Hinweis wird angezeigt).

## Lokal bauen (optional)

```
# Stockfish für arm64 bauen und als app/src/main/jniLibs/arm64-v8a/libstockfish.so ablegen (siehe Workflow), dann:
gradle assembleDebug     # JDK 17, Gradle 8.9, Android SDK 35
```

## Projektstruktur

```
app/src/main/java/com/chessacademy/ai/
  chess/    Regelschicht über chesslib (Historie, Wiederholung, 50/75 Züge, totes Material, SAN, PGN, Figur-Tracking)   [JVM-getestet]
  engine/   UCI-Wrapper für Stockfish, Spielstil-Auswahl, Partieanalyse                                               [JVM-getestet, echter Stockfish]
  learn/    Level-, Regel-, Taktik-, Endspiel-, Eröffnungsdaten                                                        [JVM-getestet]
  data/     Einstellungen, Spielstand, Lernfortschritt (SharedPreferences)
  audio/    Töne (einmal berechnet, zwischengespeichert) und Vibration
  vm/       GameViewModel (Spielablauf, Engine, Analyse, Speichern)
  ui/       Theme, Brett (ChessBoard), Eingabe (BoardController), Bildschirme
tests/jvm/  Testläufer ohne Android (kotlinc + chesslib)
docs/       Änderungen, Upload-Anleitung, Reparaturbericht
```

Die Pakete `chess/`, `engine/` und `learn/` enthalten keinen Android-Code – deshalb sind sie ohne Emulator testbar.
`setup_project.py` (erzeugte früher den gesamten Code als Python-Strings) existiert nicht mehr; der Kotlin-Code liegt direkt im Repository. Der Workflow überschreibt keinen Code mehr.

## Tests – was wirklich geprüft wurde

Ausgeführt (Linux, Kotlin 1.9, chesslib 1.3.4, **echtes Stockfish 17**, selbst gebaut): `tests/jvm/run_tests.sh`

```
KOTLINC=… CHESSLIB=… LANG3=… [SF_BIN=/pfad/stockfish] tests/jvm/run_tests.sh
```
Letzter Lauf: **2118 Prüfungen bestanden, 0 fehlgeschlagen.** Abgedeckt:
- Zuggenerierung per Perft gegen Referenzwerte (7 Stellungen), Rochade, En passant, Umwandlung, Fesselung, Doppelschach
- Matt/Patt, 3-fach/5-fach-Wiederholung, 50/75-Züge, totes Material, Undo, SAN, PGN-Export/-Import, Figur-Tracking für Animationen
- UCI-Parser, Spielstil-Auswahl, Stockfish-Suche, Matt-Erkennung, Abbruch, Skill 0, Partieanalyse
- Alle 60 Level, 19 Regel-Lektionen (jede Stellung, jeder Beispielzug, jede Übung), Taktikaufgaben, Endspiel-Stellungen, Eröffnungsbibliothek, Tagesaufgabe

**Nicht geprüft** (in dieser Umgebung nicht möglich – Android SDK/Maven gesperrt):
- Kompilierung der Compose-Oberfläche (`ui/`, `vm/`, `data/`, `audio/`, `MainActivity.kt`) – nur manuell durchgesehen
- APK-Build, der Stockfish-NDK-Schritt im Workflow, Start auf einem Gerät
- Optische Qualität/Flüssigkeit der Animationen, 120 Hz, Ton und Vibration, Figuren-Darstellung der Gerätefont
- Keine UI-Tests

Falls der erste Build fehlschlägt: den Fehlertext aus dem Actions-Log kopieren und weitergeben – dann wird gezielt korrigiert.

## Bekannte Grenzen

- Regel-Lernbereich: Stufe „Experte“ ist als Datenstruktur vorgesehen, enthält aber noch keine Lektionen.
- Figuren sind Schriftzeichen (Unicode-Schach-Glyphen, mit Kontur) – Aussehen hängt von der Gerätefont ab; Vektorfiguren sind nicht enthalten.
- Position-Editor setzt keinen En-passant-Zug; Remis anbieten nimmt die Engine nur bei ausgeglichener Stellung an.
- Spielstärke „Maximum“ ist auf 4 s pro Zug begrenzt (Akku).
