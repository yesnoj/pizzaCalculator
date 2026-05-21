package com.pizzalab.ui.cooking

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import com.pizzalab.data.TimerPreferences
import com.pizzalab.data.model.CookingPreset
import com.pizzalab.data.model.CookingTimer
import com.pizzalab.service.TimerService

/**
 * Cooking timer screen with preset selection, circular timer, and multi-timer support.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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

    // Stato nome suoneria e dialog picker
    var alarmTitle by remember { mutableStateOf(TimerPreferences.getAlarmTitle(context)) }
    var showSoundPicker by remember { mutableStateOf(false) }

    // Suono + vibrazione + notifica quando un timer scade
    LaunchedEffect(Unit) {
        viewModel.timerCompletedEvent.collect { completedTimer ->
            // 1) Suono allarme scelto dall'utente (integrato nell'app)
            try {
                TimerPreferences.playAlarm(context)
            } catch (_: Exception) {}

            // 2) Vibrazione
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vm.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 500, 200, 500, 200, 500), -1
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                }
            } catch (_: Exception) {}

            // 3) Notifica push diretta (senza avviare il foreground service)
            TimerService.fireAlert(
                context = context,
                alertId = completedTimer.id,
                title = "Pizza pronta!",
                message = "${completedTimer.preset.name} - Cottura completata"
            )
        }
    }

    // Foreground service per tenere l'app viva in background.
    // Key = set di ID dei timer attivi, così si riavvia solo quando cambia.
    val runningTimerIds = remember(timers) {
        timers.filter { it.isRunning }.map { it.id }.toSet()
    }
    LaunchedEffect(runningTimerIds) {
        if (runningTimerIds.isNotEmpty()) {
            val first = timers.first { it.id in runningTimerIds }
            TimerService.startTimer(
                context = context,
                timerId = first.id,
                label = first.preset.name,
                durationSeconds = first.remainingSeconds
            )
        } else {
            // Nessun timer attivo: ferma il foreground service (rimuove icona ongoing)
            TimerService.stop(context)
        }
    }

    // Dialog per scegliere il suono allarme
    if (showSoundPicker) {
        SoundPickerDialog(
            currentKey = TimerPreferences.getAlarmKey(context),
            onSelect = { key ->
                TimerPreferences.saveAlarmKey(context, key)
                alarmTitle = TimerPreferences.getAlarmTitle(context)
                showSoundPicker = false
            },
            onDismiss = { showSoundPicker = false }
        )
    }

    var showCustomInput by remember { mutableStateOf(false) }
    var customMinutes by remember { mutableStateOf("") }
    var customSeconds by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title
        item {
            Text(
                text = "Cottura",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Preset chips
        item {
            Text(
                text = "Tipo di cottura",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = {
                            viewModel.selectPreset(preset)
                            showCustomInput = false
                        },
                        label = {
                            Text("${preset.name} (${formatTimerSeconds(preset.durationSeconds)})")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                FilterChip(
                    selected = showCustomInput,
                    onClick = { showCustomInput = !showCustomInput },
                    label = { Text("Personalizzato") }
                )
            }
        }

        // Custom timer input
        if (showCustomInput) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Timer personalizzato",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customMinutes,
                                onValueChange = { customMinutes = it.filter { c -> c.isDigit() }.take(3) },
                                label = { Text("Minuti") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Text(":", style = MaterialTheme.typography.headlineSmall)
                            OutlinedTextField(
                                value = customSeconds,
                                onValueChange = { customSeconds = it.filter { c -> c.isDigit() }.take(2) },
                                label = { Text("Secondi") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val totalSec = (customMinutes.toIntOrNull() ?: 0) * 60 +
                                        (customSeconds.toIntOrNull() ?: 0)
                                if (totalSec > 0) {
                                    viewModel.addCustomTimer("Personalizzato", totalSec)
                                    customMinutes = ""
                                    customSeconds = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ((customMinutes.toIntOrNull() ?: 0) * 60 +
                                    (customSeconds.toIntOrNull() ?: 0)) > 0
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Avvia Timer")
                        }
                    }
                }
            }
        }

        // Main circular timer (for the primary/first active timer or the selected preset)
        item {
            val primaryTimer = timers.firstOrNull { it.isRunning } ?: timers.firstOrNull()
            CircularTimerDisplay(
                remainingSeconds = primaryTimer?.remainingSeconds ?: selectedPreset.durationSeconds,
                totalSeconds = primaryTimer?.preset?.durationSeconds ?: selectedPreset.durationSeconds,
                isRunning = primaryTimer?.isRunning == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }

        // Add pizza button
        item {
            Button(
                onClick = { viewModel.addTimer(selectedPreset) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aggiungi Pizza")
            }
        }

        // Sound picker
        item {
            OutlinedButton(
                onClick = { showSoundPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.MusicNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Suono: $alarmTitle")
            }
        }

        // Active timers list
        if (timers.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Timer attivi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(timers, key = { it.id }) { timer ->
                TimerCard(
                    timer = timer,
                    onStart = { viewModel.startTimer(timer.id) },
                    onPause = { viewModel.pauseTimer(timer.id) },
                    onReset = { viewModel.resetTimer(timer.id) },
                    onRemove = { viewModel.removeTimer(timer.id) }
                )
            }
        }

        // Pizze counter
        item {
            Spacer(modifier = Modifier.height(8.dp))
            PizzeCounter(
                count = pizzeCount,
                onReset = { viewModel.resetPizzeCount() }
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ── Circular timer ─────────────────────────────────────────────────────

@Composable
private fun CircularTimerDisplay(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val fraction = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds else 1f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 300),
        label = "timerArc"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val timerText = formatTimerSeconds(remainingSeconds)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedFraction,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timerText,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            if (remainingSeconds == 0) {
                Text(
                    text = "Pronta!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Timer card ─────────────────────────────────────────────────────────

@Composable
private fun TimerCard(
    timer: CookingTimer,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onRemove: () -> Unit
) {
    val isComplete = timer.remainingSeconds <= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isComplete)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocalPizza,
                contentDescription = null,
                tint = if (isComplete) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pizza #${timer.pizzaNumber} - ${timer.preset.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isComplete) "Pronta!" else formatTimerSeconds(timer.remainingSeconds),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isComplete) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )
            }

            if (!isComplete) {
                if (timer.isRunning) {
                    IconButton(onClick = onPause) {
                        Icon(Icons.Filled.Pause, contentDescription = "Pausa")
                    }
                } else {
                    IconButton(onClick = onStart) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Avvia")
                    }
                }
                IconButton(onClick = onReset) {
                    Icon(Icons.Filled.Replay, contentDescription = "Reset")
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Rimuovi",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ── Pizze counter ──────────────────────────────────────────────────────

@Composable
private fun PizzeCounter(count: Int, onReset: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.LocalPizza,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Pizze sfornate: $count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            if (count > 0) {
                IconButton(onClick = onReset) {
                    Icon(
                        Icons.Filled.Replay,
                        contentDescription = "Azzera contatore",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

// ── Sound picker dialog ───────────────────────────────────────────────

@Composable
private fun SoundPickerDialog(
    currentKey: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedKey by remember { mutableStateOf(currentKey) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Ferma l'anteprima quando il dialog si chiude
    DisposableEffect(Unit) {
        onDispose {
            previewPlayer?.let {
                try { it.stop() } catch (_: Exception) {}
                try { it.release() } catch (_: Exception) {}
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suono allarme") },
        text = {
            Column {
                TimerPreferences.alarmSounds.forEach { sound ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedKey = sound.key }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedKey == sound.key,
                            onClick = { selectedKey = sound.key }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sound.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                // Ferma anteprima precedente
                                previewPlayer?.let {
                                    try { it.stop() } catch (_: Exception) {}
                                    try { it.release() } catch (_: Exception) {}
                                }
                                previewPlayer = TimerPreferences.playPreview(context, sound.key)
                            }
                        ) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription = "Ascolta",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(selectedKey) }) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

// ── Formatting ─────────────────────────────────────────────────────────

private fun formatTimerSeconds(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
