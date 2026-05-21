package com.pizzalab.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QChipRow
import com.pizzalab.ui.components.QField
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.theme.QuadernoColors
import kotlin.math.roundToInt

// ============================================================
// FORZA DA PROTEINE
// ============================================================

@Composable
fun ForzaProteineCalculator() {
    var proteineStr by rememberSaveable { mutableStateOf("12.0") }

    val proteineVal = proteineStr.toDoubleOrNull() ?: 12.0

    val forza by remember {
        derivedStateOf {
            try {
                PizzaFormulas.forzaDaProteine(proteineStr.toDoubleOrNull() ?: 12.0)
            } catch (_: Exception) {
                null
            }
        }
    }

    val wDescription: String = forza?.let { w ->
        when {
            w < 130  -> "Farina debolissima — solo pasta fresca o grissini"
            w < 200  -> "Farina debole — focaccia, pane rustico, teglie veloci"
            w < 280  -> "Farina media — pane comune, pizza con maturazione breve"
            w < 360  -> "Farina forte — pizza napoletana, biga, poolish"
            else     -> "Farina molto forte — grandi lievitazioni, brioche, panettone"
        }
    } ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            Text(
                text = "W DA PROTEINE",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Forza da Proteine",
                style = MaterialTheme.typography.headlineSmall,
                color = QuadernoColors.Ink,
            )
        }

        DashedDivider()

        QField(
            label = "Proteine",
            value = "%.1f".format(proteineVal),
            suffix = "%",
            onMinus = {
                val current = proteineStr.toDoubleOrNull() ?: 12.0
                val next = (current - 0.1).coerceIn(7.0, 17.0)
                proteineStr = "%.1f".format(next)
            },
            onPlus = {
                val current = proteineStr.toDoubleOrNull() ?: 12.0
                val next = (current + 0.1).coerceIn(7.0, 17.0)
                proteineStr = "%.1f".format(next)
            },
            hint = "(7.0 – 17.0)",
        )

        forza?.let { w ->
            QCard(
                kicker = "W stimato",
                title = "W $w",
                accent = QuadernoColors.Olive,
            ) {
                // no inner rows needed — description is below the card
            }

            if (wDescription.isNotEmpty()) {
                Text(
                    text = wDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuadernoColors.Ink2,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                )
            }
        }

        Text(
            text = "formula: W ≈ proteine × 27.5 − 60",
            style = TextStyle(
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink3,
            ),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ============================================================
// TEMPERATURE FORNO
// ============================================================

enum class TipoPizza(val label: String) {
    NAPOLETANA("Napoletana"),
    CONTEMPORANEA("Contemporanea"),
    TEGLIA("Teglia"),
    PALA("Pala")
}

enum class TipoForno(val label: String) {
    LEGNA("Legna"),
    GAS("Gas"),
    ELETTRICO("Elettrico"),
    DOMESTICO("Domestico")
}

private data class TemperatureResult(
    val cieloMin: Int, val cieloMax: Int,
    val plateaMin: Int, val plateaMax: Int,
    val tempoMin: String, val tempoMax: String,
    val consigli: List<String>
)

private fun calcolaTemperature(
    tipoPizza: TipoPizza,
    tipoForno: TipoForno,
    idratazione: Int,
    pesoPanetto: Int,
    forzaW: Int
): TemperatureResult {
    var cieloMin: Int
    var cieloMax: Int
    var plateaMin: Int
    var plateaMax: Int
    var tempoMin: String
    var tempoMax: String

    when (tipoPizza) {
        TipoPizza.NAPOLETANA -> {
            cieloMin = 430; cieloMax = 480
            plateaMin = 380; plateaMax = 430
            tempoMin = "60 sec"; tempoMax = "90 sec"
        }
        TipoPizza.CONTEMPORANEA -> {
            cieloMin = 400; cieloMax = 450
            plateaMin = 350; plateaMax = 400
            tempoMin = "90 sec"; tempoMax = "2:30 min"
        }
        TipoPizza.TEGLIA -> {
            cieloMin = 250; cieloMax = 300
            plateaMin = 280; plateaMax = 320
            tempoMin = "10 min"; tempoMax = "20 min"
        }
        TipoPizza.PALA -> {
            cieloMin = 300; cieloMax = 350
            plateaMin = 320; plateaMax = 370
            tempoMin = "5 min"; tempoMax = "8 min"
        }
    }

    when (tipoForno) {
        TipoForno.DOMESTICO -> {
            cieloMin = minOf(cieloMin, 230)
            cieloMax = minOf(cieloMax, 280)
            plateaMin = minOf(plateaMin, 220)
            plateaMax = minOf(plateaMax, 270)
            when (tipoPizza) {
                TipoPizza.NAPOLETANA, TipoPizza.CONTEMPORANEA -> {
                    tempoMin = "5 min"; tempoMax = "8 min"
                }
                else -> {}
            }
        }
        TipoForno.ELETTRICO -> {
            cieloMin = minOf(cieloMin, 350)
            cieloMax = minOf(cieloMax, 400)
            plateaMin = minOf(plateaMin, 330)
            plateaMax = minOf(plateaMax, 390)
        }
        TipoForno.GAS -> {
            cieloMin = minOf(cieloMin, 400)
            cieloMax = minOf(cieloMax, 450)
            plateaMin = minOf(plateaMin, 370)
            plateaMax = minOf(plateaMax, 420)
        }
        TipoForno.LEGNA -> { /* massime temperature, nessun limite */ }
    }

    if (idratazione > 70 && tipoPizza != TipoPizza.TEGLIA && tipoPizza != TipoPizza.PALA) {
        val boost = ((idratazione - 70) * 1.5).toInt()
        cieloMin += boost
        cieloMax += boost
    }

    if (forzaW > 300) {
        val boost = ((forzaW - 300) * 0.1).toInt()
        cieloMin += boost
        cieloMax += boost
        plateaMin += boost
        plateaMax += boost
    }

    val consigli = mutableListOf<String>()
    if (idratazione >= 75) {
        consigli.add("Alta idratazione: serve cielo forte per asciugare la superficie")
    }
    if (pesoPanetto > 300) {
        consigli.add("Panetto grande: aumenta leggermente il tempo di cottura")
    }
    if (pesoPanetto < 200 && tipoPizza != TipoPizza.TEGLIA) {
        consigli.add("Panetto piccolo: riduci il tempo per evitare di seccare")
    }
    if (forzaW > 350) {
        consigli.add("Farina molto forte (W $forzaW): tollera bene alte temperature")
    }
    if (forzaW < 200 && (tipoPizza == TipoPizza.NAPOLETANA || tipoPizza == TipoPizza.CONTEMPORANEA)) {
        consigli.add("Farina debole (W $forzaW): attenzione, rischio bruciatura rapida")
    }
    if (tipoForno == TipoForno.DOMESTICO) {
        consigli.add("Forno domestico: usa una pietra refrattaria preriscaldata per migliorare la platea")
    }
    consigli.add("Base brucia / sopra crudo: riduci platea, aumenta cielo")
    consigli.add("Sopra brucia / base pallida: riduci cielo, aumenta platea")

    return TemperatureResult(
        cieloMin = cieloMin, cieloMax = cieloMax,
        plateaMin = plateaMin, plateaMax = plateaMax,
        tempoMin = tempoMin, tempoMax = tempoMax,
        consigli = consigli
    )
}

@Composable
fun TemperatureFornoCalculator() {
    var tipoPizzaLabel by rememberSaveable { mutableStateOf(TipoPizza.NAPOLETANA.label) }
    var tipoFornoLabel by rememberSaveable { mutableStateOf(TipoForno.LEGNA.label) }
    var idratazioneStr by rememberSaveable { mutableStateOf("68") }
    var pesoPanettoStr by rememberSaveable { mutableStateOf("250") }
    var forzaWStr by rememberSaveable { mutableStateOf("280") }

    val tipoPizza = TipoPizza.entries.firstOrNull { it.label == tipoPizzaLabel } ?: TipoPizza.NAPOLETANA
    val tipoForno = TipoForno.entries.firstOrNull { it.label == tipoFornoLabel } ?: TipoForno.LEGNA

    val result by remember {
        derivedStateOf {
            val idro = idratazioneStr.toIntOrNull() ?: 68
            val peso = pesoPanettoStr.toIntOrNull() ?: 250
            val w = forzaWStr.toIntOrNull() ?: 280
            val tp = TipoPizza.entries.firstOrNull { it.label == tipoPizzaLabel } ?: TipoPizza.NAPOLETANA
            val tf = TipoForno.entries.firstOrNull { it.label == tipoFornoLabel } ?: TipoForno.LEGNA
            calcolaTemperature(tp, tf, idro, peso, w)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            Text(
                text = "TEMPERATURE FORNO",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Temperature Forno",
                style = MaterialTheme.typography.headlineSmall,
                color = QuadernoColors.Ink,
            )
        }

        DashedDivider()

        // Tipo pizza chips
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
            Text(
                text = "TIPO DI PIZZA",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Ink3,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            QChipRow(
                items = listOf("Napoletana", "Contemporanea", "Teglia", "Pala"),
                selected = tipoPizzaLabel,
                onSelect = { tipoPizzaLabel = it },
                wrap = true,
            )
        }

        DashedDivider()

        // Tipo forno chips
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)) {
            Text(
                text = "TIPO DI FORNO",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Ink3,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            QChipRow(
                items = listOf("Legna", "Gas", "Elettrico", "Domestico"),
                selected = tipoFornoLabel,
                onSelect = { tipoFornoLabel = it },
            )
        }

        DashedDivider()

        // Idratazione
        QField(
            label = "Idratazione",
            value = "${idratazioneStr.toIntOrNull() ?: 68}",
            suffix = "%",
            onMinus = {
                val v = (idratazioneStr.toIntOrNull() ?: 68) - 1
                idratazioneStr = v.coerceIn(55, 85).toString()
            },
            onPlus = {
                val v = (idratazioneStr.toIntOrNull() ?: 68) + 1
                idratazioneStr = v.coerceIn(55, 85).toString()
            },
            hint = "(55 – 85)",
            dense = true,
        )

        // Peso panetto
        QField(
            label = "Peso panetto",
            value = pesoPanettoStr,
            suffix = "g",
            onMinus = {
                val v = (pesoPanettoStr.toIntOrNull() ?: 250) - 10
                pesoPanettoStr = v.coerceIn(100, 600).toString()
            },
            onPlus = {
                val v = (pesoPanettoStr.toIntOrNull() ?: 250) + 10
                pesoPanettoStr = v.coerceIn(100, 600).toString()
            },
            dense = true,
        )

        // Forza W
        QField(
            label = "W farina",
            value = forzaWStr,
            suffix = "W",
            onMinus = {
                val v = (forzaWStr.toIntOrNull() ?: 280) - 10
                forzaWStr = v.coerceIn(100, 500).toString()
            },
            onPlus = {
                val v = (forzaWStr.toIntOrNull() ?: 280) + 10
                forzaWStr = v.coerceIn(100, 500).toString()
            },
            dense = true,
        )

        // Temperature results card
        QCard(
            kicker = "Temperature consigliate",
            title = "${result.cieloMin}–${result.cieloMax}°C",
        ) {
            QLeaderRow(label = "Cielo (sopra)", value = "${result.cieloMin}–${result.cieloMax}°C")
            QLeaderRow(label = "Platea (base)", value = "${result.plateaMin}–${result.plateaMax}°C")
            QLeaderRow(label = "Tempo cottura", value = "${result.tempoMin} – ${result.tempoMax}")
        }

        // Tips section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 8.dp)
                .background(QuadernoColors.BgWarmer)
                .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            // Olive left border drawn via a nested box
            Box(
                modifier = Modifier
                    .padding(start = 0.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "CONSIGLI",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = QuadernoColors.Olive,
                        ),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    result.consigli.forEach { consiglio ->
                        Text(
                            text = "• $consiglio",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuadernoColors.Ink2,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ============================================================
// CONTENITORE
// ============================================================

@Composable
fun ContenitoreCalculator() {
    var pesoImpasto by rememberSaveable { mutableStateOf("1000") }

    val volume by remember {
        derivedStateOf {
            val peso = pesoImpasto.toDoubleOrNull() ?: return@derivedStateOf null
            if (peso <= 0) return@derivedStateOf null
            PizzaFormulas.contenitoreVolumeMl(peso)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            Text(
                text = "CONTENITORE",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Contenitore",
                style = MaterialTheme.typography.headlineSmall,
                color = QuadernoColors.Ink,
            )
        }

        DashedDivider()

        QField(
            label = "Peso impasto",
            value = pesoImpasto,
            suffix = "g",
            onMinus = {
                val v = (pesoImpasto.toIntOrNull() ?: 1000) - 50
                pesoImpasto = v.coerceAtLeast(50).toString()
            },
            onPlus = {
                val v = (pesoImpasto.toIntOrNull() ?: 1000) + 50
                pesoImpasto = v.toString()
            },
            presets = listOf("500", "1000", "1500", "2000"),
            selectedPreset = if (pesoImpasto in listOf("500", "1000", "1500", "2000")) pesoImpasto else null,
            onPresetSelect = { pesoImpasto = it },
        )

        volume?.let { v ->
            QCard(
                kicker = "Volume consigliato",
                title = "${v.roundToInt()} ml",
                accent = QuadernoColors.Olive,
            ) {
                QLeaderRow(label = "Volume minimo", value = "${v.roundToInt()} ml")
                QLeaderRow(label = "In litri", value = "${"%.1f".format(v / 1000.0)} L")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ============================================================
// CONVERSIONE LIEVITO
// ============================================================

@Composable
fun ConversioneLievitoCalculator() {
    var lievitoFresco by rememberSaveable { mutableStateOf("3") }
    var farina by rememberSaveable { mutableStateOf("600") }
    var acqua by rememberSaveable { mutableStateOf("380") }

    val result by remember {
        derivedStateOf {
            try {
                val l = lievitoFresco.toDoubleOrNull() ?: return@derivedStateOf null
                val f = farina.toDoubleOrNull() ?: return@derivedStateOf null
                val a = acqua.toDoubleOrNull() ?: return@derivedStateOf null
                if (l <= 0 || f <= 0 || a <= 0) return@derivedStateOf null
                PizzaFormulas.conversioneLievito(l, f, a)
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
        // Header
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            Text(
                text = "CONVERSIONE LIEVITO",
                style = MaterialTheme.typography.labelSmall,
                color = QuadernoColors.Primary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Conversione Lievito",
                style = MaterialTheme.typography.headlineSmall,
                color = QuadernoColors.Ink,
            )
        }

        DashedDivider()

        QField(
            label = "Lievito di birra fresco",
            value = lievitoFresco,
            suffix = "g",
            onMinus = {
                val v = (lievitoFresco.toIntOrNull() ?: 3) - 1
                lievitoFresco = v.coerceAtLeast(1).toString()
            },
            onPlus = {
                val v = (lievitoFresco.toIntOrNull() ?: 3) + 1
                lievitoFresco = v.toString()
            },
            presets = listOf("1", "2", "3", "5"),
            selectedPreset = if (lievitoFresco in listOf("1", "2", "3", "5")) lievitoFresco else null,
            onPresetSelect = { lievitoFresco = it },
        )

        QField(
            label = "Farina ricetta",
            value = farina,
            suffix = "g",
            onMinus = {
                val v = (farina.toIntOrNull() ?: 600) - 50
                farina = v.coerceAtLeast(50).toString()
            },
            onPlus = {
                val v = (farina.toIntOrNull() ?: 600) + 50
                farina = v.toString()
            },
            dense = true,
        )

        QField(
            label = "Acqua ricetta",
            value = acqua,
            suffix = "g",
            onMinus = {
                val v = (acqua.toIntOrNull() ?: 380) - 10
                acqua = v.coerceAtLeast(10).toString()
            },
            onPlus = {
                val v = (acqua.toIntOrNull() ?: 380) + 10
                acqua = v.toString()
            },
            dense = true,
        )

        result?.let { c ->
            // Card 1: Lieviti secchi
            QCard(
                kicker = "Lieviti secchi",
                title = "${c.ldbs} g",
            ) {
                QLeaderRow(label = "LDB fresco", value = "${c.ldbfInput} g")
                QLeaderRow(label = "LDB secco attivo", value = "${c.ldbs} g")
                QLeaderRow(label = "LDB secco Caputo", value = "${c.ldbc} g")
            }

            // Card 2: Lievito madre
            QCard(
                kicker = "Lievito madre",
                title = "${c.lm} g",
                accent = QuadernoColors.Olive,
            ) {
                QLeaderRow(label = "Lievito madre", value = "${c.lm} g")
                QLeaderRow(label = "Farina (corretta)", value = "${c.farinaLm} g")
                QLeaderRow(label = "Acqua (corretta)", value = "${c.acquaLm} g")
            }

            // Card 3: Li.Co.Li.
            QCard(
                kicker = "Li.Co.Li.",
                title = "${c.licoli} g",
                titleSuffix = "idro 100%",
                accent = QuadernoColors.Olive,
            ) {
                QLeaderRow(label = "Li.Co.Li.", value = "${c.licoli} g")
                QLeaderRow(label = "Farina (corretta)", value = "${c.farinaLicoli} g")
                QLeaderRow(label = "Acqua (corretta)", value = "${c.acquaLicoli} g")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
