package com.chessacademy.ai.chess

import java.util.Locale

data class PgnGame(
    val headers: Map<String, String>,
    val game: ChessGame,
    /** Fehlermeldung, falls ein Zug nicht gespielt werden konnte (Partie enthält dann alle Züge davor). */
    val error: String? = null
)

object Pgn {

    fun export(
        game: ChessGame,
        white: String = "Spieler",
        black: String = "Stockfish",
        event: String = "Chess Academy",
        date: String = "????.??.??",
        extraHeaders: Map<String, String> = emptyMap()
    ): String {
        val status = game.status()
        val result = status.result?.pgnToken ?: "*"
        val sb = StringBuilder()
        fun tag(k: String, v: String) = sb.append("[").append(k).append(" \"").append(v.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"]\n")
        tag("Event", event)
        tag("Site", "Chess Academy (Android)")
        tag("Date", date)
        tag("Round", "-")
        tag("White", white)
        tag("Black", black)
        tag("Result", result)
        if (game.startFen != START_FEN) {
            tag("SetUp", "1")
            tag("FEN", game.startFen)
        }
        for ((k, v) in extraHeaders) tag(k, v)
        sb.append('\n')

        val start = Position.fromFen(game.startFen)
        var moveNo = start.fullmove
        var whiteTurn = start.whiteToMove
        val tokens = ArrayList<String>()
        for ((i, m) in game.moves.withIndex()) {
            if (whiteTurn) tokens.add("$moveNo. ${m.san}")
            else tokens.add(if (i == 0) "$moveNo... ${m.san}" else m.san)
            if (!whiteTurn) moveNo++
            whiteTurn = !whiteTurn
        }
        tokens.add(result)
        // auf ~80 Zeichen umbrechen
        var line = StringBuilder()
        for (t in tokens) {
            if (line.isNotEmpty() && line.length + 1 + t.length > 80) {
                sb.append(line).append('\n'); line = StringBuilder()
            }
            if (line.isNotEmpty()) line.append(' ')
            line.append(t)
        }
        if (line.isNotEmpty()) sb.append(line).append('\n')
        return sb.toString()
    }

    /** Liest die erste Partie aus einem PGN-Text. */
    fun parse(text: String): PgnGame {
        val headers = LinkedHashMap<String, String>()
        val body = StringBuilder()
        var sawMovetext = false
        for (rawLine in text.replace("\r", "").split('\n')) {
            val trimmed = rawLine.trim()
            // ';' leitet einen Zeilenkommentar ein (außer in Tag-Zeilen)
            val line = if (trimmed.startsWith("[")) trimmed else trimmed.substringBefore(';').trim()
            if (line.startsWith("[") && line.endsWith("]") && !sawMovetext) {
                val m = Regex("""^\[(\w+)\s+"((?:[^"\\]|\\.)*)"\]$""").find(line)
                if (m != null) headers[m.groupValues[1]] = m.groupValues[2].replace("\\\"", "\"").replace("\\\\", "\\")
                continue
            }
            if (line.isEmpty()) {
                if (sawMovetext) break // zweite Partie beginnt nach Leerzeile + Tag; wir lesen nur die erste
                continue
            }
            if (line.startsWith("[") && sawMovetext) break
            if (line.startsWith("%")) continue
            sawMovetext = true
            body.append(line).append(' ')
        }
        val fen = headers["FEN"]?.takeIf { headers["SetUp"] == "1" || it.isNotBlank() } ?: START_FEN
        val game = try { ChessGame(fen) } catch (e: IllegalArgumentException) {
            return PgnGame(headers, ChessGame(), "Ungültiges FEN im PGN: ${e.message}")
        }
        var error: String? = null
        for (tok in tokenize(body.toString())) {
            if (isResult(tok)) break
            if (game.playSan(tok) == null) {
                error = "Zug '$tok' ist an dieser Stelle nicht legal (nach ${game.ply} Halbzügen)."
                break
            }
        }
        return PgnGame(headers, game, error)
    }

    private fun isResult(t: String) = t == "1-0" || t == "0-1" || t == "1/2-1/2" || t == "*"

    private fun tokenize(raw: String): List<String> {
        // Kommentare { ... }, ; Zeilenkommentare, Varianten ( ... ), NAGs $n und Zugnummern entfernen
        val noBrace = StringBuilder()
        var depth = 0
        var variation = 0
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            when {
                c == '{' -> depth++
                c == '}' -> if (depth > 0) depth--
                depth > 0 -> {}
                c == '(' -> variation++
                c == ')' -> if (variation > 0) variation--
                variation > 0 -> {}
                else -> noBrace.append(c)
            }
            i++
        }
        val out = ArrayList<String>()
        for (part in noBrace.toString().split(Regex("\\s+"))) {
            var t = part.trim()
            if (t.isEmpty() || t.startsWith("$")) continue
            t = t.replace(Regex("^\\d+\\.(\\.\\.)?"), "")
            if (t.isEmpty()) continue
            out.add(t)
        }
        return out
    }

    fun todayHeaderDate(): String {
        val c = java.util.Calendar.getInstance()
        return String.format(Locale.ROOT, "%04d.%02d.%02d", c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH))
    }
}
