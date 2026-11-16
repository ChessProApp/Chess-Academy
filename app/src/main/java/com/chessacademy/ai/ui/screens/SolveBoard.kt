package com.chessacademy.ai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.Position
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.learn.MarkerKind
import com.chessacademy.ai.ui.components.BoardArrow
import com.chessacademy.ai.ui.components.BoardController
import com.chessacademy.ai.ui.components.ChessBoard
import com.chessacademy.ai.ui.components.boardStyle
import com.chessacademy.ai.ui.theme.BadRed
import com.chessacademy.ai.ui.theme.GoodGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun uciMatches(expected: String, actual: String): Boolean =
    actual == expected || (expected.length == 4 && actual.length == 5 && actual.startsWith(expected))

/**
 * Beschreibung einer Aufgabe: [total] Nutzerzüge; [check] prüft den Zug Nr. i; [reply] ist die automatische Antwort danach.
 */
class SolveSpec(
    val fen: String,
    val total: Int,
    val check: (Int, String) -> Boolean,
    val reply: (Int) -> String?,
    val expected: (Int) -> String?,
    val hint: String,
    val wrong: (String) -> String? = { null }
) {
    companion object {
        /** Zugfolge: gerade Indizes = Nutzer, ungerade = automatische Antwort. */
        fun fromSequence(fen: String, seq: List<String>, hint: String, alternatives: List<String> = emptyList()) = SolveSpec(
            fen = fen, total = (seq.size + 1) / 2,
            check = { i, uci -> uciMatches(seq[2 * i], uci) || (i == 0 && alternatives.any { uciMatches(it, uci) }) },
            reply = { i -> seq.getOrNull(2 * i + 1) },
            expected = { i -> seq.getOrNull(2 * i) },
            hint = hint
        )

        fun fromSolutions(fen: String, solutions: List<String>, hint: String, wrong: Map<String, String>) = SolveSpec(
            fen = fen, total = 1,
            check = { _, uci -> solutions.any { uciMatches(it, uci) } },
            reply = { null }, expected = { solutions.firstOrNull() }, hint = hint,
            wrong = { wrong[it] }
        )
    }
}

/** Brett mit Aufgabe: prüft Züge, spielt Antworten automatisch, nimmt falsche Züge zurück und erklärt. */
@Composable
fun SolveBoard(spec: SolveSpec, onSolved: (firstTry: Boolean) -> Unit, onWrong: () -> Unit = {}) {
    val white = remember(spec.fen) { Position.fromFen(spec.fen).whiteToMove }
    val ctrl = remember(spec) { BoardController(spec.fen, autoDraws = false, userSideWhite = white) }
    var step by remember(spec) { mutableIntStateOf(0) }
    var solved by remember(spec) { mutableStateOf(false) }
    var busy by remember(spec) { mutableStateOf(false) }
    var wrongCount by remember(spec) { mutableIntStateOf(0) }
    var msg by remember(spec) { mutableStateOf<String?>(null) }
    var showHint by remember(spec) { mutableStateOf(false) }
    var showSolution by remember(spec) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version

    ctrl.mayMove = { !solved && !busy }
    ctrl.onMoved = { m ->
        if (spec.check(step, m.uci)) {
            val reply = spec.reply(step)
            val nextStep = step + 1
            showSolution = false
            if (reply == null || nextStep >= spec.total) {
                step = nextStep; solved = true; msg = "Richtig!"
                Feedback.fire(SoundType.WIN, 55L)
                onSolved(wrongCount == 0)
            } else {
                msg = "Richtig – weiter!"
                Feedback.fire(SoundType.MOVE, 15L)
                busy = true
                scope.launch {
                    delay(380)
                    ctrl.playExternal(reply)
                    step = nextStep; busy = false
                }
            }
        } else {
            wrongCount++
            msg = spec.wrong(m.uci) ?: "Das ist nicht der gesuchte Zug. Versuche es noch einmal."
            Feedback.fire(SoundType.ERROR, 30L)
            onWrong()
            busy = true
            scope.launch { delay(800); ctrl.undo(1); busy = false }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val expected = spec.expected(step)
        val arrows = if (showSolution && expected != null && expected.length >= 4)
            listOf(BoardArrow(Sq.parse(expected.substring(0, 2)), Sq.parse(expected.substring(2, 4)), MarkerKind.LEGAL)) else emptyList()
        ChessBoard(
            position = ctrl.shownPosition, lastMove = ctrl.shownLastMove, forward = ctrl.forward, flipped = !white,
            style = Store.settings.boardStyle(), modifier = Modifier.fillMaxWidth(),
            selected = ctrl.selected, targets = ctrl.targets, arrows = arrows,
            shakeKey = ctrl.shakeKey, shakeSq = ctrl.shakeSq, interactive = !solved && !busy,
            promotionWhite = ctrl.pendingPromotion?.let { white },
            onPromote = ctrl::choosePromotion, onPromoteCancel = ctrl::cancelPromotion,
            onTap = ctrl::tap, onDragStart = ctrl::dragStart, onDrop = ctrl::drop
        )
        val text = ctrl.message ?: msg
        if (text != null) Text(text, style = MaterialTheme.typography.bodyMedium, color = if (solved) GoodGreen else if (ctrl.message != null || msg?.startsWith("Richtig") != true) BadRed else GoodGreen)
        if (showHint) Text("Tipp: ${spec.hint}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showHint = true; Feedback.fire(SoundType.HINT, 10L) }, modifier = Modifier.weight(1f)) { Text("Tipp") }
            OutlinedButton(onClick = { showSolution = true; wrongCount++ }, enabled = !solved, modifier = Modifier.weight(1f)) { Text("Lösung") }
            OutlinedButton(onClick = { ctrl.reset(); step = 0; solved = false; busy = false; msg = null; showSolution = false }, modifier = Modifier.weight(1f)) { Text("Neu") }
        }
    }
}
