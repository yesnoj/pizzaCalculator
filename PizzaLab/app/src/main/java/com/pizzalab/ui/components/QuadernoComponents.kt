package com.pizzalab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.ui.theme.QuadernoColors

// ════════════════════════════════════════════════════════════════
// DASHED / DOTTED DIVIDERS
// ════════════════════════════════════════════════════════════════

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = QuadernoColors.RuleDots,
    dashLength: Float = 4f,
    gapLength: Float = 4f,
    strokeWidth: Float = 1f,
) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.dp.toPx(), gapLength.dp.toPx()))
        )
    }
}

@Composable
fun DottedSpacer(
    modifier: Modifier = Modifier,
    color: Color = QuadernoColors.RuleDots,
) {
    Canvas(modifier = modifier.height(1.dp).fillMaxWidth()) {
        val r = 0.6.dp.toPx()
        val gap = 4.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawCircle(color, r, Offset(x, size.height / 2))
            x += gap
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_HEADER — screen header with kicker + title + italic subtitle
// ════════════════════════════════════════════════════════════════

@Composable
fun QHeader(
    kicker: String? = null,
    title: String,
    italic: String? = null,
    right: @Composable (() -> Unit)? = null,
    tight: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 22.dp, end = 22.dp,
                top = if (tight) 14.dp else 20.dp,
                bottom = if (tight) 4.dp else 8.dp
            )
    ) {
        if (kicker != null) {
            Text(
                text = kicker.uppercase(),
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.2.sp,
                    color = QuadernoColors.Primary,
                ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.02).sp,
                        color = QuadernoColors.Ink,
                    ),
                )
                if (italic != null) {
                    Text(
                        text = italic,
                        style = TextStyle(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            fontSize = 18.sp,
                            color = QuadernoColors.Olive,
                        ),
                    )
                }
            }
            if (right != null) {
                right()
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_FIELD — stepper input (replaces all Sliders)
// ════════════════════════════════════════════════════════════════

@Composable
fun QField(
    label: String,
    value: String,
    suffix: String = "",
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    hint: String? = null,
    presets: List<String>? = null,
    selectedPreset: String? = null,
    onPresetSelect: ((String) -> Unit)? = null,
    dense: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // dashed bottom border
                drawLine(
                    color = QuadernoColors.RuleDots,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                )
            }
            .padding(
                horizontal = 22.dp,
                vertical = if (dense) 10.dp else 12.dp,
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Label + hint
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = QuadernoColors.Ink,
                    ),
                )
                if (hint != null) {
                    Text(
                        text = " $hint",
                        style = TextStyle(
                            fontSize = 13.5.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Normal,
                            color = QuadernoColors.Ink3,
                        ),
                    )
                }
            }

            // Stepper: (-) value (+)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                QRoundButton(onClick = onMinus, isPlus = false)
                // Value with underline
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = value,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = QuadernoColors.Ink,
                                letterSpacing = (-0.01).sp,
                            ),
                        )
                        if (suffix.isNotEmpty()) {
                            Text(
                                text = suffix,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = QuadernoColors.Ink2,
                                ),
                                modifier = Modifier.padding(start = 2.dp),
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(2.dp)
                            .background(QuadernoColors.Ink)
                    )
                }
                QRoundButton(onClick = onPlus, isPlus = true)
            }
        }

        // Presets
        if (!presets.isNullOrEmpty()) {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                presets.forEach { p ->
                    val isSelected = p == selectedPreset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) QuadernoColors.BgWarmer else Color.Transparent)
                            .clickable { onPresetSelect?.invoke(p) }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = p,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) QuadernoColors.Primary else QuadernoColors.Ink2,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QRoundButton(onClick: () -> Unit, isPlus: Boolean) {
    Box(
        modifier = Modifier
            .size(48.dp) // hit area 48dp per accessibility
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .border(1.dp, QuadernoColors.Ink2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val stroke = 2.2.dp.toPx()
                val center = size.width / 2
                // horizontal line
                drawLine(
                    color = QuadernoColors.Ink2,
                    start = Offset(2.dp.toPx(), center),
                    end = Offset(size.width - 2.dp.toPx(), center),
                    strokeWidth = stroke,
                )
                // vertical line (plus only)
                if (isPlus) {
                    drawLine(
                        color = QuadernoColors.Ink2,
                        start = Offset(center, 2.dp.toPx()),
                        end = Offset(center, size.height - 2.dp.toPx()),
                        strokeWidth = stroke,
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_LEADER_ROW — label ⋯⋯⋯ value (dotted leader)
// ════════════════════════════════════════════════════════════════

@Composable
fun QLeaderRow(
    label: String,
    value: String,
    unit: String = "",
    strong: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal,
                color = QuadernoColors.Ink,
            ),
        )
        DottedSpacer(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
                .align(Alignment.Bottom),
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = if (strong) 18.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                color = QuadernoColors.Ink,
                letterSpacing = (-0.01).sp,
            ),
        )
        if (unit.isNotEmpty()) {
            Text(
                text = " $unit",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = QuadernoColors.Ink2,
                ),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_CARD — recipe card with corner marks
// ════════════════════════════════════════════════════════════════

@Composable
fun QCard(
    modifier: Modifier = Modifier,
    kicker: String? = null,
    title: String? = null,
    titleSuffix: String? = null,
    meta: String? = null,
    accent: Color = QuadernoColors.Primary,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(QuadernoColors.Paper, RoundedCornerShape(2.dp))
                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(2.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column {
                if (kicker != null) {
                    Text(
                        text = kicker.uppercase(),
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp,
                            color = accent,
                        ),
                    )
                }
                if (title != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = title,
                                style = TextStyle(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = QuadernoColors.Ink,
                                    letterSpacing = (-0.02).sp,
                                ),
                            )
                            if (titleSuffix != null) {
                                Text(
                                    text = " $titleSuffix",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = QuadernoColors.Ink2,
                                    ),
                                )
                            }
                        }
                        if (meta != null) {
                            Text(
                                text = meta,
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = QuadernoColors.Ink3,
                                ),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(if (title != null) 10.dp else 0.dp))
                content()
            }
        }

        // Corner marks — top-left
        CornerMark(
            modifier = Modifier.align(Alignment.TopStart),
            color = accent,
            topLeft = true,
        )
        // Corner marks — bottom-right
        CornerMark(
            modifier = Modifier.align(Alignment.BottomEnd),
            color = accent,
            topLeft = false,
        )
    }
}

@Composable
private fun CornerMark(
    modifier: Modifier = Modifier,
    color: Color,
    topLeft: Boolean,
    size: Dp = 18.dp,
    strokeWidth: Dp = 2.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val sw = strokeWidth.toPx()
        if (topLeft) {
            // top border
            drawLine(color, Offset(0f, sw / 2), Offset(this.size.width, sw / 2), sw)
            // left border
            drawLine(color, Offset(sw / 2, 0f), Offset(sw / 2, this.size.height), sw)
        } else {
            // bottom border
            drawLine(color, Offset(0f, this.size.height - sw / 2), Offset(this.size.width, this.size.height - sw / 2), sw)
            // right border
            drawLine(color, Offset(this.size.width - sw / 2, 0f), Offset(this.size.width - sw / 2, this.size.height), sw)
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_SEGMENTED — segmented picker
// ════════════════════════════════════════════════════════════════

@Composable
fun QSegmented(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .border(1.dp, QuadernoColors.Ink2, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(QuadernoColors.Paper),
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = item == selected
            val bgColor by animateColorAsState(
                if (isSelected) QuadernoColors.Ink else Color.Transparent,
                animationSpec = tween(240),
                label = "segBg",
            )
            val textColor by animateColorAsState(
                if (isSelected) QuadernoColors.Paper else QuadernoColors.Ink2,
                animationSpec = tween(240),
                label = "segText",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(bgColor)
                    .clickable { onSelect(item) }
                    .then(
                        if (index < items.size - 1)
                            Modifier.drawBehind {
                                drawLine(
                                    color = QuadernoColors.Ink2,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = 1.dp.toPx(),
                                )
                            }
                        else Modifier
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = item,
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
                        color = textColor,
                    ),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_CHIP_ROW — selectable chips
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QChipRow(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    wrap: Boolean = false,
) {
    if (wrap) {
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items.forEach { item -> QChip(item, item == selected) { onSelect(item) } }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items.forEach { item -> QChip(item, item == selected) { onSelect(item) } }
        }
    }
}

@Composable
private fun QChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .border(
                1.dp,
                if (isSelected) QuadernoColors.Ink else QuadernoColors.Rule,
                RoundedCornerShape(3.dp),
            )
            .background(if (isSelected) QuadernoColors.Ink else QuadernoColors.Paper)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
                color = if (isSelected) QuadernoColors.Paper else QuadernoColors.Ink2,
            ),
        )
    }
}

// ════════════════════════════════════════════════════════════════
// BUTTONS — Primary, Dark, Secondary
// ════════════════════════════════════════════════════════════════

@Composable
fun QPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(QuadernoColors.Primary)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = QuadernoColors.Paper,
                letterSpacing = 0.04.sp,
            ),
        )
    }
}

@Composable
fun QDarkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(QuadernoColors.Ink)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = QuadernoColors.Paper,
            ),
        )
    }
}

@Composable
fun QSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    italic: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, QuadernoColors.Ink2, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                color = QuadernoColors.Ink2,
            ),
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Q_DIALOG — modal dialog with corner marks
// ════════════════════════════════════════════════════════════════

@Composable
fun QDialog(
    title: String,
    kicker: String? = null,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    confirmLabel: String = "Conferma",
    dismissLabel: String = "Annulla",
    confirmEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Backdrop
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x8C2B1F12))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDismiss?.invoke() },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Dialog card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(QuadernoColors.Paper)
                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // consume click
                ),
        ) {
            Column {
                // Header
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 4.dp)) {
                    if (kicker != null) {
                        Text(
                            text = kicker.uppercase(),
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.2.sp,
                                color = QuadernoColors.Primary,
                            ),
                        )
                    }
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = QuadernoColors.Ink,
                            letterSpacing = (-0.02).sp,
                        ),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }

                // Body
                Box(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)) {
                    content()
                }

                // Footer
                DashedDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onDismiss != null) {
                        Text(
                            text = dismissLabel,
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                color = QuadernoColors.Ink2,
                                letterSpacing = 0.04.sp,
                            ),
                            modifier = Modifier
                                .clickable { onDismiss() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                    if (onConfirm != null) {
                        Text(
                            text = confirmLabel.uppercase(),
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (confirmEnabled) QuadernoColors.Primary else QuadernoColors.Ink3,
                                letterSpacing = 0.04.sp,
                            ),
                            modifier = Modifier
                                .clickable(enabled = confirmEnabled) { onConfirm() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            // Corner marks
            CornerMark(
                modifier = Modifier.align(Alignment.TopStart),
                color = QuadernoColors.Primary,
                topLeft = true,
            )
            CornerMark(
                modifier = Modifier.align(Alignment.BottomEnd),
                color = QuadernoColors.Primary,
                topLeft = false,
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_SECTION_DIVIDER — § label ────── count
// ════════════════════════════════════════════════════════════════

@Composable
fun QSectionDivider(
    label: String,
    count: Int? = null,
    accent: Color = QuadernoColors.Primary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "§ ${label.uppercase()}",
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.2.sp,
                color = accent,
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(QuadernoColors.Rule),
        )
        if (count != null) {
            Text(
                text = count.toString().padStart(2, '0'),
                style = TextStyle(
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = QuadernoColors.Ink3,
                ),
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_TEXT_FIELD — text input with ink underline
// ════════════════════════════════════════════════════════════════

@Composable
fun QTextField(
    label: String,
    value: String,
    placeholder: String = "",
    suffix: String = "",
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = QuadernoColors.RuleDots,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                )
            }
            .padding(horizontal = 22.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink3,
                letterSpacing = 0.02.sp,
            ),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        drawLine(
                            color = QuadernoColors.Ink,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2.dp.toPx(),
                        )
                    }
                    .padding(bottom = 4.dp),
            ) {
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = QuadernoColors.Ink,
                            letterSpacing = (-0.01).sp,
                        ),
                    )
                } else {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            color = QuadernoColors.Ink3,
                        ),
                    )
                }
            }
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    style = TextStyle(fontSize = 12.sp, color = QuadernoColors.Ink2),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Q_BAR_ROW — progress bar with label and percentage
// ════════════════════════════════════════════════════════════════

@Composable
fun QBarRow(
    label: String,
    value: Int,
    color: Color = QuadernoColors.Primary,
) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = TextStyle(fontSize = 12.sp, color = QuadernoColors.Ink),
            )
            Text(
                text = "$value %",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                ),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(QuadernoColors.BgWarmer),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
