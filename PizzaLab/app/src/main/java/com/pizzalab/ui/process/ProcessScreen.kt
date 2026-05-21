package com.pizzalab.ui.process

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.pizzalab.data.TimerPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pizzalab.data.model.DoughPhase
import com.pizzalab.data.model.DoughProcess
import com.pizzalab.service.TimerService
import kotlin.math.roundToInt

/**
 * Main process screen. Shows either a template selector or the active process view.
 */
@Composable
fun ProcessScreen(
    viewModel: ProcessViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val process by viewModel.process.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val elapsed by viewModel.elapsedSeconds.collectAsState()

    // Suono + vibrazione + notifica quando una fase si completa
    LaunchedEffect(Unit) {
        viewModel.phaseCompletedEvent.collect { completedPhase ->
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
                alertId = "phase_${completedPhase.id}",
                title = "Fase completata!",
                message = completedPhase.name
            )
        }
    }

    var showAddPhaseDialog by remember { mutableStateOf(false) }
    var showNewProcessDialog by remember { mutableStateOf(false) }

    if (showAddPhaseDialog) {
        AddEditPhaseDialog(
            onConfirm = { phase ->
                viewModel.addPhase(phase)
                showAddPhaseDialog = false
            },
            onDismiss = { showAddPhaseDialog = false }
        )
    }

    if (showNewProcessDialog) {
        NewProcessNameDialog(
            onConfirm = { name ->
                viewModel.createNewProcess(name)
                showNewProcessDialog = false
            },
            onDismiss = { showNewProcessDialog = false }
        )
    }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        if (process == null) {
            TemplateSelector(
                templates = templates,
                onSelectTemplate = { viewModel.startFromTemplate(it) },
                onCreateNew = { showNewProcessDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            ActiveProcessView(
                process = process!!,
                elapsedSeconds = elapsed,
                onStart = { viewModel.startProcess() },
                onPause = { viewModel.pauseProcess() },
                onCompletePhase = { viewModel.completePhase() },
                onCancel = { viewModel.cancelProcess() },
                onEditPhase = { id, updated -> viewModel.editPhase(id, updated) },
                onDeletePhase = { id -> viewModel.removePhase(id) },
                onReorder = { from, to -> viewModel.movePhase(from, to) },
                onAddPhase = { showAddPhaseDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

// ── Template selector ──────────────────────────────────────────────────

@Composable
private fun TemplateSelector(
    templates: List<DoughProcess>,
    onSelectTemplate: (DoughProcess) -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Processo di lavorazione",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Scegli un template o crea un nuovo processo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(templates.size) { index ->
            val template = templates[index]
            TemplateCard(template = template, onClick = { onSelectTemplate(template) })
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crea Nuovo Processo")
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateCard(
    template: DoughProcess,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))

            val summary = template.phases.joinToString(" → ") { phase ->
                "${phase.name} (${formatDuration(phase.durationMinutes)})"
            }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            val totalMinutes = template.phases.sumOf { it.durationMinutes }
            Text(
                text = "Durata totale: ${formatDuration(totalMinutes)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── Active process view ────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActiveProcessView(
    process: DoughProcess,
    elapsedSeconds: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onCompletePhase: () -> Unit,
    onCancel: () -> Unit,
    onEditPhase: (String, DoughPhase) -> Unit,
    onDeletePhase: (String) -> Unit,
    onReorder: (Int, Int) -> Unit,
    onAddPhase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingPhase by remember { mutableStateOf<DoughPhase?>(null) }

    if (editingPhase != null) {
        AddEditPhaseDialog(
            existingPhase = editingPhase,
            onConfirm = { updated ->
                onEditPhase(updated.id, updated)
                editingPhase = null
            },
            onDismiss = { editingPhase = null }
        )
    }

    // Drag state
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { mutableMapOf<Int, Int>() }

    Column(modifier = modifier.padding(16.dp)) {
        // Header
        Text(
            text = process.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (process.isRunning) {
            val totalElapsed = calculateTotalElapsed(process, elapsedSeconds)
            Text(
                text = "Tempo trascorso: ${formatSeconds(totalElapsed)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Phase list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(process.phases, key = { _, phase -> phase.id }) { index, phase ->
                // rememberUpdatedState: l'indice resta aggiornato anche se
                // pointerInput(phase.id) non si ricrea dopo un riordino.
                val currentIndex by rememberUpdatedState(index)
                val isDragging = dragIndex == index
                val canDrag = !phase.isCompleted && !phase.isActive

                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 8.dp
                    else if (phase.isActive) 4.dp
                    else 1.dp,
                    label = "cardElevation"
                )

                PhaseCard(
                    phase = phase,
                    elapsedSeconds = if (phase.isActive) elapsedSeconds else 0L,
                    onEdit = { editingPhase = phase },
                    onDelete = { onDeletePhase(phase.id) },
                    elevation = elevation,
                    modifier = Modifier
                        // Smooth animation for non-dragged items
                        .then(
                            if (!isDragging) Modifier.animateItemPlacement()
                            else Modifier
                        )
                        .onGloballyPositioned { coords ->
                            itemHeights[index] = coords.size.height
                        }
                        .then(
                            if (isDragging) Modifier
                                .zIndex(1f)
                                .offset { IntOffset(0, dragOffsetY.roundToInt()) }
                            else Modifier
                        )
                        .then(
                            // Usa phase.id come key (stabile) — NON index.
                            // Così il pointerInput sopravvive al riordino.
                            if (canDrag) Modifier.pointerInput(phase.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        dragIndex = currentIndex
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y

                                        val curIdx = dragIndex
                                        val itemH = itemHeights[curIdx] ?: return@detectDragGesturesAfterLongPress
                                        val threshold = itemH * 0.6f

                                        if (dragOffsetY > threshold && curIdx < process.phases.size - 1) {
                                            onReorder(curIdx, curIdx + 1)
                                            dragIndex = curIdx + 1
                                            dragOffsetY -= itemH + 8.dp.toPx()
                                        } else if (dragOffsetY < -threshold && curIdx > 0) {
                                            onReorder(curIdx, curIdx - 1)
                                            dragIndex = curIdx - 1
                                            dragOffsetY += itemH + 8.dp.toPx()
                                        }
                                    },
                                    onDragEnd = {
                                        dragIndex = -1
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        dragIndex = -1
                                        dragOffsetY = 0f
                                    }
                                )
                            }
                            else Modifier
                        )
                )
            }
            // "Add phase" button inline (replaces old FAB)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAddPhase,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aggiungi fase")
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Action buttons
        Spacer(modifier = Modifier.height(8.dp))
        ActionButtons(
            isRunning = process.isRunning,
            hasActivePhase = process.phases.any { it.isActive },
            allCompleted = process.phases.all { it.isCompleted },
            onStart = onStart,
            onPause = onPause,
            onCompletePhase = onCompletePhase,
            onCancel = onCancel
        )
    }
}

@Composable
private fun PhaseCard(
    phase: DoughPhase,
    elapsedSeconds: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val cardAlpha = if (phase.isCompleted) 0.6f else 1f
    val containerColor by animateColorAsState(
        targetValue = when {
            phase.isActive -> MaterialTheme.colorScheme.primaryContainer
            phase.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        },
        label = "phaseCardColor"
    )

    // Context menu via pointerInput only for completed/active phases.
    // Pending phases use the outer pointerInput for drag-after-long-press instead.
    val contextMenuModifier = if (phase.isCompleted || phase.isActive) {
        Modifier.pointerInput(phase.id) {
            detectTapGestures(
                onLongPress = { showContextMenu = true }
            )
        }
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .then(contextMenuModifier),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status icon
                    Icon(
                        imageVector = when {
                            phase.isCompleted -> Icons.Filled.CheckCircle
                            phase.isActive -> Icons.Filled.HourglassEmpty
                            else -> Icons.Filled.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = when {
                            phase.isCompleted -> MaterialTheme.colorScheme.primary
                            phase.isActive -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (phase.isActive) FontWeight.Bold else FontWeight.Medium
                        )
                        if (phase.description.isNotBlank()) {
                            Text(
                                text = phase.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Duration / countdown
                    Column(horizontalAlignment = Alignment.End) {
                        if (phase.isActive) {
                            val remaining = (phase.durationMinutes * 60L) - elapsedSeconds
                            Text(
                                text = formatSeconds(remaining.coerceAtLeast(0)),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = formatDuration(phase.durationMinutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Drag handle (visual indicator for draggable phases)
                    if (!phase.isCompleted && !phase.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.DragHandle,
                            contentDescription = "Tieni premuto per riordinare",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Progress bar for active phase
                if (phase.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val totalSec = phase.durationMinutes * 60f
                    val progress = if (totalSec > 0) (elapsedSeconds / totalSec).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }

            // Context menu for long-press (only on completed/active phases)
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Modifica") },
                    onClick = {
                        showContextMenu = false
                        onEdit()
                    },
                    leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Elimina") },
                    onClick = {
                        showContextMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isRunning: Boolean,
    hasActivePhase: Boolean,
    allCompleted: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onCompletePhase: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (allCompleted) {
            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Processo Completato")
            }
        } else if (isRunning) {
            Button(
                onClick = onCompletePhase,
                modifier = Modifier.weight(1f)
            ) {
                Text("Completa Fase")
            }
            OutlinedButton(onClick = onPause) {
                Text("Pausa")
            }
        } else {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (hasActivePhase) "Riprendi" else "Avvia")
            }
        }

        if (!allCompleted) {
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Annulla")
            }
        }
    }
}

// ── New process name dialog ────────────────────────────────────────────

@Composable
private fun NewProcessNameDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo Processo") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome del processo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Crea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

// ── Formatting helpers ─────────────────────────────────────────────────

/** Format a duration in minutes to a human-readable string (e.g. "2h 30min"). */
private fun formatDuration(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}min"
        h > 0 -> "${h}h"
        else -> "${m}min"
    }
}

/** Format seconds to HH:MM:SS or MM:SS. */
private fun formatSeconds(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) {
        "%d:%02d:%02d".format(h, m, s)
    } else {
        "%02d:%02d".format(m, s)
    }
}

/** Calculate total elapsed time across all completed phases plus current active phase. */
private fun calculateTotalElapsed(process: DoughProcess, currentPhaseElapsed: Long): Long {
    val completedSeconds = process.phases
        .filter { it.isCompleted }
        .sumOf { it.durationMinutes.toLong() * 60 }
    return completedSeconds + currentPhaseElapsed
}
