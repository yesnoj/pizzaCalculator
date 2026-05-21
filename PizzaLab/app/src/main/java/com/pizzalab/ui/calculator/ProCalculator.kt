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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaPro
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.theme.QuadernoColors
import kotlin.math.roundToInt

@Composable
fun ProCalculator() {
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
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Numero panetti
        QField(
            label = "Numero panetti",
            value = "${nPanetti.roundToInt()}",
            onMinus = { if (nPanetti > 1f) nPanetti -= 1f },
            onPlus = { if (nPanetti < 20f) nPanetti += 1f },
            dense = true
        )

        // Peso panetto
        QField(
            label = "Peso panetto",
            value = "${pesoPanetto.roundToInt()}",
            suffix = "g",
            onMinus = { if (pesoPanetto > 150f) pesoPanetto -= 10f },
            onPlus = { if (pesoPanetto < 500f) pesoPanetto += 10f },
            dense = true
        )

        // Idratazione % farina
        QField(
            label = "Idratazione % farina",
            value = "${idratazione.roundToInt()}",
            suffix = "%",
            onMinus = { if (idratazione > 50f) idratazione -= 1f },
            onPlus = { if (idratazione < 85f) idratazione += 1f },
            dense = true
        )

        // Sale % farina
        QField(
            label = "Sale % farina",
            value = "${"%.1f".format(salePctFarina)}",
            suffix = "%",
            onMinus = { if (salePctFarina > 0f) salePctFarina = (salePctFarina - 0.1f).coerceAtLeast(0f) },
            onPlus = { if (salePctFarina < 5f) salePctFarina = (salePctFarina + 0.1f).coerceAtMost(5f) },
            dense = true
        )

        // Grassi % farina
        QField(
            label = "Grassi % farina",
            value = "${"%.1f".format(grassiPctFarina)}",
            suffix = "%",
            onMinus = { if (grassiPctFarina > 0f) grassiPctFarina = (grassiPctFarina - 0.1f).coerceAtLeast(0f) },
            onPlus = { if (grassiPctFarina < 5f) grassiPctFarina = (grassiPctFarina + 0.1f).coerceAtMost(5f) },
            dense = true
        )

        // Lievito % farina
        QField(
            label = "Lievito % farina",
            value = "${"%.2f".format(lievitoPctFarina)}",
            suffix = "%",
            onMinus = { if (lievitoPctFarina > 0f) lievitoPctFarina = (lievitoPctFarina - 0.05f).coerceAtLeast(0f) },
            onPlus = { if (lievitoPctFarina < 2f) lievitoPctFarina = (lievitoPctFarina + 0.05f).coerceAtMost(2f) },
            dense = true
        )

        // Malto % farina
        QField(
            label = "Malto % farina",
            value = "${"%.1f".format(maltoPctFarina)}",
            suffix = "%",
            onMinus = { if (maltoPctFarina > 0f) maltoPctFarina = (maltoPctFarina - 0.1f).coerceAtLeast(0f) },
            onPlus = { if (maltoPctFarina < 3f) maltoPctFarina = (maltoPctFarina + 0.1f).coerceAtMost(3f) },
            dense = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        ricetta?.let { r ->
            ProResultCard(r)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // No process button — Pro mode: user manages timing
        Text(
            text = "nessuna timeline · gestisci tu tempi e fasi",
            style = TextStyle(
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink3,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProResultCard(r: RicettaPro) {
    QCard(
        kicker = "Ricetta",
        title = "Ingredienti"
    ) {
        QLeaderRow("Farina", "${r.farina}", unit = "g")
        QLeaderRow("Acqua", "${r.acqua}", unit = "g")
        QLeaderRow("Sale", "${r.sale}", unit = "g")
        QLeaderRow("Olio", "${r.olio}", unit = "g")
        QLeaderRow("Malto", "${r.malto}", unit = "g")
        QLeaderRow("Lievito fresco", "${r.ldbf}", unit = "g")
        QLeaderRow("Lievito secco", "${r.ldbs}", unit = "g")
        QLeaderRow("Totale", "${r.totale}", unit = "g", strong = true)
    }
}
