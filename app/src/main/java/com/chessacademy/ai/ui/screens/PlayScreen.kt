package com.chessacademy.ai.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.DrawReason
import com.chessacademy.ai.chess.GameResult
import com.chessacademy.ai.chess.PieceKind
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.engine.AiLevel
import com.chessacademy.ai.engine.AiStyle
import com.chessacademy.ai.engine.MoveClass
import com.chessacademy.ai.learn.MarkerKind
import com.chessacademy.ai.learn.OpeningBook
import com.chessacademy.ai.ui.components.AppCard
import com.chessacademy.ai.ui.components.BoardArrow
import com.chessacademy.ai.ui.components.ChessBoard
import com.chessacademy.ai.ui.components.SectionTitle
import com.chessacademy.ai.ui.components.StatusPill
import com.chessacademy.ai.ui.components.boardStyle
import com.chessacademy.ai.ui.theme.BadRed
import com.chessacademy.ai.ui.theme.GoodGreen
import com.chessacademy.ai.ui.theme.WarnAmber
import com.chessacademy.ai.vm.GameViewModel

@Composable
fun PlayScreen(vm: GameViewModel) {
    val board = vm.board
    @Suppress("UNUSED_VARIABLE") val v = board.version // Neuzeichnen bei jeder Änderung
    val game = board.game
    val s = Store.settings
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var menu by remember { mutableStateOf(false) }
    var showOpponent by remember { mutableStateOf(false) }
    var importDialog by remember { mutableStateOf(false) }
    var confirmResign by remember { mutableStateOf(false) }

    val last = board.shownLastMove
    val shownPos = board.shownPosition
    val checkSq = if (last?.givesCheck == true) shownPos.kingSquare(shownPos.whiteToMove) else -1
    val mateSq = if (last?.givesMate == true) checkSq else -1
    val status = game.status()
    val opening = remember(v) { OpeningBook.detect(game.moves.map { it.san }) }
    val arrows = buildList {
        vm.hintArrow?.let { add(BoardArrow(it.first, it.second, MarkerKind.SAFE)) }
        val an = vm.analysis
        val vp = board.viewPly
        if (an != null && vp != null && vp >= 1) {
            an.plies.getOrNull(vp - 1)?.let { pa ->
                val best = pa.before.bestUci
                if (best != null && best.length >= 4 && best != pa.uci)
                    add(BoardArrow(Sq.parse(best.substring(0, 2)), Sq.parse(best.substring(2, 4)), MarkerKind.LEGAL))
            }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Kopfzeile: Zugrecht/Status + Menü
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            val (txt, col) = when {
                status.result != null -> resultText(status.result!!, vm.playerWhite) to MaterialTheme.colorScheme.primary
                vm.analysisProgress != null -> "Analyse läuft …" to WarnAmber
                vm.thinking -> "Stockfish denkt …" to WarnAmber
                !board.isLive -> "Zugverlauf" to MaterialTheme.colorScheme.onSurfaceVariant
                status.inCheck && game.whiteToMove == vm.playerWhite -> "Schach! Du bist am Zug" to BadRed
                status.inCheck -> "Schach!" to BadRed
                game.whiteToMove == vm.playerWhite -> "Du bist am Zug" to GoodGreen
                else -> "Gegner am Zug" to MaterialTheme.colorScheme.onSurfaceVariant
            }
            StatusPill(txt, col)
            Row {
                IconButton(onClick = { showOpponent = !showOpponent }) { Icon(Icons.Default.Tune, "Gegner") }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Menü") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Neue Partie (Weiß)") }, onClick = { menu = false; vm.newGame(true) })
                        DropdownMenuItem(text = { Text("Neue Partie (Schwarz)") }, onClick = { menu = false; vm.newGame(false) })
                        DropdownMenuItem(text = { Text("Remis anbieten") }, onClick = { menu = false; vm.offerDraw() })
                        if (status.claimable.isNotEmpty())
                            DropdownMenuItem(text = { Text("Remis reklamieren (${status.claimable.first().label})") }, onClick = { menu = false; vm.claimDraw() })
                        DropdownMenuItem(text = { Text("Aufgeben") }, onClick = { menu = false; confirmResign = true })
                        DropdownMenuItem(text = { Text("PGN teilen") }, onClick = {
                            menu = false
                            val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, vm.exportPgn()) }
                            context.startActivity(Intent.createChooser(send, "PGN teilen"))
                        })
                        DropdownMenuItem(text = { Text("PGN kopieren") }, onClick = { menu = false; clipboard.setText(AnnotatedString(vm.exportPgn())); vm.info = "PGN kopiert." })
                        DropdownMenuItem(text = { Text("PGN einfügen / laden") }, onClick = { menu = false; importDialog = true })
                    }
                }
            }
        }

        AnimatedVisibility(showOpponent, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) { OpponentCard() }

        // Brett + Ergebnis-Overlay
        Box {
            ChessBoard(
                position = shownPos, lastMove = last, forward = board.forward, flipped = !vm.playerWhite,
                style = s.boardStyle(), modifier = Modifier.fillMaxWidth(),
                selected = board.selected, targets = board.targets,
                checkSquare = checkSq, mateSquare = mateSq, arrows = arrows,
                shakeKey = board.shakeKey, shakeSq = board.shakeSq,
                interactive = board.isLive && !vm.thinking && !status.isOver,
                promotionWhite = board.pendingPromotion?.let { game.position.whiteToMove },
                onPromote = board::choosePromotion, onPromoteCancel = board::cancelPromotion,
                onTap = board::tap, onDragStart = board::dragStart, onDrop = board::drop
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = vm.showResult && status.isOver && board.isLive,
                enter = fadeIn() + scaleIn(initialScale = 0.85f) + slideInVertically { it / 6 },
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) { ResultCard(vm) }
        }

        // Info-/Fehlermeldung (kompakt, eine Zeile)
        val msg = board.message ?: vm.info
        if (msg != null) {
            Text(msg, style = MaterialTheme.typography.bodySmall, color = if (board.message != null) BadRed else MaterialTheme.colorScheme.onSurfaceVariant)
            LaunchedEffect(msg) { kotlinx.coroutines.delay(3500); if (board.message == msg) board.message = null; if (vm.info == msg) vm.info = null }
        }
        if (vm.engineMissing) Text("Hinweis: Stockfish nicht gefunden – die App spielt mit zufälligen Zügen.", style = MaterialTheme.typography.bodySmall, color = WarnAmber)

        // Steuerung
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            FilledTonalIconButton(onClick = { vm.undo() }, enabled = game.ply > 0) { Icon(Icons.Default.Undo, "Zug zurück") }
            FilledTonalIconButton(onClick = { board.jumpTo((board.viewPly ?: game.ply) - 1) }, enabled = (board.viewPly ?: game.ply) > 0) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Vorheriger Zug") }
            FilledTonalIconButton(onClick = { board.jumpTo((board.viewPly ?: game.ply) + 1) }, enabled = board.viewPly != null) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Nächster Zug") }
            FilledTonalIconButton(onClick = { vm.hint() }, enabled = !vm.thinking && !status.isOver) { Icon(Icons.Default.Lightbulb, "Tipp") }
            FilledTonalIconButton(onClick = { vm.analyze() }, enabled = game.ply > 0 && vm.analysisProgress == null) { Icon(Icons.Default.Analytics, "Analyse") }
        }
        vm.analysisProgress?.let { (d, t) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LinearProgressIndicator(progress = { if (t == 0) 0f else d / t.toFloat() }, modifier = Modifier.weight(1f))
                TextButton(onClick = { vm.stopAnalysis() }) { Text("Abbrechen") }
            }
        }
        vm.evalLabel?.let { Text("Bewertung: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }

        if (opening != null) Text("${opening.eco} · ${opening.name}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        MoveList(vm)
        vm.analysis?.let { AnalysisCard(vm) }
    }

    if (confirmResign) AlertDialog(
        onDismissRequest = { confirmResign = false },
        title = { Text("Aufgeben?") }, text = { Text("Die Partie wird als verloren gewertet.") },
        confirmButton = { TextButton(onClick = { confirmResign = false; vm.resign() }) { Text("Aufgeben") } },
        dismissButton = { TextButton(onClick = { confirmResign = false }) { Text("Weiterspielen") } }
    )
    if (importDialog) {
        var text by remember { mutableStateOf(clipboard.getText()?.text ?: "") }
        var err by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { importDialog = false },
            title = { Text("PGN laden") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 5, maxLines = 8, label = { Text("PGN-Text") })
                    err?.let { Text(it, color = BadRed, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = { TextButton(onClick = { val e = vm.importPgn(text); if (e == null) importDialog = false else { err = e; if (!e.startsWith("Teilweise")) return@TextButton; importDialog = false; vm.info = e } }) { Text("Laden") } },
            dismissButton = { TextButton(onClick = { importDialog = false }) { Text("Abbrechen") } }
        )
    }
}

private fun resultText(r: GameResult, playerWhite: Boolean): String = when (r) {
    is GameResult.Checkmate -> if (r.whiteWins == playerWhite) "Schachmatt – Du gewinnst!" else "Schachmatt – Du verlierst"
    is GameResult.Resignation -> if (r.whiteWins == playerWhite) "Gegner gibt auf" else "Aufgegeben"
    is GameResult.Draw -> "Remis – ${r.reason.label}"
}

@Composable
private fun OpponentCard() {
    val s = Store.settings
    AppCard {
        SectionTitle("Spielstärke")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AiLevel.entries.toList()) { l ->
                FilterChip(selected = s.aiLevel == l.ordinal, onClick = { Feedback.fire(SoundType.CLICK, 10L); Store.update { copy(aiLevel = l.ordinal) } }, label = { Text(l.label) })
            }
        }
        SectionTitle("Spielstil")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AiStyle.entries.toList()) { st ->
                FilterChip(selected = s.aiStyle == st.ordinal, onClick = { Feedback.fire(SoundType.CLICK, 10L); Store.update { copy(aiStyle = st.ordinal) } }, label = { Text(st.label) })
            }
        }
        Text("Gilt ab dem nächsten Zug der Engine.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ResultCard(vm: GameViewModel) {
    val r = vm.board.game.status().result ?: return
    val mate = r is GameResult.Checkmate
    val won = (r as? GameResult.Checkmate)?.whiteWins == vm.playerWhite || (r as? GameResult.Resignation)?.whiteWins == vm.playerWhite
    val lost = (r is GameResult.Checkmate || r is GameResult.Resignation) && !won
    val col = when { won -> GoodGreen; lost -> BadRed; else -> WarnAmber }
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xF2101412), tonalElevation = 8.dp, modifier = Modifier.padding(24.dp)) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (mate) "♚︎" else "½", style = MaterialTheme.typography.titleLarge.copy(fontSize = androidx.compose.ui.unit.TextUnit(40f, androidx.compose.ui.unit.TextUnitType.Sp)), color = col)
            Text(resultText(r, vm.playerWhite), style = MaterialTheme.typography.titleMedium, color = col, fontWeight = FontWeight.Bold)
            Text("${vm.board.game.moves.size} Halbzüge", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.newGame(vm.playerWhite) }) { Text("Neue Partie") }
                OutlinedButton(onClick = { vm.showResult = false; vm.analyze() }) { Text("Analyse") }
            }
            TextButton(onClick = { vm.showResult = false }) { Text("Brett ansehen") }
        }
    }
}

@Composable
private fun MoveList(vm: GameViewModel) {
    val board = vm.board
    val moves = board.game.moves
    if (moves.isEmpty()) return
    val state = rememberLazyListState()
    val cur = board.viewPly ?: moves.size
    LaunchedEffect(moves.size, board.viewPly) { if (moves.isNotEmpty()) state.animateScrollToItem(((cur - 1) / 2).coerceAtLeast(0)) }
    val startNo = com.chessacademy.ai.chess.Position.fromFen(board.game.startFen).fullmove
    val startBlack = !com.chessacademy.ai.chess.Position.fromFen(board.game.startFen).whiteToMove
    val rows = (moves.size + (if (startBlack) 1 else 0) + 1) / 2
    LazyRow(state = state, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(rows) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${startNo + r}.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                for (k in 0..1) {
                    val idx = r * 2 + k - (if (startBlack) 1 else 0)
                    if (idx in moves.indices) {
                        val mv = moves[idx]
                        val sel = idx + 1 == cur
                        val cls = vm.analysis?.plies?.getOrNull(idx)?.moveClass
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (sel) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                            modifier = Modifier.clickable { board.jumpTo(idx + 1) }
                        ) {
                            Text(mv.san + (cls?.takeIf { it != MoveClass.BEST && it != MoveClass.GOOD }?.symbol ?: ""),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = when (cls) { MoveClass.BLUNDER, MoveClass.MISTAKE -> BadRed; MoveClass.INACCURACY -> WarnAmber; else -> MaterialTheme.colorScheme.onSurface })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(vm: GameViewModel) {
    val an = vm.analysis ?: return
    val board = vm.board
    AppCard(accent = true) {
        SectionTitle("Partieanalyse")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Weiß: ${"%.0f".format(an.accuracyWhite)} % Genauigkeit", style = MaterialTheme.typography.bodyMedium)
            Text("Schwarz: ${"%.0f".format(an.accuracyBlack)} %", style = MaterialTheme.typography.bodyMedium)
        }
        for (white in listOf(true, false)) {
            Text(
                (if (white) "Weiß" else "Schwarz") + ": " + listOf(MoveClass.BLUNDER, MoveClass.MISTAKE, MoveClass.INACCURACY)
                    .joinToString(" · ") { "${an.count(white, it)} ${it.label}" },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val vp = board.viewPly ?: board.game.ply
        an.plies.getOrNull(vp - 1)?.let { pa ->
            // Bewertungsbalken (Weiß-Anteil)
            val share = (com.chessacademy.ai.engine.EngineScore.winPercent(pa.after.whiteCp) / 100.0).toFloat()
            Box(Modifier.fillMaxWidth().padding(top = 4.dp).background(Color(0xFF222222), RoundedCornerShape(6.dp))) {
                Box(Modifier.fillMaxWidth(share.coerceIn(0.02f, 1f)).background(Color(0xFFF2F2F2), RoundedCornerShape(6.dp)).padding(vertical = 5.dp))
            }
            Text("${pa.san} ${pa.moveClass.symbol} ${pa.moveClass.label} · Bewertung ${pa.after.label}" + (pa.bestSan?.takeIf { pa.moveClass != MoveClass.BEST && pa.moveClass != MoveClass.GOOD }?.let { " · besser: $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall)
        }
        val critical = an.plies.filter { it.moveClass == MoveClass.BLUNDER || it.moveClass == MoveClass.MISTAKE }
        if (critical.isNotEmpty()) {
            Text("Kritische Züge", style = MaterialTheme.typography.labelMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(critical) { p ->
                    FilterChip(selected = board.viewPly == p.ply, onClick = { board.jumpTo(p.ply) }, label = { Text("${(p.ply + 1) / 2}${if (p.whiteMoved) "." else "..."} ${p.san} ${p.moveClass.symbol}") })
                }
            }
        }
    }
}
