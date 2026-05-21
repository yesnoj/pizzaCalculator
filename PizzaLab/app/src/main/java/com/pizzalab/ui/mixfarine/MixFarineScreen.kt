package com.pizzalab.ui.mixfarine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.domain.MixFarineResult
import com.pizzalab.domain.PizzaFormulas
import com.pizzalab.ui.components.DashedDivider
import com.pizzalab.ui.components.QBarRow
import com.pizzalab.ui.components.QCard
import com.pizzalab.ui.components.QLeaderRow
import com.pizzalab.ui.theme.QuadernoColors

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

@Composable
fun MixFarineScreen() {

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
        modifier = Modifier
            .fillMaxSize()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Flour cards
        farine.forEachIndexed { index, farina ->
            FarinaCard(
                farina = farina,
                index = index + 1,
                canRemove = farine.size > 2,
                onUpdate = { updated -> farine[index] = updated },
                onRemove = { farine.removeAt(index) }
            )
        }

        // "Aggiungi una farina" dashed button
        if (farine.size < 4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 4.dp)
                    .border(
                        width = 1.dp,
                        color = QuadernoColors.RuleDots,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .drawBehind {
                        val dashOn = 6.dp.toPx()
                        val dashOff = 4.dp.toPx()
                        val effect = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff))
                        val sw = 1.dp.toPx()
                        // overdraw dashed border on top of solid border
                        drawRect(
                            color = QuadernoColors.RuleDots,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = sw,
                                pathEffect = effect
                            )
                        )
                    }
                    .clickable {
                        farine.add(FarinaEntry(id = nextId))
                        nextId++
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = QuadernoColors.Ink2,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Aggiungi una farina",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = QuadernoColors.Ink2,
                        ),
                    )
                }
            }
        }

        // Result card
        risultato?.let { result ->
            Spacer(modifier = Modifier.height(4.dp))
            MixResultCard(result, farine)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun FarinaCard(
    farina: FarinaEntry,
    index: Int,
    canRemove: Boolean,
    onUpdate: (FarinaEntry) -> Unit,
    onRemove: () -> Unit
) {
    var showPresets by remember { mutableStateOf(false) }

    // Small card: Paper background, Rule border, 2dp radius
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp)
            .background(QuadernoColors.Paper, RoundedCornerShape(2.dp))
            .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(2.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Header: kicker "FARINA N" in Primary + optional remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FARINA $index",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp,
                        color = QuadernoColors.Primary,
                    ),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Preset toggle
                    Text(
                        text = if (showPresets) "Chiudi" else "Preset",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            color = QuadernoColors.Ink2,
                        ),
                        modifier = Modifier
                            .border(1.dp, QuadernoColors.Rule, RoundedCornerShape(2.dp))
                            .clickable { showPresets = !showPresets }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    if (canRemove) {
                        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Rimuovi",
                                tint = QuadernoColors.Ink2,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // Flour name in bold
            if (farina.nome.isNotEmpty()) {
                Text(
                    text = farina.nome,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = QuadernoColors.Ink,
                    ),
                )
            }

            // Preset list
            AnimatedVisibility(visible = showPresets) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    FarinaPreset.presets.forEach { preset ->
                        Text(
                            text = "${preset.nome} — W${preset.w} (${preset.proteine}%)",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = QuadernoColors.Ink2,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(
                                        farina.copy(
                                            nome = preset.nome,
                                            wValue = preset.w.toString(),
                                            proteineValue = preset.proteine.toString(),
                                            inputMode = FarinaEntry.InputMode.W
                                        )
                                    )
                                    showPresets = false
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                        )
                    }
                }
            }

            // Fields row: Peso (ink underline) + W or Proteine (olive underline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Peso field
                QInlineField(
                    label = "peso",
                    value = farina.pesoG,
                    placeholder = "—",
                    suffix = "g",
                    underlineColor = QuadernoColors.Ink,
                    keyboardType = KeyboardType.Number,
                    onValueChange = {
                        onUpdate(farina.copy(pesoG = it.filter { c -> c.isDigit() || c == '.' }.take(7)))
                    },
                    modifier = Modifier.weight(1f),
                )

                // W or Proteine field
                if (farina.inputMode == FarinaEntry.InputMode.W) {
                    QInlineField(
                        label = "W",
                        value = farina.wValue,
                        placeholder = "—",
                        suffix = "",
                        underlineColor = QuadernoColors.Olive,
                        keyboardType = KeyboardType.Number,
                        onValueChange = {
                            onUpdate(farina.copy(wValue = it.filter { c -> c.isDigit() }.take(3)))
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    val wStimato = farina.getW()
                    QInlineField(
                        label = "proteine %",
                        value = farina.proteineValue,
                        placeholder = "—",
                        suffix = if (wStimato != null) "W$wStimato" else "%",
                        underlineColor = QuadernoColors.Olive,
                        keyboardType = KeyboardType.Decimal,
                        onValueChange = {
                            onUpdate(farina.copy(proteineValue = it.filter { c -> c.isDigit() || c == '.' }.take(5)))
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Toggle W / Proteine
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(
                    "W" to FarinaEntry.InputMode.W,
                    "Proteine %" to FarinaEntry.InputMode.PROTEINE,
                ).forEach { (label, mode) ->
                    val isSelected = farina.inputMode == mode
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp,
                                if (isSelected) QuadernoColors.Olive else QuadernoColors.Rule,
                                RoundedCornerShape(2.dp),
                            )
                            .background(
                                if (isSelected) QuadernoColors.Olive else QuadernoColors.Paper,
                                RoundedCornerShape(2.dp),
                            )
                            .clickable { onUpdate(farina.copy(inputMode = mode)) }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = label,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
                                color = if (isSelected) QuadernoColors.Paper else QuadernoColors.Ink2,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/** Inline editable field with a solid underline. */
@Composable
private fun QInlineField(
    label: String,
    value: String,
    placeholder: String,
    suffix: String,
    underlineColor: androidx.compose.ui.graphics.Color,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink3,
                letterSpacing = 0.02.sp,
            ),
            modifier = Modifier.padding(bottom = 2.dp),
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = underlineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
                .padding(bottom = 3.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = QuadernoColors.Ink,
                    letterSpacing = (-0.01).sp,
                ),
                cursorBrush = SolidColor(QuadernoColors.Primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic,
                                    color = QuadernoColors.Ink3,
                                ),
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f),
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = " $suffix",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = QuadernoColors.Ink2,
                    ),
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun MixResultCard(result: MixFarineResult, farine: List<FarinaEntry>) {
    QCard(
        kicker = "Risultato mix",
        title = "W ${result.wMedio}",
        titleSuffix = "· ${result.pesoTotale} g totali",
    ) {
        // Flour percentage breakdown
        farine.forEachIndexed { i, f ->
            val nome = if (f.nome.isNotEmpty()) f.nome else "Farina ${i + 1}"
            val w = f.getW() ?: 0
            val pct = result.percentuali.getOrNull(i) ?: 0.0
            QLeaderRow(
                label = "$nome (W$w)",
                value = "${pct}",
                unit = "%",
            )
        }

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Hydration bars
        Text(
            text = "IDRATAZIONE MAX",
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = QuadernoColors.Primary,
            ),
            modifier = Modifier.padding(bottom = 2.dp),
        )

        QBarRow(
            label = "sicura (a mano)",
            value = result.idroMaxSicura,
            color = QuadernoColors.Primary,
        )

        QBarRow(
            label = "avanzata (planetaria)",
            value = result.idroMaxAvanzata,
            color = QuadernoColors.Olive,
        )

        DashedDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Footer classification text
        Text(
            text = when {
                result.wMedio < 180 -> "Farina debole: adatta a lievitazioni brevi (1–4h), impasto croccante."
                result.wMedio < 250 -> "Farina media: buona per pizze classiche con lievitazione di 8–12h."
                result.wMedio < 320 -> "Farina forte: ideale per lievitazioni lunghe (24h+), buona struttura."
                else -> "Farina molto forte: perfetta per lievitazioni molto lunghe (48h+) o impasti ad alta idratazione."
            },
            style = TextStyle(
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink2,
                lineHeight = 17.sp,
            ),
        )
    }
}
