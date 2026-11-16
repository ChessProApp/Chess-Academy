package com.chessacademy.ai.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.chessacademy.ai.data.Store
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SoundType { MOVE, CAPTURE, CHECK, WIN, CLICK, HINT, ERROR }

/**
 * Klang und Haptik. Die Klänge sind dieselben synthetischen Töne wie zuvor, werden aber nur
 * einmal berechnet (Cache) und auf einem eigenen Scope abgespielt – kein Rechnen pro Zug, kein Ruckeln.
 */
object Feedback {
    private const val RATE = 22050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cache = HashMap<SoundType, ShortArray>()
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        scope.launch { SoundType.entries.forEach { samples(it) } } // vorab erzeugen
    }

    @Synchronized
    private fun samples(type: SoundType): ShortArray = cache.getOrPut(type) {
        val ms = when (type) {
            SoundType.CLICK -> 30; SoundType.MOVE -> 75; SoundType.CAPTURE -> 130
            SoundType.CHECK -> 210; SoundType.HINT -> 160; SoundType.WIN -> 420; SoundType.ERROR -> 140
        }
        val n = RATE * ms / 1000
        ShortArray(n) { i ->
            val t = i.toDouble() / RATE
            val decay = when (type) {
                SoundType.CLICK -> 85.0; SoundType.MOVE -> 42.0; SoundType.CAPTURE -> 28.0
                SoundType.CHECK -> 15.0; SoundType.HINT -> 18.0; SoundType.WIN -> 7.5; SoundType.ERROR -> 22.0
            }
            val wave = when (type) {
                SoundType.CLICK -> sin(2.0 * PI * 940.0 * t)
                SoundType.MOVE -> 0.72 * sin(2.0 * PI * 230.0 * t) + 0.28 * sin(2.0 * PI * 460.0 * t)
                SoundType.CAPTURE -> 0.65 * sin(2.0 * PI * 155.0 * t) + 0.35 * sin(2.0 * PI * 640.0 * t)
                SoundType.CHECK -> sin(2.0 * PI * (if (t < 0.09) 587.33 else 880.0) * t)
                SoundType.HINT -> sin(2.0 * PI * 659.25 * t) + 0.4 * sin(2.0 * PI * 987.77 * t)
                SoundType.WIN -> sin(2.0 * PI * (when { t < 0.13 -> 523.25; t < 0.26 -> 659.25; else -> 783.99 }) * t)
                SoundType.ERROR -> sin(2.0 * PI * (if (t < 0.06) 220.0 else 165.0) * t)
            }
            (wave * exp(-t * decay) * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun play(type: SoundType) {
        scope.launch {
            try {
                val s = samples(type)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                    .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                    .setBufferSizeInBytes(s.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()
                track.write(s, 0, s.size)
                track.play()
                delay(s.size * 1000L / RATE + 60L)
                track.release()
            } catch (_: Exception) { }
        }
    }

    private fun vibrate(ms: Long) {
        val ctx = appContext ?: return
        try {
            val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            else @Suppress("DEPRECATION") (ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            v?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) { }
    }

    /** Ton und Vibration gemäß den Einstellungen. */
    fun fire(type: SoundType, vibMs: Long = 18L) {
        val s = Store.settings
        if (s.sound) play(type)
        if (s.vibration && vibMs > 0) vibrate(vibMs)
    }

    /** Für die Einstellungs-Schalter: Vorschau unabhängig vom aktuellen Wert. */
    fun preview(sound: Boolean, vibration: Boolean) {
        if (sound) play(SoundType.HINT)
        if (vibration) vibrate(40L)
    }
}
