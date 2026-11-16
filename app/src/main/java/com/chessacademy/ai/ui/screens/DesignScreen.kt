package com.chessacademy.ai.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.chessacademy.ai.audio.Feedback
import com.chessacademy.ai.audio.SoundType
import com.chessacademy.ai.chess.Position
import com.chessacademy.ai.data.Store
import com.chessacademy.ai.ui.components.*
import com.chessacademy.ai.ui.theme.BoardPresets
import com.chessacademy.ai.ui.theme.UiAccentTheme

private const val PREVIEW_FEN = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 6 5"

/** Design: alle bisherigen Optionen (Akzent, 8 Brett-Themes, eigene Farben, Rahmen, Koordinaten, Hinweise, Serif, Ton, Vibration) + neue. */
@Composable
fun DesignScreen() {
    val s = Store.settings
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StaticBoard(Position.fromFen(PREVIEW_FEN), s.boardStyle(), modifier = Modifier.fillMaxWidth())

        AppCard {
            SectionTitle("Akzentfarbe")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(UiAccentTheme.entries.toList()) { t ->
                    FilterChip(selected = s.accent == t.ordinal, onClick = { Feedback.fire(SoundType.CLICK, 12L); Store.update { copy(accent = t.ordinal) } },
                        label = { Text(t.label) }, leadingIcon = { Box(Modifier.size(12.dp).clip(RoundedCornerShape(50)).background(t.primary)) })
                }
            }
        }

        AppCard {
            SectionTitle("Brett-Themes")
            for (rowItems in BoardPresets.withIndex().toList().chunked(4)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((i, p) in rowItems) {
                        val sel = s.presetIndex == i
                        Surface(
                            shape = RoundedCornerShape(10.dp), color = p.border,
                            border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.3f)),
                            modifier = Modifier.weight(1f).clickable {
                                Feedback.fire(SoundType.CLICK, 15L)
                                Store.update { copy(presetIndex = i, lightColor = p.light.toArgb(), darkColor = p.dark.toArgb(), borderColor = p.border.toArgb()) }
                            }
                        ) {
                            Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(6.dp))) {
                                    Box(Modifier.weight(1f).fillMaxSize().background(p.light)); Box(Modifier.weight(1f).fillMaxSize().background(p.dark))
                                }
                                Text(p.name, style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1, modifier = Modifier.padding(top = 3.dp))
                            }
                        }
                    }
                }
            }
            SectionTitle("Eigene Farben")
            Text("Helle Felder", style = MaterialTheme.typography.bodySmall)
            Slider(value = s.lightHue, onValueChange = { h -> Store.update { copy(presetIndex = -1, lightHue = h, lightColor = Color.hsv(h * 360f, 0.25f, 0.95f).toArgb()) } })
            Text("Dunkle Felder", style = MaterialTheme.typography.bodySmall)
            Slider(value = s.darkHue, onValueChange = { h -> Store.update { copy(presetIndex = -1, darkHue = h, darkColor = Color.hsv(h * 360f, 0.55f, 0.55f).toArgb(), borderColor = Color.hsv(h * 360f, 0.65f, 0.35f).toArgb()) } })
        }

        AppCard {
            SectionTitle("Brett")
            ToggleRow("Brett-Rahmen anzeigen", s.showBorder, { Store.update { copy(showBorder = it) } })
            ToggleRow("Koordinaten (a–h / 1–8)", s.showCoords, { Store.update { copy(showCoords = it) } })
            ToggleRow("Zug-Ringe statt Punkte", s.ringHints, { Store.update { copy(ringHints = it) } })
            ToggleRow("Mögliche Züge anzeigen", s.showLegalMoves, { Store.update { copy(showLegalMoves = it) } })
            ToggleRow("Letzten Zug hervorheben", s.highlightLast, { Store.update { copy(highlightLast = it) } })
            ToggleRow("Klassische Serif-Typografie", s.serifPieces, { Store.update { copy(serifPieces = it) } })
        }

        AppCard {
            SectionTitle("Bedienung & Bewegung")
            ToggleRow("Animationen", s.animations, { Store.update { copy(animations = it) } }, "Gleiten, Schlag-, Schach- und Matt-Effekte")
            ToggleRow("Figuren ziehen (Drag & Drop)", s.dragEnabled, { Store.update { copy(dragEnabled = it) } }, "Tippen funktioniert immer")
            ToggleRow("Umwandlung automatisch in Dame", s.autoQueen, { Store.update { copy(autoQueen = it) } })
            ToggleRow("Als Weiß spielen", s.playerWhite, { Store.update { copy(playerWhite = it) } }, "Gilt ab der nächsten neuen Partie")
        }

        AppCard {
            SectionTitle("Ton & Haptik")
            ToggleRow("Schach-Sounds", s.sound, { Store.update { copy(sound = it) }; Feedback.preview(it, false) })
            ToggleRow("Vibration (Haptik)", s.vibration, { Store.update { copy(vibration = it) }; Feedback.preview(false, it) })
        }
    }
}
