package com.chessacademy.ai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.ChessGame
import com.chessacademy.ai.chess.Pc
import com.chessacademy.ai.chess.PieceKind
import com.chessacademy.ai.chess.Position
import com.chessacademy.ai.chess.START_FEN
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.engine.StockfishRunner
import com.chessacademy.ai.learn.MarkerKind
import com.chessacademy.ai.ui.components.*
import com.chessacademy.ai.ui.theme.BadRed
import com.chessacademy.ai.ui.theme.GoodGreen
import com.chessacademy.ai.vm.GameViewModel
import kotlinx.coroutines.launch

/** Labor: freies Spiel mit beiden Seiten (nur legale Züge), Stellungseditor mit Prüfung, Engine-Bewertung. */
@Composable
fun LabScreen(vm: GameViewModel, onPlayVsEngine: () -> Unit) {
    var mode by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(mode == 0, { mode = 0 }, { Text("Freies Brett") })
            FilterChip(mode == 1, { mode = 1 }, { Text("Stellungseditor") })
        }
        val pending = remember { mutableStateOf<String?>(null) }
        if (mode == 0) FreeBoard(vm, pending.value) { pending.value = null }
        else Editor(vm, onPlayVsEngine, onToFree = { fen -> pending.value = fen; mode = 0 })
    }
}

@Composable
private fun FreeBoard(vm: GameViewModel, startFen: String?, consumed: () -> Unit) {
    val ctrl = remember { BoardController(autoDraws = false) }
    @Suppress("UNUSED_VARIABLE") val v = ctrl.version
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var evalText by remember { mutableStateOf<String?>(null) }
    var best by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    androidx.compose.runtime.LaunchedEffect(startFen) { if (startFen != null) { ctrl.reset(startFen); evalText = null; best = null; consumed() } }
    ctrl.onMoved = { m ->
        evalText = null; best = null
        val st = ctrl.game.status()
        Feedback.fire(when { st.result is com.chessacademy.ai.chess.GameResult.Checkmate -> SoundType.WIN; st.inCheck -> SoundType.CHECK; m.isCapture -> SoundType.CAPTURE; else -> SoundType.MOVE }, 15L)
    }
    val game = ctrl.game
    val last = ctrl.shownLastMove
    val shown = ctrl.shownPosition
    val checkSq = if (last?.givesCheck == true) shown.kingSquare(shown.whiteToMove) else -1
    Text(
        when (val r = game.status().result) {
            null -> (if (game.whiteToMove) "Weiß" else "Schwarz") + " am Zug" + if (game.inCheck) " – Schach!" else ""
            is com.chessacademy.ai.chess.GameResult.Checkmate -> "Schachmatt – ${if (r.whiteWins) "Weiß" else "Schwarz"} gewinnt"
            is com.chessacademy.ai.chess.GameResult.Draw -> "Remis – ${r.reason.label}"
            else -> "Beendet"
        }, style = MaterialTheme.typography.titleSmall
    )
    ChessBoard(
        shown, last, ctrl.forward, false, Store.settings.boardStyle(), Modifier.fillMaxWidth(),
        selected = ctrl.selected, targets = ctrl.targets, checkSquare = checkSq,
        mateSquare = if (last?.givesMate == true) checkSq else -1,
        arrows = best?.let { listOf(BoardArrow(it.first, it.second, MarkerKind.SAFE)) } ?: emptyList(),
        shakeKey = ctrl.shakeKey, shakeSq = ctrl.shakeSq, interactive = ctrl.isLive,
        promotionWhite = ctrl.pendingPromotion?.let { game.position.whiteToMove },
        onPromote = ctrl::choosePromotion, onPromoteCancel = ctrl::cancelPromotion,
        onTap = ctrl::tap, onDragStart = ctrl::dragStart, onDrop = ctrl::drop
    )
    ctrl.message?.let { Text(it, color = BadRed, style = MaterialTheme.typography.bodySmall) }
    evalText?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { ctrl.undo(1) }, enabled = game.ply > 0, modifier = Modifier.weight(1f)) { Text("Zurück") }
        OutlinedButton(onClick = { ctrl.reset(START_FEN); evalText = null; best = null }, modifier = Modifier.weight(1f)) { Text("Neu") }
        Button(onClick = {
            val fen = game.fen
            scope.launch {
                evalText = "Bewertung läuft …"
                val res = runner(vm).search(fen, 14, 1, 20)
                val line = res?.lines?.firstOrNull()
                if (line == null) evalText = "Engine nicht verfügbar."
                else {
                    evalText = "Bewertung ${line.score.label(game.whiteToMove)} · bester Zug ${res.bestMove ?: line.pv.firstOrNull() ?: "-"}"
                    val u = res.bestMove ?: line.pv.firstOrNull()
                    if (u != null && u.length >= 4) best = Sq.parse(u.substring(0, 2)) to Sq.parse(u.substring(2, 4))
                }
            }
        }, modifier = Modifier.weight(1f)) { Text("Bewerten") }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(game.fen)); evalText = "FEN kopiert." }, modifier = Modifier.weight(1f)) { Text("FEN kopieren") }
        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(com.chessacademy.ai.chess.Pgn.export(game, "Weiß", "Schwarz", date = com.chessacademy.ai.chess.Pgn.todayHeaderDate()))); evalText = "PGN kopiert." }, modifier = Modifier.weight(1f)) { Text("PGN kopieren") }
    }
}

private fun runner(vm: GameViewModel): StockfishRunner = vm.runner

@Composable
private fun Editor(vm: GameViewModel, onPlayVsEngine: () -> Unit, onToFree: (String) -> Unit) {
    val squares = remember { mutableStateListOf<Pc?>().apply { addAll(Position.fromFen(START_FEN).squares.toList()) } }
    var whiteToMove by remember { mutableStateOf(true) }
    var wK by remember { mutableStateOf(true) }
    var wQ by remember { mutableStateOf(true) }
    var bK by remember { mutableStateOf(true) }
    var bQ by remember { mutableStateOf(true) }
    var tool by remember { mutableStateOf<Pc?>(Pc(PieceKind.QUEEN, true)) } // null = Radierer
    var msg by remember { mutableStateOf<String?>(null) }
    var fenText by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current

    // Rochaderechte nur, wenn König und Turm tatsächlich auf ihren Grundfeldern stehen
    fun has(sq: String, k: PieceKind, w: Boolean) = squares[Sq.parse(sq)] == Pc(k, w)
    val castling = buildString {
        if (wK && has("e1", PieceKind.KING, true) && has("h1", PieceKind.ROOK, true)) append('K')
        if (wQ && has("e1", PieceKind.KING, true) && has("a1", PieceKind.ROOK, true)) append('Q')
        if (bK && has("e8", PieceKind.KING, false) && has("h8", PieceKind.ROOK, false)) append('k')
        if (bQ && has("e8", PieceKind.KING, false) && has("a8", PieceKind.ROOK, false)) append('q')
    }
    val position = Position(squares.toTypedArray(), whiteToMove, castling, -1, 0, 1)
    // Rochaderechte werden von der Prüfung bereinigt, falls König/Turm nicht stehen (siehe ChessGame.validate)
    ChessBoard(position, null, false, false, Store.settings.boardStyle(), Modifier.fillMaxWidth(),
        onTap = { sq -> squares[sq] = if (squares[sq] == tool) null else tool; msg = null; Feedback.fire(SoundType.CLICK, 6L) })
    Text("Figur wählen, dann Feld antippen (nochmals tippen = entfernen).", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    val palette = buildList { for (w in listOf(true, false)) for (k in listOf(PieceKind.KING, PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT, PieceKind.PAWN)) add(Pc(k, w)) }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { FilterChip(tool == null, { tool = null }, { Text("Radierer") }) }
        items(palette) { pc ->
            FilterChip(tool == pc, { tool = pc }, { Text((if (pc.white) "W " else "S ") + pc.kind.germanName) })
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(whiteToMove, { whiteToMove = true }, { Text("Weiß am Zug") })
        FilterChip(!whiteToMove, { whiteToMove = false }, { Text("Schwarz am Zug") })
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(wK, { wK = it }); Text("O-O", style = MaterialTheme.typography.bodySmall)
        Checkbox(wQ, { wQ = it }); Text("O-O-O Weiß", style = MaterialTheme.typography.bodySmall)
        Checkbox(bK, { bK = it }); Text("O-O", style = MaterialTheme.typography.bodySmall)
        Checkbox(bQ, { bQ = it }); Text("O-O-O Schwarz", style = MaterialTheme.typography.bodySmall)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { val p = Position.fromFen(START_FEN); for (i in 0..63) squares[i] = p.squares[i]; whiteToMove = true; wK = true; wQ = true; bK = true; bQ = true; msg = null }, modifier = Modifier.weight(1f)) { Text("Grundstellung") }
        OutlinedButton(onClick = { for (i in 0..63) squares[i] = null; msg = null }, modifier = Modifier.weight(1f)) { Text("Leeren") }
    }
    OutlinedTextField(fenText, { fenText = it }, label = { Text("FEN einfügen / anzeigen") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            val t = fenText.ifBlank { clipboard.getText()?.text ?: "" }.trim()
            val problem = ChessGame.validate(t)
            if (problem != null) msg = problem else {
                val p = Position.fromFen(t)
                for (i in 0..63) squares[i] = p.squares[i]
                whiteToMove = p.whiteToMove; wK = 'K' in p.castling; wQ = 'Q' in p.castling; bK = 'k' in p.castling; bQ = 'q' in p.castling
                fenText = t; msg = "FEN geladen."
            }
        }, modifier = Modifier.weight(1f)) { Text("FEN laden") }
        OutlinedButton(onClick = { fenText = position.toFen(); clipboard.setText(AnnotatedString(fenText)); msg = "FEN kopiert." }, modifier = Modifier.weight(1f)) { Text("FEN kopieren") }
    }
    msg?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = if (it.endsWith("geladen.") || it.endsWith("kopiert.") || it.startsWith("Stellung ist")) GoodGreen else BadRed) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = {
            val fen = position.toFen()
            val problem = ChessGame.validate(fen)
            if (problem != null) msg = problem else onToFree(fen)
        }, modifier = Modifier.weight(1f)) { Text("Frei spielen") }
        Button(onClick = {
            val fen = position.toFen()
            val problem = ChessGame.validate(fen)
            if (problem != null) msg = problem else { val e = vm.startFromFen(fen, whiteToMove); if (e != null) msg = e else onPlayVsEngine() }
        }, modifier = Modifier.weight(1f)) { Text("Gegen Stockfish") }
    }
}
