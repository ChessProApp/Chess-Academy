package com.chessacademy.ai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.chessacademy.ai.chess.PieceKind
import com.chessacademy.ai.chess.Pc
import com.chessacademy.ai.chess.PieceTracker
import com.chessacademy.ai.chess.PlayedMove
import com.chessacademy.ai.chess.Position
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.chess.TrackedPiece
import com.chessacademy.ai.data.Settings
import com.chessacademy.ai.learn.MarkerKind
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Optische Brett-Einstellungen (aus [Settings] abgeleitet). */
data class BoardStyle(
    val light: Color,
    val dark: Color,
    val border: Color,
    val showBorder: Boolean = true,
    val showCoords: Boolean = true,
    val ringHints: Boolean = false,
    val serif: Boolean = false,
    val animations: Boolean = true,
    val showLegal: Boolean = true,
    val highlightLast: Boolean = true,
    val drag: Boolean = true
)

fun Settings.boardStyle() = BoardStyle(
    light = Color(lightColor), dark = Color(darkColor), border = Color(borderColor),
    showBorder = showBorder, showCoords = showCoords, ringHints = ringHints, serif = serifPieces,
    animations = animations, showLegal = showLegalMoves, highlightLast = highlightLast, drag = dragEnabled
)

data class BoardArrow(val from: Int, val to: Int, val kind: MarkerKind = MarkerKind.FOCUS)

private fun glyph(kind: PieceKind): String = when (kind) {
    PieceKind.KING -> "♚"; PieceKind.QUEEN -> "♛"; PieceKind.ROOK -> "♜"
    PieceKind.BISHOP -> "♝"; PieceKind.KNIGHT -> "♞"; PieceKind.PAWN -> "♟"
} + "︎" // U+FE0E erzwingt Textdarstellung (sonst erscheint der Bauer als Emoji)

private fun markerColor(k: MarkerKind): Color = when (k) {
    MarkerKind.ATTACK -> Color(0xFFEF4444)
    MarkerKind.LEGAL -> Color(0xFF22C55E)
    MarkerKind.ILLEGAL -> Color(0xFFDC2626)
    MarkerKind.FOCUS -> Color(0xFFFACC15)
    MarkerKind.SAFE -> Color(0xFF38BDF8)
}

private class Snapshot(val pieces: List<TrackedPiece>, val removed: List<TrackedPiece>, val token: Int)
private class DragHolder { var units: Offset? = null }

/**
 * Das Brett. Reine Darstellung + Gesten; die Spiellogik liegt in [BoardController]/[com.chessacademy.ai.chess.ChessGame].
 *
 * Performance: Figuren sind je eine Composable mit stabiler ID. Bewegung (Gleiten, Ziehen, Wackeln, Kippen)
 * wird ausschließlich in graphicsLayer-Lambdas gelesen – pro Animationsbild wird also nur gezeichnet,
 * nicht neu komponiert. Dauer-Animationen (Schach-Puls) laufen nur, solange sie sichtbar sind.
 */
@Composable
fun ChessBoard(
    position: Position,
    lastMove: PlayedMove?,
    forward: Boolean,
    flipped: Boolean,
    style: BoardStyle,
    modifier: Modifier = Modifier,
    selected: Int = -1,
    targets: List<Int> = emptyList(),
    checkSquare: Int = -1,
    mateSquare: Int = -1,
    markers: Map<Int, MarkerKind> = emptyMap(),
    arrows: List<BoardArrow> = emptyList(),
    shakeKey: Int = 0,
    shakeSq: Int = -1,
    interactive: Boolean = true,
    promotionWhite: Boolean? = null,
    onPromote: (PieceKind) -> Unit = {},
    onPromoteCancel: () -> Unit = {},
    onTap: (Int) -> Unit = {},
    onDragStart: (Int) -> Boolean = { false },
    onDrop: (Int, Int) -> Unit = { _, _ -> }
) {
    val tracker = remember { PieceTracker().also { it.reset(position) } }
    var token by remember { mutableIntStateOf(0) }
    val snap = remember(position) {
        val removed = tracker.update(position, lastMove?.from ?: -1, lastMove?.to ?: -1)
        token++
        Snapshot(tracker.pieces, removed, token)
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0 && style.animations) {
            repeat(3) { shake.animateTo(1f, tween(45, easing = LinearEasing)); shake.animateTo(-1f, tween(45, easing = LinearEasing)) }
            shake.animateTo(0f, tween(45, easing = LinearEasing))
        }
    }

    var dragSq by remember { mutableIntStateOf(-1) }
    var dragPos by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val framePad = if (style.showBorder) 5.dp else 0.dp

    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(if (style.showBorder) style.border else Color.Transparent)
            .padding(framePad)
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(if (style.showBorder) 9.dp else 14.dp))
        ) {
            val cellPx = constraints.maxWidth / 8f
            val cellDp = with(density) { cellPx.toDp() }
            fun col(sq: Int) = if (flipped) 7 - Sq.file(sq) else Sq.file(sq)
            fun row(sq: Int) = if (flipped) Sq.rank(sq) else 7 - Sq.rank(sq)
            fun squareAt(o: Offset): Int {
                val c = (o.x / cellPx).toInt().coerceIn(0, 7)
                val r = (o.y / cellPx).toInt().coerceIn(0, 7)
                return Sq.of(if (flipped) 7 - c else c, if (flipped) r else 7 - r)
            }

            // ---- Ebene 1: Felder, Markierungen, Hinweise
            val hint = remember(selected) { Animatable(0f) }
            LaunchedEffect(selected, targets) { hint.animateTo(1f, tween(if (style.animations) 130 else 0)) }
            Canvas(Modifier.fillMaxSize()) {
                for (sq in 0..63) {
                    val tl = Offset(col(sq) * cellPx, row(sq) * cellPx)
                    drawRect(if (Sq.isLight(sq)) style.light else style.dark, tl, Size(cellPx, cellPx))
                }
                fun tint(sq: Int, c: Color) = drawRect(c, Offset(col(sq) * cellPx, row(sq) * cellPx), Size(cellPx, cellPx))
                if (style.highlightLast && lastMove != null) {
                    tint(lastMove.from, Color(0x55E6D84A)); tint(lastMove.to, Color(0x66E6D84A))
                }
                for ((sq, k) in markers) {
                    tint(sq, markerColor(k).copy(alpha = 0.42f))
                    if (k == MarkerKind.ILLEGAL) {
                        val tl = Offset(col(sq) * cellPx, row(sq) * cellPx)
                        val p = cellPx * 0.26f
                        drawLine(Color.White.copy(alpha = 0.9f), tl + Offset(p, p), tl + Offset(cellPx - p, cellPx - p), cellPx * 0.07f)
                        drawLine(Color.White.copy(alpha = 0.9f), tl + Offset(cellPx - p, p), tl + Offset(p, cellPx - p), cellPx * 0.07f)
                    }
                }
                if (selected in 0..63) tint(selected, Color(0x88F6F669))
                if (style.showLegal) {
                    val h = hint.value
                    for (t in targets) {
                        val c = Offset(col(t) * cellPx + cellPx / 2, row(t) * cellPx + cellPx / 2)
                        val isCapture = position.pieceAt(t) != null || (lastMoveEpTarget(position, selected, t))
                        when {
                            isCapture -> drawCircle(Color(0x66000000), cellPx * 0.46f * h, c, style = Stroke(cellPx * 0.08f))
                            style.ringHints -> drawCircle(Color(0x66000000), cellPx * 0.24f * h, c, style = Stroke(cellPx * 0.06f))
                            else -> drawCircle(Color(0x55000000), cellPx * 0.16f * h, c)
                        }
                    }
                }
            }

            // ---- Ebene 2: Schach-/Mattleuchten (nur sichtbar, solange nötig)
            if (checkSquare in 0..63 && mateSquare != checkSquare && style.animations) CheckGlow(col(checkSquare), row(checkSquare), cellPx)
            if (checkSquare in 0..63 && mateSquare != checkSquare && !style.animations) StaticGlow(col(checkSquare), row(checkSquare), cellPx)
            if (mateSquare in 0..63) MateBurst(col(mateSquare), row(mateSquare), cellPx, style.animations)

            // ---- Ebene 3: Figuren (mit stabiler Identität)
            val ghostsOn = forward && style.animations
            if (ghostsOn) for (g in snap.removed) key(snap.token, g.id) { CaptureGhost(g, col(g.square), row(g.square), cellPx, cellDp, style) }

            val holders = remember { HashMap<Int, DragHolder>() }
            for (tp in snap.pieces) key(tp.id) {
                val holder = holders.getOrPut(tp.id) { DragHolder() }
                PieceSprite(
                    tp = tp, tx = col(tp.square), ty = row(tp.square), cellPx = cellPx, cellDp = cellDp, style = style,
                    isSelected = tp.square == selected,
                    dragging = dragSq == tp.square,
                    dragPx = { if (dragSq == tp.square) dragPos else null },
                    holder = holder,
                    shake = { if (tp.square == shakeSq) shake.value else 0f },
                    tilted = tp.square == mateSquare,
                    glide = style.animations
                )
            }

            // ---- Ebene 4: Pfeile
            if (arrows.isNotEmpty()) Canvas(Modifier.fillMaxSize()) {
                for (a in arrows) drawArrow(
                    Offset(col(a.from) * cellPx + cellPx / 2, row(a.from) * cellPx + cellPx / 2),
                    Offset(col(a.to) * cellPx + cellPx / 2, row(a.to) * cellPx + cellPx / 2),
                    markerColor(a.kind).copy(alpha = 0.85f), cellPx
                )
            }

            // ---- Ebene 5: Koordinaten
            if (style.showCoords) {
                val pad = with(density) { 2.dp.roundToPx() }
                for (c in 0..7) {
                    val file = if (flipped) 7 - c else c
                    val sq = Sq.of(file, if (flipped) 7 else 0)
                    Text(
                        "${'a' + file}", fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp,
                        color = if (Sq.isLight(sq)) style.dark else style.light,
                        modifier = Modifier.offset { IntOffset((c * cellPx + cellPx).toInt() - pad - 18, (7 * cellPx + cellPx).toInt() - pad - 26) }
                    )
                }
                for (r in 0..7) {
                    val rank = if (flipped) r else 7 - r
                    val sq = Sq.of(if (flipped) 7 else 0, rank)
                    Text(
                        "${rank + 1}", fontSize = 9.sp, fontWeight = FontWeight.Bold, lineHeight = 10.sp,
                        color = if (Sq.isLight(sq)) style.dark else style.light,
                        modifier = Modifier.offset { IntOffset(pad, (r * cellPx).toInt() + pad) }
                    )
                }
            }

            // ---- Ebene 6: Eingabe (Tippen + Ziehen)
            if (interactive) Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(5f)
                    .pointerInput(flipped, cellPx) { detectTapGestures { o -> onTap(squareAt(o)) } }
                    .then(
                        if (style.drag) Modifier.pointerInput(flipped, cellPx) {
                            detectDragGestures(
                                onDragStart = { o -> val s = squareAt(o); if (onDragStart(s)) { dragSq = s; dragPos = o } },
                                onDrag = { change, amount -> change.consume(); if (dragSq >= 0) dragPos += amount },
                                onDragEnd = { val from = dragSq; val to = squareAt(dragPos); dragSq = -1; if (from >= 0) onDrop(from, to) },
                                onDragCancel = { dragSq = -1 }
                            )
                        } else Modifier
                    )
            )

            // ---- Ebene 7: Umwandlungs-Auswahl
            AnimatedVisibility(
                visible = promotionWhite != null,
                enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.9f),
                exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.9f),
                modifier = Modifier.align(Alignment.Center).zIndex(10f)
            ) {
                PromotionPicker(promotionWhite ?: true, style.serif, cellDp, onPromote, onPromoteCancel)
            }
        }
    }
}

private fun lastMoveEpTarget(pos: Position, from: Int, to: Int): Boolean {
    val p = pos.pieceAt(from) ?: return false
    return p.kind == PieceKind.PAWN && pos.epSquare == to
}

// ------------------------------------------------------------------------------------ Figur

@Composable
private fun PieceSprite(
    tp: TrackedPiece, tx: Int, ty: Int, cellPx: Float, cellDp: androidx.compose.ui.unit.Dp, style: BoardStyle,
    isSelected: Boolean, dragging: Boolean, dragPx: () -> Offset?, holder: DragHolder,
    shake: () -> Float, tilted: Boolean, glide: Boolean
) {
    val target = Offset(tx.toFloat(), ty.toFloat())
    val anim = remember { Animatable(target, Offset.VectorConverter) }
    LaunchedEffect(target, dragging) {
        if (dragging) return@LaunchedEffect
        holder.units?.let { anim.snapTo(it); holder.units = null } // von der Loslassstelle weitergleiten
        if (anim.value != target) {
            if (!glide) anim.snapTo(target) else {
                val d = (anim.value - target).getDistance()
                anim.animateTo(target, tween((120 + 45 * d).toInt().coerceAtMost(340), easing = FastOutSlowInEasing))
            }
        }
    }
    val scale by animateFloatAsState(
        if (dragging) 1.2f else if (isSelected) 1.1f else 1f,
        spring(dampingRatio = 0.55f, stiffness = 500f), label = "pieceScale"
    )
    val tilt = remember { Animatable(0f) }
    LaunchedEffect(tilted) {
        if (tilted) { if (glide) tilt.animateTo(78f, tween(520, delayMillis = 380, easing = FastOutSlowInEasing)) else tilt.snapTo(78f) }
        else tilt.snapTo(0f)
    }
    val fontSp = with(LocalDensity.current) { (cellPx * 0.74f).toSp() }
    val white = tp.piece.white
    val fill = if (white) Color(0xFFFFFFFF) else Color(0xFF1B1B1B)
    val outline = if (white) Color(0xFF1B1B1B) else Color(0x99FFFFFF)
    val family = if (style.serif) FontFamily.Serif else FontFamily.Default
    val text = glyph(tp.piece.kind)
    val moving = anim.isRunning || dragging

    Box(
        Modifier
            .size(cellDp)
            .zIndex(if (dragging) 4f else if (moving) 2f else 1f)
            .graphicsLayer {
                val d = dragPx()
                if (d != null) {
                    translationX = d.x - cellPx / 2
                    translationY = d.y - cellPx / 2 - cellPx * 0.18f // Figur über dem Finger sichtbar
                    holder.units = Offset((d.x - cellPx / 2) / cellPx, (d.y - cellPx / 2 - cellPx * 0.18f) / cellPx)
                } else {
                    val p = anim.value
                    translationX = p.x * cellPx + shake() * cellPx * 0.07f
                    translationY = p.y * cellPx
                }
                scaleX = scale; scaleY = scale
                rotationZ = tilt.value
                transformOrigin = TransformOrigin(0.5f, 0.85f)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = TextStyle(fontSize = fontSp, fontFamily = family, color = outline, drawStyle = Stroke(width = cellPx * 0.045f, join = StrokeJoin.Round)), lineHeight = fontSp)
        Text(text, style = TextStyle(fontSize = fontSp, fontFamily = family, color = fill), lineHeight = fontSp)
    }
}

/** Geschlagene Figur: kurz sichtbar bleiben, dann schrumpfen + ausblenden + dezente Partikel. */
@Composable
private fun CaptureGhost(tp: TrackedPiece, tx: Int, ty: Int, cellPx: Float, cellDp: androidx.compose.ui.unit.Dp, style: BoardStyle) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(300, delayMillis = 90, easing = FastOutSlowInEasing)) }
    val fontSp = with(LocalDensity.current) { (cellPx * 0.74f).toSp() }
    val white = tp.piece.white
    val family = if (style.serif) FontFamily.Serif else FontFamily.Default
    val text = glyph(tp.piece.kind)
    Box(
        Modifier
            .size(cellDp)
            .offset { IntOffset((tx * cellPx).toInt(), (ty * cellPx).toInt()) }
            .zIndex(1.5f),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer { alpha = 1f - p.value; scaleX = 1f - 0.45f * p.value; scaleY = 1f - 0.45f * p.value },
            contentAlignment = Alignment.Center
        ) {
            Text(text, style = TextStyle(fontSize = fontSp, fontFamily = family, color = if (white) Color(0xFF1B1B1B) else Color(0x99FFFFFF), drawStyle = Stroke(width = cellPx * 0.045f, join = StrokeJoin.Round)), lineHeight = fontSp)
            Text(text, style = TextStyle(fontSize = fontSp, fontFamily = family, color = if (white) Color.White else Color(0xFF1B1B1B)), lineHeight = fontSp)
        }
        Canvas(Modifier.fillMaxSize()) {
            val t = p.value
            if (t > 0f && t < 1f) {
                val c = Offset(size.width / 2, size.height / 2)
                for (i in 0 until 8) {
                    val a = i * (2 * PI / 8).toFloat() + 0.4f
                    val r = cellPx * (0.18f + 0.42f * t)
                    drawCircle(
                        (if (white) Color.White else Color(0xFFFACC15)).copy(alpha = (1f - t) * 0.75f),
                        cellPx * 0.055f * (1f - t * 0.6f),
                        c + Offset(cos(a) * r, sin(a) * r)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------ Effekte

@Composable
private fun CheckGlow(cx: Int, cy: Int, cellPx: Float) {
    val t = rememberInfiniteTransition(label = "checkGlow")
    val a by t.animateFloat(0.30f, 0.90f, infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "alpha")
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset((cx + 0.5f) * cellPx, (cy + 0.5f) * cellPx)
        val r = cellPx * (0.85f + 0.15f * a)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFF2D2D).copy(alpha = a), Color.Transparent), c, r), r, c)
    }
}

@Composable
private fun StaticGlow(cx: Int, cy: Int, cellPx: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset((cx + 0.5f) * cellPx, (cy + 0.5f) * cellPx)
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFF2D2D).copy(alpha = 0.7f), Color.Transparent), c, cellPx), cellPx, c)
    }
}

/** Matt: König kippt (im Sprite), zusätzlich ein Lichtring, der sich einmal ausbreitet. */
@Composable
private fun MateBurst(cx: Int, cy: Int, cellPx: Float, animate: Boolean) {
    val p = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) { if (animate) p.animateTo(1f, tween(1100, delayMillis = 150, easing = FastOutSlowInEasing)) }
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset((cx + 0.5f) * cellPx, (cy + 0.5f) * cellPx)
        val t = p.value
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFF2D2D).copy(alpha = 0.75f), Color.Transparent), c, cellPx * 1.1f), cellPx * 1.1f, c)
        if (t < 1f) {
            drawCircle(Color(0xFFFFD54F).copy(alpha = (1f - t) * 0.9f), cellPx * (0.4f + 3.2f * t), c, style = Stroke(cellPx * 0.09f * (1f - t) + 1f))
            drawCircle(Color(0xFFFF5252).copy(alpha = (1f - t) * 0.6f), cellPx * (0.2f + 2.2f * t), c, style = Stroke(cellPx * 0.06f * (1f - t) + 1f))
        }
    }
}

private fun DrawScope.drawArrow(from: Offset, to: Offset, color: Color, cell: Float) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return
    val ux = dx / len
    val uy = dy / len
    val head = cell * 0.34f
    val start = Offset(from.x + ux * cell * 0.18f, from.y + uy * cell * 0.18f)
    val neck = Offset(to.x - ux * head, to.y - uy * head)
    drawLine(color, start, neck, cell * 0.14f)
    val path = Path().apply {
        moveTo(to.x - ux * cell * 0.05f, to.y - uy * cell * 0.05f)
        lineTo(neck.x - uy * head * 0.55f, neck.y + ux * head * 0.55f)
        lineTo(neck.x + uy * head * 0.55f, neck.y - ux * head * 0.55f)
        close()
    }
    drawPath(path, color)
}

// ------------------------------------------------------------------------------------ Umwandlung

@Composable
fun PromotionPicker(
    white: Boolean, serif: Boolean, cell: androidx.compose.ui.unit.Dp,
    onPick: (PieceKind) -> Unit, onCancel: () -> Unit
) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xEE111111), tonalElevation = 6.dp) {
        Row(Modifier.padding(8.dp)) {
            for (k in listOf(PieceKind.QUEEN, PieceKind.ROOK, PieceKind.BISHOP, PieceKind.KNIGHT)) {
                Box(
                    Modifier.size(cell * 1.05f).clip(RoundedCornerShape(12.dp)).clickable { onPick(k) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        glyph(k),
                        style = TextStyle(
                            fontSize = with(LocalDensity.current) { (cell.toPx() * 0.8f).toSp() },
                            fontFamily = if (serif) FontFamily.Serif else FontFamily.Default,
                            color = if (white) Color.White else Color(0xFFBBBBBB)
                        )
                    )
                }
            }
        }
    }
}

/** Kleines, nicht interaktives Brett für Kapitel- und Aufgabenvorschau. */
@Composable
fun StaticBoard(position: Position, style: BoardStyle, flipped: Boolean = false, modifier: Modifier = Modifier,
                markers: Map<Int, MarkerKind> = emptyMap(), arrows: List<BoardArrow> = emptyList()) {
    ChessBoard(position, null, false, flipped, style.copy(animations = false), modifier, markers = markers, arrows = arrows, interactive = false)
}
