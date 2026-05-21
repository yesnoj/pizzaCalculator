package com.pizzalab.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaBiga
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.theme.QuadernoColors
import kotlin.math.roundToInt

@Composable
fun BigaCalculator() {
    var nPanetti by rememberSaveable { mutableStateOf(4) }
    var pesoPanetto by rememberSaveable { mutableStateOf(250) }
    var idratazione by rememberSaveable { mutableStateOf(65) }
    var saleGPerL by rememberSaveable { mutableStateOf(40) }
    var grassiGPerL by rememberSaveable { mutableStateOf(0) }
    var temperatura by rememberSaveable { mutableStateOf(20) }
    var bigaPct by rememberSaveable { mutableStateOf(50) }

    val nPanettiPresets = listOf("2", "4", "6", "8")
    val bigaPctPresets = listOf("30", "50", "70")

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcBiga(
                    nPanetti = nPanetti,
                    pesoPanetto = pesoPanetto.toDouble(),
                    idroPct = idratazione.toDouble(),
                    saleGPerL = saleGPerL.toDouble(),
                    grassiGPerL = grassiGPerL.toDouble(),
                    ta = temperatura,
                    bigaPct = bigaPct.toDouble()
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        QField(
            label = "Numero panetti",
            value = nPanetti.toString(),
            onMinus = { nPanetti = (nPanetti - 1).coerceAtLeast(1) },
            onPlus = { nPanetti = (nPanetti + 1).coerceAtMost(12) },
            presets = nPanettiPresets,
            selectedPreset = nPanetti.toString(),
            onPresetSelect = { nPanetti = it.toInt() },
        )

        QField(
            label = "Peso panetto",
            value = pesoPanetto.toString(),
            suffix = "g",
            onMinus = { pesoPanetto = (pesoPanetto - 10).coerceAtLeast(180) },
            onPlus = { pesoPanetto = (pesoPanetto + 10).coerceAtMost(350) },
        )

        QField(
            label = "Idratazione totale",
            value = idratazione.toString(),
            suffix = "%",
            onMinus = { idratazione = (idratazione - 1).coerceAtLeast(55) },
            onPlus = { idratazione = (idratazione + 1).coerceAtMost(75) },
            dense = true,
        )

        QField(
            label = "Sale",
            value = saleGPerL.toString(),
            suffix = "g/L",
            onMinus = { saleGPerL = (saleGPerL - 5).coerceAtLeast(20) },
            onPlus = { saleGPerL = (saleGPerL + 5).coerceAtMost(60) },
            dense = true,
        )

        QField(
            label = "Grassi (olio)",
            value = grassiGPerL.toString(),
            suffix = "g/L",
            onMinus = { grassiGPerL = (grassiGPerL - 5).coerceAtLeast(0) },
            onPlus = { grassiGPerL = (grassiGPerL + 5).coerceAtMost(50) },
            dense = true,
        )

        QField(
            label = "Temperatura",
            value = temperatura.toString(),
            suffix = "°C",
            onMinus = { temperatura = (temperatura - 1).coerceAtLeast(17) },
            onPlus = { temperatura = (temperatura + 1).coerceAtMost(28) },
        )

        QField(
            label = "Biga %",
            value = bigaPct.toString(),
            suffix = "%",
            onMinus = { bigaPct = (bigaPct - 10).coerceAtLeast(10) },
            onPlus = { bigaPct = (bigaPct + 10).coerceAtMost(70) },
            presets = bigaPctPresets,
            selectedPreset = bigaPct.toString(),
            onPresetSelect = { bigaPct = it.toInt() },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Result cards
        ricetta?.let { r ->
            BigaCardBiga(r)
            BigaCardRinfresco(r)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BigaCardBiga(r: RicettaBiga) {
    QCard(
        kicker = "Pre-fermento",
        title = "Biga",
        accent = QuadernoColors.Olive,
    ) {
        QLeaderRow(label = "Farina biga", value = "${r.farinaBiga}", unit = "g")
        QLeaderRow(label = "Acqua biga", value = "${r.acquaBiga}", unit = "g")
        QLeaderRow(label = "Lievito biga", value = "${r.lievitoBiga}", unit = "g")

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        QLeaderRow(label = "Idratazione biga", value = "${r.idroBigaPct}", unit = "%")
        QLeaderRow(label = "Forza biga", value = "W ${r.forzaBiga}", strong = true)
        QLeaderRow(label = "Ore maturazione", value = r.oreBigaStr)
    }
}

@Composable
private fun BigaCardRinfresco(r: RicettaBiga) {
    QCard(
        kicker = "Impasto finale",
        title = "Rinfresco",
    ) {
        QLeaderRow(label = "Farina totale", value = "${r.farinaTot}", unit = "g")
        QLeaderRow(label = "Acqua totale", value = "${r.acquaTot}", unit = "g")
        QLeaderRow(label = "Sale totale", value = "${r.saleTot}", unit = "g")
        QLeaderRow(label = "Grassi totale", value = "${r.grassiTot}", unit = "g")

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        QLeaderRow(label = "Farina rinfresco", value = "${r.farinaRinfresco}", unit = "g")
        QLeaderRow(label = "Acqua rinfresco", value = "${r.acquaRinfresco}", unit = "g")
        QLeaderRow(label = "Sale rinfresco", value = "${r.saleRinfresco}", unit = "g")
        QLeaderRow(label = "Grassi rinfresco", value = "${r.grassiRinfresco}", unit = "g")

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        QLeaderRow(label = "Forza rinfresco", value = "W ${r.forzaRinfresco}", strong = true)
    }
}
