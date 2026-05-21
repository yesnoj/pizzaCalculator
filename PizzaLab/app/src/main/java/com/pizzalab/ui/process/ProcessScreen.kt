package com.pizzalab.ui.process

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.pizzalab.data.TimerPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pizzalab.data.model.DoughPhase
import com.pizzalab.data.model.DoughProcess
import com.pizzalab.service.TimerService
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QDarkButton
import com.pizzalab.ui.components.QHeader
import com.pizzalab.ui.components.QPrimaryButton
import com.pizzalab.ui.components.QSecondaryButton
import com.pizzalab.ui.components.QSectionDivider
import com.pizzalab.ui.theme.QuadernoColors
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

@Composable
fun ProcessScreen(
    onNavigateToCooking: () -> Unit = {},
    viewModel: ProcessViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val process by viewModel.process.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val elapsed by viewModel.elapsedSeconds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.phaseCompletedEvent.collect { completedPhase ->
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
            TimerService.fireAlert(context = context, alertId = "phase_${completedPhase.id}", title = "Fase completata!", message = completedPhase.name)
        }
    }

    var showAddPhaseDialog by remember { mutableStateOf(false) }
    var showNewProcessDialog by remember { mutableStateOf(false) }

    if (showAddPhaseDialog) {
        AddEditPhaseDialog(
            onConfirm = { phase -> viewModel.addPhase(phase); showAddPhaseDialog = false },
            onDismiss = { showAddPhaseDialog = false }
        )
    }

    if (showNewProcessDialog) {
        NewProcessNameDialog(
            onConfirm = { name -> viewModel.createNewProcess(name); showNewProcessDialog = false },
            onDismiss = { showNewProcessDialog = false }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(QuadernoColors.Bg)) {
        if (process == null) {
            TemplateSelector(
                templates = templates,
                onSelectTemplate = { viewModel.startFromTemplate(it) },
                onCreateNew = { showNewProcessDialog = true },
            )
        } else {
            val proc = process!!
            if (proc.phases.isNotEmpty() && proc.phases.all { it.isCompleted }) {
                CompletedView(
                    process = proc,
                    onNewProcess = { viewModel.cancelProcess() },
                    onGoToCooking = onNavigateToCooking,
                )
            } else {
                ActiveProcessView(
                    process = proc,
                    elapsedSeconds = elapsed,
                    onStart = { viewModel.startProcess() },
                    onPause = { viewModel.pauseProcess() },
                    onCompletePhase = { viewModel.completePhase() },
                    onCancel = { viewModel.cancelProcess() },
                    onEditPhase = { id, updated -> viewModel.editPhase(id, updated) },
                    onDeletePhase = { id -> viewModel.removePhase(id) },
                    onReorder = { from, to -> viewModel.movePhase(from, to) },
                    onAddPhase = { showAddPhaseDialog = true },
                )
            }
        }
    }
}

// ── Template selector ──────────────────────────────────────────────────

@Composable
private fun TemplateSelector(
    templates: List<DoughProcess>,
    onSelectTemplate: (DoughProcess) -> Unit,
    onCreateNew: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        QHeader(
            kicker = "Processo",
            title = "Lievitazione",
            italic = "scegli un metodo",
        )

        Text(
            text = "Parti da un template, o crea il tuo processo da zero.",
            style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        // § TEMPLATE section divider
        QSectionDivider(label = "Template", count = templates.size)

        templates.forEachIndexed { index, template ->
            TemplateCard(template = template, index = index, onClick = { onSelectTemplate(template) })
        }

        // "Crea nuovo processo" button — right below templates, dashed border (same style as "aggiungi fase")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp)
                .drawBehind {
                    drawRoundRect(
                        color = QuadernoColors.Rule,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())),
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                    )
                }
                .clip(RoundedCornerShape(4.dp))
                .clickable { onCreateNew() }
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Add, null, Modifier.size(13.dp), tint = QuadernoColors.Ink2)
                Text("crea nuovo processo", style = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun TemplateCard(template: DoughProcess, index: Int, onClick: () -> Unit) {
    // Outer box for corner mark positioning (same pattern as QCard)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 5.dp),
    ) {
        // Card body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(2.dp))
                .background(QuadernoColors.Paper)
                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(2.dp))
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = template.name,
                    style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink, letterSpacing = (-0.02).sp),
                )
                Text(
                    text = "${template.phases.size} fasi",
                    style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                )
            }

            Text(
                text = template.phases.joinToString(" • ") { it.name },
                style = TextStyle(fontSize = 12.5.sp, color = QuadernoColors.Ink2, lineHeight = 18.sp),
                modifier = Modifier.padding(top = 4.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "durata totale",
                    style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "${formatDuration(template.phases.sumOf { it.durationMinutes })}",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Primary),
                    )
                    Text(
                        text = "›",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink2),
                    )
                }
            }
        }
        }

        // Corner mark — top-left (same as QCard's CornerMark: 18dp Canvas, 2dp stroke)
        Canvas(modifier = Modifier.size(18.dp).align(Alignment.TopStart)) {
            val sw = 2.dp.toPx()
            drawLine(QuadernoColors.Primary, Offset(0f, sw / 2), Offset(size.width, sw / 2), sw)
            drawLine(QuadernoColors.Primary, Offset(sw / 2, 0f), Offset(sw / 2, size.height), sw)
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
) {
    var editingPhase by remember { mutableStateOf<DoughPhase?>(null) }

    if (editingPhase != null) {
        AddEditPhaseDialog(
            existingPhase = editingPhase,
            onConfirm = { updated -> onEditPhase(updated.id, updated); editingPhase = null },
            onDismiss = { editingPhase = null }
        )
    }

    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { mutableMapOf<Int, Int>() }

    // Compute estimated start times for each phase
    val startTimes = remember(process.phases) {
        val firstStart = process.phases.firstOrNull { it.startedAt != null }?.startedAt
            ?: java.time.LocalDateTime.now()
        var cumMinutes = 0
        process.phases.map { phase ->
            val t = firstStart.plusMinutes(cumMinutes.toLong())
            cumMinutes += phase.durationMinutes
            t
        }
    }
    val totalMinutes = process.phases.sumOf { it.durationMinutes }
    val endTime = remember(startTimes, totalMinutes) {
        val firstStart = process.phases.firstOrNull { it.startedAt != null }?.startedAt
            ?: java.time.LocalDateTime.now()
        firstStart.plusMinutes(totalMinutes.toLong())
    }

    Column(modifier = Modifier.fillMaxSize()) {
        QHeader(
            kicker = "Processo · ${process.name}",
            title = process.name,
            right = {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (process.isRunning) QuadernoColors.Primary else QuadernoColors.Ink2,
                            RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (process.isRunning) "IN CORSO" else "IN PAUSA",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (process.isRunning) QuadernoColors.Primary else QuadernoColors.Ink2,
                            letterSpacing = 0.6.sp,
                        ),
                    )
                }
            },
        )

        if (process.isRunning) {
            val firstStartTime = process.phases.firstOrNull { it.startedAt != null }?.startedAt
            val startLabel = firstStartTime?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "--:--"
            val endLabel = "%02d:%02d".format(endTime.hour, endTime.minute)
            Text(
                text = "partito alle $startLabel · pronti alle $endLabel",
                style = TextStyle(fontSize = 12.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                modifier = Modifier.padding(horizontal = 22.dp).drawBehind {
                    drawLine(
                        color = QuadernoColors.RuleDots,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                    )
                }.padding(bottom = 12.dp),
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            itemsIndexed(process.phases, key = { _, phase -> phase.id }) { index, phase ->
                val currentIndex by rememberUpdatedState(index)
                val currentPhaseCount by rememberUpdatedState(process.phases.size)
                val isDragging = dragIndex == index
                val canDrag = !phase.isCompleted && !phase.isActive

                QuadernoPhaseCard(
                    phase = phase,
                    index = index,
                    estimatedStart = startTimes.getOrNull(index),
                    elapsedSeconds = if (phase.isActive) elapsedSeconds else 0L,
                    onEdit = { editingPhase = phase },
                    onDelete = { onDeletePhase(phase.id) },
                    isLast = index == process.phases.size - 1,
                    modifier = Modifier
                        // Only animate items that are NOT being dragged
                        .then(if (dragIndex == -1) Modifier.animateItemPlacement() else Modifier)
                        .onGloballyPositioned { coords -> itemHeights[currentIndex] = coords.size.height }
                        .then(
                            if (isDragging) Modifier.zIndex(1f).offset { IntOffset(0, dragOffsetY.roundToInt()) }
                            else Modifier
                        )
                        .then(
                            if (canDrag) Modifier.pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        dragIndex = currentIndex
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                        val curIdx = dragIndex
                                        if (curIdx < 0) return@detectDragGesturesAfterLongPress
                                        val itemH = itemHeights[curIdx]?.toFloat()
                                            ?: return@detectDragGesturesAfterLongPress
                                        val threshold = itemH * 0.5f
                                        if (dragOffsetY > threshold && curIdx < currentPhaseCount - 1) {
                                            onReorder(curIdx, curIdx + 1)
                                            dragIndex = curIdx + 1
                                            dragOffsetY -= itemH
                                        } else if (dragOffsetY < -threshold && curIdx > 0) {
                                            onReorder(curIdx, curIdx - 1)
                                            dragIndex = curIdx - 1
                                            dragOffsetY += itemH
                                        }
                                    },
                                    onDragEnd = { dragIndex = -1; dragOffsetY = 0f },
                                    onDragCancel = { dragIndex = -1; dragOffsetY = 0f },
                                )
                            } else Modifier
                        ),
                )
            }

            item {
                // "Aggiungi una fase" button
                val dashedColor = QuadernoColors.RuleDots
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .drawBehind {
                            drawRoundRect(
                                color = dashedColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                            )
                        }
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onAddPhase() }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Add, null, Modifier.size(13.dp), tint = QuadernoColors.Ink2)
                        Text("aggiungi una fase", style = TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2))
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (process.isRunning) {
                Box(modifier = Modifier.weight(1f)) { QDarkButton("Completa fase", onClick = onCompletePhase) }
                QSecondaryButton("pausa", onClick = onPause, italic = true)
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    QPrimaryButton(if (process.phases.any { it.isActive }) "Riprendi" else "Avvia", onClick = onStart)
                }
                QSecondaryButton("Annulla", onClick = onCancel)
            }
        }
    }
}

@Composable
private fun QuadernoPhaseCard(
    phase: DoughPhase,
    index: Int,
    estimatedStart: java.time.LocalDateTime?,
    elapsedSeconds: Long,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    val done = phase.isCompleted
    val active = phase.isActive

    val contextMenuModifier = if (done || active) {
        Modifier.pointerInput(phase.id) { detectTapGestures(onLongPress = { showContextMenu = true }) }
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (done) 0.6f else 1f)
            .then(contextMenuModifier)
            .drawBehind {
                if (!isLast) {
                    drawLine(QuadernoColors.Rule, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
            }
            .padding(vertical = 12.dp),
    ) {
        // Time column
        Column(modifier = Modifier.width(56.dp).padding(top = 4.dp)) {
            if (estimatedStart != null) {
                Text(
                    text = "%02d:%02d".format(estimatedStart.hour, estimatedStart.minute),
                    style = TextStyle(
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (active) QuadernoColors.Primary else QuadernoColors.Ink2,
                        letterSpacing = (-0.01).sp,
                    ),
                )
            }
            Text(
                text = "nº ${(index + 1).toString().padStart(2, '0')}",
                style = TextStyle(fontSize = 9.5.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = phase.name,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink, letterSpacing = (-0.02).sp),
                )
                Text(
                    text = formatDuration(phase.durationMinutes),
                    style = TextStyle(fontSize = 11.5.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                )
                if (!done && !active) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.DragHandle, null, Modifier.size(16.dp), tint = QuadernoColors.Ink3)
                }
            }

            if (phase.description.isNotBlank()) {
                Text(
                    text = phase.description,
                    style = TextStyle(fontSize = 12.5.sp, color = QuadernoColors.Ink2),
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (active) {
                val totalSec = phase.durationMinutes * 60f
                val progress = if (totalSec > 0) (elapsedSeconds / totalSec).coerceIn(0f, 1f) else 0f
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(QuadernoColors.BgWarmer),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(QuadernoColors.Primary),
                        )
                    }
                    val remaining = (phase.durationMinutes * 60L) - elapsedSeconds
                    Text(
                        text = formatSeconds(remaining.coerceAtLeast(0)),
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Primary),
                    )
                }
            }

            if (done) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(Icons.Filled.Check, null, Modifier.size(12.dp), tint = QuadernoColors.Olive)
                    Text(
                        text = "fatta",
                        style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Olive),
                    )
                }
            }
        }

        // Context menu
        Box {
            DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Modifica", style = TextStyle(fontWeight = FontWeight.SemiBold, color = QuadernoColors.Ink)) },
                    onClick = { showContextMenu = false; onEdit() },
                    leadingIcon = { Icon(Icons.Filled.Edit, null, Modifier.size(16.dp), tint = QuadernoColors.Ink) },
                )
                DropdownMenuItem(
                    text = { Text("Elimina", style = TextStyle(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic, color = QuadernoColors.Primary)) },
                    onClick = { showContextMenu = false; onDelete() },
                    leadingIcon = { Icon(Icons.Filled.Delete, null, Modifier.size(16.dp), tint = QuadernoColors.Primary) },
                )
            }
        }
    }
}

// ── Completed view ────────────────────────────────────────────────────

@Composable
private fun CompletedView(
    process: DoughProcess,
    onNewProcess: () -> Unit,
    onGoToCooking: () -> Unit,
) {
    val now = remember { java.time.LocalTime.now() }
    val timeStr = remember { String.format("%02d:%02d", now.hour, now.minute) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        QHeader(
            kicker = "Processo · Completato",
            title = "Pronta!",
            italic = "impasto al punto",
            right = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(QuadernoColors.Olive)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("FATTO", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper, letterSpacing = 0.6.sp))
                }
            },
        )

        // ── "QUANDO · ADESSO" time card ──
        QCard(
            kicker = "Quando · Adesso",
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = timeStr,
                    style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Black, color = QuadernoColors.Ink, letterSpacing = (-1).sp),
                )
                Text(
                    text = "stendi i panetti e accendi il forno.",
                    style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                )
            }
        }

        // ── Riepilogo fasi ──
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                text = "§ RIEPILOGO",
                style = TextStyle(
                    fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp, color = QuadernoColors.Primary,
                ),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            process.phases.forEach { phase ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Checkmark
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = QuadernoColors.Olive,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = phase.name,
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = formatDuration(phase.durationMinutes),
                        style = TextStyle(fontSize = 14.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Buttons ──
        Row(
            modifier = Modifier.padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Vai in cottura (primary)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(QuadernoColors.Primary)
                    .clickable { onGoToCooking() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Vai in cottura →",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper),
                )
            }
            // Nuovo (outlined)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(6.dp))
                    .clickable { onNewProcess() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Nuovo",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ── New process name dialog ────────────────────────────────────────────

@Composable
private fun NewProcessNameDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
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
                Text(
                    text = "Nuovo processo",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "dagli un nome per ritrovarlo",
                    style = TextStyle(fontSize = 12.5.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Label
                Text(
                    text = "NOME DEL PROCESSO",
                    style = TextStyle(
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                // Input field with border
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (name.isEmpty()) {
                                Text(
                                    text = "es. La mia napoletana del weekend",
                                    style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "potrai aggiungere le fasi al passo successivo.",
                    style = TextStyle(fontSize = 11.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                )

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
                        Text(
                            text = "Annulla",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    // Crea
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (name.isNotBlank()) QuadernoColors.Primary else QuadernoColors.Ink3)
                            .clickable(enabled = name.isNotBlank()) { onConfirm(name.trim()) }
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                    ) {
                        Text(
                            text = "Crea",
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper),
                        )
                    }
                }
            }
        }
    }
}

// ── Formatting helpers ─────────────────────────────────────────────────

private fun formatDuration(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}min"
        h > 0 -> "${h}h"
        else -> "${m}min"
    }
}

private fun formatSeconds(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}