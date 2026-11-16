package com.chessacademy.ai.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.GameResult
import com.chessacademy.ai.chess.Position
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.engine.StockfishRunner
import com.chessacademy.ai.learn.*
import com.chessacademy.ai.ui.components.*
import com.chessacademy.ai.ui.theme.BadRed
import com.chessacademy.ai.ui.theme.GoodGreen
import com.chessacademy.ai.ui.theme.WarnAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed class Route {
    data class Rule(val id: String) : Route()
    data class Level(val level: Int) : Route()
    data class Tactic(val id: String) : Route()
    data class Endgame(val id: String) : Route()
    data class Opening(val index: Int) : Route()
    object Repeat : Route()
}

private val TABS = listOf("Regeln", "Lektionen", "Taktik", "Heute", "Endspiele", "Eröffnungen")

@Composable
fun LearnScreen(runner: StockfishRunner) {
    var tab by remember { mutableIntStateOf(0) }
    var route by remember { mutableStateOf<Route?>(null) }
    BackHandler(enabled = route != null) { route = null }

    val r = route
    if (r != null) {
        Column(Modifier.fillMaxSize()) {
            IconButton(onClick = { route = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück") }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp)) {
                when (r) {
                    is Route.Rule -> RuleLessonView(RulesCurriculum.byId(r.id)!!, onOpen = { route = Route.Rule(it) })
                    is Route.Level -> LevelView(r.level, onNext = { route = Route.Level(it) })
                    is Route.Tactic -> TacticView(TacticsData.puzzles.first { it.id == r.id })
                    is Route.Endgame -> EndgameView(EndgamesData.drills.first { it.id == r.id }, runner)
                    is Route.Opening -> OpeningView(OpeningBook.entries[r.index])
                    Route.Repeat -> RepeatView()
                }
            }
        }
        return
    }
    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp, containerColor = MaterialTheme.colorScheme.background) {
            TABS.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
        }
        when (tab) {
            0 -> RulesTab({ route = Route.Rule(it) }, { route = Route.Repeat })
            1 -> LevelsTab { route = Route.Level(it) }
            2 -> TacticsTab { route = Route.Tactic(it) }
            3 -> DailyTab()
            4 -> EndgamesTab { route = Route.Endgame(it) }
            else -> OpeningsTab { route = Route.Opening(it) }
        }
    }
}

// ------------------------------------------------------------------------------------ Regeln

@Composable
private fun RulesTab(onOpen: (String) -> Unit, onRepeat: () -> Unit) {
    val done = Store.completedRules
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)) {
        item {
            val total = RulesCurriculum.all.size
            AppCard(accent = true) {
                Text("Schachregeln lernen", style = MaterialTheme.typography.titleMedium)
                Text("${done.size} von $total Lektionen abgeschlossen. Die Lektionen bauen aufeinander auf.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(progress = { if (total == 0) 0f else done.size / total.toFloat() }, modifier = Modifier.fillMaxWidth())
            }
        }
        val hard = Store.difficult.size
        if (hard > 0) item {
            AppCard(onClick = onRepeat) {
                Text("Schwierige Aufgaben wiederholen", style = MaterialTheme.typography.titleSmall, color = WarnAmber)
                Text("$hard Aufgaben, bei denen du Fehler gemacht hast.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        for (level in RuleLevel.entries) {
            val list = RulesCurriculum.byLevel(level)
            if (list.isEmpty()) continue
            item { SectionTitle(level.label) }
            items(list) { l ->
                AppCard(onClick = { onOpen(l.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(l.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (l.id in done) Text("✓ ${Store.ruleScores[l.id] ?: 0} %", color = GoodGreen, style = MaterialTheme.typography.labelLarge)
                    }
                    Text(l.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DemoBoard(fen: String, markers: List<Marker>, arrows: List<Arrow>, moveUci: String?, forbidden: Boolean, caption: String) {
    val ctrl = remember(fen, moveUci) { BoardController(fen, autoDraws = false) }
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version
    var shown by remember(fen, moveUci) { mutableStateOf(false) }
    val mk = remember(markers) { markers.associate { Sq.parse(it.square) to it.kind } }
    val ar = remember(arrows) { arrows.map { BoardArrow(Sq.parse(it.from), Sq.parse(it.to), it.kind) } }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ChessBoard(ctrl.shownPosition, ctrl.shownLastMove, ctrl.forward, flipped = fen.contains(" b "), style = Store.settings.boardStyle(),
            modifier = Modifier.fillMaxWidth(), markers = mk, arrows = if (shown) emptyList() else ar,
            shakeKey = ctrl.shakeKey, shakeSq = ctrl.shakeSq, interactive = false)
        Text(caption, style = MaterialTheme.typography.bodyMedium)
        ctrl.message?.let { Text("Regel-Prüfung: $it", style = MaterialTheme.typography.bodySmall, color = BadRed) }
        if (moveUci != null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                if (forbidden) { ctrl.showIllegal(moveUci) }
                else { ctrl.reset(); ctrl.playExternal(moveUci); shown = true }
            }) { Text(if (forbidden) "Ausprobieren" else "Zug zeigen") }
            if (shown || ctrl.message != null) OutlinedButton(onClick = { ctrl.reset(); ctrl.message = null; shown = false }) { Text("Zurücksetzen") }
        }
    }
}

@Composable
private fun RuleLessonView(l: RuleLesson, onOpen: (String) -> Unit) {
    var part by remember(l.id) { mutableIntStateOf(0) }
    val parts = listOf("Lernen", "Beispiele", "Üben", "Quiz")
    Text(l.title, style = MaterialTheme.typography.titleLarge)
    Text("${l.level.label} · ${l.summary}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val missing = l.prerequisites.filter { it !in Store.completedRules }
    if (missing.isNotEmpty()) Text("Empfohlen vorher: " + missing.mapNotNull { RulesCurriculum.byId(it)?.title }.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = WarnAmber)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
        items(parts.indices.toList()) { i -> FilterChip(selected = part == i, onClick = { part = i }, label = { Text(parts[i]) }) }
    }
    when (part) {
        0 -> {
            var step by remember(l.id) { mutableIntStateOf(0) }
            val st = l.steps[step]
            Text("Schritt ${step + 1} / ${l.steps.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            DemoBoard(st.fen, st.markers, st.arrows, st.moveUci, false, st.text)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { step-- }, enabled = step > 0, modifier = Modifier.weight(1f)) { Text("Zurück") }
                Button(onClick = { if (step < l.steps.lastIndex) step++ else part = 1 }, modifier = Modifier.weight(1f)) { Text(if (step < l.steps.lastIndex) "Weiter" else "Zu den Beispielen") }
            }
            if (l.mistakes.isNotEmpty()) {
                SectionTitle("Typische Fehler", Modifier.padding(top = 12.dp))
                for (m in l.mistakes) Text("• $m", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
        1 -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle("Erlaubt")
            for (e in l.allowed) AppCard { DemoBoard(e.fen, e.markers, e.arrows, e.moveUci, false, e.explanation) }
            SectionTitle("Verboten")
            for (e in l.forbidden) AppCard { DemoBoard(e.fen, e.markers, e.arrows, e.moveUci, true, e.explanation) }
        }
        2 -> {
            var i by remember(l.id) { mutableIntStateOf(0) }
            var solved by remember(l.id, i) { mutableStateOf(false) }
            if (l.exercises.isEmpty()) Text("Keine Übungen in dieser Lektion.")
            else {
                val ex = l.exercises[i]
                Text("Übung ${i + 1} / ${l.exercises.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(ex.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 6.dp))
                val spec = remember(l.id, i) { SolveSpec.fromSolutions(ex.fen, ex.solutions, ex.hint, ex.wrongFeedback) }
                SolveBoard(spec, onSolved = { first -> solved = true; Store.markDifficult("${l.id}#e$i", !first) }, onWrong = { Store.markDifficult("${l.id}#e$i", true) })
                if (solved) {
                    Text(ex.explanation, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = { if (i < l.exercises.lastIndex) i++ else part = 3 }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(if (i < l.exercises.lastIndex) "Nächste Übung" else "Zum Quiz")
                    }
                }
            }
        }
        else -> {
            var q by remember(l.id) { mutableIntStateOf(0) }
            var correct by remember(l.id) { mutableIntStateOf(0) }
            var finished by remember(l.id) { mutableStateOf(false) }
            if (finished) {
                val pct = if (l.quiz.isEmpty()) 100 else correct * 100 / l.quiz.size
                AppCard(accent = true) {
                    Text("Ergebnis: $correct von ${l.quiz.size} richtig ($pct %)", style = MaterialTheme.typography.titleMedium)
                    Text(if (pct >= 70) "Lektion abgeschlossen." else "Wiederhole die schwierigen Teile – sie erscheinen unter „Schwierige Aufgaben“.", style = MaterialTheme.typography.bodyMedium)
                    val next = RulesCurriculum.all.firstOrNull { it.order > l.order }
                    if (next != null) Button(onClick = { onOpen(next.id) }) { Text("Nächste Lektion: ${next.title}") }
                    OutlinedButton(onClick = { q = 0; correct = 0; finished = false }) { Text("Quiz wiederholen") }
                }
            } else {
                val qq = l.quiz[q]
                Text("Frage ${q + 1} / ${l.quiz.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                QuizCard(qq, key = "${l.id}#$q") { ok ->
                    Store.markDifficult("${l.id}#q$q", !ok)
                    if (ok) correct++
                }
                NextQuizButton(last = q == l.quiz.lastIndex) {
                    if (q < l.quiz.lastIndex) q++ else { finished = true; Store.completeRule(l.id, if (l.quiz.isEmpty()) 100 else correct * 100 / l.quiz.size) }
                }
            }
        }
    }
}

private var quizAnswered by mutableStateOf(false)

@Composable
private fun NextQuizButton(last: Boolean, onClick: () -> Unit) {
    // Der Button erscheint erst nach der Antwort (QuizCard setzt quizAnswered).
    if (quizAnswered) Button(onClick = { quizAnswered = false; onClick() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(if (last) "Ergebnis anzeigen" else "Nächste Frage") }
}

@Composable
private fun QuizCard(q: QuizQuestion, key: Any, onAnswer: (Boolean) -> Unit) {
    var chosen by remember(key) { mutableIntStateOf(-1) }
    LaunchedEffectReset(key)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        q.fen?.let { fen -> StaticBoard(Position.fromFen(fen), Store.settings.boardStyle(), flipped = fen.contains(" b "), modifier = Modifier.fillMaxWidth()) }
        Text(q.question, style = MaterialTheme.typography.bodyLarge)
        q.options.forEachIndexed { i, opt ->
            val colors = when {
                chosen == -1 -> null
                i == q.correctIndex -> GoodGreen
                i == chosen -> BadRed
                else -> null
            }
            OutlinedButton(
                onClick = { if (chosen == -1) { chosen = i; quizAnswered = true; Feedback.fire(if (i == q.correctIndex) SoundType.WIN else SoundType.ERROR, 20L); onAnswer(i == q.correctIndex) } },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = colors ?: MaterialTheme.colorScheme.onSurface)
            ) { Text(opt) }
        }
        if (chosen != -1) Text((if (chosen == q.correctIndex) "Richtig. " else "Leider falsch. ") + q.explanation, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LaunchedEffectReset(key: Any) {
    androidx.compose.runtime.LaunchedEffect(key) { quizAnswered = false }
}

// ------------------------------------------------------------------------------------ Wiederholung

@Composable
private fun RepeatView() {
    val keys = remember { Store.difficult.sorted() }
    var i by remember { mutableIntStateOf(0) }
    if (keys.isEmpty()) { Text("Nichts zu wiederholen – super!"); return }
    if (i >= keys.size) { Text("Wiederholung beendet.", style = MaterialTheme.typography.titleMedium); return }
    val key = keys[i]
    val parts = key.split("#")
    val lesson = RulesCurriculum.byId(parts[0])
    Text("Wiederholung ${i + 1} / ${keys.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    if (lesson == null) { i++; return }
    val tag = parts.getOrElse(1) { "" }
    val idx = tag.drop(if (tag.startsWith("q")) 1 else 1).toIntOrNull() ?: 0
    var done by remember(key) { mutableStateOf(false) }
    if (tag.startsWith("e")) {
        val ex = lesson.exercises.getOrNull(idx) ?: run { i++; return }
        Text(lesson.title, style = MaterialTheme.typography.titleSmall)
        Text(ex.prompt, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 6.dp))
        SolveBoard(remember(key) { SolveSpec.fromSolutions(ex.fen, ex.solutions, ex.hint, ex.wrongFeedback) },
            onSolved = { first -> done = true; if (first) Store.markDifficult(key, false) })
        if (done) { Text(ex.explanation, modifier = Modifier.padding(top = 8.dp)); Button(onClick = { i++ }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Weiter") } }
    } else {
        val qq = lesson.quiz.getOrNull(idx) ?: run { i++; return }
        Text(lesson.title, style = MaterialTheme.typography.titleSmall)
        QuizCard(qq, key) { ok -> if (ok) Store.markDifficult(key, false) }
        NextQuizButton(last = i == keys.lastIndex) { i++ }
    }
}

// ------------------------------------------------------------------------------------ 60 Level

@Composable
private fun LevelsTab(onOpen: (Int) -> Unit) {
    val all = AcademyData.allLessons
    val chapters = remember { all.map { it.chapter }.distinct() }
    var chapter by remember { mutableStateOf("Alle") }
    val list = if (chapter == "Alle") all else all.filter { it.chapter == chapter }
    Column(Modifier.fillMaxSize()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 10.dp)) {
            item { FilterChip(chapter == "Alle", { chapter = "Alle" }, { Text("Alle (${all.size})") }) }
            items(chapters) { c -> FilterChip(chapter == c, { chapter = c }, { Text(c) }) }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
            items(list) { l ->
                AppCard(onClick = { onOpen(l.level) }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Level ${l.level} · ${l.title}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (l.level in Store.completedLevels) Text("✓", color = GoodGreen)
                    }
                    Text(l.task, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun LevelView(level: Int, onNext: (Int) -> Unit) {
    val l = AcademyData.allLessons.first { it.level == level }
    var solved by remember(level) { mutableStateOf(false) }
    Text("Level ${l.level} · ${l.title}", style = MaterialTheme.typography.titleLarge)
    Text(l.chapter, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text(l.theory, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 6.dp))
    Text(l.task, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
    SolveBoard(remember(level) { SolveSpec.fromSequence(l.fen, listOf(l.solutionUci) + l.followUp, l.hint, l.alternativeSolutions) },
        onSolved = { solved = true; Store.completeLevel(l.level) })
    val next = AcademyData.allLessons.firstOrNull { it.level == l.level + 1 }
    if (solved && next != null) Button(onClick = { onNext(next.level) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Nächstes Level") }
}

// ------------------------------------------------------------------------------------ Taktik / Heute

@Composable
private fun TacticsTab(onOpen: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)) {
        item { Text("${Store.solvedTactics.size} von ${TacticsData.puzzles.size} gelöst", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        for ((theme, list) in TacticsData.puzzles.groupBy { it.theme }) {
            item { SectionTitle(theme) }
            items(list) { p ->
                AppCard(onClick = { onOpen(p.id) }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("★".repeat(p.difficulty), color = WarnAmber)
                        if (p.id in Store.solvedTactics) Text(" ✓", color = GoodGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun TacticView(p: TacticPuzzle) {
    var solved by remember(p.id) { mutableStateOf(false) }
    Text(p.title, style = MaterialTheme.typography.titleLarge)
    Text("${p.theme} · ${"★".repeat(p.difficulty)} · ${if (Position.fromFen(p.fen).whiteToMove) "Weiß" else "Schwarz"} am Zug", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
    SolveBoard(remember(p.id) { SolveSpec.fromSequence(p.fen, p.solution, p.theme) }, onSolved = { solved = true; Store.solveTactic(p.id) })
    if (solved) AppCard(accent = true, modifier = Modifier.padding(top = 8.dp)) { Text(p.explanation, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun DailyTab() {
    val today = remember { java.time.LocalDate.now().toEpochDay() }
    val p = remember(today) { DailyPuzzle.forEpochDay(today) }
    val streak = remember(Store.solvedDaily) { var s = 0; var d = if (today in Store.solvedDaily) today else today - 1; while (d in Store.solvedDaily) { s++; d-- }; s }
    var solved by remember(today) { mutableStateOf(today in Store.solvedDaily) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tagesaufgabe", style = MaterialTheme.typography.titleLarge)
        Text("${p.theme} · ${"★".repeat(p.difficulty)} · Serie: $streak ${if (streak == 1) "Tag" else "Tage"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        SolveBoard(remember(today) { SolveSpec.fromSequence(p.fen, p.solution, p.theme) }, onSolved = { solved = true; Store.solveDaily(today); Store.solveTactic(p.id) })
        if (solved) AppCard(accent = true) { Text(p.explanation, style = MaterialTheme.typography.bodyMedium) }
    }
}

// ------------------------------------------------------------------------------------ Endspiele

@Composable
private fun EndgamesTab(onOpen: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)) {
        items(EndgamesData.drills) { d ->
            AppCard(onClick = { onOpen(d.id) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(d.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    if (d.id in Store.solvedEndgames) Text("✓", color = GoodGreen)
                }
                Text(d.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EndgameView(d: EndgameDrill, runner: StockfishRunner) {
    var thinking by remember(d.id) { mutableStateOf(false) }
    var outcome by remember(d.id) { mutableStateOf<String?>(null) }
    var success by remember(d.id) { mutableStateOf(false) }
    var showTips by remember(d.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val ctrl = remember(d.id) { BoardController(d.fen, autoDraws = true, userSideWhite = d.playerWhite) }
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version
    val game = ctrl.game

    fun evaluate(): Boolean {
        val r = game.status().result
        val userMoves = (game.ply + 1) / 2
        when {
            r is GameResult.Checkmate -> {
                success = r.whiteWins == d.playerWhite
                outcome = if (success) "Matt! Aufgabe gelöst." else "Du wurdest mattgesetzt."
            }
            r is GameResult.Draw -> {
                success = d.goal == EndgameGoal.DRAW
                outcome = if (success) "Remis gehalten (${r.reason.label}). Aufgabe gelöst." else "Remis (${r.reason.label}) – leider kein Sieg. Versuche es noch einmal."
            }
            d.goal == EndgameGoal.DRAW && userMoves >= 30 -> { success = true; outcome = "30 Züge überstanden – Remis gehalten. Aufgabe gelöst." }
            else -> return false
        }
        if (success) { Store.solveEndgame(d.id); Feedback.fire(SoundType.WIN, 60L) }
        return true
    }

    ctrl.mayMove = { !thinking && outcome == null }
    ctrl.onMoved = { m ->
        Feedback.fire(if (m.isCapture) SoundType.CAPTURE else SoundType.MOVE, 15L)
        if (!evaluate()) {
            thinking = true
            scope.launch {
                delay(250)
                val res = runner.search(game.fen, 12, 1, 20, 2500)
                val uci = res?.bestMove ?: res?.lines?.firstOrNull()?.pv?.firstOrNull()
                if (uci != null) ctrl.playExternal(uci)
                else { // Notlösung ohne Engine
                    val f = game.movableSquares().randomOrNull(); val t = f?.let { game.legalTargets(it).randomOrNull() }
                    if (f != null && t != null) ctrl.playExternal(Sq.name(f) + Sq.name(t) + if (game.needsPromotion(f, t)) "q" else "")
                }
                thinking = false
                evaluate()
            }
        }
    }
    if (!d.playerWhite && game.ply == 0 && !thinking && outcome == null) {
        androidx.compose.runtime.LaunchedEffect(d.id) { // Gegner (Weiß) beginnt
            thinking = true
            val res = runner.search(game.fen, 12, 1, 20, 2500)
            res?.bestMove?.let { ctrl.playExternal(it) }
            thinking = false
        }
    }
    Text(d.title, style = MaterialTheme.typography.titleLarge)
    Text(d.goalText, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 6.dp))
    val shownMate = game.status().result as? GameResult.Checkmate
    val kingSq = if (shownMate != null) game.position.kingSquare(game.position.whiteToMove) else -1
    ChessBoard(
        ctrl.shownPosition, ctrl.shownLastMove, ctrl.forward, flipped = !d.playerWhite, style = Store.settings.boardStyle(),
        modifier = Modifier.fillMaxWidth(), selected = ctrl.selected, targets = ctrl.targets,
        checkSquare = if (ctrl.shownLastMove?.givesCheck == true) ctrl.shownPosition.kingSquare(ctrl.shownPosition.whiteToMove) else -1,
        mateSquare = kingSq, shakeKey = ctrl.shakeKey, shakeSq = ctrl.shakeSq,
        interactive = !thinking && outcome == null,
        promotionWhite = ctrl.pendingPromotion?.let { d.playerWhite },
        onPromote = ctrl::choosePromotion, onPromoteCancel = ctrl::cancelPromotion,
        onTap = ctrl::tap, onDragStart = ctrl::dragStart, onDrop = ctrl::drop
    )
    Text(outcome ?: ctrl.message ?: if (thinking) "Stockfish denkt …" else "Zug ${(game.ply) / 2 + 1}", style = MaterialTheme.typography.bodyMedium,
        color = if (outcome != null) (if (success) GoodGreen else BadRed) else if (ctrl.message != null) BadRed else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp))
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { showTips = !showTips }, modifier = Modifier.weight(1f)) { Text("Tipps") }
        OutlinedButton(onClick = { ctrl.reset(); outcome = null; success = false; thinking = false }, modifier = Modifier.weight(1f)) { Text("Neu starten") }
    }
    if (showTips) for (t in d.tips) Text("• $t", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
}

// ------------------------------------------------------------------------------------ Eröffnungen

@Composable
private fun OpeningsTab(onOpen: (Int) -> Unit) {
    var q by remember { mutableStateOf("") }
    val list = remember(q) { OpeningBook.entries.withIndex().filter { q.isBlank() || it.value.name.contains(q, true) || it.value.eco.contains(q, true) || it.value.moves.contains(q, true) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(q, { q = it }, label = { Text("Eröffnung suchen") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)) {
            items(list) { (idx, e) ->
                AppCard(onClick = { onOpen(idx) }) {
                    Text("${e.eco} · ${e.name}", style = MaterialTheme.typography.titleSmall)
                    Text(e.moves, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun OpeningView(e: OpeningEntry) {
    val moves = remember(e) { e.moves.split(" ") }
    val ctrl = remember(e) { BoardController(autoDraws = false) }
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version
    val n = ctrl.game.ply
    Text(e.name, style = MaterialTheme.typography.titleLarge)
    Text("${e.eco} · ${e.moves}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
    Text(e.idea, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 6.dp))
    ChessBoard(ctrl.shownPosition, ctrl.shownLastMove, ctrl.forward, false, Store.settings.boardStyle(), Modifier.fillMaxWidth(), interactive = false)
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { ctrl.undo(1) }, enabled = n > 0, modifier = Modifier.weight(1f)) { Text("Zurück") }
        Button(onClick = { if (n < moves.size) { val m = ctrl.playSanExternal(moves[n]); Feedback.fire(if (m?.isCapture == true) SoundType.CAPTURE else SoundType.MOVE, 12L) } }, enabled = n < moves.size, modifier = Modifier.weight(1f)) { Text("Weiter") }
    }
    Text("Zug $n von ${moves.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
    val cont = OpeningBook.continuations(moves.take(n)).filter { it.name != e.name }.take(6)
    if (cont.isNotEmpty()) {
        SectionTitle("Verwandte Fortsetzungen", Modifier.padding(top = 8.dp))
        for (c in cont) Text("• ${c.name}: ${c.moves}", style = MaterialTheme.typography.bodySmall)
    }
}
