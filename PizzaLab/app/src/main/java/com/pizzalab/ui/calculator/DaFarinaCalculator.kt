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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import kotlin.math.roundToInt

@Composable
fun DaFarinaCalculator(onStartProcess: () -> Unit = {}, onModeChange: () -> Unit = {}) {
    // ── State ───────────────────────────────────────────────────
    var farina by rememberSaveable { mutableIntStateOf(500) }
    var oreLievitazione by rememberSaveable { mutableIntStateOf(24) }
    var temperatura by rememberSaveable { mutableIntStateOf(22) }
    var idratazione by rememberSaveable { mutableIntStateOf(63) }

    // Scelta panetti: "Fisso peso" / "Fisso numero"
    var subMode by rememberSaveable { mutableStateOf("Fisso peso") }
    var pesoPanetto by rememberSaveable { mutableIntStateOf(250) }
    var nPanetti by rememberSaveable { mutableIntStateOf(4) }

    val isRapido = oreLievitazione <= 6

    // ── Derived: totale impasto ─────────────────────────────────
    val totaleImpasto by remember {
        derivedStateOf {
            try {
                PizzaFormulas.totaleDaFarina(
                    farinaG = farina.toDouble(),
                    idroPct = idratazione.toDouble(),
                    lievOre = oreLievitazione,
                    T = temperatura
                )
            } catch (_: Exception) {
                farina.toDouble() * 1.65
            }
        }
    }

    // ── Derived: limits ─────────────────────────────────────────
    val maxPesoPanetto by remember {
        derivedStateOf { totaleImpasto.toInt().coerceIn(181, 500) }
    }
    val maxNPanetti by remember {
        derivedStateOf { (totaleImpasto / 180.0).toInt().coerceIn(1, 20) }
    }

    // Clamp when limits change
    if (pesoPanetto > maxPesoPanetto) pesoPanetto = maxPesoPanetto
    if (pesoPanetto < 180) pesoPanetto = 180
    if (nPanetti > maxNPanetti) nPanetti = maxNPanetti
    if (nPanetti < 1) nPanetti = 1

    // ── Derived: panetti info ───────────────────────────────────
    val panettiInfo by remember {
        derivedStateOf {
            try {
                if (subMode == "Fisso peso") {
                    val n = (totaleImpasto / pesoPanetto).roundToInt().coerceAtLeast(1)
                    Pair(n, pesoPanetto.toDouble())
                } else {
                    val n = nPanetti.coerceAtLeast(1)
                    val peso = totaleImpasto / n
                    Pair(n, peso)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    // ── Derived: recipe ─────────────────────────────────────────
    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcDaFarina(
                    farinaG = farina.toDouble(),
                    idroPct = idratazione.toDouble(),
                    lievOre = oreLievitazione,
                    T = temperatura
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    // ── Temperature and hydration bounds (depend on isRapido) ───
    val tempMax = if (isRapido) 35 else 30
    val idrMin = if (isRapido) 55 else 59
    val idrMax = if (isRapido) 80 else 70

    // Clamp when mode changes
    if (temperatura > tempMax) temperatura = tempMax
    if (idratazione < idrMin) idratazione = idrMin
    if (idratazione > idrMax) idratazione = idrMax

    // ── Ore presets ─────────────────────────────────────────────
    val orePresets = listOf("1", "2", "4", "6", "8", "12", "24", "48")

    // ── UI ──────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Mode toggle: Per Panetti / Da Farina
        QSegmented(
            items = listOf("Per Panetti", "Da Farina"),
            selected = "Da Farina",
            onSelect = { if (it == "Per Panetti") onModeChange() },
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
        )

        // Sub-mode toggle: Fisso peso / Fisso numero
        QSegmented(
            items = listOf("Fisso peso", "Fisso numero"),
            selected = subMode,
            onSelect = { subMode = it },
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ── Farina ──────────────────────────────────────────────
        QField(
            label = "Farina",
            value = farina.toString(),
            suffix = "g",
            onMinus = { if (farina > 200) farina = (farina - 50).coerceAtLeast(200) },
            onPlus = { farina = (farina + 50).coerceAtMost(2000) },
            presets = listOf("300", "500", "1000", "1500"),
            selectedPreset = if (farina in listOf(300, 500, 1000, 1500)) farina.toString() else null,
            onPresetSelect = { farina = it.toInt() }
        )

        // ── Peso panetto or Numero panetti ───────────────────────
        if (subMode == "Fisso peso") {
            QField(
                label = "Peso panetto",
                value = pesoPanetto.toString(),
                suffix = "g",
                onMinus = { if (pesoPanetto > 180) pesoPanetto = (pesoPanetto - 10).coerceAtLeast(180) },
                onPlus = { pesoPanetto = (pesoPanetto + 10).coerceAtMost(maxPesoPanetto) },
                presets = listOf("180", "220", "250", "280"),
                selectedPreset = if (pesoPanetto in listOf(180, 220, 250, 280)) pesoPanetto.toString() else null,
                onPresetSelect = { pesoPanetto = it.toInt().coerceIn(180, maxPesoPanetto) }
            )
            panettiInfo?.let { (n, _) ->
                QField(
                    label = "Panetti risultanti",
                    value = n.toString(),
                    onMinus = {},
                    onPlus = {},
                    dense = true
                )
            }
        } else {
            QField(
                label = "Numero panetti",
                value = nPanetti.toString(),
                onMinus = { if (nPanetti > 1) nPanetti-- },
                onPlus = { nPanetti = (nPanetti + 1).coerceAtMost(maxNPanetti) },
            )
            panettiInfo?.let { (_, peso) ->
                QField(
                    label = "Peso panetto",
                    value = peso.roundToInt().toString(),
                    suffix = "g",
                    onMinus = {},
                    onPlus = {},
                    dense = true
                )
            }
        }

        // ── Ore lievitazione ────────────────────────────────────
        QField(
            label = "Ore lievitazione",
            value = oreLievitazione.toString(),
            suffix = if (isRapido) "ore (rapido)" else "ore",
            onMinus = {
                val opts = listOf(1, 2, 4, 6, 8, 12, 24, 48)
                val idx = opts.indexOf(oreLievitazione)
                if (idx > 0) oreLievitazione = opts[idx - 1]
                else if (idx == -1) oreLievitazione = opts.last { it < oreLievitazione }
            },
            onPlus = {
                val opts = listOf(1, 2, 4, 6, 8, 12, 24, 48)
                val idx = opts.indexOf(oreLievitazione)
                if (idx < opts.lastIndex) oreLievitazione = opts[idx + 1]
                else if (idx == -1) oreLievitazione = opts.first { it > oreLievitazione }
            },
            presets = orePresets,
            selectedPreset = if (oreLievitazione in listOf(1, 2, 4, 6, 8, 12, 24, 48)) oreLievitazione.toString() else null,
            onPresetSelect = { oreLievitazione = it.toInt() }
        )

        // ── Temperatura ambiente ─────────────────────────────────
        QField(
            label = "Temperatura",
            value = temperatura.toString(),
            suffix = "°C",
            onMinus = { if (temperatura > 18) temperatura-- },
            onPlus = { if (temperatura < tempMax) temperatura++ }
        )

        // ── Idratazione ──────────────────────────────────────────
        QField(
            label = "Idratazione",
            value = idratazione.toString(),
            suffix = "%",
            onMinus = { if (idratazione > idrMin) idratazione-- },
            onPlus = { if (idratazione < idrMax) idratazione++ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── Result card + CTA ────────────────────────────────────
        ricetta?.let { r ->
            DaFarinaResultCard(r, panettiInfo)

            QPrimaryButton(
                text = "Avvia Processo",
                onClick = onStartProcess,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DaFarinaResultCard(r: RicettaFacile, panettiInfo: Pair<Int, Double>?) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    QCard(
        kicker = "Ricetta",
        title = panettiInfo?.let { (n, peso) -> "$n × ${peso.roundToInt()} g" }
    ) {
        // ── Ingredienti ──────────────────────────────────────────
        panettiInfo?.let {
            DashedDivider(modifier = Modifier.padding(vertical = 6.dp))
        }

        QLeaderRow(label = "Farina", value = "${r.farina} g")
        QLeaderRow(label = "Acqua", value = "${r.acqua} g")
        QLeaderRow(label = "Sale", value = "${r.sale} g")
        QLeaderRow(label = "Lievito fresco", value = "${r.ldbf} g")
        QLeaderRow(label = "Lievito secco", value = "${r.ldbs} g")

        DashedDivider(modifier = Modifier.padding(vertical = 6.dp))

        QLeaderRow(label = "Totale", value = "${r.totale} g", strong = true)
        QLeaderRow(label = "Forza consigliata", value = "W${r.forza}")
        QLeaderRow(label = "Temp. chiusura", value = "${r.tempChiusura} °C")

        DashedDivider(modifier = Modifier.padding(vertical = 6.dp))

        // ── Timeline ─────────────────────────────────────────────
        QLeaderRow(label = "Puntata", value = PizzaFormulas.fmtMinutes(r.puntataMin))
        if (r.frigoMin > 0) {
            QLeaderRow(label = "Frigo", value = PizzaFormulas.fmtMinutes(r.frigoMin))
        }
        QLeaderRow(label = "Appretto", value = PizzaFormulas.fmtMinutes(r.aprettoMin))
        QLeaderRow(label = "Inizio appretto", value = r.inizioApretto.format(timeFmt))
        QLeaderRow(label = "Pronti", value = r.pronti.format(timeFmt), strong = true)
    }
}
