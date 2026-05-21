package com.pizzalab.ui.process

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pizzalab.data.model.DoughPhase

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
 *
 * @param existingPhase If non-null, the dialog pre-fills with this phase's data (edit mode).
 * @param onConfirm Called with the new/updated [DoughPhase] when the user confirms.
 * @param onDismiss Called when the dialog is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPhaseDialog(
    existingPhase: DoughPhase? = null,
    onConfirm: (DoughPhase) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = existingPhase != null

    // Track which preset chip is selected (null = no preset / custom)
    var selectedPreset by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf(existingPhase?.name ?: "") }
    var description by remember { mutableStateOf(existingPhase?.description ?: "") }
    var hours by remember {
        mutableStateOf(
            ((existingPhase?.durationMinutes ?: 0) / 60).toString().let {
                if (it == "0" && existingPhase == null) "" else it
            }
        )
    }
    var minutes by remember {
        mutableStateOf(
            ((existingPhase?.durationMinutes ?: 0) % 60).toString().let {
                if (it == "0" && existingPhase == null) "" else it
            }
        )
    }

    val isValid = name.isNotBlank() &&
            ((hours.toIntOrNull() ?: 0) > 0 || (minutes.toIntOrNull() ?: 0) > 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Modifica Fase" else "Nuova Fase")
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Show preset chips only when adding a new phase
                if (!isEditing) {
                    Text("Fase predefinita")
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        phasePresets.forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset.label,
                                onClick = {
                                    selectedPreset = preset.label
                                    name = preset.label
                                    description = preset.description
                                    val h = preset.durationMinutes / 60
                                    val m = preset.durationMinutes % 60
                                    hours = h.toString()
                                    minutes = m.toString()
                                },
                                label = { Text(preset.label) }
                            )
                        }
                        FilterChip(
                            selected = selectedPreset == CUSTOM_LABEL,
                            onClick = {
                                selectedPreset = CUSTOM_LABEL
                                name = ""
                                description = ""
                                hours = ""
                                minutes = ""
                            },
                            label = { Text(CUSTOM_LABEL) }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        // If user manually edits the name, switch to custom
                        if (selectedPreset != null && selectedPreset != CUSTOM_LABEL) {
                            val preset = phasePresets.find { p -> p.label == selectedPreset }
                            if (preset != null && it != preset.label) {
                                selectedPreset = CUSTOM_LABEL
                            }
                        }
                    },
                    label = { Text("Nome fase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrizione") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Durata")
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { hours = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Ore") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Minuti") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalMinutes = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
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
                },
                enabled = isValid
            ) {
                Text(if (isEditing) "Salva" else "Aggiungi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}
