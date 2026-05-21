package com.pizzalab.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pizzalab.MainActivity
import com.pizzalab.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps pizza timers running even when the app is in the background.
 * Shows an ongoing notification with the countdown and fires an alert when a timer completes.
 */
class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "pizza_timers"
        private const val ALERT_CHANNEL_ID = "pizza_timer_alerts"
        private const val ONGOING_NOTIFICATION_ID = 1
        private const val ALERT_NOTIFICATION_BASE_ID = 1000

        const val ACTION_START = "com.pizzalab.service.START"
        const val ACTION_STOP = "com.pizzalab.service.STOP"
        const val EXTRA_TIMER_LABEL = "timer_label"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_TIMER_ID = "timer_id"

        fun startTimer(context: Context, timerId: String, label: String, durationSeconds: Int) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TIMER_ID, timerId)
                putExtra(EXTRA_TIMER_LABEL, label)
                putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }

        /**
         * Post an alert notification directly without starting the foreground service.
         * Used for timer completion alerts where we only need a notification, not a countdown.
         */
        fun fireAlert(context: Context, alertId: String, title: String, message: String) {
            // Ensure notification channel exists
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (nm.getNotificationChannel(ALERT_CHANNEL_ID) == null) {
                    nm.deleteNotificationChannel(ALERT_CHANNEL_ID)
                    val alertChannel = NotificationChannel(
                        ALERT_CHANNEL_ID,
                        "Avvisi Cottura",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "Avviso quando la pizza e' pronta"
                        enableVibration(true)
                        vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                        setSound(
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build()
                        )
                    }
                    nm.createNotificationChannel(alertChannel)
                }
            }

            val notifId = ALERT_NOTIFICATION_BASE_ID + alertId.hashCode()
            val contentIntent = PendingIntent.getActivity(
                context,
                notifId,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_pizza)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(notifId, notification)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val timerJobs = mutableMapOf<String, Job>()
    private val activeTimers = mutableMapOf<String, TimerState>()

    private data class TimerState(
        val id: String,
        val label: String,
        var remainingSeconds: Int,
        val totalSeconds: Int
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val timerId = intent.getStringExtra(EXTRA_TIMER_ID) ?: return START_NOT_STICKY
                val label = intent.getStringExtra(EXTRA_TIMER_LABEL) ?: "Pizza"
                val duration = intent.getIntExtra(EXTRA_DURATION_SECONDS, 0)
                if (duration <= 0) return START_NOT_STICKY

                val state = TimerState(
                    id = timerId,
                    label = label,
                    remainingSeconds = duration,
                    totalSeconds = duration
                )
                activeTimers[timerId] = state
                startForeground(ONGOING_NOTIFICATION_ID, buildOngoingNotification())
                startCountdown(timerId)
            }
            ACTION_STOP -> {
                stopAllTimers()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopAllTimers()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Timer logic ────────────────────────────────────────────────────

    private fun startCountdown(timerId: String) {
        timerJobs[timerId]?.cancel()
        timerJobs[timerId] = serviceScope.launch {
            while (true) {
                delay(1_000L)
                val state = activeTimers[timerId] ?: break
                state.remainingSeconds -= 1

                if (state.remainingSeconds <= 0) {
                    onTimerComplete(state)
                    activeTimers.remove(timerId)
                    timerJobs.remove(timerId)

                    if (activeTimers.isEmpty()) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    } else {
                        updateOngoingNotification()
                    }
                    break
                } else {
                    updateOngoingNotification()
                }
            }
        }
    }

    private fun onTimerComplete(state: TimerState) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alertId = ALERT_NOTIFICATION_BASE_ID + state.id.hashCode()

        val contentIntent = PendingIntent.getActivity(
            this,
            alertId,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_pizza)
            .setContentTitle("Pizza pronta!")
            .setContentText("${state.label} - La cottura e' completata")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .build()

        nm.notify(alertId, notification)
    }

    private fun stopAllTimers() {
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        activeTimers.clear()
    }

    // ── Notifications ──────────────────────────────────────────────────

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Ongoing low-priority channel for the foreground timer
            val ongoingChannel = NotificationChannel(
                CHANNEL_ID,
                "Timer Pizza",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Timer di cottura pizza in corso"
            }
            nm.createNotificationChannel(ongoingChannel)

            // Delete existing alert channel to force sound settings update
            // (Android caches channel settings; programmatic changes are ignored once created)
            nm.deleteNotificationChannel(ALERT_CHANNEL_ID)

            // High-priority channel for completion alerts
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Avvisi Cottura",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avviso quando la pizza e' pronta"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            nm.createNotificationChannel(alertChannel)
        }
    }

    private fun buildOngoingNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (activeTimers.size == 1) {
            val t = activeTimers.values.first()
            "${t.label}: ${formatSeconds(t.remainingSeconds)}"
        } else {
            "${activeTimers.size} timer attivi"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_pizza)
            .setContentTitle("PizzaLab - Cottura")
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateOngoingNotification() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(ONGOING_NOTIFICATION_ID, buildOngoingNotification())
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "%d:%02d".format(m, s)
    }
}
