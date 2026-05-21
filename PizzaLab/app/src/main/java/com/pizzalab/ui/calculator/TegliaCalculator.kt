package com.pizzalab.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaTeglia
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.components.QSegmented
import com.pizzalab.ui.theme.QuadernoColors
import kotlin.math.roundToInt

@Composable
fun TegliaCalculator() {
    // Shape — driven by QSegmented labels
    val formaLabels = listOf("Rotonda", "Quadrata", "Rettangolare")
    var formaLabel by rememberSaveable { mutableStateOf("Rotonda") }
    val forma = when (formaLabel) {
        "Rotonda"      -> "rotonda"
        "Quadrata"     -> "quadrata"
        "Rettangolare" -> "rettangolare"
        else           -> "rotonda"
    }

    // Dimension fields
    var diametro by rememberSaveable { mutableStateOf("32") }
    var lato1 by rememberSaveable { mutableStateOf("40") }
    var lato2 by rememberSaveable { mutableStateOf("30") }

    // Numeric state — kept as Int/Float and exposed as strings to QField
    var nTeglia by rememberSaveable { mutableStateOf(1) }
    var idratazione by rememberSaveable { mutableStateOf(68) }
    var saleGPerL by rememberSaveable { mutableStateOf(40) }
    var grassiGPerL by rememberSaveable { mutableStateOf(30) }
    var oreLievitazione by rememberSaveable { mutableStateOf(24) }
    var temperatura by rememberSaveable { mutableStateOf(22) }
    var puntataMin by rememberSaveable { mutableStateOf(120) }
    var frigoH by rememberSaveable { mutableStateOf(16) }
    var aprettoMin by rememberSaveable { mutableStateOf(330) }

    val oreLievOptions = listOf("8", "12", "24", "48")
    val diametroPresets = listOf("26", "28", "30", "32", "36")

    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcTeglia(
                    forma = forma,
                    nTeglia = nTeglia,
                    idroPct = idratazione.toDouble(),
                    saleGPerL = saleGPerL.toDouble(),
                    grassiGPerL = grassiGPerL.toDouble(),
                    liev = oreLievitazione,
                    frigoH = frigoH,
                    puntataMin = puntataMin,
                    aprettoMin = aprettoMin,
                    T = temperatura,
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
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Shape selector
        QSegmented(
            items = formaLabels,
            selected = formaLabel,
            onSelect = { formaLabel = it },
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp)
        )

        // Dimension fields
        when (forma) {
            "rotonda" -> {
                QField(
                    label = "Diametro",
                    value = diametro,
                    suffix = "cm",
                    onMinus = {
                        val v = diametro.toIntOrNull() ?: 32
                        diametro = (v - 1).coerceAtLeast(1).toString()
                    },
                    onPlus = {
                        val v = diametro.toIntOrNull() ?: 32
                        diametro = (v + 1).toString()
                    },
                    presets = diametroPresets,
                    selectedPreset = diametro,
                    onPresetSelect = { diametro = it },
                )
            }
            "quadrata" -> {
                QField(
                    label = "Lato",
                    value = lato1,
                    suffix = "cm",
                    onMinus = {
                        val v = lato1.toIntOrNull() ?: 40
                        lato1 = (v - 1).coerceAtLeast(1).toString()
                    },
                    onPlus = {
                        val v = lato1.toIntOrNull() ?: 40
                        lato1 = (v + 1).toString()
                    },
                )
            }
            "rettangolare" -> {
                QField(
                    label = "Lato 1",
                    value = lato1,
                    suffix = "cm",
                    onMinus = {
                        val v = lato1.toIntOrNull() ?: 40
                        lato1 = (v - 1).coerceAtLeast(1).toString()
                    },
                    onPlus = {
                        val v = lato1.toIntOrNull() ?: 40
                        lato1 = (v + 1).toString()
                    },
                )
                QField(
                    label = "Lato 2",
                    value = lato2,
                    suffix = "cm",
                    onMinus = {
                        val v = lato2.toIntOrNull() ?: 30
                        lato2 = (v - 1).coerceAtLeast(1).toString()
                    },
                    onPlus = {
                        val v = lato2.toIntOrNull() ?: 30
                        lato2 = (v + 1).toString()
                    },
                )
            }
        }

        QField(
            label = "Numero teglie",
            value = nTeglia.toString(),
            onMinus = { nTeglia = (nTeglia - 1).coerceAtLeast(1) },
            onPlus = { nTeglia = (nTeglia + 1).coerceAtMost(6) },
            dense = true,
        )

        QField(
            label = "Idratazione",
            value = idratazione.toString(),
            suffix = "%",
            onMinus = { idratazione = (idratazione - 1).coerceAtLeast(60) },
            onPlus = { idratazione = (idratazione + 1).coerceAtMost(80) },
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
            onPlus = { grassiGPerL = (grassiGPerL + 5).coerceAtMost(80) },
            dense = true,
        )

        QField(
            label = "Ore lievitazione",
            value = oreLievitazione.toString(),
            suffix = "ore",
            onMinus = {
                val idx = oreLievOptions.indexOf(oreLievitazione.toString())
                if (idx > 0) oreLievitazione = oreLievOptions[idx - 1].toInt()
            },
            onPlus = {
                val idx = oreLievOptions.indexOf(oreLievitazione.toString())
                if (idx < oreLievOptions.lastIndex) oreLievitazione = oreLievOptions[idx + 1].toInt()
            },
            presets = oreLievOptions,
            selectedPreset = oreLievitazione.toString(),
            onPresetSelect = { oreLievitazione = it.toInt() },
        )

        QField(
            label = "Temperatura",
            value = temperatura.toString(),
            suffix = "°C",
            onMinus = { temperatura = (temperatura - 1).coerceAtLeast(18) },
            onPlus = { temperatura = (temperatura + 1).coerceAtMost(30) },
        )

        QField(
            label = "Puntata",
            value = puntataMin.toString(),
            suffix = "min",
            onMinus = { puntataMin = (puntataMin - 30).coerceAtLeast(30) },
            onPlus = { puntataMin = (puntataMin + 30).coerceAtMost(300) },
        )

        QField(
            label = "Frigo",
            value = frigoH.toString(),
            suffix = "h",
            onMinus = { frigoH = (frigoH - 4).coerceAtLeast(0) },
            onPlus = { frigoH = (frigoH + 4).coerceAtMost(48) },
        )

        QField(
            label = "Appretto",
            value = aprettoMin.toString(),
            suffix = "min",
            onMinus = { aprettoMin = (aprettoMin - 30).coerceAtLeast(60) },
            onPlus = { aprettoMin = (aprettoMin + 30).coerceAtMost(600) },
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Result card
        ricetta?.let { r ->
            TegliaResultCard(r)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TegliaResultCard(r: RicettaTeglia) {
    QCard(
        kicker = "Ricetta",
        title = "Teglia",
        titleSuffix = "${r.pesoPanetto} g",
    ) {
        QLeaderRow(label = "Area teglia", value = "${r.areaCm2}", unit = "cm²")
        QLeaderRow(label = "Farina", value = "${r.farina}", unit = "g")
        QLeaderRow(label = "Acqua", value = "${r.acqua}", unit = "g")
        QLeaderRow(label = "Sale", value = "${r.sale}", unit = "g")
        QLeaderRow(label = "Olio", value = "${r.olio}", unit = "g")
        QLeaderRow(label = "Lievito fresco", value = "${r.ldbf}", unit = "g")
        QLeaderRow(label = "Lievito secco", value = "${r.ldbs}", unit = "g")

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        QLeaderRow(label = "Totale", value = "${r.totale}", unit = "g", strong = true)

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Footer: forza consigliata in olive
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "forza consigliata",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = QuadernoColors.Olive,
                ),
            )
            Text(
                text = "W ${r.forza}",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuadernoColors.Olive,
                ),
            )
        }
    }
}
