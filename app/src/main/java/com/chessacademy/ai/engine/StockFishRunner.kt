package com.chessacademy.ai.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class SearchResult(val bestMove: String?, val lines: List<InfoLine>)

/**
 * Schlanker UCI-Wrapper um einen Stockfish-Prozess (läuft auf Android aus nativeLibraryDir, im Test von beliebigem Pfad).
 * - Ein Prozess, Suchen werden per Mutex serialisiert (kein Vermischen von Ausgaben).
 * - Wird die aufrufende Coroutine abgebrochen, wird die Suche per "stop" beendet und die Ausgabe leergelesen.
 * - [close] beendet den Prozess (vom ViewModel aufgerufen → keine Prozess-Lecks).
 */
class StockfishRunner(private val binaryPath: String, private val threads: Int = 2, private val hashMb: Int = 32) : Closeable {
    private var process: Process? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private val lock = Mutex()
    private var lastSkill = -1
    private var lastMultiPv = -1

    val isAvailable: Boolean get() = File(binaryPath).exists()

    private fun startLocked(): Boolean {
        if (process?.isAlive == true) return true
        val bin = File(binaryPath)
        if (!bin.exists()) return false
        return try {
            val p = ProcessBuilder(bin.absolutePath).redirectErrorStream(true).start()
            process = p
            reader = BufferedReader(InputStreamReader(p.inputStream))
            writer = BufferedWriter(OutputStreamWriter(p.outputStream))
            send("uci"); waitFor("uciok")
            send("setoption name Threads value $threads")
            send("setoption name Hash value $hashMb")
            send("isready"); waitFor("readyok")
            lastSkill = -1; lastMultiPv = -1
            true
        } catch (e: Exception) { process = null; false }
    }

    suspend fun newGame() = withContext(Dispatchers.IO) {
        lock.withLock { if (startLocked()) { send("ucinewgame"); send("isready"); waitFor("readyok") } }
    }

    /**
     * Sucht in [fen]. [skill] 0..20 (20 = volle Stärke), [multiPv] Anzahl Varianten.
     * Gibt je MultiPV-Index die letzte (tiefste) Info-Zeile zurück, sortiert nach Index.
     */
    suspend fun search(fen: String, depth: Int, multiPv: Int = 1, skill: Int = 20, moveTimeMs: Int? = null): SearchResult? =
        withContext(Dispatchers.IO) {
            lock.withLock {
                if (!startLocked()) return@withLock null
                try {
                    if (skill != lastSkill) { send("setoption name Skill Level value $skill"); lastSkill = skill }
                    if (multiPv != lastMultiPv) { send("setoption name MultiPV value $multiPv"); lastMultiPv = multiPv }
                    send("position fen $fen")
                    send(if (moveTimeMs != null) "go depth $depth movetime $moveTimeMs" else "go depth $depth")
                    val latest = HashMap<Int, InfoLine>()
                    var best: String? = null
                    while (true) {
                        if (!currentCoroutineContext().isActive) { abortSearch(); return@withLock null }
                        val line = reader?.readLine() ?: break
                        UciParser.parseInfo(line)?.let { info ->
                            val prev = latest[info.multiPv]
                            if (prev == null || info.depth >= prev.depth) latest[info.multiPv] = info
                        }
                        val bm = UciParser.parseBestMove(line)
                        if (bm != null) { best = bm; break }
                    }
                    SearchResult(best?.takeIf { it.isNotEmpty() }, latest.toSortedMap().values.toList())
                } catch (e: java.io.IOException) {
                    shutdownLocked(); null
                }
            }
        }

    /** Beendet eine laufende Suche asynchron (best effort). */
    fun requestStop() { try { send("stop") } catch (_: Exception) {} }

    private fun abortSearch() {
        try {
            send("stop")
            while (true) { val l = reader?.readLine() ?: break; if (l.startsWith("bestmove")) break }
        } catch (_: Exception) {}
    }

    private fun send(cmd: String) { writer?.apply { write(cmd); newLine(); flush() } }

    private fun waitFor(token: String) {
        while (true) { val l = reader?.readLine() ?: break; if (l.contains(token)) break }
    }

    private fun shutdownLocked() {
        try { writer?.apply { write("quit"); newLine(); flush() } } catch (_: Exception) {}
        try { process?.destroy() } catch (_: Exception) {}
        process = null; reader = null; writer = null
    }

    override fun close() { shutdownLocked() }
}
