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
import androidx.compose.runtime.mutableIntStateOf
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
import com.pizzalab.domain.RicettaAvanzata
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.components.QPrimaryButton
import com.pizzalab.ui.theme.QuadernoColors
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun AvanzatoCalculator(onStartProcess: () -> Unit = {}) {
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

    val panettiPresets = listOf("2", "4", "6", "8")
    val orePresets = listOf("1", "2", "4", "6", "8", "12", "24", "48")

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
            onPlus = { if (nPanetti < 12f) nPanetti += 1f },
            dense = true,
            presets = panettiPresets,
            selectedPreset = "${nPanetti.roundToInt()}",
            onPresetSelect = { nPanetti = it.toFloat() }
        )

        // Peso panetto
        QField(
            label = "Peso panetto",
            value = "${pesoPanetto.roundToInt()}",
            suffix = "g",
            onMinus = { if (pesoPanetto > 180f) pesoPanetto -= 10f },
            onPlus = { if (pesoPanetto < 350f) pesoPanetto += 10f },
            dense = true
        )

        // Idratazione
        QField(
            label = "Idratazione",
            value = "${idratazione.roundToInt()}",
            suffix = "%",
            onMinus = { if (idratazione > 59f) idratazione -= 1f },
            onPlus = { if (idratazione < 75f) idratazione += 1f },
            dense = true
        )

        // Sale
        QField(
            label = "Sale",
            value = "${saleGPerL.roundToInt()}",
            suffix = "g/L",
            onMinus = { if (saleGPerL > 20f) saleGPerL -= 5f },
            onPlus = { if (saleGPerL < 60f) saleGPerL += 5f },
            dense = true
        )

        // Grassi
        QField(
            label = "Grassi (olio)",
            value = "${grassiGPerL.roundToInt()}",
            suffix = "g/L",
            onMinus = { if (grassiGPerL > 0f) grassiGPerL -= 5f },
            onPlus = { if (grassiGPerL < 50f) grassiGPerL += 5f },
            dense = true
        )

        // Malto
        QField(
            label = "Malto",
            value = "${"%.1f".format(maltoPct)}",
            suffix = "%",
            onMinus = { if (maltoPct > 0f) maltoPct = (maltoPct - 0.5f).coerceAtLeast(0f) },
            onPlus = { if (maltoPct < 3f) maltoPct = (maltoPct + 0.5f).coerceAtMost(3f) },
            dense = true
        )

        // Ore lievitazione
        QField(
            label = "Ore lievitazione totali",
            value = "$oreLievitazione",
            suffix = "h",
            onMinus = {
                val opts = listOf(1, 2, 4, 6, 8, 12, 24, 48)
                val idx = opts.indexOf(oreLievitazione)
                if (idx > 0) oreLievitazione = opts[idx - 1]
                else if (idx == -1 && oreLievitazione > 1) oreLievitazione = opts.last { it < oreLievitazione }
            },
            onPlus = {
                val opts = listOf(1, 2, 4, 6, 8, 12, 24, 48)
                val idx = opts.indexOf(oreLievitazione)
                if (idx < opts.lastIndex) oreLievitazione = opts[idx + 1]
                else if (idx == -1 && oreLievitazione < 48) oreLievitazione = opts.first { it > oreLievitazione }
            },
            dense = true,
            presets = orePresets,
            selectedPreset = "$oreLievitazione",
            onPresetSelect = { oreLievitazione = it.toInt() }
        )

        // Temperatura
        QField(
            label = "Temperatura ambiente",
            value = "${temperatura.roundToInt()}",
            suffix = "°C",
            onMinus = { if (temperatura > 18f) temperatura -= 1f },
            onPlus = { if (temperatura < 30f) temperatura += 1f },
            dense = true
        )

        // Puntata
        QField(
            label = "Puntata",
            value = "${puntataMin.roundToInt()}",
            suffix = "min",
            onMinus = { if (puntataMin > 30f) puntataMin -= 30f },
            onPlus = { if (puntataMin < 300f) puntataMin += 30f },
            dense = true
        )

        // Frigo
        QField(
            label = "Frigo",
            value = "${frigoH.roundToInt()}",
            suffix = "h",
            onMinus = { if (frigoH > 0f) frigoH -= 4f.coerceAtMost(frigoH) },
            onPlus = { if (frigoH < 48f) frigoH += 4f },
            dense = true
        )

        // Appretto
        QField(
            label = "Appretto",
            value = "${aprettoMin.roundToInt()}",
            suffix = "min",
            onMinus = { if (aprettoMin > 60f) aprettoMin -= 60f },
            onPlus = { if (aprettoMin < 600f) aprettoMin += 60f },
            dense = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Results
        ricetta?.let { r ->
            AvanzatoResultCard(r)
            Spacer(modifier = Modifier.height(8.dp))
            QPrimaryButton(
                text = "Avvia Processo",
                onClick = onStartProcess,
                modifier = Modifier.padding(horizontal = 22.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AvanzatoResultCard(r: RicettaAvanzata) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    QCard(
        kicker = "Ricetta",
        title = "Ingredienti"
    ) {
        // Recipe rows
        QLeaderRow("Farina", "${r.farina}", unit = "g")
        QLeaderRow("Acqua", "${r.acqua}", unit = "g")
        QLeaderRow("Sale", "${r.sale}", unit = "g")
        QLeaderRow("Olio", "${r.olio}", unit = "g")
        QLeaderRow("Malto", "${r.malto}", unit = "g")
        QLeaderRow("Lievito fresco", "${r.ldbf}", unit = "g")
        QLeaderRow("Lievito secco", "${r.ldbs}", unit = "g")
        QLeaderRow("Totale", "${r.totale}", unit = "g", strong = true)

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Footer: W consigliato and ciclo
        Text(
            text = "W consigliato: W${r.forza}",
            style = TextStyle(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = QuadernoColors.Olive,
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "Ciclo: ${r.ciclo}",
            style = TextStyle(
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink2,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        QLeaderRow("Temp. chiusura", "${r.tempChiusura}", unit = "°C")

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "TIMELINE",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = QuadernoColors.Primary,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        QLeaderRow("Puntata", PizzaFormulas.fmtMinutes(r.puntataMin))
        if (r.frigoMin > 0) {
            QLeaderRow("Frigo", PizzaFormulas.fmtMinutes(r.frigoMin))
        }
        QLeaderRow("Appretto", PizzaFormulas.fmtMinutes(r.aprettoMin))
        QLeaderRow("Inizio appretto", r.inizioApretto.format(timeFmt))
        QLeaderRow("Pronti", r.pronti.format(timeFmt), strong = true)
    }
}
