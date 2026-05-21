package com.pizzalab.ui.cooking

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.pizzalab.data.TimerPreferences
import com.pizzalab.data.model.CookingPreset
import com.pizzalab.data.model.CookingTimer
import com.pizzalab.service.TimerService
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QChipRow
import com.pizzalab.ui.components.QDialog
import com.pizzalab.ui.components.QHeader
import com.pizzalab.ui.components.QPrimaryButton
import com.pizzalab.ui.theme.QuadernoColors
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ════════════════════════════════════════════════════════════════
// COOKING SCREEN
// ════════════════════════════════════════════════════════════════

@Composable
fun CookingScreen(
    viewModel: CookingViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timers by viewModel.timers.collectAsState()
    val pizzeCount by viewModel.pizzeCount.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()
    val presets = CookingPreset.defaultPresets

    var alarmTitle by remember { mutableStateOf(TimerPreferences.getAlarmTitle(context)) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Sound + vibration + notification on timer completion
    LaunchedEffect(Unit) {
        viewModel.timerCompletedEvent.collect { completedTimer ->
            try { TimerPreferences.playAlarm(context) } catch (_: Exception) {}
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vm.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                }
            } catch (_: Exception) {}
            TimerService.fireAlert(context = context, alertId = completedTimer.id, title = "Pizza pronta!", message = "${completedTimer.preset.name} - Cottura completata")
        }
    }

    // Foreground service
    val runningTimerIds = remember(timers) { timers.filter { it.isRunning }.map { it.id }.toSet() }
    LaunchedEffect(runningTimerIds) {
        if (runningTimerIds.isNotEmpty()) {
            val first = timers.first { it.id in runningTimerIds }
            TimerService.startTimer(context = context, timerId = first.id, label = first.preset.name, durationSeconds = first.remainingSeconds)
        } else {
            TimerService.stop(context)
        }
    }

    // Sound picker dialog (uses QDialog which has its own layout)
    if (showSoundPicker) {
        Dialog(onDismissRequest = { showSoundPicker = false }) {
            SoundPickerDialog(
                currentKey = TimerPreferences.getAlarmKey(context),
                onSelect = { key ->
                    TimerPreferences.saveAlarmKey(context, key)
                    alarmTitle = TimerPreferences.getAlarmTitle(context)
                    showSoundPicker = false
                },
                onDismiss = { showSoundPicker = false },
            )
        }
    }

    // Custom timer dialog
    if (showCustomDialog) {
        CustomTimerDialog(
            onConfirm = { totalSeconds ->
                viewModel.addCustomTimer("Personalizzato", totalSeconds)
                showCustomDialog = false
            },
            onDismiss = { showCustomDialog = false },
        )
    }

    // Show running timer on the main dial, or a just-completed timer (Pronta!).
    // Paused/idle timers do NOT override the dial — the user's selected preset shows instead.
    // The single active timer: running, paused (remaining > 0), or just completed (remaining <= 0)
    val primaryTimer = timers.firstOrNull()
    val isComplete = primaryTimer != null && primaryTimer.remainingSeconds <= 0

    LazyColumn(
        modifier = modifier.fillMaxSize().background(QuadernoColors.Bg),
    ) {
        // ── Header ──
        item {
            QHeader(
                kicker = "Cottura",
                title = "Forno",
                italic = if (isComplete) "pizza pronta" else "al lavoro",
                tight = true,
            )
        }

        // ── Hint text from preset ──
        item {
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 0.dp)) {
                if (selectedPreset.description.isNotBlank()) {
                    Text(
                        text = selectedPreset.description,
                        style = TextStyle(fontSize = 12.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // ── Preset chips ──
        item {
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                QChipRow(
                    items = presets.map { it.name },
                    selected = selectedPreset.name,
                    onSelect = { name ->
                        val preset = presets.find { it.name == name }
                        if (preset != null) {
                            viewModel.selectPreset(preset)
                        }
                    },
                    wrap = true,
                )
                Spacer(modifier = Modifier.height(6.dp))
                // "+ personalizzato" chip — opens dialog
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .border(1.dp, QuadernoColors.Ink2, RoundedCornerShape(3.dp))
                        .clickable { showCustomDialog = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = QuadernoColors.Ink2, modifier = Modifier.size(10.dp))
                        Text(
                            text = "Personalizzato",
                            style = TextStyle(
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                color = QuadernoColors.Ink2,
                            ),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                DashedDivider()
            }
        }

        // Custom timer is now a dialog — see CustomTimerDialog

        // ── Clock Dial ──
        item {
            val remaining = primaryTimer?.remainingSeconds ?: selectedPreset.durationSeconds
            val total = primaryTimer?.preset?.durationSeconds ?: selectedPreset.durationSeconds
            val isRunning = primaryTimer?.isRunning == true
            val timerIsComplete = primaryTimer != null && primaryTimer.remainingSeconds <= 0
            val presetName = primaryTimer?.preset?.name ?: selectedPreset.name

            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                QClockDial(
                    presetName = presetName,
                    remainingSeconds = remaining,
                    totalSeconds = total,
                    isRunning = isRunning,
                    isComplete = timerIsComplete,
                )
            }
        }

        // ── Control buttons (below dial) ──
        item {
            val hasActiveTimer = primaryTimer != null && primaryTimer.remainingSeconds > 0
            val timerIsComplete = primaryTimer != null && primaryTimer.remainingSeconds <= 0

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                if (timerIsComplete) {
                    // "Sfornata!" button
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(QuadernoColors.Olive)
                            .clickable {
                                primaryTimer?.let { viewModel.removeTimer(it.id) }
                            }
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "SFORNATA!",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper, letterSpacing = 0.06.sp),
                        )
                    }
                } else if (hasActiveTimer) {
                    // Play/Pause (bigger, primary action)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (primaryTimer?.isRunning == true) QuadernoColors.Primary else QuadernoColors.Paper)
                            .border(1.dp, if (primaryTimer?.isRunning == true) QuadernoColors.Primary else QuadernoColors.Ink2, CircleShape)
                            .clickable {
                                primaryTimer?.let {
                                    if (it.isRunning) viewModel.pauseTimer(it.id) else viewModel.startTimer(it.id)
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (primaryTimer?.isRunning == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (primaryTimer?.isRunning == true) "Pausa" else "Avvia",
                            tint = if (primaryTimer?.isRunning == true) QuadernoColors.Paper else QuadernoColors.Ink,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    // Reset (secondary)
                    QCtrlButton(onClick = { primaryTimer?.let { viewModel.resetTimer(it.id) } }) {
                        Icon(Icons.Filled.Replay, contentDescription = "Reset", tint = QuadernoColors.Ink, modifier = Modifier.size(18.dp))
                    }
                } else {
                    // Idle: just the add button
                    QPrimaryButton(
                        text = "Inforna Pizza",
                        onClick = { viewModel.addTimer(selectedPreset) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // ── Sound picker + pizza counter (Paper footer section) ──
        item {
            Spacer(modifier = Modifier.height(14.dp))
            DashedDivider()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuadernoColors.Paper)
                    .padding(horizontal = 22.dp, vertical = 12.dp),
            ) {
                // Sound row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSoundPicker = true }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = QuadernoColors.Ink2, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "suono: ",
                        style = TextStyle(fontSize = 12.5.sp, color = QuadernoColors.Ink),
                    )
                    Text(
                        text = alarmTitle,
                        style = TextStyle(fontSize = 12.5.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold, color = QuadernoColors.Primary),
                    )
                }

                // Separator
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(QuadernoColors.RuleDots).padding(vertical = 6.dp))

                // Pizza counter
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "pizze sfornate · oggi",
                            style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                        )
                        Text(
                            text = "$pizzeCount",
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink, letterSpacing = (-0.02).sp),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        // Simple tally bars (Q_Tally = width:3, height:24, primary)
                        repeat(pizzeCount.coerceAtMost(20)) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .background(QuadernoColors.Primary, RoundedCornerShape(1.dp)),
                            )
                        }
                    }
                }
                if (pizzeCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clickable { viewModel.resetPizzeCount() }
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = "azzera contatore",
                            style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_CTRL_BUTTON — circular control button (44dp, Paper bg, Ink2 border)
// ════════════════════════════════════════════════════════════════

@Composable
private fun QCtrlButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(QuadernoColors.Paper)
            .border(1.dp, QuadernoColors.Ink2, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

// ════════════════════════════════════════════════════════════════
// Q_CLOCK_DIAL — exact match to design spec (280dp)
// ════════════════════════════════════════════════════════════════

@Composable
private fun QClockDial(
    presetName: String,
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    isComplete: Boolean,
) {
    val pct = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 0f
    val animatedPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(durationMillis = 400),
        label = "dialArc",
    )
    val handAngle = (1f - animatedPct) * 360f

    val timerText = formatTimerSeconds(remainingSeconds)
    val totalText = formatTimerSeconds(totalSeconds)

    // Colors hoisted for Canvas
    val inkColor = QuadernoColors.Ink
    val ink3Color = QuadernoColors.Ink3
    val paperColor = QuadernoColors.Paper
    val primaryColor = QuadernoColors.Primary

    // Complete state: pulsing alpha
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulseAlpha",
    )

    Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            val center = Offset(cx, cy)

            if (isComplete) {
                // ── COMPLETE STATE ──
                // Filled primary circle
                drawCircle(color = primaryColor, radius = 132.dp.toPx(), center = center)
                drawCircle(color = inkColor, radius = 132.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))

                // Tick marks in Paper color
                for (i in 0 until 60) {
                    val a = (i / 60.0) * 2 * PI - PI / 2
                    val r1 = 122.dp.toPx()
                    val r2 = if (i % 5 == 0) 108.dp.toPx() else 116.dp.toPx()
                    drawLine(
                        color = paperColor.copy(alpha = if (i % 5 == 0) 0.9f else 0.5f),
                        start = Offset(cx + (r1 * cos(a)).toFloat(), cy + (r1 * sin(a)).toFloat()),
                        end = Offset(cx + (r2 * cos(a)).toFloat(), cy + (r2 * sin(a)).toFloat()),
                        strokeWidth = if (i % 5 == 0) 1.5.dp.toPx() else 1.dp.toPx(),
                    )
                }
            } else {
                // ── RUNNING / IDLE STATE ──
                // Outer circle: fill Paper, stroke Ink
                drawCircle(color = paperColor, radius = 132.dp.toPx(), center = center)
                drawCircle(color = inkColor, radius = 132.dp.toPx(), center = center, style = Stroke(1.5.dp.toPx()))

                // 60 tick marks
                for (i in 0 until 60) {
                    val a = (i / 60.0) * 2 * PI - PI / 2
                    val r1 = 122.dp.toPx()
                    val r2 = if (i % 5 == 0) 108.dp.toPx() else 116.dp.toPx()
                    drawLine(
                        color = if (i % 5 == 0) inkColor else ink3Color,
                        start = Offset(cx + (r1 * cos(a)).toFloat(), cy + (r1 * sin(a)).toFloat()),
                        end = Offset(cx + (r2 * cos(a)).toFloat(), cy + (r2 * sin(a)).toFloat()),
                        strokeWidth = if (i % 5 == 0) 1.5.dp.toPx() else 1.dp.toPx(),
                    )
                }

                // Progress arc — on the r=122 circle, strokeWidth=3, opacity=0.6
                val arcR = 122.dp.toPx()
                val circumference = 2 * PI.toFloat() * arcR
                drawArc(
                    color = primaryColor.copy(alpha = 0.6f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedPct,
                    useCenter = false,
                    topLeft = Offset(cx - arcR, cy - arcR),
                    size = Size(arcR * 2, arcR * 2),
                    style = Stroke(width = 3.dp.toPx()),
                )

                // Hub — r=6, Ink
                drawCircle(color = inkColor, radius = 6.dp.toPx(), center = center)

                // Hand — from center to outer edge, length = 108dp equiv
                val handAngleRad = (handAngle - 90f) * PI.toFloat() / 180f
                val handLength = 108.dp.toPx()
                val handEndX = cx + handLength * cos(handAngleRad)
                val handEndY = cy + handLength * sin(handAngleRad)
                val handEnd = Offset(handEndX, handEndY)

                drawLine(
                    color = primaryColor,
                    start = center,
                    end = handEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                // Tip circle r=4
                drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = handEnd)
            }
        }

        // ── Text overlay ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = if (isComplete) 0.dp else 80.dp),
        ) {
            if (isComplete) {
                // Complete text: preset name, "Pronta!", "0:00 alla cottura"
                Text(
                    text = presetName.uppercase(),
                    style = TextStyle(
                        fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.22.sp, color = QuadernoColors.Paper.copy(alpha = 0.85f),
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pronta!",
                    style = TextStyle(
                        fontSize = 56.sp, fontWeight = FontWeight.Black,
                        color = QuadernoColors.Paper, letterSpacing = (-0.04).sp,
                    ),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "0:00 alla cottura",
                    style = TextStyle(
                        fontSize = 13.sp, fontStyle = FontStyle.Italic,
                        color = QuadernoColors.Paper.copy(alpha = 0.85f),
                    ),
                )
            } else {
                // Running/idle text: preset name (kicker), time, "su X:XX totali"
                Text(
                    text = presetName.uppercase(),
                    style = TextStyle(
                        fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.22.sp, color = QuadernoColors.Primary,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timerText,
                    style = TextStyle(
                        fontSize = 38.sp, fontWeight = FontWeight.ExtraBold,
                        color = QuadernoColors.Ink, letterSpacing = (-0.04).sp,
                    ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "su $totalText totali",
                    style = TextStyle(
                        fontSize = 11.sp, fontStyle = FontStyle.Italic,
                        color = QuadernoColors.Ink3,
                    ),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// SOUND PICKER DIALOG
// ════════════════════════════════════════════════════════════════

@Composable
private fun SoundPickerDialog(
    currentKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var selectedKey by remember { mutableStateOf(currentKey) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.let { try { it.stop() } catch (_: Exception) {}; try { it.release() } catch (_: Exception) {} }
        }
    }

    QDialog(
        title = "Suono allarme",
        kicker = "Cottura",
        onConfirm = { onSelect(selectedKey) },
        onDismiss = onDismiss,
        confirmLabel = "Conferma",
    ) {
        Column {
            Text(
                text = "Tocca per provarlo · scegli quello che ti piace.",
                style = TextStyle(fontSize = 12.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TimerPreferences.alarmSounds.forEach { sound ->
                val on = selectedKey == sound.key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedKey = sound.key }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Custom radio circle
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.5.dp, if (on) QuadernoColors.Primary else QuadernoColors.Ink2, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) {
                            Box(modifier = Modifier.size(8.dp).background(QuadernoColors.Primary, CircleShape))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = sound.displayName,
                        style = TextStyle(fontSize = 14.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = QuadernoColors.Ink),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable {
                                previewPlayer?.let { try { it.stop() } catch (_: Exception) {}; try { it.release() } catch (_: Exception) {} }
                                previewPlayer = TimerPreferences.playPreview(context, sound.key)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.VolumeUp, contentDescription = "Ascolta", tint = QuadernoColors.Primary, modifier = Modifier.size(18.dp))
                    }
                }
                // Dotted separator
                Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                    val dotR = 0.5.dp.toPx(); val gap = 4.dp.toPx()
                    var dx = 0f
                    while (dx < size.width) { drawCircle(QuadernoColors.RuleDots, dotR, Offset(dx, size.height / 2)); dx += gap }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// CUSTOM TIMER DIALOG
// ════════════════════════════════════════════════════════════════

private val quickPresets = listOf(
    "1:00" to 60,
    "1:30" to 90,
    "2:00" to 120,
    "3:00" to 180,
    "4:00" to 240,
    "5:00" to 300,
    "8:00" to 480,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomTimerDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var minutes by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    val totalSeconds = minutes * 60 + seconds

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(QuadernoColors.Paper, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(22.dp)
                .navigationBarsPadding(),
        ) {
            // Title
            Text(
                text = "Timer personalizzato",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink),
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle
            Text(
                text = "quanto deve durare?",
                style = TextStyle(fontSize = 12.5.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // MINUTI & SECONDI side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                // MINUTI
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MINUTI",
                        style = TextStyle(
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    BasicTextField(
                        value = if (minutes == 0) "" else minutes.toString(),
                        onValueChange = { v ->
                            minutes = v.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (minutes == 0) {
                                        Text("0", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink3))
                                    }
                                    innerTextField()
                                }
                                Text("min", style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3))
                            }
                        },
                    )
                }

                // Separator
                Text(
                    text = ":",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink3),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 22.dp),
                )

                // SECONDI
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SECONDI",
                        style = TextStyle(
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                    BasicTextField(
                        value = if (seconds == 0) "" else seconds.toString(),
                        onValueChange = { v ->
                            val parsed = v.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                            seconds = parsed.coerceAtMost(59)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (seconds == 0) {
                                        Text("0", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink3))
                                    }
                                    innerTextField()
                                }
                                Text("s", style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3))
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preset rapidi
            Text(
                text = "o scegli uno dei preset rapidi:",
                style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                modifier = Modifier.padding(bottom = 8.dp),
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                quickPresets.forEach { (label, secs) ->
                    val isSelected = totalSeconds == secs
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .border(
                                1.dp,
                                if (isSelected) QuadernoColors.Primary else QuadernoColors.Rule,
                                RoundedCornerShape(4.dp),
                            )
                            .background(if (isSelected) QuadernoColors.Primary else Color.Transparent)
                            .clickable {
                                minutes = secs / 60
                                seconds = secs % 60
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) QuadernoColors.Paper else QuadernoColors.Ink2,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Annulla
                Box(
                    modifier = Modifier
                        .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("Annulla", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink))
                }
                Spacer(modifier = Modifier.width(10.dp))
                // Avvia
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (totalSeconds > 0) QuadernoColors.Primary else QuadernoColors.Ink3)
                        .clickable(enabled = totalSeconds > 0) { onConfirm(totalSeconds) }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("Avvia", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper))
                }
            }
        }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// FORMATTING
// ════════════════════════════════════════════════════════════════

private fun formatTimerSeconds(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
