package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaFacile
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacileCalculator(
    onStartProcess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nPanetti by rememberSaveable { mutableFloatStateOf(4f) }
    var pesoPanetto by rememberSaveable { mutableFloatStateOf(250f) }
    var oreLievitazione by rememberSaveable { mutableIntStateOf(24) }
    var temperatura by rememberSaveable { mutableFloatStateOf(22f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(63f) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }

    val oreLievOptions = listOf(1, 2, 3, 4, 6, 8, 12, 24, 48)

    val ricetta by remember {
        derivedStateOf {
            try {
                if (oreLievitazione <= 6) {
                    PizzaFormulas.calcRapido(
                        nPanetti = nPanetti.roundToInt(),
                        pesoPanetto = pesoPanetto.toDouble(),
                        lievOre = oreLievitazione,
                        T = temperatura.roundToInt(),
                        idroPct = idratazione.toDouble()
                    )
                } else {
                    PizzaFormulas.calcFacile(
                        nPanetti = nPanetti.roundToInt(),
                        pesoPanetto = pesoPanetto.toDouble(),
                        liev = oreLievitazione,
                        T = temperatura.roundToInt(),
                        idroPct = idratazione.toDouble()
                    )
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Numero panetti
        SliderInput(
            label = "Numero panetti",
            value = nPanetti,
            onValueChange = { nPanetti = it },
            valueRange = 1f..12f,
            steps = 10,
            valueDisplay = "${nPanetti.roundToInt()}"
        )

        // Peso panetto
        SliderInput(
            label = "Peso panetto",
            value = pesoPanetto,
            onValueChange = { pesoPanetto = it },
            valueRange = 180f..350f,
            steps = 16,
            valueDisplay = "${pesoPanetto.roundToInt()} g"
        )

        // Ore lievitazione (dropdown)
        val isRapido = oreLievitazione <= 6
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = if (isRapido) "$oreLievitazione ore (rapido)" else "$oreLievitazione ore",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ore lievitazione") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                oreLievOptions.forEach { ore ->
                    val label = if (ore <= 6) "$ore ore (rapido)" else "$ore ore"
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            oreLievitazione = ore
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Temperatura ambiente — range più ampio in modalità rapida
        SliderInput(
            label = "Temperatura ambiente",
            value = temperatura,
            onValueChange = { temperatura = it },
            valueRange = 18f..(if (isRapido) 35f else 30f),
            steps = if (isRapido) 16 else 11,
            valueDisplay = "${temperatura.roundToInt()} °C"
        )

        // Idratazione — range più ampio in modalità rapida
        SliderInput(
            label = "Idratazione",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = (if (isRapido) 55f else 59f)..(if (isRapido) 80f else 70f),
            steps = if (isRapido) 24 else 10,
            valueDisplay = "${idratazione.roundToInt()} %"
        )

        // Results
        ricetta?.let { r ->
            ResultCard(r)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartProcess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Avvia Processo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResultCard(r: RicettaFacile) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Ricetta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ResultRow("Farina", "${r.farina} g")
            ResultRow("Acqua", "${r.acqua} g")
            ResultRow("Sale", "${r.sale} g")
            ResultRow("Lievito fresco", "${r.ldbf} g")
            ResultRow("Lievito secco", "${r.ldbs} g")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ResultRow("Totale", "${r.totale} g")
            ResultRow("Forza consigliata", "W${r.forza}")
            ResultRow("Temp. chiusura", "${r.tempChiusura} °C")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Timeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            ResultRow("Puntata", PizzaFormulas.fmtMinutes(r.puntataMin))
            if (r.frigoMin > 0) {
                ResultRow("Frigo", PizzaFormulas.fmtMinutes(r.frigoMin))
            }
            ResultRow("Appretto", PizzaFormulas.fmtMinutes(r.aprettoMin))
            ResultRow("Inizio appretto", r.inizioApretto.format(timeFmt))
            ResultRow("Pronti", r.pronti.format(timeFmt))
        }
    }
}

@Composable
internal fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun SliderInput(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueDisplay: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
