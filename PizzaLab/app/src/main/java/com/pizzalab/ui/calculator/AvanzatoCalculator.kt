package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.pizzalab.domain.RicettaAvanzata
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvanzatoCalculator(
    onStartProcess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var nPanetti by rememberSaveable { mutableFloatStateOf(4f) }
    var pesoPanetto by rememberSaveable { mutableFloatStateOf(250f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(63f) }
    var saleGPerL by rememberSaveable { mutableFloatStateOf(40f) }
    var grassiGPerL by rememberSaveable { mutableFloatStateOf(0f) }
    var maltoPct by rememberSaveable { mutableFloatStateOf(0f) }
    var oreLievitazione by rememberSaveable { mutableIntStateOf(24) }
    var temperatura by rememberSaveable { mutableFloatStateOf(22f) }
    var puntataMin by rememberSaveable { mutableFloatStateOf(120f) }
    var frigoH by rememberSaveable { mutableFloatStateOf(16f) }
    var aprettoMin by rememberSaveable { mutableFloatStateOf(330f) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }

    val oreLievOptions = listOf(8, 12, 24, 48)

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcAvanzato(
                    nPanetti = nPanetti.roundToInt(),
                    pesoPanetto = pesoPanetto.toDouble(),
                    idroPct = idratazione.toDouble(),
                    saleGPerL = saleGPerL.toDouble(),
                    grassiGPerL = grassiGPerL.toDouble(),
                    maltoPct = maltoPct.toDouble(),
                    liev = oreLievitazione,
                    frigoH = frigoH.roundToInt(),
                    puntataMin = puntataMin.roundToInt(),
                    aprettoMin = aprettoMin.roundToInt(),
                    T = temperatura.roundToInt()
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
            text = "Calcolatore Avanzato",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

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

        // Idratazione
        SliderInput(
            label = "Idratazione",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = 59f..75f,
            steps = 15,
            valueDisplay = "${idratazione.roundToInt()} %"
        )

        // Sale
        SliderInput(
            label = "Sale",
            value = saleGPerL,
            onValueChange = { saleGPerL = it },
            valueRange = 20f..60f,
            steps = 7,
            valueDisplay = "${saleGPerL.roundToInt()} g/L"
        )

        // Grassi
        SliderInput(
            label = "Grassi (olio)",
            value = grassiGPerL,
            onValueChange = { grassiGPerL = it },
            valueRange = 0f..50f,
            steps = 9,
            valueDisplay = "${grassiGPerL.roundToInt()} g/L"
        )

        // Malto
        SliderInput(
            label = "Malto",
            value = maltoPct,
            onValueChange = { maltoPct = it },
            valueRange = 0f..3f,
            steps = 5,
            valueDisplay = "${"%.1f".format(maltoPct)} %"
        )

        // Ore lievitazione (dropdown)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = "$oreLievitazione ore",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ore lievitazione totali") },
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
                    DropdownMenuItem(
                        text = { Text("$ore ore") },
                        onClick = {
                            oreLievitazione = ore
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Temperatura
        SliderInput(
            label = "Temperatura ambiente",
            value = temperatura,
            onValueChange = { temperatura = it },
            valueRange = 18f..30f,
            steps = 11,
            valueDisplay = "${temperatura.roundToInt()} °C"
        )

        // Puntata
        SliderInput(
            label = "Puntata",
            value = puntataMin,
            onValueChange = { puntataMin = it },
            valueRange = 30f..300f,
            steps = 8,
            valueDisplay = "${puntataMin.roundToInt()} min"
        )

        // Frigo
        SliderInput(
            label = "Frigo",
            value = frigoH,
            onValueChange = { frigoH = it },
            valueRange = 0f..48f,
            steps = 47,
            valueDisplay = "${frigoH.roundToInt()} h"
        )

        // Appretto
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
            AvanzatoResultCard(r)
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
private fun AvanzatoResultCard(r: RicettaAvanzata) {
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
            ResultRow("Olio", "${r.olio} g")
            ResultRow("Malto", "${r.malto} g")
            ResultRow("Lievito fresco", "${r.ldbf} g")
            ResultRow("Lievito secco", "${r.ldbs} g")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ResultRow("Totale", "${r.totale} g")
            ResultRow("Forza consigliata", "W${r.forza}")
            ResultRow("Temp. chiusura", "${r.tempChiusura} °C")
            ResultRow("Ciclo", r.ciclo)

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
