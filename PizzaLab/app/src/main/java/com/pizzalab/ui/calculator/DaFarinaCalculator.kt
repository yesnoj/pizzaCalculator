package com.pizzalab.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.domain.RicettaFacile
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaFarinaCalculator(
    onStartProcess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var farinaInput by rememberSaveable { mutableFloatStateOf(500f) }
    var oreLievitazione by rememberSaveable { mutableIntStateOf(24) }
    var temperatura by rememberSaveable { mutableFloatStateOf(22f) }
    var idratazione by rememberSaveable { mutableFloatStateOf(63f) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }

    // Scelta panetti: true = fisso peso, false = fisso numero
    var scegliPeso by rememberSaveable { mutableStateOf(true) }
    var pesoPanetto by rememberSaveable { mutableFloatStateOf(250f) }
    var nPanetti by rememberSaveable { mutableFloatStateOf(4f) }

    val oreLievOptions = listOf(1, 2, 3, 4, 6, 8, 12, 24, 48)
    val isRapido = oreLievitazione <= 6

    // Calcolo totale impasto dalla farina (per limiti slider e info panetti)
    val totaleImpasto by remember {
        derivedStateOf {
            try {
                PizzaFormulas.totaleDaFarina(
                    farinaG = farinaInput.toDouble(),
                    idroPct = idratazione.toDouble(),
                    lievOre = oreLievitazione,
                    T = temperatura.roundToInt()
                )
            } catch (_: Exception) {
                farinaInput.toDouble() * 1.65
            }
        }
    }

    // Limiti dinamici
    val maxPesoPanetto by remember {
        derivedStateOf { totaleImpasto.toFloat().coerceIn(180f, 500f) }
    }
    val maxNPanetti by remember {
        derivedStateOf { (totaleImpasto / 180.0).toInt().coerceIn(1, 20).toFloat() }
    }

    // Clamp se limiti cambiano
    val effPeso = pesoPanetto.coerceIn(180f, maxPesoPanetto)
    if (effPeso != pesoPanetto) pesoPanetto = effPeso
    val effN = nPanetti.coerceIn(1f, maxNPanetti)
    if (effN != nPanetti) nPanetti = effN

    // Info panetti calcolati
    val panettiInfo by remember {
        derivedStateOf {
            try {
                if (scegliPeso) {
                    val n = (totaleImpasto / pesoPanetto).roundToInt().coerceAtLeast(1)
                    Pair(n, pesoPanetto.toDouble())
                } else {
                    val n = nPanetti.roundToInt().coerceAtLeast(1)
                    val peso = totaleImpasto / n
                    Pair(n, peso)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    // Ricetta calcolata direttamente dalla farina
    val ricetta by remember {
        derivedStateOf {
            try {
                PizzaFormulas.calcDaFarina(
                    farinaG = farinaInput.toDouble(),
                    idroPct = idratazione.toDouble(),
                    lievOre = oreLievitazione,
                    T = temperatura.roundToInt()
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
        // Farina
        SliderInput(
            label = "Farina",
            value = farinaInput,
            onValueChange = { farinaInput = it },
            valueRange = 200f..2000f,
            steps = 35,
            valueDisplay = "${farinaInput.roundToInt()} g"
        )

        // Scelta: fisso peso o fisso numero
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = scegliPeso,
                onClick = { scegliPeso = true },
                label = { Text("Fisso peso") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !scegliPeso,
                onClick = { scegliPeso = false },
                label = { Text("Fisso numero") },
                modifier = Modifier.weight(1f)
            )
        }

        if (scegliPeso) {
            val pesoMax = maxPesoPanetto.coerceAtLeast(181f)
            val pesoSteps = ((pesoMax - 180f) / 10f).toInt().coerceAtLeast(1)
            SliderInput(
                label = "Peso panetto",
                value = pesoPanetto.coerceIn(180f, pesoMax),
                onValueChange = { pesoPanetto = it },
                valueRange = 180f..pesoMax,
                steps = pesoSteps,
                valueDisplay = "${pesoPanetto.coerceIn(180f, pesoMax).roundToInt()} g"
            )
            panettiInfo?.let { (n, _) ->
                ResultRow("Panetti risultanti", "$n")
            }
        } else {
            val nMax = maxNPanetti.coerceAtLeast(2f)
            // steps = numero di stop INTERMEDI tra min e max.
            // Per valori interi da 1 a nMax servono (nMax - 1 - 1) stop intermedi.
            // Es: range 1..4 → steps=2 → posizioni 1,2,3,4
            val nSteps = (nMax.toInt() - 2).coerceAtLeast(0)
            SliderInput(
                label = "Numero panetti",
                value = nPanetti.coerceIn(1f, nMax),
                onValueChange = { nPanetti = it },
                valueRange = 1f..nMax,
                steps = nSteps,
                valueDisplay = "${nPanetti.coerceIn(1f, nMax).roundToInt()}"
            )
            panettiInfo?.let { (_, peso) ->
                ResultRow("Peso panetto", "${peso.roundToInt()} g")
            }
        }

        // Ore lievitazione
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = if (isRapido) "$oreLievitazione ore (rapido)" else "$oreLievitazione ore",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ore lievitazione") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                oreLievOptions.forEach { ore ->
                    val label = if (ore <= 6) "$ore ore (rapido)" else "$ore ore"
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            oreLievitazione = ore
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Temperatura ambiente
        SliderInput(
            label = "Temperatura ambiente",
            value = temperatura,
            onValueChange = { temperatura = it },
            valueRange = 18f..(if (isRapido) 35f else 30f),
            steps = if (isRapido) 16 else 11,
            valueDisplay = "${temperatura.roundToInt()} °C"
        )

        // Idratazione
        SliderInput(
            label = "Idratazione",
            value = idratazione,
            onValueChange = { idratazione = it },
            valueRange = (if (isRapido) 55f else 59f)..(if (isRapido) 80f else 70f),
            steps = if (isRapido) 24 else 10,
            valueDisplay = "${idratazione.roundToInt()} %"
        )

        // Risultati
        ricetta?.let { r ->
            DaFarinaResultCard(r, panettiInfo)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onStartProcess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Avvia Processo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DaFarinaResultCard(r: RicettaFacile, panettiInfo: Pair<Int, Double>?) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

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

            panettiInfo?.let { (n, peso) ->
                ResultRow("Panetti", "$n × ${peso.roundToInt()} g")
                Divider(modifier = Modifier.padding(vertical = 4.dp))
            }

            ResultRow("Farina", "${r.farina} g")
            ResultRow("Acqua", "${r.acqua} g")
            ResultRow("Sale", "${r.sale} g")
            ResultRow("Lievito fresco", "${r.ldbf} g")
            ResultRow("Lievito secco", "${r.ldbs} g")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            ResultRow("Totale", "${r.totale} g")
            ResultRow("Forza consigliata", "W${r.forza}")
            ResultRow("Temp. chiusura", "${r.tempChiusura} °C")

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                "Timeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            ResultRow("Puntata", PizzaFormulas.fmtMinutes(r.puntataMin))
            if (r.frigoMin > 0) {
                ResultRow("Frigo", PizzaFormulas.fmtMinutes(r.frigoMin))
            }
            ResultRow("Appretto", PizzaFormulas.fmtMinutes(r.aprettoMin))
            ResultRow("Inizio appretto", r.inizioApretto.format(timeFmt))
            ResultRow("Pronti", r.pronti.format(timeFmt))
        }
    }
}
