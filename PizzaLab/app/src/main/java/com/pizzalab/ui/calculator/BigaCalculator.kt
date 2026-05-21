package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaBiga
import kotlin.math.roundToInt

@Composable
fun BigaCalculator(modifier: Modifier = Modifier) {
    var nPanetti by rememberSaveable { mutableFloatStateOf(4f) }
    var pesoPanetto by rememberSaveable { mutableFloatStateOf(250f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(65f) }
    var saleGPerL by rememberSaveable { mutableFloatStateOf(40f) }
    var grassiGPerL by rememberSaveable { mutableFloatStateOf(0f) }
    var temperatura by rememberSaveable { mutableFloatStateOf(20f) }
    var bigaPct by rememberSaveable { mutableFloatStateOf(50f) }

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcBiga(
                    nPanetti = nPanetti.roundToInt(),
                    pesoPanetto = pesoPanetto.toDouble(),
                    idroPct = idratazione.toDouble(),
                    saleGPerL = saleGPerL.toDouble(),
                    grassiGPerL = grassiGPerL.toDouble(),
                    ta = temperatura.roundToInt(),
                    bigaPct = bigaPct.toDouble()
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
            "Calcolatore Biga",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        SliderInput(
            label = "Numero panetti",
            value = nPanetti,
            onValueChange = { nPanetti = it },
            valueRange = 1f..12f,
            steps = 10,
            valueDisplay = "${nPanetti.roundToInt()}"
        )

        SliderInput(
            label = "Peso panetto",
            value = pesoPanetto,
            onValueChange = { pesoPanetto = it },
            valueRange = 180f..350f,
            steps = 16,
            valueDisplay = "${pesoPanetto.roundToInt()} g"
        )

        SliderInput(
            label = "Idratazione totale",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = 55f..75f,
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
            valueRange = 0f..50f,
            steps = 9,
            valueDisplay = "${grassiGPerL.roundToInt()} g/L"
        )

        SliderInput(
            label = "Temperatura ambiente",
            value = temperatura,
            onValueChange = { temperatura = it },
            valueRange = 17f..28f,
            steps = 10,
            valueDisplay = "${temperatura.roundToInt()} °C"
        )

        SliderInput(
            label = "Biga %",
            value = bigaPct,
            onValueChange = { bigaPct = it },
            valueRange = 10f..70f,
            steps = 11,
            valueDisplay = "${bigaPct.roundToInt()} %"
        )

        // Results
        ricetta?.let { r ->
            BigaResultCard(r)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BigaResultCard(r: RicettaBiga) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Impasto Totale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ResultRow("Farina totale", "${r.farinaTot} g")
            ResultRow("Acqua totale", "${r.acquaTot} g")
            ResultRow("Sale totale", "${r.saleTot} g")
            ResultRow("Grassi totale", "${r.grassiTot} g")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Biga",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ResultRow("Farina biga", "${r.farinaBiga} g")
            ResultRow("Acqua biga", "${r.acquaBiga} g")
            ResultRow("Lievito biga", "${r.lievitoBiga} g")
            ResultRow("Idratazione biga", "${r.idroBigaPct} %")
            ResultRow("Forza biga", "W${r.forzaBiga}")
            ResultRow("Ore maturazione", r.oreBigaStr)

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Rinfresco",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ResultRow("Farina rinfresco", "${r.farinaRinfresco} g")
            ResultRow("Acqua rinfresco", "${r.acquaRinfresco} g")
            ResultRow("Sale rinfresco", "${r.saleRinfresco} g")
            ResultRow("Grassi rinfresco", "${r.grassiRinfresco} g")
            ResultRow("Forza rinfresco", "W${r.forzaRinfresco}")
        }
    }
}
