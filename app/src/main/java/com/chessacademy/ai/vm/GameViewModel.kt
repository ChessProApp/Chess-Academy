package com.chessacademy.ai.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.ChessGame
import com.chessacademy.ai.chess.GameResult
import com.chessacademy.ai.chess.PlayedMove
import com.chessacademy.ai.chess.Pgn
import com.chessacademy.ai.chess.Sq
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.engine.AiLevel
import com.chessacademy.ai.engine.AiMoveSelector
import com.chessacademy.ai.engine.AiStyle
import com.chessacademy.ai.engine.EngineEvaluator
import com.chessacademy.ai.engine.GameAnalysis
import com.chessacademy.ai.engine.GameAnalyzer
import com.chessacademy.ai.engine.StockfishRunner
import com.chessacademy.ai.ui.components.BoardController
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Zustand und Ablauf einer Partie gegen Stockfish.
 * Eingabesperre: Der Spieler darf nur ziehen, wenn er am Zug ist, die Engine nicht denkt,
 * die Partie läuft und die Live-Stellung (nicht der Zugverlauf) angezeigt wird – Doppelzüge sind damit ausgeschlossen.
 */
class GameViewModel(app: Application) : AndroidViewModel(app) {

    val runner = StockfishRunner(File(app.applicationInfo.nativeLibraryDir, "libstockfish.so").absolutePath)

    var playerWhite by mutableStateOf(Store.settings.playerWhite)
        private set
    var thinking by mutableStateOf(false)
        private set
    var showResult by mutableStateOf(false)
    var info by mutableStateOf<String?>(null)
    var hintArrow by mutableStateOf<Pair<Int, Int>?>(null)
    var evalLabel by mutableStateOf<String?>(null)
    var analysis by mutableStateOf<GameAnalysis?>(null)
        private set
    var analysisProgress by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    var engineMissing by mutableStateOf(!runner.isAvailable)
        private set

    private var engineJob: Job? = null
    private var analysisJob: Job? = null

    val board = BoardController(
        autoDraws = true,
        userSideWhite = playerWhite,
        mayMove = { !thinking },
        onMoved = { afterMove(it, byPlayer = true) }
    )

    init {
        val saved = Store.loadGame()
        if (saved != null) {
            val parsed = Pgn.parse(saved)
            if (parsed.error == null && !parsed.game.isOver && parsed.game.ply > 0) {
                playerWhite = Store.loadGameSide()
                board.userSideWhite = playerWhite
                board.load(parsed.game)
                info = "Gespeicherte Partie fortgesetzt."
                if (board.game.whiteToMove != playerWhite) engineMove()
            }
        }
    }

    // ------------------------------------------------------------------ Ablauf

    fun newGame(white: Boolean = Store.settings.playerWhite) {
        cancelEngine(); analysisJob?.cancel()
        analysis = null; analysisProgress = null; hintArrow = null; evalLabel = null; showResult = false; info = null
        playerWhite = white
        board.userSideWhite = white
        board.reset()
        Store.saveGame(null)
        viewModelScope.launch { runner.newGame() }
        if (!white) engineMove()
    }

    /** Startet eine Partie ab einer eigenen Stellung (Editor). Der Nutzer spielt die Seite, die am Zug ist. */
    fun startFromFen(fen: String, userIsWhite: Boolean): String? {
        val problem = ChessGame.validate(fen)
        if (problem != null) return problem
        cancelEngine(); analysisJob?.cancel()
        analysis = null; analysisProgress = null; hintArrow = null; evalLabel = null; showResult = false; info = null
        playerWhite = userIsWhite
        board.userSideWhite = userIsWhite
        board.reset(fen)
        Store.saveGame(null)
        if (board.game.isOver) showResult = true
        else if (board.game.whiteToMove != playerWhite) engineMove()
        return null
    }

    private fun afterMove(m: PlayedMove, byPlayer: Boolean) {
        hintArrow = null; evalLabel = null; analysis = null
        val st = board.game.status()
        when {
            st.result is GameResult.Checkmate -> Feedback.fire(SoundType.WIN, 75L)
            st.inCheck -> Feedback.fire(SoundType.CHECK, 45L)
            m.isCapture -> Feedback.fire(SoundType.CAPTURE, 32L)
            else -> Feedback.fire(SoundType.MOVE, 18L)
        }
        persist()
        if (st.isOver) { showResult = true; return }
        if (byPlayer) engineMove()
    }

    private fun persist() {
        val g = board.game
        if (g.isOver || g.ply == 0) Store.saveGame(null)
        else { Store.saveGame(Pgn.export(g, date = Pgn.todayHeaderDate())); Store.saveGameSide(playerWhite) }
    }

    private fun cancelEngine() {
        engineJob?.cancel(); engineJob = null
        runner.requestStop()
        thinking = false
    }

    private fun engineMove() {
        if (board.game.isOver || board.game.whiteToMove == playerWhite) return
        thinking = true
        engineJob = viewModelScope.launch {
            try {
                delay(220) // kurze Atempause: die Spieler-Animation soll zu Ende laufen
                val s = Store.settings
                val level = AiLevel.entries[s.aiLevel]
                val style = AiStyle.entries[s.aiStyle]
                val fen = board.game.fen
                val res = runner.search(fen, level.depth, style.multiPv, level.skill, if (level.depth >= 14) 4000 else null)
                var uci = if (res != null) AiMoveSelector.choose(fen, res.lines, res.bestMove, style) else null
                if (uci == null) { engineMissing = !runner.isAvailable; uci = fallbackMove() }
                val m = uci?.let { board.playExternal(it) }
                thinking = false
                if (m != null) afterMove(m, byPlayer = false)
            } catch (e: CancellationException) {
                throw e
            } finally { thinking = false }
        }
    }

    /** Notlösung ohne Engine (z. B. Binärdatei fehlt): zufälliger legaler Zug, damit die App nie stehen bleibt. */
    private fun fallbackMove(): String? {
        val g = board.game
        val from = g.movableSquares().randomOrNull() ?: return null
        val to = g.legalTargets(from).randomOrNull() ?: return null
        val promo = if (g.needsPromotion(from, to)) "q" else ""
        return Sq.name(from) + Sq.name(to) + promo
    }

    // ------------------------------------------------------------------ Aktionen

    fun undo() {
        if (board.game.ply == 0) return
        cancelEngine()
        hintArrow = null; analysis = null; showResult = false
        board.undo(1)
        while (board.game.ply > 0 && board.game.whiteToMove != playerWhite) board.undo(1)
        persist()
        if (board.game.whiteToMove != playerWhite) engineMove()
    }

    fun resign() {
        if (board.game.isOver) return
        cancelEngine()
        board.game.resign(whiteResigns = playerWhite)
        board.touch(); showResult = true; persist()
        Feedback.fire(SoundType.CLICK, 15L)
    }

    fun claimDraw() {
        if (board.game.claimDraw()) { board.touch(); showResult = true; persist() }
    }

    /** Remis anbieten: Die Engine nimmt an, wenn sie die Stellung nicht klar besser einschätzt. */
    fun offerDraw() {
        val g = board.game
        if (g.isOver || thinking) return
        viewModelScope.launch {
            thinking = true
            val res = try { runner.search(g.fen, 10, 1, 20) } finally { thinking = false }
            val cp = res?.lines?.firstOrNull()?.score?.asCp
            if (cp != null && cp <= 40) {
                g.agreeDraw(); board.touch(); showResult = true; persist()
            } else info = "Stockfish lehnt das Remis ab."
        }
    }

    fun hint() {
        if (thinking || board.game.isOver || board.game.whiteToMove != playerWhite) return
        val fen = board.game.fen
        viewModelScope.launch {
            thinking = true
            val res = try { runner.search(fen, 13, 1, 20) } finally { thinking = false }
            val line = res?.lines?.firstOrNull() ?: return@launch
            val uci = res.bestMove ?: line.pv.firstOrNull() ?: return@launch
            if (uci.length >= 4) hintArrow = Sq.parse(uci.substring(0, 2)) to Sq.parse(uci.substring(2, 4))
            evalLabel = line.score.label(board.game.whiteToMove)
            Feedback.fire(SoundType.HINT, 20L)
        }
    }

    fun exportPgn(): String = Pgn.export(
        board.game,
        white = if (playerWhite) "Spieler" else "Stockfish",
        black = if (playerWhite) "Stockfish" else "Spieler",
        date = Pgn.todayHeaderDate()
    )

    /** Lädt eine Partie aus PGN-Text. Gibt eine Fehlermeldung zurück oder null bei Erfolg. */
    fun importPgn(text: String): String? {
        val p = Pgn.parse(text)
        if (p.game.ply == 0 && p.error != null) return p.error
        cancelEngine(); analysisJob?.cancel()
        analysis = null; hintArrow = null; showResult = false
        board.load(p.game)
        board.userSideWhite = playerWhite
        persist()
        if (p.game.isOver) showResult = true
        else if (p.game.whiteToMove != playerWhite) engineMove()
        return p.error?.let { "Teilweise geladen: $it" }
    }

    // ------------------------------------------------------------------ Analyse

    fun analyze() {
        if (analysisJob?.isActive == true || board.game.ply == 0) return
        cancelEngine()
        val copy = Pgn.parse(Pgn.export(board.game)).game
        analysisProgress = 0 to copy.ply
        analysisJob = viewModelScope.launch {
            try {
                val ev = EngineEvaluator(runner, 12)
                analysis = GameAnalyzer { fen -> ev.evaluate(fen) }.analyze(copy) { done, total -> analysisProgress = done to total }
                if (analysis == null) info = "Analyse nicht möglich (Engine nicht verfügbar)."
            } finally { analysisProgress = null }
        }
    }

    fun stopAnalysis() { analysisJob?.cancel(); runner.requestStop() }

    override fun onCleared() {
        engineJob?.cancel(); analysisJob?.cancel()
        runner.close()
        super.onCleared()
    }
}
