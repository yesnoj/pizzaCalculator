package com.pizzalab.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaFacile
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.components.QPrimaryButton
import com.pizzalab.ui.components.QSegmented
import com.pizzalab.ui.theme.QuadernoColors
import java.time.format.DateTimeFormatter

// ── Preset lists ────────────────────────────────────────────────
private val PRESET_PANETTI     = listOf("2", "4", "6", "8")
private val PRESET_PESO        = listOf("180", "220", "250", "280")
private val PRESET_ORE         = listOf("1", "2", "4", "6", "8", "12", "24", "48")
private val PRESET_IDRATAZIONE = listOf("60", "63", "65", "70")

private val MODE_PANETTI = "Per Panetti"
private val MODE_FARINA  = "Da Farina"

@Composable
fun FacileCalculator(onStartProcess: () -> Unit = {}, onModeChange: () -> Unit = {}) {
    // ── State ──────────────────────────────────────────────────
    var nPanetti     by rememberSaveable { mutableIntStateOf(4) }
    var pesoPanetto  by rememberSaveable { mutableIntStateOf(250) }
    var oreLiev      by rememberSaveable { mutableIntStateOf(24) }
    var temperatura  by rememberSaveable { mutableIntStateOf(22) }
    var idratazione  by rememberSaveable { mutableIntStateOf(63) }
    var modeSelected by rememberSaveable { mutableStateOf(MODE_PANETTI) }

    // ── Derived recipe ─────────────────────────────────────────
    val ricetta by remember {
        derivedStateOf {
            try {
                if (oreLiev <= 6) {
                    PizzaFormulas.calcRapido(
                        nPanetti    = nPanetti,
                        pesoPanetto = pesoPanetto.toDouble(),
                        lievOre     = oreLiev,
                        T           = temperatura,
                        idroPct     = idratazione.toDouble()
                    )
                } else {
                    PizzaFormulas.calcFacile(
                        nPanetti    = nPanetti,
                        pesoPanetto = pesoPanetto.toDouble(),
                        liev        = oreLiev,
                        T           = temperatura,
                        idroPct     = idratazione.toDouble()
                    )
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // ── Mode toggle ────────────────────────────────────────
        QSegmented(
            items    = listOf(MODE_PANETTI, MODE_FARINA),
            selected = modeSelected,
            onSelect = { item ->
                if (item == MODE_FARINA) {
                    onModeChange()
                } else {
                    modeSelected = item
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Numero panetti ─────────────────────────────────────
        QField(
            label    = "Numero panetti",
            value    = "$nPanetti",
            onMinus  = { if (nPanetti > 1) nPanetti-- },
            onPlus   = { if (nPanetti < 20) nPanetti++ },
            presets  = PRESET_PANETTI,
            selectedPreset = "$nPanetti",
            onPresetSelect = { nPanetti = it.toIntOrNull() ?: nPanetti }
        )

        // ── Peso panetto ───────────────────────────────────────
        QField(
            label    = "Peso panetto",
            value    = "$pesoPanetto",
            suffix   = "g",
            onMinus  = { if (pesoPanetto > 150) pesoPanetto -= 5 },
            onPlus   = { if (pesoPanetto < 400) pesoPanetto += 5 },
            presets  = PRESET_PESO,
            selectedPreset = "$pesoPanetto",
            onPresetSelect = { pesoPanetto = it.toIntOrNull() ?: pesoPanetto }
        )

        // ── Ore lievitazione ───────────────────────────────────
        QField(
            label    = "Ore lievitazione",
            value    = "$oreLiev",
            suffix   = "h",
            hint     = if (oreLiev <= 6) "(rapido)" else null,
            onMinus  = { if (oreLiev > 1) oreLiev-- },
            onPlus   = { if (oreLiev < 72) oreLiev++ },
            presets  = PRESET_ORE,
            selectedPreset = "$oreLiev",
            onPresetSelect = { oreLiev = it.toIntOrNull() ?: oreLiev }
        )

        // ── Temperatura ambiente ───────────────────────────────
        QField(
            label    = "Temperatura ambiente",
            value    = "$temperatura",
            suffix   = "°C",
            onMinus  = { if (temperatura > 15) temperatura-- },
            onPlus   = { if (temperatura < 40) temperatura++ }
        )

        // ── Idratazione ────────────────────────────────────────
        QField(
            label    = "Idratazione",
            value    = "$idratazione",
            suffix   = "%",
            onMinus  = { if (idratazione > 50) idratazione-- },
            onPlus   = { if (idratazione < 85) idratazione++ },
            presets  = PRESET_IDRATAZIONE,
            selectedPreset = "$idratazione",
            onPresetSelect = { idratazione = it.toIntOrNull() ?: idratazione }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Result card ────────────────────────────────────────
        ricetta?.let { r ->
            FacileResultCard(r)

            Spacer(modifier = Modifier.height(4.dp))

            // ── Action button ──────────────────────────────────
            QPrimaryButton(
                text    = "Avvia Processo",
                onClick = onStartProcess,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Result card ────────────────────────────────────────────────
@Composable
private fun FacileResultCard(r: RicettaFacile) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    QCard(
        kicker = "Ricetta",
        title  = "${r.farina.toInt()} g",
        titleSuffix = "farina"
    ) {
        // Ingredienti
        QLeaderRow("Farina",        "${r.farina.toInt()} g")
        QLeaderRow("Acqua",         "${r.acqua.toInt()} g")
        QLeaderRow("Sale",          "${r.sale.toInt()} g")
        QLeaderRow("Lievito fresco","${r.ldbf} g")
        QLeaderRow("Lievito secco", "${r.ldbs} g")

        DashedDivider(modifier = Modifier.padding(vertical = 6.dp))

        // Totali
        QLeaderRow("Totale",            "${r.totale.toInt()} g", strong = true)
        QLeaderRow("Temp. chiusura",    "${r.tempChiusura} °C")

        DashedDivider(modifier = Modifier.padding(vertical = 6.dp))

        // Timeline
        QLeaderRow("Puntata",           PizzaFormulas.fmtMinutes(r.puntataMin))
        if (r.frigoMin > 0) {
            QLeaderRow("Frigo",         PizzaFormulas.fmtMinutes(r.frigoMin))
        }
        QLeaderRow("Appretto",          PizzaFormulas.fmtMinutes(r.aprettoMin))
        QLeaderRow("Inizio appretto",   r.inizioApretto.format(timeFmt))

        DashedDivider(modifier = Modifier.padding(vertical = 6.dp))

        // Footer summary
        Text(
            text = "W consigliata  W${r.forza}",
            style = TextStyle(
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontStyle  = FontStyle.Italic,
                color      = QuadernoColors.Olive,
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text  = "pronti alle",
            style = TextStyle(
                fontSize   = 11.sp,
                fontWeight = FontWeight.Medium,
                color      = QuadernoColors.Ink2,
            )
        )
        Text(
            text  = r.pronti.format(timeFmt),
            style = TextStyle(
                fontSize      = 38.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = QuadernoColors.Primary,
                letterSpacing = (-1.5).sp,
            )
        )
    }
}
