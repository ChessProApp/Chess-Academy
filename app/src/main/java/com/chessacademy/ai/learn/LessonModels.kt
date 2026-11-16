package com.chessacademy.ai.learn

/*
 * Datenmodelle der Academy. Reine Kotlin-Daten ohne Android-Abhängigkeit (JVM-testbar).
 * Felder werden in der UI so verwendet, wie bei den Klassen beschrieben.
 */

// ------------------------------------------------------------------ Bestehende 60 Level

/**
 * Eine Level-Lektion der bisherigen Akademie (Bauern bis Meisterstufe).
 *
 * Der Spieler ist die Seite, die in [fen] am Zug ist. Er löst die Aufgabe, indem er [solutionUci]
 * oder einen Zug aus [alternativeSolutions] spielt. Bei Umwandlungen ohne Figurbuchstaben ("a7a8")
 * zählt jede Umwandlung als richtig. Ist [followUp] gefüllt, antwortet die App automatisch
 * mit followUp[0] (Gegenzug), der Spieler muss dann followUp[1] spielen usw.
 * (gerade Indizes = Gegner, ungerade Indizes = Spieler).
 */
data class AcademyLesson(
    val level: Int,
    val chapter: String,
    val title: String,
    val theory: String,
    val task: String,
    val fen: String,
    val solutionUci: String,
    val hint: String,
    val alternativeSolutions: List<String> = emptyList(),
    val followUp: List<String> = emptyList()
)

// ------------------------------------------------------------------ Regel-Lernbereich

enum class RuleLevel(val label: String) {
    BEGINNER("Anfänger"),
    ADVANCED("Fortgeschritten"),
    EXPERT("Experte")
}

enum class MarkerKind {
    /** Feld wird angegriffen / bedroht (rot). */
    ATTACK,
    /** erlaubtes Zielfeld (grün). */
    LEGAL,
    /** verbotenes Zielfeld (rot/durchgestrichen). */
    ILLEGAL,
    /** Aufmerksamkeit auf dieses Feld (gelb). */
    FOCUS,
    /** sicheres Feld (blau). */
    SAFE
}

/** Feld-Hervorhebung, [square] in algebraischer Schreibweise ("e4"). */
data class Marker(val square: String, val kind: MarkerKind)

/** Pfeil zwischen zwei Feldern ("e2" -> "e4"). */
data class Arrow(val from: String, val to: String, val kind: MarkerKind = MarkerKind.FOCUS)

/**
 * Ein Erklärungsschritt. Die App zeigt [fen] mit [markers] und [arrows] und darunter [text].
 * Ist [moveUci] gesetzt, kann der Nutzer „Zug zeigen“ tippen: der Zug wird (animiert) auf dem Brett ausgeführt.
 * [moveUci] muss in [fen] legal sein.
 */
data class ExplainStep(
    val text: String,
    val fen: String,
    val markers: List<Marker> = emptyList(),
    val arrows: List<Arrow> = emptyList(),
    val moveUci: String? = null
)

/**
 * Beispielzug zur Regel. Für `allowed` muss [moveUci] in [fen] LEGAL sein; für `forbidden` muss er
 * ILLEGAL sein (die App führt ihn probehalber aus, zeigt das Wackeln und die Begründung der Engine
 * plus [explanation]).
 */
data class RuleExample(
    val fen: String,
    val moveUci: String,
    val explanation: String,
    val markers: List<Marker> = emptyList(),
    val arrows: List<Arrow> = emptyList()
)

/**
 * Interaktive Übung: Der Nutzer spielt als Seite am Zug in [fen] einen Zug. Jeder Zug aus [solutions]
 * (UCI, bei Umwandlung mit Buchstaben, z. B. "a7a8q"; ohne Buchstaben zählt jede Umwandlung) ist richtig.
 * Für falsche, aber typische Züge kann [wrongFeedback] (UCI -> Erklärung) eine gezielte Rückmeldung geben.
 */
data class RuleExercise(
    val prompt: String,
    val fen: String,
    val solutions: List<String>,
    val hint: String,
    val explanation: String,
    val wrongFeedback: Map<String, String> = emptyMap()
)

/** Multiple-Choice-Frage. [fen] ist optional (zeigt eine Stellung zur Frage). */
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val fen: String? = null
)

data class RuleLesson(
    val id: String,
    /** Sortierung innerhalb des Lehrplans (aufbauend); kleiner = früher. */
    val order: Int,
    val level: RuleLevel,
    val title: String,
    /** Kurzbeschreibung in ein bis zwei Sätzen (Listenansicht). */
    val summary: String,
    /** IDs der Lektionen, die vorher abgeschlossen sein sollten (nur Empfehlung, keine Sperre). */
    val prerequisites: List<String>,
    val steps: List<ExplainStep>,
    val allowed: List<RuleExample>,
    val forbidden: List<RuleExample>,
    val mistakes: List<String>,
    val exercises: List<RuleExercise>,
    val quiz: List<QuizQuestion>
)

// ------------------------------------------------------------------ Training

/**
 * Taktikaufgabe. [solution] ist die vollständige Zugfolge in UCI: Index 0 spielt der Nutzer
 * (Seite am Zug in [fen]), Index 1 antwortet die App automatisch, Index 2 der Nutzer usw.
 * Der erste Zug muss eindeutig der beste sein (Stockfish-geprüft).
 */
data class TacticPuzzle(
    val id: String,
    /** z. B. "Gabel", "Fesselung", "Spieß", "Abzugsschach", "Mattmuster", "Grundreihenmatt", "Mattnetz". */
    val theme: String,
    /** 1 = leicht, 2 = mittel, 3 = schwer. */
    val difficulty: Int,
    val title: String,
    val fen: String,
    val solution: List<String>,
    val explanation: String
)

enum class EndgameGoal { WIN, DRAW }

/**
 * Endspiel-Trainingsaufgabe: Der Nutzer spielt [fen] gegen Stockfish.
 * [playerWhite] gibt die Seite des Nutzers an. Ziel WIN = Matt setzen; Ziel DRAW = Remis halten
 * (z. B. Patt/50 Züge/Material-Remis oder 30 Züge überleben, wie in [goalText] beschrieben).
 */
data class EndgameDrill(
    val id: String,
    val title: String,
    val description: String,
    val fen: String,
    val playerWhite: Boolean,
    val goal: EndgameGoal,
    val goalText: String,
    val tips: List<String>
)

/** Eröffnung für Bibliothek und Erkennung. [moves] in SAN ohne Zugnummern, z. B. "e4 e5 Nf3 Nc6 Bb5". */
data class OpeningEntry(
    val eco: String,
    val name: String,
    val moves: String,
    val idea: String
)
