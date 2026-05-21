package com.pizzalab.ui.mixfarine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pizzalab.domain.MixFarineResult
import com.pizzalab.domain.PizzaFormulas

/**
 * Preset di farine comuni con nome, W e proteine tipiche.
 */
data class FarinaPreset(
    val nome: String,
    val w: Int,
    val proteine: Double
) {
    companion object {
        val presets = listOf(
            // Dati da schede tecniche Caputo + Confraternita della Pizza
            FarinaPreset("Caputo Classica (BLU)", 230, 11.5),
            FarinaPreset("Caputo Pizzeria (BLU)", 265, 12.5),
            FarinaPreset("Caputo Nuvola", 270, 12.5),
            FarinaPreset("Caputo Cuoco / Chef Rosso", 310, 13.5),
            FarinaPreset("Caputo Manitoba", 380, 15.0),
            FarinaPreset("Caputo Tipo 1", 260, 12.5),
            FarinaPreset("Caputo Integrale", 180, 13.0),
            FarinaPreset("Semola rimacinata", 250, 12.5),
            // Generiche
            FarinaPreset("00 debole (dolci)", 150, 10.0),
            FarinaPreset("00 media generica", 220, 11.5),
            FarinaPreset("Tipo 2 (semi-integrale)", 180, 12.0)
        )
    }
}

/**
 * Stato di una singola farina nel mix.
 */
data class FarinaEntry(
    val id: Int,
    val nome: String = "",
    val pesoG: String = "",
    val inputMode: InputMode = InputMode.W,
    val wValue: String = "",
    val proteineValue: String = ""
) {
    enum class InputMode { W, PROTEINE }

    fun getW(): Int? {
        return when (inputMode) {
            InputMode.W -> wValue.toIntOrNull()
            InputMode.PROTEINE -> {
                val p = proteineValue.toDoubleOrNull() ?: return null
                if (p < 6) return null
                PizzaFormulas.forzaDaProteine(p)
            }
        }
    }

    fun getPeso(): Double? = pesoG.toDoubleOrNull()?.takeIf { it > 0 }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MixFarineScreen(modifier: Modifier = Modifier) {

    // Lista farine (inizia con 2)
    var nextId by rememberSaveable { mutableStateOf(3) }
    val farine = remember {
        mutableStateListOf(
            FarinaEntry(id = 1),
            FarinaEntry(id = 2)
        )
    }

    // Risultato calcolato
    val risultato by remember {
        derivedStateOf {
            val pesi = farine.mapNotNull { it.getPeso() }
            val ws = farine.mapNotNull { it.getW() }
            if (pesi.size == farine.size && ws.size == farine.size && pesi.size >= 2) {
                try {
                    PizzaFormulas.calcMixFarine(pesi, ws)
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mix Farine",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Inserisci le farine del tuo mix per calcolare la forza risultante e l'idratazione massima consigliata.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Carte farine
        farine.forEachIndexed { index, farina ->
            FarinaCard(
                farina = farina,
                index = index + 1,
                canRemove = farine.size > 2,
                onUpdate = { updated -> farine[index] = updated },
                onRemove = { farine.removeAt(index) }
            )
        }

        // Bottone aggiungi farina
        if (farine.size < 4) {
            OutlinedButton(
                onClick = {
                    farine.add(FarinaEntry(id = nextId))
                    nextId++
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aggiungi farina")
            }
        }

        // Risultato
        risultato?.let { result ->
            Spacer(modifier = Modifier.height(4.dp))
            MixResultCard(result, farine)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarinaCard(
    farina: FarinaEntry,
    index: Int,
    canRemove: Boolean,
    onUpdate: (FarinaEntry) -> Unit,
    onRemove: () -> Unit
) {
    var showPresets by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header con titolo e bottone rimuovi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Farina $index",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    OutlinedButton(
                        onClick = { showPresets = !showPresets }
                    ) {
                        Text(if (showPresets) "Chiudi" else "Preset", style = MaterialTheme.typography.labelSmall)
                    }
                    if (canRemove) {
                        IconButton(onClick = onRemove) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Rimuovi",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Preset farine
            AnimatedVisibility(visible = showPresets) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FarinaPreset.presets.forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                onUpdate(
                                    farina.copy(
                                        nome = preset.nome,
                                        wValue = preset.w.toString(),
                                        proteineValue = preset.proteine.toString(),
                                        inputMode = FarinaEntry.InputMode.W
                                    )
                                )
                                showPresets = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${preset.nome} — W${preset.w} (${preset.proteine}%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Peso
            OutlinedTextField(
                value = farina.pesoG,
                onValueChange = { onUpdate(farina.copy(pesoG = it.filter { c -> c.isDigit() || c == '.' }.take(7))) },
                label = { Text("Peso (g)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Toggle W / Proteine
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = farina.inputMode == FarinaEntry.InputMode.W,
                    onClick = { onUpdate(farina.copy(inputMode = FarinaEntry.InputMode.W)) },
                    label = { Text("Forza (W)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = farina.inputMode == FarinaEntry.InputMode.PROTEINE,
                    onClick = { onUpdate(farina.copy(inputMode = FarinaEntry.InputMode.PROTEINE)) },
                    label = { Text("Proteine (%)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Input W o Proteine
            if (farina.inputMode == FarinaEntry.InputMode.W) {
                OutlinedTextField(
                    value = farina.wValue,
                    onValueChange = { onUpdate(farina.copy(wValue = it.filter { c -> c.isDigit() }.take(3))) },
                    label = { Text("Valore W") },
                    placeholder = { Text("es. 260") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = farina.proteineValue,
                    onValueChange = {
                        onUpdate(farina.copy(
                            proteineValue = it.filter { c -> c.isDigit() || c == '.' }.take(5)
                        ))
                    },
                    label = { Text("Proteine (%)") },
                    placeholder = { Text("es. 12.5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    supportingText = {
                        val w = farina.getW()
                        if (w != null && farina.proteineValue.isNotEmpty()) {
                            Text("W stimato: $w")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MixResultCard(result: MixFarineResult, farine: List<FarinaEntry>) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Risultato Mix",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Composizione mix
            Text(
                "Composizione",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            farine.forEachIndexed { i, f ->
                val nome = if (f.nome.isNotEmpty()) f.nome else "Farina ${i + 1}"
                val w = f.getW() ?: 0
                val pct = result.percentuali.getOrNull(i) ?: 0.0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$nome (W$w)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${pct}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // W medio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Forza risultante",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "W ${result.wMedio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peso totale", style = MaterialTheme.typography.bodyMedium)
                Text("${result.pesoTotale} g", style = MaterialTheme.typography.bodyMedium)
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            // Idratazione massima
            Text(
                "Idratazione massima consigliata",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Barra visuale sicura
            HydrationBar(
                label = "Sicura (a mano)",
                value = result.idroMaxSicura,
                color = MaterialTheme.colorScheme.primary
            )

            // Barra visuale avanzata
            HydrationBar(
                label = "Avanzata (planetaria)",
                value = result.idroMaxAvanzata,
                color = MaterialTheme.colorScheme.tertiary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Suggerimento testuale
            Text(
                text = when {
                    result.wMedio < 180 -> "Farina debole: adatta a lievitazioni brevi (1-4h), impasto croccante."
                    result.wMedio < 250 -> "Farina media: buona per pizze classiche con lievitazione di 8-12h."
                    result.wMedio < 320 -> "Farina forte: ideale per lievitazioni lunghe (24h+), buona struttura."
                    else -> "Farina molto forte: perfetta per lievitazioni molto lunghe (48h+) o impasti ad alta idratazione."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HydrationBar(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$value%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = value / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
