package com.pizzalab.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.ui.components.QHeader
import com.pizzalab.ui.components.QSectionDivider
import com.pizzalab.ui.theme.QuadernoColors

/**
 * Glossary data model.
 */
private data class GlossaryTerm(
    val term: String,
    val definition: String,
)

private data class GlossarySection(
    val title: String,
    val terms: List<GlossaryTerm>,
)

private val glossarySections = listOf(
    GlossarySection(
        "Ingredienti e misure",
        listOf(
            GlossaryTerm(
                "Idratazione",
                "La percentuale di acqua rispetto alla farina. Ad esempio, " +
                    "63% di idratazione significa 630 ml di acqua per 1 kg di farina. " +
                    "Un'idratazione più alta dà un impasto più morbido e una pizza più soffice, " +
                    "ma è anche più difficile da lavorare."
            ),
            GlossaryTerm(
                "Forza della farina (W)",
                "Indica la capacità della farina di assorbire acqua e trattenere " +
                    "i gas della lievitazione. Farine deboli (W 170–200) per lievitazioni brevi, " +
                    "farine forti (W 280–350) per lievitazioni lunghe. Sul pacco della farina " +
                    "spesso non è indicato il W, ma puoi stimarlo dalle proteine."
            ),
            GlossaryTerm(
                "Baker's %",
                "Sistema in cui ogni ingrediente è espresso come percentuale " +
                    "rispetto al peso della farina. La farina è sempre il 100%. " +
                    "Esempio: 63% idratazione, 2% sale, 0.1% lievito."
            ),
            GlossaryTerm(
                "LDB (Lievito di birra fresco)",
                "Il classico cubetto di lievito che trovi in frigo al supermercato. " +
                    "Va sciolto in acqua tiepida. Si conserva pochi giorni in frigo."
            ),
            GlossaryTerm(
                "Lievito secco",
                "Lievito disidratato in granuli. Ne serve circa 1/3 rispetto al " +
                    "fresco. Il secco attivo va reidratato, l'istantaneo si aggiunge " +
                    "direttamente alla farina."
            ),
            GlossaryTerm(
                "Sale",
                "Oltre al sapore, il sale rafforza il glutine e rallenta " +
                    "la lievitazione. La dose classica napoletana è 40 g per litro " +
                    "di acqua (circa il 2.5% sulla farina)."
            ),
            GlossaryTerm(
                "Malto",
                "Zucchero naturale che nutre il lievito e favorisce la " +
                    "colorazione della crosta. Si usa in piccole dosi (0.1–0.5% sulla farina). " +
                    "Non sempre necessario."
            ),
        )
    ),
    GlossarySection(
        "Fasi della lievitazione",
        listOf(
            GlossaryTerm(
                "Impasto",
                "La fase in cui si mescolano tutti gli ingredienti e si lavora " +
                    "l'impasto fino a sviluppare il glutine (la «maglia glutinica»). " +
                    "Un buon impasto è liscio, elastico e non appiccicoso."
            ),
            GlossaryTerm(
                "Autolisi",
                "Riposo di farina e acqua (senza sale e lievito) per 20–60 minuti " +
                    "prima di impastare. Permette alla farina di idratarsi e al glutine di " +
                    "iniziare a formarsi naturalmente."
            ),
            GlossaryTerm(
                "Puntata",
                "La prima fase di riposo dell'impasto intero, subito dopo " +
                    "l'impastamento. L'impasto fermenta e sviluppa aromi. Dura da " +
                    "30 minuti a diverse ore."
            ),
            GlossaryTerm(
                "Pieghe",
                "Piegature dell'impasto durante la puntata per rafforzare la " +
                    "struttura senza re-impastare. Si fanno 2–4 serie a " +
                    "intervalli regolari."
            ),
            GlossaryTerm(
                "Staglio",
                "La divisione dell'impasto in panetti del peso desiderato. " +
                    "Ogni panetto viene arrotondato in una pallina liscia e tesa, " +
                    "che diventerà una pizza."
            ),
            GlossaryTerm(
                "Appretto",
                "Il riposo dei panetti dopo lo staglio. È la fase finale " +
                    "prima della stesura: i panetti si rilassano e lievitano " +
                    "ulteriormente. Pronto quando, premuto con un dito, " +
                    "torna su lentamente."
            ),
            GlossaryTerm(
                "Frigo",
                "Fase di lievitazione in frigorifero (4–6 °C) che rallenta " +
                    "il lievito e favorisce la maturazione. Sviluppa aromi più complessi " +
                    "e rende l'impasto più digeribile. Usato nelle lievitazioni lunghe " +
                    "(12–72 ore)."
            ),
            GlossaryTerm(
                "Maturazione",
                "Processo in cui gli enzimi della farina scompongono amidi " +
                    "e proteine, rendendo l'impasto più digeribile. Non è la stessa " +
                    "cosa della lievitazione: un impasto può essere lievitato ma " +
                    "non ancora maturo."
            ),
            GlossaryTerm(
                "Temp. di chiusura",
                "La temperatura dell'impasto alla fine dell'impastamento. " +
                    "Influenza la velocità della lievitazione. " +
                    "Idealmente 22–24 °C."
            ),
        )
    ),
    GlossarySection(
        "Tecniche e prefermenti",
        listOf(
            GlossaryTerm(
                "Biga",
                "Pre-impasto asciutto (44–50% di idratazione) fatto con " +
                    "farina, acqua e poco lievito, che fermenta 16–24 ore. " +
                    "Dà alla pizza più struttura, friabilità e aromi complessi. " +
                    "Si aggiunge poi all'impasto finale (rinfresco)."
            ),
            GlossaryTerm(
                "Rinfresco",
                "L'aggiunta di farina, acqua e altri ingredienti alla biga " +
                    "matura per completare l'impasto. Anche usato per il lievito " +
                    "madre: rinfrescare = nutrire con nuova farina e acqua."
            ),
            GlossaryTerm(
                "Lievito madre",
                "Impasto di farina e acqua fermentato con lieviti e " +
                    "batteri naturali. Dà sapori più complessi del lievito di birra, " +
                    "ma richiede manutenzione regolare (rinfreschi)."
            ),
            GlossaryTerm(
                "Li.Co.Li.",
                "Lievito in Coltura Liquida: versione del lievito madre con " +
                    "idratazione al 100% (pari quantità di farina e acqua). Più facile " +
                    "da gestire rispetto alla pasta madre solida."
            ),
            GlossaryTerm(
                "Mix farine",
                "Miscela di due o più farine con forza (W) diversa per ottenere " +
                    "una forza intermedia. Il W risultante è la media ponderata dei W " +
                    "delle singole farine."
            ),
        )
    ),
    GlossarySection(
        "Cottura",
        listOf(
            GlossaryTerm(
                "Stesura",
                "L'operazione di allargare il panetto a disco. La pizza " +
                    "napoletana si stende a mano (mai col mattarello!) partendo " +
                    "dal centro e lasciando il bordo più alto (il cornicione)."
            ),
            GlossaryTerm(
                "Cornicione",
                "Il bordo rialzato della pizza. Si forma naturalmente se " +
                    "durante la stesura si lascia l'aria nel bordo senza schiacciarlo."
            ),
            GlossaryTerm(
                "Pala",
                "Pizza stesa su teglia o pietra, tipicamente rettangolare, " +
                    "cotta in forno a 280–320 °C per 3–5 minuti."
            ),
            GlossaryTerm(
                "Teglia",
                "Pizza cotta direttamente nella teglia, spesso con " +
                    "idratazione alta (70–80%). Il peso dell'impasto si calcola " +
                    "in base alla superficie: 0.50 g/cm² per condita, " +
                    "0.375 g/cm² per fina, 0.58 g/cm² per bianca."
            ),
            GlossaryTerm(
                "Tonda Romana",
                "Pizza tonda romana al piatto, croccante e sottile. " +
                    "Panetti da 180 g, farina W 300–320 con aggiunta di semola. " +
                    "Si stende col mattarello. Cottura a 290 °C, ~2 minuti."
            ),
        )
    ),
    GlossarySection(
        "Strumenti",
        listOf(
            GlossaryTerm(
                "Contenitore",
                "Il recipiente dove riposa l'impasto. La regola è: " +
                    "volume del contenitore = peso impasto × 2.4. " +
                    "Esempio: 1 kg di impasto → contenitore da almeno 2.4 litri."
            ),
            GlossaryTerm(
                "Conversione lievito",
                "I diversi tipi di lievito non sono intercambiabili 1:1. " +
                    "Regola: 1 g fresco = 0.33 g secco attivo = " +
                    "0.5 g secco Caputo. Il lievito madre richiede circa 53× " +
                    "il peso del lievito fresco equivalente."
            ),
        )
    ),
)

/**
 * Help / Glossary screen — Quaderno design with § section dividers,
 * two-column layout (term | definition), and dotted separators.
 */
@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(QuadernoColors.Bg)
            .verticalScroll(rememberScrollState()),
    ) {
        QHeader(
            kicker = "Aiuto",
            title = "Glossario",
            italic = "dei pizzaioli",
        )

        Text(
            text = "Tutti i termini dell'app, spiegati come fareste in cucina.",
            style = TextStyle(
                fontSize = 12.5.sp,
                fontStyle = FontStyle.Italic,
                color = QuadernoColors.Ink2,
            ),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Info bar — "XX voci · Y sezioni"
        val totalTerms = glossarySections.sumOf { it.terms.size }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .drawBehind {
                    // Top border
                    drawLine(QuadernoColors.Ink, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    // Bottom border
                    drawLine(QuadernoColors.Ink, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = "$totalTerms voci · ${glossarySections.size} sezioni",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = QuadernoColors.Ink2,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        glossarySections.forEach { section ->
            QSectionDivider(
                label = section.title,
                count = section.terms.size,
            )

            section.terms.forEachIndexed { index, entry ->
                val isLast = index == section.terms.size - 1
                GlossaryEntry(
                    term = entry.term,
                    definition = entry.definition,
                    borderStyle = if (isLast) GlossaryBorderStyle.SOLID else GlossaryBorderStyle.DOTTED,
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

private enum class GlossaryBorderStyle { DOTTED, SOLID }

@Composable
private fun GlossaryEntry(
    term: String,
    definition: String,
    borderStyle: GlossaryBorderStyle = GlossaryBorderStyle.DOTTED,
) {
    val ruleDotsColor = QuadernoColors.RuleDots
    val ruleColor = QuadernoColors.Rule

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .drawBehind {
                when (borderStyle) {
                    GlossaryBorderStyle.DOTTED -> {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                        drawLine(
                            color = ruleDotsColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect,
                        )
                    }
                    GlossaryBorderStyle.SOLID -> {
                        drawLine(
                            color = ruleColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                }
            }
            .padding(vertical = 8.dp),
    ) {
        // Term column — fixed width
        Box(modifier = Modifier.width(110.dp).padding(top = 1.dp)) {
            Text(
                text = term,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = QuadernoColors.Ink,
                    letterSpacing = (-0.01).sp,
                    lineHeight = 18.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Definition column — fills remaining space
        Text(
            text = definition,
            style = TextStyle(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = QuadernoColors.Ink2,
                lineHeight = (12.5 * 1.55).sp,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}
