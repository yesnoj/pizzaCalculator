package com.pizzalab.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Help / Glossary screen explaining pizza-making terminology for beginners.
 */
@Composable
fun HelpScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Glossario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tutti i termini che trovi nell'app, spiegati in modo semplice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Ingredienti e misure ──
        SectionTitle("Ingredienti e misure")

        GlossaryEntry(
            term = "Idratazione",
            definition = "La percentuale di acqua rispetto alla farina. Ad esempio, " +
                "63% di idratazione significa 630 ml di acqua per 1 kg di farina. " +
                "Un'idratazione più alta dà un impasto più morbido e una pizza più soffice, " +
                "ma è anche più difficile da lavorare."
        )

        GlossaryEntry(
            term = "Forza della farina (W)",
            definition = "Indica la capacità della farina di assorbire acqua e trattenere " +
                "i gas della lievitazione. Farine deboli (W 170-200) per lievitazioni brevi, " +
                "farine forti (W 280-350) per lievitazioni lunghe. Sul pacco della farina " +
                "spesso non è indicato il W, ma puoi stimarlo dalle proteine."
        )

        GlossaryEntry(
            term = "Percentuale del panificatore (Baker's %)",
            definition = "Sistema in cui ogni ingrediente è espresso come percentuale " +
                "rispetto al peso della farina. La farina è sempre il 100%. " +
                "Esempio: 63% idratazione, 2% sale, 0.1% lievito."
        )

        GlossaryEntry(
            term = "Lievito di birra fresco (LDB)",
            definition = "Il classico cubetto di lievito che trovi in frigo al supermercato. " +
                "Va sciolto in acqua tiepida. Si conserva pochi giorni in frigo."
        )

        GlossaryEntry(
            term = "Lievito secco (attivo / istantaneo)",
            definition = "Lievito disidratato in granuli. Ne serve circa 1/3 rispetto al " +
                "fresco. Il secco attivo va reidratato, l'istantaneo si aggiunge " +
                "direttamente alla farina."
        )

        GlossaryEntry(
            term = "Sale",
            definition = "Oltre al sapore, il sale rafforza il glutine e rallenta " +
                "la lievitazione. La dose classica napoletana è 40 g per litro " +
                "di acqua (circa il 2.5% sulla farina)."
        )

        GlossaryEntry(
            term = "Malto",
            definition = "Zucchero naturale che nutre il lievito e favorisce la " +
                "colorazione della crosta. Si usa in piccole dosi (0.1-0.5% sulla farina). " +
                "Non sempre necessario."
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Fasi della lievitazione ──
        SectionTitle("Fasi della lievitazione")

        GlossaryEntry(
            term = "Impasto",
            definition = "La fase in cui si mescolano tutti gli ingredienti e si lavora " +
                "l'impasto fino a sviluppare il glutine (la \"maglia glutinica\"). " +
                "Un buon impasto è liscio, elastico e non appiccicoso."
        )

        GlossaryEntry(
            term = "Autolisi",
            definition = "Riposo di farina e acqua (senza sale e lievito) per 20-60 minuti " +
                "prima di impastare. Permette alla farina di idratarsi e al glutine di " +
                "iniziare a formarsi naturalmente, rendendo l'impasto più estensibile."
        )

        GlossaryEntry(
            term = "Puntata (prima lievitazione)",
            definition = "La prima fase di riposo dell'impasto intero, subito dopo " +
                "l'impastamento. L'impasto fermenta e sviluppa aromi. Dura da " +
                "30 minuti (lievitazione rapida) a diverse ore."
        )

        GlossaryEntry(
            term = "Pieghe (folding)",
            definition = "Piegature dell'impasto durante la puntata per rafforzare la " +
                "struttura senza re-impastare. Si fanno 2-4 serie di pieghe a " +
                "intervalli regolari."
        )

        GlossaryEntry(
            term = "Staglio",
            definition = "La divisione dell'impasto in panetti del peso desiderato. " +
                "Ogni panetto viene arrotondato in una pallina liscia e tesa, " +
                "che diventerà una pizza."
        )

        GlossaryEntry(
            term = "Appretto (seconda lievitazione)",
            definition = "Il riposo dei panetti dopo lo staglio. È la fase finale " +
                "prima della stesura: i panetti si rilassano e lievitano " +
                "ulteriormente. L'impasto è pronto quando, premuto con un dito, " +
                "torna su lentamente."
        )

        GlossaryEntry(
            term = "Frigo (retarding)",
            definition = "Fase di lievitazione in frigorifero (4-6°C) che rallenta " +
                "il lievito e favorisce la maturazione. Sviluppa aromi più complessi " +
                "e rende l'impasto più digeribile. Usato nelle lievitazioni lunghe " +
                "(12-72 ore)."
        )

        GlossaryEntry(
            term = "Maturazione",
            definition = "Processo in cui gli enzimi della farina scompongono amidi " +
                "e proteine, rendendo l'impasto più digeribile. Non è la stessa " +
                "cosa della lievitazione: un impasto può essere lievitato ma " +
                "non ancora maturo."
        )

        GlossaryEntry(
            term = "Temperatura di chiusura",
            definition = "La temperatura dell'impasto alla fine dell'impastamento. " +
                "È importante perché influenza la velocità della lievitazione. " +
                "Idealmente 22-24°C."
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Tecniche e prefermenti ──
        SectionTitle("Tecniche e prefermenti")

        GlossaryEntry(
            term = "Biga",
            definition = "Pre-impasto asciutto (44-50% di idratazione) fatto con " +
                "farina, acqua e poco lievito, che fermenta 16-24 ore. " +
                "Dà alla pizza più struttura, friabilità e aromi complessi. " +
                "Si aggiunge poi all'impasto finale (\"rinfresco\")."
        )

        GlossaryEntry(
            term = "Rinfresco",
            definition = "L'aggiunta di farina, acqua e altri ingredienti alla biga " +
                "matura per completare l'impasto. Anche usato per il lievito " +
                "madre: rinfrescare = nutrire con nuova farina e acqua."
        )

        GlossaryEntry(
            term = "Lievito madre (pasta madre)",
            definition = "Impasto di farina e acqua fermentato con lieviti e " +
                "batteri naturali. Dà sapori più complessi del lievito di birra, " +
                "ma richiede manutenzione regolare (rinfreschi)."
        )

        GlossaryEntry(
            term = "Li.Co.Li.",
            definition = "Lievito in Coltura Liquida: versione del lievito madre con " +
                "idratazione al 100% (pari quantità di farina e acqua). Più facile " +
                "da gestire rispetto alla pasta madre solida."
        )

        GlossaryEntry(
            term = "Mix farine",
            definition = "Miscela di due o più farine con forza (W) diversa per ottenere " +
                "una forza intermedia. Utile quando non si trova la farina della " +
                "forza desiderata. Il W risultante è la media ponderata dei W " +
                "delle singole farine. Il calcolatore Mix nell'app stima anche " +
                "l'idratazione massima raggiungibile con il mix."
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Cottura ──
        SectionTitle("Cottura")

        GlossaryEntry(
            term = "Stesura",
            definition = "L'operazione di allargare il panetto a disco. La pizza " +
                "napoletana si stende a mano (mai col mattarello!) partendo " +
                "dal centro e lasciando il bordo più alto (il \"cornicione\")."
        )

        GlossaryEntry(
            term = "Cornicione",
            definition = "Il bordo rialzato della pizza. Si forma naturalmente se " +
                "durante la stesura si lascia l'aria nel bordo senza schiacciarlo."
        )

        GlossaryEntry(
            term = "Pala",
            definition = "Pizza stesa su teglia o pietra, tipicamente rettangolare, " +
                "cotta in forno a 280-320°C per 3-5 minuti."
        )

        GlossaryEntry(
            term = "Teglia",
            definition = "Pizza cotta direttamente nella teglia, spesso con " +
                "idratazione alta (70-80%). Il peso dell'impasto si calcola " +
                "in base alla superficie della teglia: 0.50 g/cm² per teglia " +
                "spessa condita, 0.375 g/cm² per fina (stile Roscioli), " +
                "0.58 g/cm² per bianca."
        )

        GlossaryEntry(
            term = "Tonda Romana",
            definition = "Pizza tonda romana al piatto, croccante e sottile. " +
                "Panetti da 180g, farina W 300-320 con aggiunta di semola " +
                "e tipo 1/integrale. Si stende col mattarello (a differenza " +
                "della napoletana). Cottura a 290°C platea e cielo, ~2 minuti."
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // ── Contenitore ──
        SectionTitle("Strumenti")

        GlossaryEntry(
            term = "Contenitore per lievitazione",
            definition = "Il recipiente dove riposa l'impasto. La regola è: " +
                "volume del contenitore = peso impasto × 2.4. " +
                "Per esempio, 1 kg di impasto ha bisogno di un contenitore " +
                "da almeno 2.4 litri."
        )

        GlossaryEntry(
            term = "Conversione lievito",
            definition = "I diversi tipi di lievito non sono intercambiabili 1:1. " +
                "Regola base: 1 g di lievito fresco = 0.33 g di secco attivo = " +
                "0.5 g di secco Caputo. Il lievito madre richiede circa 53 volte " +
                "il peso del lievito fresco equivalente."
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun GlossaryEntry(term: String, definition: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = term,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
