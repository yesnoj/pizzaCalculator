package com.pizzalab.ui.process

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pizzalab.data.model.DoughPhase
import com.pizzalab.ui.theme.QuadernoColors

/**
 * A predefined phase preset with suggested name, description and duration.
 */
private data class PhasePreset(
    val label: String,
    val description: String,
    val durationMinutes: Int
)

private val phasePresets = listOf(
    PhasePreset("Autolisi", "Riposo farina e acqua", 30),
    PhasePreset("Impasto", "Impastamento ingredienti", 20),
    PhasePreset("Puntata", "Prima lievitazione in massa", 120),
    PhasePreset("Pieghe", "Pieghe di rinforzo", 5),
    PhasePreset("Staglio", "Formatura dei panetti", 15),
    PhasePreset("Frigo", "Maturazione in frigorifero", 1440),
    PhasePreset("Appretto", "Lievitazione finale a temperatura", 120),
    PhasePreset("Rinfresco", "Aggiunta ingredienti al pre-fermento", 10),
)

private const val CUSTOM_LABEL = "Personalizzata"

/**
 * Dialog for adding or editing a dough process phase.
 * Styled with Quaderno design system: custom Dialog with corner mark,
 * uppercase labels, bordered inputs, and FlowRow chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditPhaseDialog(
    existingPhase: DoughPhase? = null,
    onConfirm: (DoughPhase) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = existingPhase != null

    var selectedPreset by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf(existingPhase?.name ?: "") }
    var description by remember { mutableStateOf(existingPhase?.description ?: "") }
    var hours by remember {
        mutableIntStateOf(
            if (existingPhase != null) (existingPhase.durationMinutes / 60) else 0
        )
    }
    var minutes by remember {
        mutableIntStateOf(
            if (existingPhase != null) (existingPhase.durationMinutes % 60) else 0
        )
    }

    val isValid = name.isNotBlank() && (hours > 0 || minutes > 0)

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
                text = if (isEditing) "Modifica fase" else "Nuova fase",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = QuadernoColors.Ink),
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle
            Text(
                text = if (isEditing) "modifica i dettagli" else "aggiungi un passo al tuo processo",
                style = TextStyle(fontSize = 12.5.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink2),
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Preset chips — only when adding
                if (!isEditing) {
                    // PREDEFINITE label
                    Text(
                        text = "PREDEFINITE",
                        style = TextStyle(
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                        ),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val allLabels = phasePresets.map { it.label } + CUSTOM_LABEL
                        allLabels.forEach { label ->
                            val isSelected = selectedPreset == label
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) QuadernoColors.Primary else QuadernoColors.Rule,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .background(if (isSelected) QuadernoColors.Primary else Color.Transparent)
                                    .clickable {
                                        selectedPreset = label
                                        val preset = phasePresets.find { it.label == label }
                                        if (preset != null) {
                                            name = preset.label
                                            description = preset.description
                                            hours = preset.durationMinutes / 60
                                            minutes = preset.durationMinutes % 60
                                        } else {
                                            name = ""
                                            description = ""
                                            hours = 0
                                            minutes = 0
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) QuadernoColors.Paper else QuadernoColors.Ink2,
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // NOME label
                Text(
                    text = "NOME",
                    style = TextStyle(
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                // Name input
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
                                    text = "es. Puntata",
                                    style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                // DESCRIZIONE label
                Text(
                    text = "DESCRIZIONE",
                    style = TextStyle(
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                    ),
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                // Description input
                BasicTextField(
                    value = description,
                    onValueChange = { description = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (description.isEmpty()) {
                                Text(
                                    text = "es. Prima lievitazione in massa",
                                    style = TextStyle(fontSize = 15.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                                )
                            }
                            innerTextField()
                        }
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ORE and MINUTI side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    // ORE
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ORE",
                            style = TextStyle(
                                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp, color = QuadernoColors.Ink3,
                            ),
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                        BasicTextField(
                            value = if (hours == 0) "" else hours.toString(),
                            onValueChange = { value ->
                                hours = value.filter { it.isDigit() }.take(3).toIntOrNull() ?: 0
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (hours == 0) {
                                            Text(
                                                text = "0",
                                                style = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink3),
                                            )
                                        }
                                        innerTextField()
                                    }
                                    Text(
                                        text = "h",
                                        style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                                    )
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
                            onValueChange = { value ->
                                val parsed = value.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                                minutes = parsed.coerceAtMost(59)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(4.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        if (minutes == 0) {
                                            Text(
                                                text = "0",
                                                style = TextStyle(fontSize = 15.sp, color = QuadernoColors.Ink3),
                                            )
                                        }
                                        innerTextField()
                                    }
                                    Text(
                                        text = "min",
                                        style = TextStyle(fontSize = 13.sp, fontStyle = FontStyle.Italic, color = QuadernoColors.Ink3),
                                    )
                                }
                            },
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
                    Text(
                        text = "Annulla",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Ink),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                // Aggiungi / Salva
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isValid) QuadernoColors.Primary else QuadernoColors.Ink3)
                        .clickable(enabled = isValid) {
                            val totalMinutes = hours * 60 + minutes
                            val phase = existingPhase?.copy(
                                name = name.trim(),
                                description = description.trim(),
                                durationMinutes = totalMinutes
                            ) ?: DoughPhase(
                                name = name.trim(),
                                description = description.trim(),
                                durationMinutes = totalMinutes
                            )
                            onConfirm(phase)
                        }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (isEditing) "Salva" else "Aggiungi",
                        style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = QuadernoColors.Paper),
                    )
                }
            }
        }
        }
    }
}
