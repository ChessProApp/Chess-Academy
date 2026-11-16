package com.chessacademy.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.ui.screens.DesignScreen
import com.chessacademy.ai.ui.screens.LabScreen
import com.chessacademy.ai.ui.screens.LearnScreen
import com.chessacademy.ai.ui.screens.PlayScreen
import com.chessacademy.ai.ui.theme.ChessAcademyTheme
import com.chessacademy.ai.ui.theme.UiAccentTheme
import com.chessacademy.ai.vm.GameViewModel

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Store.init(this)
        Feedback.init(this)
        requestHighRefreshRate()
        enableEdgeToEdge()
        setContent {
            val accent = UiAccentTheme.entries[Store.settings.accent]
            ChessAcademyTheme(accent) {
                var tab by rememberSaveable { mutableIntStateOf(0) }
                Scaffold(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface, tonalElevation = androidx.compose.ui.unit.Dp.Hairline) {
                            val items = listOf(
                                "Spielen" to Icons.Filled.SportsEsports, "Lernen" to Icons.Filled.School,
                                "Labor" to Icons.Filled.Science, "Design" to Icons.Filled.Palette
                            )
                            items.forEachIndexed { i, (label, icon) ->
                                NavigationBarItem(selected = tab == i, onClick = { if (tab != i) { Feedback.fire(SoundType.CLICK, 10L); tab = i } },
                                    icon = { Icon(icon, label) }, label = { Text(label) })
                            }
                        }
                    }
                ) { pad ->
                    AnimatedContent(
                        targetState = tab, modifier = Modifier.padding(pad), label = "tabs",
                        transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) }
                    ) { t ->
                        when (t) {
                            0 -> PlayScreen(vm)
                            1 -> LearnScreen(vm.runner)
                            2 -> LabScreen(vm, onPlayVsEngine = { tab = 0 })
                            else -> DesignScreen()
                        }
                    }
                }
            }
        }
    }

    /** Bittet das System um die höchste Bildwiederholrate bei gleicher Auflösung (Hinweis, kein Zwang; das System darf adaptiv bleiben). */
    private fun requestHighRefreshRate() {
        try {
            @Suppress("DEPRECATION") val display = windowManager.defaultDisplay
            val cur = display.mode
            val best = display.supportedModes
                .filter { it.physicalWidth == cur.physicalWidth && it.physicalHeight == cur.physicalHeight }
                .maxByOrNull { it.refreshRate }
            if (best != null && best.modeId != cur.modeId) {
                window.attributes = window.attributes.also { it.preferredDisplayModeId = best.modeId }
            }
        } catch (_: Exception) { }
    }
}
