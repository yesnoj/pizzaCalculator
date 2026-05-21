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
import com.pizzalab.domain.RicettaPro
import kotlin.math.roundToInt

@Composable
fun ProCalculator(
    modifier: Modifier = Modifier
) {
    var nPanetti by rememberSaveable { mutableFloatStateOf(4f) }
    var pesoPanetto by rememberSaveable { mutableFloatStateOf(250f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(63f) }
    var salePctFarina by rememberSaveable { mutableFloatStateOf(2.5f) }
    var grassiPctFarina by rememberSaveable { mutableFloatStateOf(0f) }
    var lievitoPctFarina by rememberSaveable { mutableFloatStateOf(0.2f) }
    var maltoPctFarina by rememberSaveable { mutableFloatStateOf(0f) }

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcPro(
                    nPanetti = nPanetti.roundToInt(),
                    pesoPanetto = pesoPanetto.toDouble(),
                    idroPct = idratazione.toDouble(),
                    salePctFarina = salePctFarina.toDouble(),
                    grassiPctFarina = grassiPctFarina.toDouble(),
                    lievitoPctFarina = lievitoPctFarina.toDouble(),
                    maltoPctFarina = maltoPctFarina.toDouble()
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
            text = "Calcolatore PRO",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Numero panetti
        SliderInput(
            label = "Numero panetti",
            value = nPanetti,
            onValueChange = { nPanetti = it },
            valueRange = 1f..20f,
            steps = 18,
            valueDisplay = "${nPanetti.roundToInt()}"
        )

        // Peso panetto
        SliderInput(
            label = "Peso panetto",
            value = pesoPanetto,
            onValueChange = { pesoPanetto = it },
            valueRange = 150f..500f,
            steps = 34,
            valueDisplay = "${pesoPanetto.roundToInt()} g"
        )

        // Idratazione
        SliderInput(
            label = "Idratazione % farina",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = 50f..85f,
            steps = 34,
            valueDisplay = "${idratazione.roundToInt()} %"
        )

        // Sale % farina
        SliderInput(
            label = "Sale % farina",
            value = salePctFarina,
            onValueChange = { salePctFarina = it },
            valueRange = 0f..5f,
            steps = 49,
            valueDisplay = "${"%.1f".format(salePctFarina)} %"
        )

        // Grassi % farina
        SliderInput(
            label = "Grassi % farina",
            value = grassiPctFarina,
            onValueChange = { grassiPctFarina = it },
            valueRange = 0f..5f,
            steps = 49,
            valueDisplay = "${"%.1f".format(grassiPctFarina)} %"
        )

        // Lievito % farina
        SliderInput(
            label = "Lievito % farina",
            value = lievitoPctFarina,
            onValueChange = { lievitoPctFarina = it },
            valueRange = 0f..2f,
            steps = 39,
            valueDisplay = "${"%.2f".format(lievitoPctFarina)} %"
        )

        // Malto % farina
        SliderInput(
            label = "Malto % farina",
            value = maltoPctFarina,
            onValueChange = { maltoPctFarina = it },
            valueRange = 0f..3f,
            steps = 29,
            valueDisplay = "${"%.1f".format(maltoPctFarina)} %"
        )

        // Results
        ricetta?.let { r ->
            ProResultCard(r)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProResultCard(r: RicettaPro) {
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
        }
    }
}
