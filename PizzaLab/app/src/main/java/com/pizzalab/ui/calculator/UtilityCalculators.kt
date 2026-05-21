package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import kotlin.math.roundToInt

// ============================================================
// MIX FARINE
// ============================================================

@Composable
fun MixFarineCalculator(modifier: Modifier = Modifier) {
    var totaleG by rememberSaveable { mutableStateOf("1000") }
    var w1 by rememberSaveable { mutableStateOf("200") }
    var w2 by rememberSaveable { mutableStateOf("350") }
    var wMix by rememberSaveable { mutableStateOf("280") }

    val result by remember {
        derivedStateOf {
            try {
                val tg = totaleG.toDoubleOrNull() ?: return@derivedStateOf null
                val v1 = w1.toDoubleOrNull() ?: return@derivedStateOf null
                val v2 = w2.toDoubleOrNull() ?: return@derivedStateOf null
                val vm = wMix.toDoubleOrNull() ?: return@derivedStateOf null
                PizzaFormulas.mixFarine(tg, v1, v2, vm)
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
            "Mix Farine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = totaleG,
            onValueChange = { totaleG = it },
            label = { Text("Farina totale (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = w1,
            onValueChange = { w1 = it },
            label = { Text("W farina 1") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = w2,
            onValueChange = { w2 = it },
            label = { Text("W farina 2") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = wMix,
            onValueChange = { wMix = it },
            label = { Text("W desiderato") },
            modifier = Modifier.fillMaxWidth()
        )

        result?.let { (f1, f2) ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Risultato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ResultRow("Farina 1 (W${w1})", "$f1 g")
                    ResultRow("Farina 2 (W${w2})", "$f2 g")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================
// FORZA DA PROTEINE
// ============================================================

@Composable
fun ForzaProteineCalculator(modifier: Modifier = Modifier) {
    var proteine by rememberSaveable { mutableFloatStateOf(12f) }

    val forza by remember {
        derivedStateOf {
            try {
                PizzaFormulas.forzaDaProteine(proteine.toDouble())
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
            "W da Proteine",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        SliderInput(
            label = "Proteine",
            value = proteine,
            onValueChange = { proteine = it },
            valueRange = 7f..17f,
            steps = 99,
            valueDisplay = "${"%.1f".format(proteine)} %"
        )

        forza?.let { w ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Risultato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ResultRow("Forza stimata", "W$w")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================
// CONTENITORE
// ============================================================

@Composable
fun ContenitoreCalculator(modifier: Modifier = Modifier) {
    var pesoImpasto by rememberSaveable { mutableStateOf("1000") }

    val volume by remember {
        derivedStateOf {
            val peso = pesoImpasto.toDoubleOrNull() ?: return@derivedStateOf null
            if (peso <= 0) return@derivedStateOf null
            PizzaFormulas.contenitoreVolumeMl(peso)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Contenitore",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = pesoImpasto,
            onValueChange = { pesoImpasto = it },
            label = { Text("Peso impasto (g)") },
            modifier = Modifier.fillMaxWidth()
        )

        volume?.let { v ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Risultato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ResultRow("Volume consigliato", "$v ml")
                    ResultRow("", "${"%.1f".format(v / 1000.0)} L")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================================
// CONVERSIONE LIEVITO
// ============================================================

@Composable
fun ConversioneLievitoCalculator(modifier: Modifier = Modifier) {
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
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Conversione Lievito",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = lievitoFresco,
            onValueChange = { lievitoFresco = it },
            label = { Text("Lievito di birra fresco (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = farina,
            onValueChange = { farina = it },
            label = { Text("Farina ricetta (g)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = acqua,
            onValueChange = { acqua = it },
            label = { Text("Acqua ricetta (g)") },
            modifier = Modifier.fillMaxWidth()
        )

        result?.let { c ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Risultato",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ResultRow("LDB fresco", "${c.ldbfInput} g")
                    ResultRow("LDB secco attivo", "${c.ldbs} g")
                    ResultRow("LDB secco Caputo", "${c.ldbc} g")

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "Lievito Madre Solido",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    ResultRow("Lievito madre", "${c.lm} g")
                    ResultRow("Farina (corretta)", "${c.farinaLm} g")
                    ResultRow("Acqua (corretta)", "${c.acquaLm} g")

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "Li.Co.Li (idro 100%)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    ResultRow("Li.Co.Li", "${c.licoli} g")
                    ResultRow("Farina (corretta)", "${c.farinaLicoli} g")
                    ResultRow("Acqua (corretta)", "${c.acquaLicoli} g")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
    // Temperature base per tipo pizza
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

    // Adattamento per tipo forno
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

    // Correzione per idratazione alta (>70% → cielo più forte)
    if (idratazione > 70 && tipoPizza != TipoPizza.TEGLIA && tipoPizza != TipoPizza.PALA) {
        val boost = ((idratazione - 70) * 1.5).toInt()
        cieloMin += boost
        cieloMax += boost
    }

    // Correzione per farina forte (W > 300)
    if (forzaW > 300) {
        val boost = ((forzaW - 300) * 0.1).toInt()
        cieloMin += boost
        cieloMax += boost
        plateaMin += boost
        plateaMax += boost
    }

    // Consigli pratici
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureFornoCalculator(modifier: Modifier = Modifier) {
    var tipoPizza by rememberSaveable { mutableStateOf(TipoPizza.NAPOLETANA) }
    var tipoForno by rememberSaveable { mutableStateOf(TipoForno.LEGNA) }
    var idratazione by rememberSaveable { mutableFloatStateOf(68f) }
    var pesoPanetto by rememberSaveable { mutableStateOf("250") }
    var forzaW by rememberSaveable { mutableStateOf("280") }

    val result by remember {
        derivedStateOf {
            val peso = pesoPanetto.toIntOrNull() ?: 250
            val w = forzaW.toIntOrNull() ?: 280
            calcolaTemperature(tipoPizza, tipoForno, idratazione.roundToInt(), peso, w)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Temperature Forno",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Tipo pizza
        Text("Tipo di pizza", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TipoPizza.entries.forEach { tipo ->
                FilterChip(
                    selected = tipoPizza == tipo,
                    onClick = { tipoPizza = tipo },
                    label = { Text(tipo.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        // Tipo forno
        Text("Tipo di forno", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TipoForno.entries.forEach { tipo ->
                FilterChip(
                    selected = tipoForno == tipo,
                    onClick = { tipoForno = tipo },
                    label = { Text(tipo.label, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        // Idratazione slider
        Text(
            "Idratazione: ${idratazione.roundToInt()}%",
            style = MaterialTheme.typography.labelLarge
        )
        Slider(
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = 55f..85f,
            steps = 5
        )

        // Peso panetto e forza W
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = pesoPanetto,
                onValueChange = { pesoPanetto = it.filter { c -> c.isDigit() }.take(4) },
                label = { Text("Panetto (g)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = forzaW,
                onValueChange = { forzaW = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("W farina") },
                modifier = Modifier.weight(1f)
            )
        }

        // Risultato
        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Whatshot,
                        contentDescription = null,
                        tint = Color(0xFFD84315)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Temperature consigliate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                ResultRow("Cielo (sopra)", "${result.cieloMin}–${result.cieloMax}°C")
                ResultRow("Platea (base)", "${result.plateaMin}–${result.plateaMax}°C")
                ResultRow("Tempo cottura", "${result.tempoMin} – ${result.tempoMax}")

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.TipsAndUpdates,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Consigli",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                result.consigli.forEach { consiglio ->
                    Text(
                        text = "• $consiglio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
