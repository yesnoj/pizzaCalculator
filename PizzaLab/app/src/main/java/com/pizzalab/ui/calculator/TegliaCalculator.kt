package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaTeglia
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TegliaCalculator(modifier: Modifier = Modifier) {
    var forma by rememberSaveable { mutableStateOf("rotonda") }
    var formaExpanded by rememberSaveable { mutableStateOf(false) }
    var diametro by rememberSaveable { mutableStateOf("32") }
    var lato1 by rememberSaveable { mutableStateOf("40") }
    var lato2 by rememberSaveable { mutableStateOf("30") }
    var nTeglia by rememberSaveable { mutableFloatStateOf(1f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(68f) }
    var saleGPerL by rememberSaveable { mutableFloatStateOf(40f) }
    var grassiGPerL by rememberSaveable { mutableFloatStateOf(30f) }
    var oreLievitazione by rememberSaveable { mutableIntStateOf(24) }
    var temperatura by rememberSaveable { mutableFloatStateOf(22f) }
    var puntataMin by rememberSaveable { mutableFloatStateOf(120f) }
    var frigoH by rememberSaveable { mutableFloatStateOf(16f) }
    var aprettoMin by rememberSaveable { mutableFloatStateOf(330f) }
    var lievExpanded by rememberSaveable { mutableStateOf(false) }

    val formaOptions = listOf("rotonda" to "Rotonda", "quadrata" to "Quadrata", "rettangolare" to "Rettangolare")
    val oreLievOptions = listOf(8, 12, 24, 48)

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcTeglia(
                    forma = forma,
                    nTeglia = nTeglia.roundToInt(),
                    idroPct = idratazione.toDouble(),
                    saleGPerL = saleGPerL.toDouble(),
                    grassiGPerL = grassiGPerL.toDouble(),
                    liev = oreLievitazione,
                    frigoH = frigoH.roundToInt(),
                    puntataMin = puntataMin.roundToInt(),
                    aprettoMin = aprettoMin.roundToInt(),
                    T = temperatura.roundToInt(),
                    d = diametro.toDoubleOrNull() ?: 0.0,
                    rl1 = lato1.toDoubleOrNull() ?: 0.0,
                    rl2 = lato2.toDoubleOrNull() ?: 0.0
                )
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
        Text(
            "Calcolatore Teglia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Forma teglia
        ExposedDropdownMenuBox(
            expanded = formaExpanded,
            onExpandedChange = { formaExpanded = it }
        ) {
            OutlinedTextField(
                value = formaOptions.first { it.first == forma }.second,
                onValueChange = {},
                readOnly = true,
                label = { Text("Forma teglia") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formaExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = formaExpanded,
                onDismissRequest = { formaExpanded = false }
            ) {
                formaOptions.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            forma = key
                            formaExpanded = false
                        }
                    )
                }
            }
        }

        // Dimension inputs based on forma
        when (forma) {
            "rotonda" -> {
                OutlinedTextField(
                    value = diametro,
                    onValueChange = { diametro = it },
                    label = { Text("Diametro (cm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "quadrata" -> {
                OutlinedTextField(
                    value = lato1,
                    onValueChange = { lato1 = it },
                    label = { Text("Lato (cm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "rettangolare" -> {
                OutlinedTextField(
                    value = lato1,
                    onValueChange = { lato1 = it },
                    label = { Text("Lato 1 (cm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lato2,
                    onValueChange = { lato2 = it },
                    label = { Text("Lato 2 (cm)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SliderInput(
            label = "Numero teglie",
            value = nTeglia,
            onValueChange = { nTeglia = it },
            valueRange = 1f..6f,
            steps = 4,
            valueDisplay = "${nTeglia.roundToInt()}"
        )

        SliderInput(
            label = "Idratazione",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = 60f..80f,
            steps = 19,
            valueDisplay = "${idratazione.roundToInt()} %"
        )

        SliderInput(
            label = "Sale",
            value = saleGPerL,
            onValueChange = { saleGPerL = it },
            valueRange = 20f..60f,
            steps = 7,
            valueDisplay = "${saleGPerL.roundToInt()} g/L"
        )

        SliderInput(
            label = "Grassi (olio)",
            value = grassiGPerL,
            onValueChange = { grassiGPerL = it },
            valueRange = 0f..80f,
            steps = 15,
            valueDisplay = "${grassiGPerL.roundToInt()} g/L"
        )

        // Ore lievitazione
        ExposedDropdownMenuBox(
            expanded = lievExpanded,
            onExpandedChange = { lievExpanded = it }
        ) {
            OutlinedTextField(
                value = "$oreLievitazione ore",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ore lievitazione") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lievExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = lievExpanded,
                onDismissRequest = { lievExpanded = false }
            ) {
                oreLievOptions.forEach { ore ->
                    DropdownMenuItem(
                        text = { Text("$ore ore") },
                        onClick = {
                            oreLievitazione = ore
                            lievExpanded = false
                        }
                    )
                }
            }
        }

        SliderInput(
            label = "Temperatura ambiente",
            value = temperatura,
            onValueChange = { temperatura = it },
            valueRange = 18f..30f,
            steps = 11,
            valueDisplay = "${temperatura.roundToInt()} °C"
        )

        SliderInput(
            label = "Puntata",
            value = puntataMin,
            onValueChange = { puntataMin = it },
            valueRange = 30f..300f,
            steps = 8,
            valueDisplay = "${puntataMin.roundToInt()} min"
        )

        SliderInput(
            label = "Frigo",
            value = frigoH,
            onValueChange = { frigoH = it },
            valueRange = 0f..48f,
            steps = 47,
            valueDisplay = "${frigoH.roundToInt()} h"
        )

        SliderInput(
            label = "Appretto",
            value = aprettoMin,
            onValueChange = { aprettoMin = it },
            valueRange = 60f..600f,
            steps = 8,
            valueDisplay = "${aprettoMin.roundToInt()} min"
        )

        // Results
        ricetta?.let { r ->
            TegliaResultCard(r)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TegliaResultCard(r: RicettaTeglia) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Ricetta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ResultRow("Peso panetto", "${r.pesoPanetto} g")
            ResultRow("Area teglia", "${r.areaCm2} cm²")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ResultRow("Farina", "${r.farina} g")
            ResultRow("Acqua", "${r.acqua} g")
            ResultRow("Sale", "${r.sale} g")
            ResultRow("Olio", "${r.olio} g")
            ResultRow("Lievito fresco", "${r.ldbf} g")
            ResultRow("Lievito secco", "${r.ldbs} g")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ResultRow("Totale", "${r.totale} g")
            ResultRow("Forza consigliata", "W${r.forza}")
        }
    }
}
