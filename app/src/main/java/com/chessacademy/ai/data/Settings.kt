package com.chessacademy.ai.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.chessacademy.ai.engine.AiLevel
import com.chessacademy.ai.engine.AiStyle

/** Alle Einstellungen. Farben als ARGB-Int; presetIndex -1 = eigene Farben (Regler). */
data class Settings(
    val accent: Int = 0,
    val presetIndex: Int = 0,
    val lightColor: Int = 0xFFEEEED2.toInt(),
    val darkColor: Int = 0xFF769656.toInt(),
    val borderColor: Int = 0xFF4B6335.toInt(),
    val lightHue: Float = 0.15f,
    val darkHue: Float = 0.27f,
    val showBorder: Boolean = true,
    val showCoords: Boolean = true,
    val ringHints: Boolean = false,
    val serifPieces: Boolean = false,
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val playerWhite: Boolean = true,
    val aiLevel: Int = AiLevel.NORMAL.ordinal,
    val aiStyle: Int = AiStyle.BALANCED.ordinal,
    val animations: Boolean = true,
    val showLegalMoves: Boolean = true,
    val highlightLast: Boolean = true,
    val dragEnabled: Boolean = true,
    val autoQueen: Boolean = false
)

/** Einstellungen, Spielstand und Lernfortschritt – einfache SharedPreferences, keine Datenbank nötig. */
object Store {
    private lateinit var prefs: SharedPreferences
    var settings by mutableStateOf(Settings())
        private set
    var completedLevels by mutableStateOf(setOf<Int>())
        private set
    var completedRules by mutableStateOf(setOf<String>())
        private set
    /** Regel-Lektion -> beste Quiz-Punktzahl in Prozent. */
    var ruleScores by mutableStateOf(mapOf<String, Int>())
        private set
    /** Schwierige Inhalte, die wiederholt werden sollen ("regelId#übungsnummer" / "regelId#q#nummer"). */
    var difficult by mutableStateOf(setOf<String>())
        private set
    var solvedTactics by mutableStateOf(setOf<String>())
        private set
    var solvedEndgames by mutableStateOf(setOf<String>())
        private set
    var solvedDaily by mutableStateOf(setOf<Long>())
        private set

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("chess_academy", Context.MODE_PRIVATE)
        val d = Settings()
        settings = Settings(
            accent = prefs.getInt("accent", d.accent).coerceIn(0, 3),
            presetIndex = prefs.getInt("preset", d.presetIndex),
            lightColor = prefs.getInt("light", d.lightColor),
            darkColor = prefs.getInt("dark", d.darkColor),
            borderColor = prefs.getInt("border", d.borderColor),
            lightHue = prefs.getFloat("lightHue", d.lightHue),
            darkHue = prefs.getFloat("darkHue", d.darkHue),
            showBorder = prefs.getBoolean("showBorder", d.showBorder),
            showCoords = prefs.getBoolean("showCoords", d.showCoords),
            ringHints = prefs.getBoolean("ringHints", d.ringHints),
            serifPieces = prefs.getBoolean("serif", d.serifPieces),
            sound = prefs.getBoolean("sound", d.sound),
            vibration = prefs.getBoolean("vibration", d.vibration),
            playerWhite = prefs.getBoolean("playerWhite", d.playerWhite),
            aiLevel = prefs.getInt("aiLevel", d.aiLevel).coerceIn(0, AiLevel.entries.lastIndex),
            aiStyle = prefs.getInt("aiStyle", d.aiStyle).coerceIn(0, AiStyle.entries.lastIndex),
            animations = prefs.getBoolean("animations", d.animations),
            showLegalMoves = prefs.getBoolean("showLegal", d.showLegalMoves),
            highlightLast = prefs.getBoolean("highlightLast", d.highlightLast),
            dragEnabled = prefs.getBoolean("drag", d.dragEnabled),
            autoQueen = prefs.getBoolean("autoQueen", d.autoQueen)
        )
        completedLevels = prefs.getStringSet("levels", emptySet())!!.mapNotNull { it.toIntOrNull() }.toSet()
        completedRules = prefs.getStringSet("rules", emptySet())!!.toSet()
        ruleScores = prefs.getStringSet("ruleScores", emptySet())!!.mapNotNull {
            val p = it.split("=")
            if (p.size == 2) p[1].toIntOrNull()?.let { v -> p[0] to v } else null
        }.toMap()
        difficult = prefs.getStringSet("difficult", emptySet())!!.toSet()
        solvedTactics = prefs.getStringSet("tactics", emptySet())!!.toSet()
        solvedEndgames = prefs.getStringSet("endgames", emptySet())!!.toSet()
        solvedDaily = prefs.getStringSet("daily", emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()
    }

    fun update(change: Settings.() -> Settings) {
        val s = settings.change()
        settings = s
        prefs.edit()
            .putInt("accent", s.accent).putInt("preset", s.presetIndex)
            .putInt("light", s.lightColor).putInt("dark", s.darkColor).putInt("border", s.borderColor)
            .putFloat("lightHue", s.lightHue).putFloat("darkHue", s.darkHue)
            .putBoolean("showBorder", s.showBorder).putBoolean("showCoords", s.showCoords)
            .putBoolean("ringHints", s.ringHints).putBoolean("serif", s.serifPieces)
            .putBoolean("sound", s.sound).putBoolean("vibration", s.vibration)
            .putBoolean("playerWhite", s.playerWhite).putInt("aiLevel", s.aiLevel).putInt("aiStyle", s.aiStyle)
            .putBoolean("animations", s.animations).putBoolean("showLegal", s.showLegalMoves)
            .putBoolean("highlightLast", s.highlightLast).putBoolean("drag", s.dragEnabled)
            .putBoolean("autoQueen", s.autoQueen)
            .apply()
    }

    fun completeLevel(level: Int) {
        completedLevels = completedLevels + level
        prefs.edit().putStringSet("levels", completedLevels.map { it.toString() }.toSet()).apply()
    }

    fun completeRule(id: String, scorePercent: Int) {
        completedRules = completedRules + id
        ruleScores = ruleScores + (id to maxOf(scorePercent, ruleScores[id] ?: 0))
        prefs.edit().putStringSet("rules", completedRules)
            .putStringSet("ruleScores", ruleScores.map { "${it.key}=${it.value}" }.toSet()).apply()
    }

    fun markDifficult(key: String, hard: Boolean) {
        difficult = if (hard) difficult + key else difficult - key
        prefs.edit().putStringSet("difficult", difficult).apply()
    }

    fun solveTactic(id: String) {
        solvedTactics = solvedTactics + id
        prefs.edit().putStringSet("tactics", solvedTactics).apply()
    }

    fun solveEndgame(id: String) {
        solvedEndgames = solvedEndgames + id
        prefs.edit().putStringSet("endgames", solvedEndgames).apply()
    }

    fun solveDaily(epochDay: Long) {
        solvedDaily = solvedDaily + epochDay
        prefs.edit().putStringSet("daily", solvedDaily.map { it.toString() }.toSet()).apply()
    }

    // ---- Spielstand (PGN + Spieleinstellungen) -------------------------------------------------
    fun saveGame(pgn: String?) {
        prefs.edit().apply { if (pgn == null) remove("savedGame") else putString("savedGame", pgn) }.apply()
    }
    fun loadGame(): String? = prefs.getString("savedGame", null)
    fun saveGameSide(playerWhite: Boolean) { prefs.edit().putBoolean("savedGameWhite", playerWhite).apply() }
    fun loadGameSide(): Boolean = prefs.getBoolean("savedGameWhite", true)
}
