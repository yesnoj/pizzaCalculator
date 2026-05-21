package com.pizzalab.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import com.pizzalab.R

/**
 * Timer sound settings and built-in alarm sounds.
 */
object TimerPreferences {

    private const val PREFS_NAME = "pizzalab_timer_prefs"
    private const val KEY_ALARM_KEY = "alarm_sound_key"

    /**
     * Available built-in alarm sounds.
     */
    data class AlarmSound(
        val key: String,
        val displayName: String,
        val rawResId: Int
    )

    val alarmSounds = listOf(
        AlarmSound("bell",  "Campanella",    R.raw.alarm_bell),
        AlarmSound("beep",  "Bip elettronico", R.raw.alarm_beep),
        AlarmSound("ding",  "Ding forno",    R.raw.alarm_ding),
        AlarmSound("trill", "Sveglia",       R.raw.alarm_trill),
        AlarmSound("chime", "Carillon",      R.raw.alarm_chime),
    )

    private val defaultKey = "bell"

    /**
     * Save the selected alarm sound key.
     */
    fun saveAlarmKey(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ALARM_KEY, key)
            .apply()
    }

    /**
     * Get the saved alarm sound key.
     */
    fun getAlarmKey(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ALARM_KEY, defaultKey) ?: defaultKey
    }

    /**
     * Get the AlarmSound object for the saved key.
     */
    fun getSelectedSound(context: Context): AlarmSound {
        val key = getAlarmKey(context)
        return alarmSounds.find { it.key == key } ?: alarmSounds.first()
    }

    /**
     * Get the display name of the saved alarm sound.
     */
    fun getAlarmTitle(context: Context): String {
        return getSelectedSound(context).displayName
    }

    /**
     * Get the resource URI for the saved alarm sound (for notifications).
     */
    fun getAlarmUri(context: Context): Uri {
        val sound = getSelectedSound(context)
        return Uri.parse("android.resource://${context.packageName}/${sound.rawResId}")
    }

    /**
     * Play a preview of the given alarm sound. Returns the MediaPlayer for stop control.
     */
    fun playPreview(context: Context, key: String): MediaPlayer? {
        val sound = alarmSounds.find { it.key == key } ?: return null
        return try {
            MediaPlayer.create(context, sound.rawResId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Play the saved alarm sound. Returns the MediaPlayer for stop control.
     */
    fun playAlarm(context: Context): MediaPlayer? {
        val sound = getSelectedSound(context)
        return try {
            MediaPlayer.create(context, sound.rawResId)?.apply {
                start()
            }
        } catch (_: Exception) {
            null
        }
    }
}
