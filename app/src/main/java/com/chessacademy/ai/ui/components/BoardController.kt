package com.chessacademy.ai.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.ChessGame
import com.chessacademy.ai.chess.PieceKind
import com.chessacademy.ai.chess.PlayedMove
import com.chessacademy.ai.chess.START_FEN
import com.chessacademy.ai.data.Store

/**
 * Eingabe-Logik für ein Brett (Tippen + Ziehen). Wird von Spielmodus, Lektionen, Übungen und Aufgaben genutzt.
 *
 *  - [mayMove] entscheidet, ob gerade gezogen werden darf (Spieler am Zug, Engine denkt nicht, Partie läuft).
 *    So sind Doppelzüge und Züge für die falsche Seite ausgeschlossen.
 *  - Ein Zug wird immer von [ChessGame] geprüft; unzulässige Züge lösen Wackeln + Begründung aus.
 *  - Animationen blockieren nie: Die Logik ist synchron, die Optik läuft nachgelagert.
 */
class BoardController(
    startFen: String = START_FEN,
    private val autoDraws: Boolean = true,
    /** Welche Seite der Nutzer bewegen darf; null = jeweils die Seite am Zug. */
    var userSideWhite: Boolean? = null,
    var mayMove: () -> Boolean = { true },
    var onMoved: (PlayedMove) -> Unit = {},
    /** Wird mit der deutschen Begründung aufgerufen, wenn ein Zug unzulässig war. */
    var onIllegal: (String) -> Unit = {}
) {
    var game: ChessGame = ChessGame(startFen, autoDraws)
        private set
    /** Wird bei jeder Änderung erhöht, damit Compose neu zeichnet. */
    var version by mutableIntStateOf(0)
        private set
    var selected by mutableIntStateOf(-1)
        private set
    var targets by mutableStateOf<List<Int>>(emptyList())
        private set
    var shakeKey by mutableIntStateOf(0)
        private set
    var shakeSq by mutableIntStateOf(-1)
        private set
    var message by mutableStateOf<String?>(null)
    var pendingPromotion by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    /** true nach einem Zug nach vorn (Schlag-Effekte nur dann; bei Zugrücknahme/Springen aus). */
    var forward by mutableStateOf(true)
        private set
    /** Angezeigter Halbzug (null = aktuelle Stellung). Beim Blättern ist die Eingabe gesperrt. */
    var viewPly by mutableStateOf<Int?>(null)
        private set

    val isLive get() = viewPly == null
    val shownPosition get() = game.positionAt(viewPly ?: game.ply)
    val shownLastMove: PlayedMove? get() = (viewPly ?: game.ply).let { if (it == 0) null else game.moves[it - 1] }

    private fun bump() { version++ }

    fun reset(fen: String = game.startFen, autoDraws: Boolean = this.autoDraws) {
        game = ChessGame(fen, autoDraws)
        clearSelection(); viewPly = null; forward = false; message = null; pendingPromotion = null
        bump()
    }

    fun load(g: ChessGame) {
        game = g; clearSelection(); viewPly = null; forward = false; message = null; pendingPromotion = null; bump()
    }

    fun clearSelection() { selected = -1; targets = emptyList() }

    private fun canTouch(): Boolean =
        isLive && !game.isOver && pendingPromotion == null && mayMove() &&
            (userSideWhite == null || userSideWhite == game.whiteToMove)

    private fun select(sq: Int) {
        selected = sq
        targets = game.legalTargets(sq)
        Feedback.fire(SoundType.CLICK, 6L)
    }

    private fun ownPiece(sq: Int): Boolean = game.position.pieceAt(sq)?.white == game.whiteToMove

    fun tap(sq: Int) {
        if (!canTouch()) return
        message = null
        val cur = selected
        when {
            cur == -1 -> if (ownPiece(sq)) select(sq)
            cur == sq -> clearSelection()
            ownPiece(sq) -> select(sq)
            else -> attempt(cur, sq)
        }
    }

    /** Ziehen beginnt: Figur auswählen, damit Zielfelder sichtbar sind. */
    fun dragStart(sq: Int): Boolean {
        if (!canTouch() || !ownPiece(sq)) return false
        message = null
        select(sq)
        return true
    }

    fun drop(from: Int, to: Int) {
        if (!canTouch()) return
        if (from == to) return // bleibt ausgewählt (wie Tippen)
        attempt(from, to)
    }

    private fun attempt(from: Int, to: Int) {
        if (game.isLegal(from, to)) {
            if (game.needsPromotion(from, to)) {
                if (Store.settings.autoQueen) commit(from, to, PieceKind.QUEEN) else pendingPromotion = from to to
            } else commit(from, to, null)
        } else {
            val reason = game.explainIllegal(from, to).message
            message = reason
            shakeSq = from; shakeKey++
            Feedback.fire(SoundType.ERROR, 25L)
            onIllegal(reason)
            clearSelection()
        }
    }

    fun choosePromotion(kind: PieceKind) {
        val p = pendingPromotion ?: return
        pendingPromotion = null
        commit(p.first, p.second, kind)
    }

    fun cancelPromotion() { pendingPromotion = null; clearSelection() }

    private fun commit(from: Int, to: Int, promo: PieceKind?) {
        val m = game.play(from, to, promo) ?: return
        clearSelection(); forward = true; viewPly = null
        bump()
        onMoved(m)
    }

    /** Zug von außen (Engine, „Zug zeigen“, Lösungsketten). Prüft die Legalität selbst. */
    fun playExternal(uci: String): PlayedMove? {
        val m = game.playUci(uci) ?: return null
        clearSelection(); forward = true; viewPly = null; pendingPromotion = null
        bump()
        return m
    }

    /** Zug in SAN von außen (Eröffnungs-Viewer). */
    fun playSanExternal(san: String): PlayedMove? {
        val m = game.playSan(san) ?: return null
        clearSelection(); forward = true; viewPly = null; pendingPromotion = null
        bump()
        return m
    }

    /** Zeigt einen unzulässigen Zug (Wackeln + Begründung der Regel-Engine), führt ihn aber nicht aus. */
    fun showIllegal(uci: String): String? {
        if (uci.length < 4) return null
        val from = com.chessacademy.ai.chess.Sq.parse(uci.substring(0, 2))
        val to = com.chessacademy.ai.chess.Sq.parse(uci.substring(2, 4))
        val reason = game.explainIllegal(from, to).message
        message = reason; shakeSq = from; shakeKey++
        Feedback.fire(SoundType.ERROR, 25L)
        return reason
    }

    fun undo(plies: Int = 1): Int {
        var n = 0
        repeat(plies) { if (game.undo() != null) n++ }
        if (n > 0) { clearSelection(); forward = false; viewPly = null; bump() }
        return n
    }

    fun jumpTo(ply: Int?) {
        viewPly = if (ply == null || ply >= game.ply) null else ply.coerceAtLeast(0)
        clearSelection(); forward = false
        bump()
    }

    /** Ändert den Zustand der Partie von außen (Aufgabe, Remis…) und löst Neuzeichnen aus. */
    fun touch() = bump()
}
