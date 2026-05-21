/**
 * PizzaFormulas.kt — Implementazione delle formule pizzaiole utilizzate da Pizza Calc.
 *
 * Tutte le funzioni sono pure: non fanno I/O, non hanno side-effects,
 * sono facilmente testabili e riutilizzabili.
 *
 * ================================================================
 * Note sulle scelte di calibrazione (bug-fix interni)
 * ================================================================
 * Le tabelle di lookup per il Calcolatore Facile presentavano alcune
 * incoerenze nella versione iniziale che sono state corrette:
 *
 *   1. Facile 8H, T 28-30°C: la distribuzione (puntata + frigo + apretto)
 *      sommava 7h invece delle 8h prescritte. Fix: apretto esteso da
 *      4:00 a 5:00, totale ripristinato a 8h.
 *
 *   2. Facile 12H, T=21°C: la tabella del lievito fresco aveva un
 *      valore (1.32 g) che rompeva la monotonicità decrescente del
 *      regime no-frigo (T 18-21). Fix: valore corretto a 1.05 g,
 *      coerente con il decay osservato a T 18-20 (~-0.18 g/°C).
 *
 *   3. Teglia quadrata: la formula corretta `peso = 0.58 × lato²` è
 *      applicata; nessun valore fisso.
 *
 * Moduli coperti:
 * - Calcolatori pizza: Facile, Avanzato, PRO, Teglia
 * - Strumenti farina: Mix Farine, Forza da Proteine
 * - Altri: Contenitore, Conversione Lievito
 * - Pre-fermenti: Biga, Biga Fast
 */

package com.pizzalab.domain

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.round

// ============================================================
// DATA CLASSES
// ============================================================

data class RicettaFacile(
    val farina: Double,
    val acqua: Double,
    val sale: Double,
    val ldbf: Double,
    val ldbs: Double,
    val totale: Double,
    val forza: Int,
    val tempChiusura: Int,
    val puntataMin: Int,
    val frigoMin: Int,
    val aprettoMin: Int,
    val inizio: LocalDateTime,
    val inizioApretto: LocalDateTime,
    val pronti: LocalDateTime
)

data class RicettaAvanzata(
    val farina: Double,
    val acqua: Double,
    val sale: Double,
    val olio: Double,
    val malto: Double,
    val ldbf: Double,
    val ldbs: Double,
    val totale: Double,
    val forza: Int,
    val tempChiusura: Int,
    val puntataMin: Int,
    val frigoMin: Int,
    val aprettoMin: Int,
    val ciclo: String,
    val inizio: LocalDateTime,
    val inizioApretto: LocalDateTime,
    val pronti: LocalDateTime
)

data class RicettaPro(
    val farina: Double,
    val acqua: Double,
    val sale: Double,
    val olio: Double,
    val malto: Double,
    val ldbf: Double,
    val ldbs: Double,
    val totale: Double
)

data class RicettaTeglia(
    val pesoPanetto: Double,
    val areaCm2: Double,
    val farina: Double,
    val acqua: Double,
    val sale: Double,
    val olio: Double,
    val ldbf: Double,
    val ldbs: Double,
    val totale: Double,
    val forza: Int
)

data class ConversioneLievito(
    val ldbfInput: Double,       // input: lievito di birra fresco
    val ldbs: Double,            // lievito secco attivo
    val ldbc: Double,            // lievito secco Caputo
    val lm: Double,              // lievito madre solido (idro 50%)
    val farinaLm: Double,        // farina aggiustata per LM
    val acquaLm: Double,         // acqua aggiustata per LM
    val licoli: Double,          // lievito madre Li.Co.Li (idro 100%)
    val farinaLicoli: Double,
    val acquaLicoli: Double
)

data class RicettaBiga(
    val farinaTot: Double,
    val acquaTot: Double,
    val saleTot: Double,
    val grassiTot: Double,
    val farinaBiga: Double,
    val acquaBiga: Double,
    val lievitoBiga: Double,
    val farinaRinfresco: Double,
    val acquaRinfresco: Double,
    val saleRinfresco: Double,
    val grassiRinfresco: Double,
    val oreBiga: Double,
    val oreBigaStr: String,
    val idroBigaPct: Int,
    val forzaBiga: Int,
    val forzaRinfresco: Int
)

data class MixFarineResult(
    val wMedio: Int,
    val idroMaxSicura: Int,       // idratazione max sicura (principianti)
    val idroMaxAvanzata: Int,     // idratazione max per esperti
    val pesoTotale: Double,
    val percentuali: List<Double> // % di ogni farina sul totale
)

data class RicettaBigaFast(
    val farinaTot: Double,
    val acquaTot: Double,
    val saleTot: Double,
    val grassiTot: Double,
    val acquaBiga: Double,
    val lievitoBiga: Double,
    val acquaRinfresco: Double,
    val saleRinfresco: Double,
    val grassiRinfresco: Double,
    val oreBigaStr: String = "03:00",
    val idroBigaPct: Int = 50,
    val forzaBiga: Int = 300
)

// ============================================================
// OGGETTO PRINCIPALE
// ============================================================

object PizzaFormulas {

    // ============================================================
    // COSTANTI
    // ============================================================
    const val SALE_PER_LITRO_NAPOLETANO = 40.0       // g/L acqua (4% acqua)
    const val TEGLIA_GR_PER_CM2 = 0.58                // peso impasto per cm² di teglia (bianca)
    const val TEGLIA_GR_PER_CM2_SPESSA = 0.50         // teglia spessa condita
    const val TEGLIA_GR_PER_CM2_FINA = 0.375          // teglia fina / stile Roscioli
    const val TEGLIA_GR_PER_CM2_BIANCA = 0.58         // teglia bianca (più alta)
    const val CONTENITORE_MOLTIPLICATORE = 2.4        // volume contenitore = peso × 2.4
    const val LM_LICOLI_FACTOR = 53                   // convenzione "ricca di LM" (vedi README)
    const val LIEVITO_FRESCO_SECCO_ATTIVO_RATIO = 3   // LDBS = LDB / 3
    const val LIEVITO_FRESCO_SECCO_CAPUTO_RATIO = 2   // LDBC = LDB / 2

    // ============================================================
    // CALCOLATORE RAPIDO — lievitazioni brevi (1-6 ore)
    // ============================================================

    /**
     * Tabella di riferimento dough.school — percentuali di lievito secco istantaneo
     * sulla farina (Baker's %) in funzione di ore di lievitazione e temperatura.
     *
     * Fonte: dough.school (tabella ampiamente usata nella comunità pizzaiola).
     * Conversioni: lievito fresco = istantaneo × 3, secco Caputo = istantaneo × 1.5.
     *
     * Per tempi < 4h (non presenti nella tabella originale), i valori sono estrapolati
     * mantenendo la stessa curva esponenziale osservata tra 4h e 6h.
     */
    private val RAPID_YEAST_TABLE: Map<Int, Map<Int, Double>> = mapOf(
        // ore -> (temperatura °C -> % lievito secco istantaneo sulla farina)
        // Valori a 4h e 6h: dati dough.school (riferimento)
        // Valori a 1h, 2h, 3h: estrapolazione esponenziale dalla curva 4h→6h
        1 to mapOf(20 to 3.85, 25 to 2.83, 30 to 1.98),
        2 to mapOf(20 to 2.81, 25 to 2.00, 30 to 1.40),
        3 to mapOf(20 to 2.05, 25 to 1.41, 30 to 0.99),
        4 to mapOf(20 to 1.50, 25 to 1.00, 30 to 0.70),
        6 to mapOf(20 to 0.80, 25 to 0.50, 30 to 0.35)
    )

    /** Temperature di ancoraggio nella tabella rapida. */
    private val RAPID_TEMPS = listOf(20, 25, 30)

    /** Ore di ancoraggio nella tabella rapida. */
    private val RAPID_HOURS = listOf(1, 2, 3, 4, 6)

    /**
     * Interpolazione log-lineare del lievito secco istantaneo (%) dalla tabella.
     * Il lievito segue una curva esponenziale sia nel tempo che nella temperatura,
     * quindi interpoliamo in scala logaritmica per risultati più accurati.
     */
    private fun interpolateYeastPct(ore: Int, tempC: Int): Double {
        // Clamp ai limiti della tabella
        val t = tempC.coerceIn(20, 30).toDouble()
        val h = ore.coerceIn(1, 6).toDouble()

        // Trova gli intervalli di ore che racchiudono il valore
        val hLo = RAPID_HOURS.lastOrNull { it <= ore } ?: RAPID_HOURS.first()
        val hHi = RAPID_HOURS.firstOrNull { it >= ore } ?: RAPID_HOURS.last()

        // Trova gli intervalli di temperatura che racchiudono il valore
        val tLo = RAPID_TEMPS.lastOrNull { it <= tempC } ?: RAPID_TEMPS.first()
        val tHi = RAPID_TEMPS.firstOrNull { it >= tempC } ?: RAPID_TEMPS.last()

        // Valori ai 4 angoli della griglia
        val vLoLo = RAPID_YEAST_TABLE[hLo]!![tLo]!!
        val vLoHi = RAPID_YEAST_TABLE[hLo]!![tHi]!!
        val vHiLo = RAPID_YEAST_TABLE[hHi]!![tLo]!!
        val vHiHi = RAPID_YEAST_TABLE[hHi]!![tHi]!!

        // Interpolazione bilineare in scala log
        val lnLoLo = Math.log(vLoLo)
        val lnLoHi = Math.log(vLoHi)
        val lnHiLo = Math.log(vHiLo)
        val lnHiHi = Math.log(vHiHi)

        // Frazioni di interpolazione
        val fH = if (hHi == hLo) 0.0 else (h - hLo) / (hHi - hLo)
        val fT = if (tHi == tLo) 0.0 else (t - tLo) / (tHi - tLo)

        // Bilineare: interpola prima lungo le ore, poi lungo la temperatura
        val lnAtTLo = lnLoLo + fH * (lnHiLo - lnLoLo)
        val lnAtTHi = lnLoHi + fH * (lnHiHi - lnLoHi)
        val lnResult = lnAtTLo + fT * (lnAtTHi - lnAtTLo)

        return Math.exp(lnResult)
    }

    /**
     * Calcola il lievito per lievitazioni brevi (1-6h).
     *
     * Usa la tabella dough.school (lievito secco istantaneo %) con interpolazione
     * bilineare in scala logaritmica. Per temperature fuori dal range 20-30°C,
     * estrapola dalla curva esponenziale osservata nella tabella.
     */
    fun calcRapido(
        nPanetti: Int,
        pesoPanetto: Double,
        lievOre: Int,
        T: Int,
        idroPct: Double,
        salePctAcqua: Double = 4.0,  // default napoletano: 40g/L = 4%
        inizio: LocalDateTime? = null
    ): RicettaFacile {
        require(lievOre in 1..6) { "Ore lievitazione rapida: 1-6" }
        require(T in 18..35) { "Temperatura ambiente deve essere tra 18 e 35 °C" }
        require(idroPct in 55.0..80.0) { "Idratazione deve essere tra 55 e 80 %" }

        val start = inizio ?: LocalDateTime.now().withSecond(0).withNano(0)

        val totaleTarget = nPanetti * pesoPanetto + nPanetti * 0.5
        val idro = idroPct / 100.0

        // Lievito secco istantaneo (%) dalla tabella dough.school
        var istantaneoPct = interpolateYeastPct(lievOre, T)

        // Estrapolazione per temperature fuori tabella (18-19°C e 31-35°C)
        // Usa il fattore esponenziale osservato nella tabella: ~-34% per +5°C
        if (T < 20) {
            val ref = interpolateYeastPct(lievOre, 20)
            istantaneoPct = ref * Math.pow(1.34, (20.0 - T) / 5.0)
        } else if (T > 30) {
            val ref = interpolateYeastPct(lievOre, 30)
            istantaneoPct = ref * Math.pow(0.66, (T - 30.0) / 5.0)
        }

        // Conversione: lievito fresco = istantaneo × 3
        val ldbfPct = istantaneoPct / 100.0 * 3.0

        val salePct = idro * (salePctAcqua / 100.0)

        val farina = totaleTarget / (1 + idro + salePct + ldbfPct)
        val acqua = idro * farina
        val sale = salePct * farina
        val ldbf = ldbfPct * farina
        val ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

        // Distribuzione: niente frigo, puntata breve + appretto
        val totaleMinuti = lievOre * 60
        val puntataMin = (totaleMinuti * 0.6).toInt()  // 60% puntata
        val aprettoMin = totaleMinuti - puntataMin       // 40% appretto

        val inizioApretto = start.plusMinutes(puntataMin.toLong())
        val pronti = start.plusMinutes(totaleMinuti.toLong())

        // Forza consigliata: per tempi brevi servono farine meno forti
        val forza = when {
            lievOre <= 2 -> 200
            lievOre <= 4 -> 220
            else -> 240
        }

        return RicettaFacile(
            farina = roundTo(farina, 1),
            acqua = roundTo(acqua, 1),
            sale = roundTo(sale, 2),
            ldbf = roundTo(ldbf, 2),
            ldbs = roundTo(ldbs, 2),
            totale = roundTo(farina + acqua + sale + ldbf, 1),
            forza = forza,
            tempChiusura = 24,
            puntataMin = puntataMin,
            frigoMin = 0,
            aprettoMin = aprettoMin,
            inizio = start,
            inizioApretto = inizioApretto,
            pronti = pronti
        )
    }

    // ============================================================
    // CALCOLATORE DA FARINA — logica inversa
    // ============================================================

    /**
     * Calcola la % di lievito fresco sulla farina per un dato tempo/temperatura.
     * Usata internamente per il calcolo diretto da farina.
     */
    private fun ldbfPctForParams(lievOre: Int, T: Int): Double {
        return if (lievOre <= 6) {
            var ist = interpolateYeastPct(lievOre, T)
            if (T < 20) {
                ist = interpolateYeastPct(lievOre, 20) * Math.pow(1.34, (20.0 - T) / 5.0)
            } else if (T > 30) {
                ist = interpolateYeastPct(lievOre, 30) * Math.pow(0.66, (T - 30.0) / 5.0)
            }
            ist / 100.0 * 3.0
        } else {
            val bucket = listOf(8, 12, 24, 48).minByOrNull { abs(it - lievOre) }!!
            LDBF_TABLE_FACILE[bucket]!![T.coerceIn(18, 30)]!! / 604.0
        }
    }

    /**
     * Dato un peso di farina, calcola il peso totale dell'impasto.
     * Utile per determinare limiti dinamici degli slider.
     */
    fun totaleDaFarina(farinaG: Double, idroPct: Double, lievOre: Int, T: Int): Double {
        val idro = idroPct / 100.0
        val salePct = idro * 0.04
        return farinaG * (1 + idro + salePct + ldbfPctForParams(lievOre, T))
    }

    /**
     * Calcolatore "Da Farina" — l'utente fissa la farina e il calcolatore
     * calcola direttamente acqua, sale, lievito e il numero/peso dei panetti.
     *
     * A differenza di calcFacile/calcRapido che partono da nPanetti × pesoPanetto,
     * qui la farina è il dato di partenza e non viene ricalcolata.
     *
     * @param farinaG     grammi di farina (input utente)
     * @param idroPct     idratazione in %
     * @param lievOre     ore di lievitazione
     * @param T           temperatura ambiente in °C
     * @param salePctAcqua sale in % sull'acqua (default 4%)
     * @param inizio      orario di inizio (default: ora attuale)
     * @return RicettaFacile con i valori calcolati dalla farina esatta
     */
    fun calcDaFarina(
        farinaG: Double,
        idroPct: Double,
        lievOre: Int,
        T: Int,
        salePctAcqua: Double = 4.0,
        inizio: LocalDateTime? = null
    ): RicettaFacile {
        val start = inizio ?: LocalDateTime.now().withSecond(0).withNano(0)
        val idro = idroPct / 100.0
        val salePct = idro * (salePctAcqua / 100.0)
        val ldbfPct = ldbfPctForParams(lievOre, T)

        val farina = farinaG
        val acqua = idro * farina
        val sale = salePct * farina
        val ldbf = ldbfPct * farina
        val ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

        // Timeline e forza
        val forza: Int
        val tempChiusura: Int
        val puntataMin: Int
        val frigoMin: Int
        val aprettoMin: Int

        if (lievOre <= 6) {
            // Modalità rapida
            val totaleMinuti = lievOre * 60
            puntataMin = (totaleMinuti * 0.6).toInt()
            aprettoMin = totaleMinuti - puntataMin
            frigoMin = 0
            forza = when {
                lievOre <= 2 -> 200
                lievOre <= 4 -> 220
                else -> 240
            }
            tempChiusura = 24
        } else {
            // Modalità classica: usa le stesse tabelle di distribuzione
            val bucket = listOf(8, 12, 24, 48).minByOrNull { abs(it - lievOre) }!!
            val clampedT = T.coerceIn(18, 30)
            val (p, f, a) = getDistributionFacile(bucket, clampedT)
            puntataMin = p
            frigoMin = f
            aprettoMin = a
            forza = forzaFacile(bucket, clampedT)
            tempChiusura = tempChiusuraFacile(bucket, clampedT)
        }

        val inizioApretto = start.plusMinutes((puntataMin + frigoMin).toLong())
        val pronti = inizioApretto.plusMinutes(aprettoMin.toLong())

        return RicettaFacile(
            farina = roundTo(farina, 1),
            acqua = roundTo(acqua, 1),
            sale = roundTo(sale, 2),
            ldbf = roundTo(ldbf, 2),
            ldbs = roundTo(ldbs, 2),
            totale = roundTo(farina + acqua + sale + ldbf, 1),
            forza = forza,
            tempChiusura = tempChiusura,
            puntataMin = puntataMin,
            frigoMin = frigoMin,
            aprettoMin = aprettoMin,
            inizio = start,
            inizioApretto = inizioApretto,
            pronti = pronti
        )
    }

    // ============================================================
    // CALCOLATORE FACILE — tabelle di lookup
    // ============================================================

    // Lievito fresco (g) di riferimento per ~604g di farina (4 panetti × 250g,
    // idro 63%, default). Per altri pesi il valore scala linearmente con la farina.
    val LDBF_TABLE_FACILE: Map<Int, Map<Int, Double>> = mapOf(
        8 to mapOf(
            18 to 2.54, 19 to 2.22, 20 to 1.95, 21 to 1.73, 22 to 1.54, 23 to 1.38,
            24 to 1.24, 25 to 1.12, 26 to 1.01, 27 to 0.92, 28 to 0.84, 29 to 0.77, 30 to 0.71
        ),
        // 12H: bug-fix a T=21 (sito: 1.32, rompe la monotonicità del regime no-frigo).
        // In regime no-frigo (T 18-21) i valori 1.56, 1.38, 1.20 hanno decay -0.18 per +1°C.
        // L'estrapolazione naturale a T=21 è ~1.05 (lineare 1.02, esponenziale 1.04).
        // Sostituiamo 1.32 → 1.05 per ripristinare il decay monotono. A T=22 entra il
        // frigo e il valore risale legittimamente (cambio di regime).
        12 to mapOf(
            18 to 1.56, 19 to 1.38, 20 to 1.20, 21 to 1.05, 22 to 1.45, 23 to 1.40,
            24 to 1.34, 25 to 1.31, 26 to 1.28, 27 to 1.27, 28 to 1.26, 29 to 1.16, 30 to 1.06
        ),
        24 to mapOf(
            18 to 1.83, 19 to 1.70, 20 to 1.57, 21 to 1.44, 22 to 1.31, 23 to 1.25,
            24 to 1.19, 25 to 1.15, 26 to 1.12, 27 to 1.06, 28 to 1.01, 29 to 0.93, 30 to 0.85
        ),
        48 to mapOf(
            18 to 1.43, 19 to 1.32, 20 to 1.20, 21 to 1.10, 22 to 0.99, 23 to 0.94,
            24 to 0.88, 25 to 0.84, 26 to 0.80, 27 to 0.75, 28 to 0.70, 29 to 0.65, 30 to 0.59
        )
    )

    // Forza farina (W) consigliata
    fun forzaFacile(liev: Int, T: Int): Int {
        if (liev == 8) return if (T >= 25) 260 else 240
        if (liev == 12) {
            if (T <= 21) return 260
            if (T <= 25) return 290
            return 330
        }
        if (liev == 24) {
            if (T <= 23) return 250
            if (T <= 25) return 240
            return 260
        }
        if (liev == 48) {
            if (T <= 21) return 300
            if (T <= 25) return 320
            return 330
        }
        return 250
    }

    /** Temperatura target dell'impasto a fine impastatura. */
    fun tempChiusuraFacile(liev: Int, T: Int): Int {
        if (liev == 8 && T >= 25) return 22
        return 24
    }

    /** Restituisce Triple(puntata_min, frigo_min, apretto_min) per liev/T date. */
    fun getDistributionFacile(liev: Int, T: Int): Triple<Int, Int, Int> {
        if (liev == 8) {
            if (T <= 23) return Triple(30, 0, 450)
            if (T == 24) return Triple(60, 0, 420)
            if (T == 25) return Triple(90, 0, 390)
            if (T == 26) return Triple(105, 0, 375)
            if (T == 27) return Triple(120, 0, 360)
            // Bug-fix: la calibrazione originaria per T 28-30 restituiva (180, 0, 240),
            // somma 7h invece di 8h come per le altre temperature. Estendiamo
            // l'apretto a 300 min per ripristinare il totale di 8h.
            return Triple(180, 0, 300)
        }
        if (liev == 12) {
            if (T <= 21) return Triple(180, 0, 540)
            if (T <= 23) return Triple(120, 240, 360)
            if (T <= 25) return Triple(120, 300, 300)
            if (T <= 27) return Triple(60, 360, 300)
            return Triple(60, 420, 240)
        }
        if (liev == 24) {
            if (T <= 20) return Triple(150, 900, 390)
            if (T <= 23) return Triple(120, 990, 330)
            if (T <= 25) return Triple(120, 1050, 270)
            if (T <= 27) return Triple(60, 1110, 270)
            return Triple(60, 1140, 240)
        }
        if (liev == 48) {
            if (T <= 20) return Triple(150, 2340, 390)
            if (T <= 23) return Triple(120, 2430, 330)
            if (T <= 25) return Triple(120, 2490, 270)
            if (T <= 27) return Triple(60, 2550, 270)
            return Triple(60, 2580, 240)
        }
        throw IllegalArgumentException("liev non valido: $liev")
    }

    // ============================================================
    // HELPER: arrotondamento
    // ============================================================
    private fun roundTo(value: Double, decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return round(value * factor) / factor
    }

    // ============================================================
    // CALCOLATORE FACILE
    // ============================================================

    /** Calcolatore Facile — modalità guidata con scelte automatiche di tempi e lievito. */
    fun calcFacile(
        nPanetti: Int,
        pesoPanetto: Double,
        liev: Int,
        T: Int,
        idroPct: Double,
        inizio: LocalDateTime? = null
    ): RicettaFacile {
        require(liev in listOf(8, 12, 24, 48)) { "liev deve essere 8, 12, 24 o 48" }
        require(T in 18..30) { "Temperatura ambiente deve essere tra 18 e 30 °C" }
        require(idroPct in 59.0..70.0) { "Idratazione deve essere tra 59 e 70 %" }

        val start = inizio ?: LocalDateTime.now().withSecond(0).withNano(0)

        val totaleTarget = nPanetti * pesoPanetto + nPanetti * 0.5  // margine
        val idro = idroPct / 100.0

        // ldbf per 604g di farina (riferimento empirico)
        val ldbfRef = LDBF_TABLE_FACILE[liev]!![T]!!
        val ldbfPct = ldbfRef / 604.0  // frazione fissa di farina
        val salePct = idro * 0.04       // sale = 4% acqua = 4% × idro × farina

        // totale = farina × (1 + idro + sale_pct + ldbf_pct)
        val farina = totaleTarget / (1 + idro + salePct + ldbfPct)
        val acqua = idro * farina
        val sale = salePct * farina
        val ldbf = ldbfPct * farina
        val ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

        val (p, f, a) = getDistributionFacile(liev, T)
        val inizioApretto = start.plusMinutes((p + f).toLong())
        val pronti = start.plusMinutes((p + f + a).toLong())

        return RicettaFacile(
            farina = roundTo(farina, 1),
            acqua = roundTo(acqua, 1),
            sale = roundTo(sale, 2),
            ldbf = roundTo(ldbf, 2),
            ldbs = roundTo(ldbs, 2),
            totale = roundTo(farina + acqua + sale + ldbf, 1),
            forza = forzaFacile(liev, T),
            tempChiusura = tempChiusuraFacile(liev, T),
            puntataMin = p,
            frigoMin = f,
            aprettoMin = a,
            inizio = start,
            inizioApretto = inizioApretto,
            pronti = pronti
        )
    }

    // ============================================================
    // CALCOLATORE AVANZATO
    // ============================================================

    /** Classificazione dei cicli osservata empiricamente. */
    private fun cicloAvanzato(liev: Int, frigo: Int, T: Int): String {
        if (frigo == 0) {
            return if (T >= 25) "CICLO2" else "CICLO1"
        }
        if (liev <= 24) return "CICLO3"
        if (liev <= 36) return "CICLO4"
        return "CICLO5"
    }

    /** ldbf di riferimento per ~530g farina (default Avanzato 4×220, idro 63%). */
    private fun ldbfAvanzatoRef(liev: Int, T: Int): Double {
        val bucket = listOf(8, 12, 24, 48).minByOrNull { abs(it - liev) }!!
        val base = LDBF_TABLE_FACILE[bucket]!![T]!!
        // rapporto su farina: ldbf/604 nella tabella → applichiamo su 530
        return base * 530 / 604
    }

    /** Calcolatore Avanzato — controllo manuale di sale, grassi, tempi. */
    fun calcAvanzato(
        nPanetti: Int,
        pesoPanetto: Double,
        idroPct: Double,
        saleGPerL: Double,
        grassiGPerL: Double,
        maltoPct: Double,
        liev: Int,
        frigoH: Int,
        puntataMin: Int,
        aprettoMin: Int,
        T: Int,
        inizio: LocalDateTime? = null
    ): RicettaAvanzata {
        val start = inizio ?: LocalDateTime.now().withSecond(0).withNano(0)

        val totaleTarget = nPanetti * pesoPanetto + nPanetti * 0.5
        val idro = idroPct / 100.0

        // ldbf empirico (% sulla farina costante per tabella facile)
        val ldbfRef = ldbfAvanzatoRef(liev, T)
        val ldbfPct = ldbfRef / 530.0
        val salePct = idro * saleGPerL / 1000.0
        val olioPct = idro * grassiGPerL / 1000.0
        val maltoPctFrac = maltoPct / 100.0

        val farina = totaleTarget / (1 + idro + salePct + olioPct + ldbfPct + maltoPctFrac)
        val acqua = idro * farina
        val sale = salePct * farina
        val olio = olioPct * farina
        val malto = maltoPctFrac * farina
        val ldbf = ldbfPct * farina
        val ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

        val inizioApretto = start.plusMinutes((puntataMin + frigoH * 60).toLong())
        val pronti = inizioApretto.plusMinutes(aprettoMin.toLong())

        val bucket = listOf(8, 12, 24, 48).minByOrNull { abs(it - liev) }!!

        return RicettaAvanzata(
            farina = roundTo(farina, 1),
            acqua = roundTo(acqua, 1),
            sale = roundTo(sale, 2),
            olio = roundTo(olio, 2),
            malto = roundTo(malto, 2),
            ldbf = roundTo(ldbf, 2),
            ldbs = roundTo(ldbs, 2),
            totale = roundTo(farina + acqua + sale + olio + malto + ldbf, 1),
            forza = forzaFacile(bucket, T),
            tempChiusura = if (T < 25) 24 else 22,
            puntataMin = puntataMin,
            frigoMin = frigoH * 60,
            aprettoMin = aprettoMin,
            ciclo = cicloAvanzato(liev, frigoH, T),
            inizio = start,
            inizioApretto = inizioApretto,
            pronti = pronti
        )
    }

    // ============================================================
    // CALCOLATORE PRO — aritmetica pura
    // ============================================================

    /** Calcolatore PRO — tutto in % sulla farina, niente logica di lievitazione. */
    fun calcPro(
        nPanetti: Int,
        pesoPanetto: Double,
        idroPct: Double,
        salePctFarina: Double,
        grassiPctFarina: Double,
        lievitoPctFarina: Double,
        maltoPctFarina: Double
    ): RicettaPro {
        require(salePctFarina <= 5) { "Sale max 5% nel PRO" }
        require(grassiPctFarina <= 5) { "Grassi max 5% nel PRO" }

        val totaleTarget = nPanetti * pesoPanetto.toDouble()
        val i = idroPct / 100.0
        val s = salePctFarina / 100.0
        val g = grassiPctFarina / 100.0
        val l = lievitoPctFarina / 100.0
        val m = maltoPctFarina / 100.0

        val farina = totaleTarget / (1 + i + s + g + l + m)
        val acqua = i * farina
        val sale = s * farina
        val olio = g * farina
        val malto = m * farina
        val ldbf = l * farina
        val ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

        return RicettaPro(
            farina = roundTo(farina, 1),
            acqua = roundTo(acqua, 1),
            sale = roundTo(sale, 2),
            olio = roundTo(olio, 2),
            malto = roundTo(malto, 2),
            ldbf = roundTo(ldbf, 2),
            ldbs = roundTo(ldbs, 2),
            totale = roundTo(farina + acqua + sale + olio + malto + ldbf, 1)
        )
    }

    // ============================================================
    // CALCOLATORE TEGLIA
    // ============================================================

    /**
     * Calcola peso impasto e area dalla geometria della teglia.
     *
     * forma: "rotonda" (d=diametro), "rettangolare" (rl1, rl2), "quadrata" (rl1)
     * tipoTeglia: "spessa" (condita), "fina" (Roscioli), "bianca" (default)
     * Restituisce Pair(peso_g, area_cm2).
     */
    fun calcolaPesoTeglia(
        forma: String,
        d: Double = 0.0,
        rl1: Double = 0.0,
        rl2: Double = 0.0,
        tipoTeglia: String = "bianca"
    ): Pair<Double, Double> {
        val area = when (forma) {
            "rotonda" -> PI * (d / 2.0) * (d / 2.0)
            "quadrata" -> rl1 * rl1
            "rettangolare" -> rl1 * rl2
            else -> throw IllegalArgumentException("Forma sconosciuta: $forma")
        }
        val coefficiente = when (tipoTeglia) {
            "spessa" -> TEGLIA_GR_PER_CM2_SPESSA
            "fina" -> TEGLIA_GR_PER_CM2_FINA
            "bianca" -> TEGLIA_GR_PER_CM2_BIANCA
            else -> TEGLIA_GR_PER_CM2
        }
        val peso = coefficiente * area
        return Pair(peso, area)
    }

    /** Calcolatore Teglia — peso panetto dall'area teglia, idro libera. */
    fun calcTeglia(
        forma: String,
        nTeglia: Int,
        idroPct: Double,
        saleGPerL: Double,
        grassiGPerL: Double,
        liev: Int,
        frigoH: Int,
        puntataMin: Int,
        aprettoMin: Int,
        T: Int,
        inizio: LocalDateTime? = null,
        d: Double = 0.0,
        rl1: Double = 0.0,
        rl2: Double = 0.0
    ): RicettaTeglia {
        val (peso, area) = calcolaPesoTeglia(forma, d = d, rl1 = rl1, rl2 = rl2)
        val avanzata = calcAvanzato(
            nPanetti = nTeglia,
            pesoPanetto = peso,
            idroPct = idroPct,
            saleGPerL = saleGPerL,
            grassiGPerL = grassiGPerL,
            maltoPct = 0.0,
            liev = liev,
            frigoH = frigoH,
            puntataMin = puntataMin,
            aprettoMin = aprettoMin,
            T = T,
            inizio = inizio
        )
        return RicettaTeglia(
            pesoPanetto = roundTo(peso, 1),
            areaCm2 = roundTo(area, 1),
            farina = avanzata.farina,
            acqua = avanzata.acqua,
            sale = avanzata.sale,
            olio = avanzata.olio,
            ldbf = avanzata.ldbf,
            ldbs = avanzata.ldbs,
            totale = avanzata.totale,
            forza = avanzata.forza
        )
    }

    // ============================================================
    // MIX FARINE — regola dell'alligation
    // ============================================================

    /** Restituisce Pair(farina_1_g, farina_2_g) per ottenere Wmix da W1+W2. */
    fun mixFarine(tG: Double, w1: Double, w2: Double, wMix: Double): Pair<Double, Double> {
        require(w1 != w2) { "W1 e W2 devono essere diversi" }
        val lo = minOf(w1, w2)
        val hi = maxOf(w1, w2)
        require(wMix in lo..hi) { "Wmix=$wMix deve essere tra $lo e $hi" }
        val f1 = tG * (w2 - wMix) / (w2 - w1)
        val f2 = tG - f1
        return Pair(roundTo(f1, 1), roundTo(f2, 1))
    }

    // ============================================================
    // CALCOLATORE MIX FARINE — W medio + idratazione massima
    // ============================================================

    /**
     * Stima l'idratazione massima raccomandata dato un valore di forza W.
     *
     * Formula empirica calibrata su dati della comunità pizzaiola:
     *   sicura = 40 + W × 0.108     (adatta a principianti / impasto a mano)
     *   avanzata = sicura + 5        (per esperti / impasto con planetaria)
     *
     * Range W supportato: 100-450. Valori fuori range vengono clampati.
     */
    fun idroMaxDaW(w: Int): Pair<Int, Int> {
        val wClamped = w.coerceIn(100, 450)
        val sicura = (40.0 + wClamped * 0.108).toInt().coerceIn(50, 90)
        val avanzata = (sicura + 5).coerceIn(55, 95)
        return Pair(sicura, avanzata)
    }

    /**
     * Calcola il W medio ponderato di un mix di farine e stima l'idratazione
     * massima raggiungibile.
     *
     * @param pesiG    Lista dei pesi in grammi di ogni farina
     * @param wValues  Lista dei valori W corrispondenti
     * @return MixFarineResult con W medio, idratazione max e percentuali
     */
    fun calcMixFarine(pesiG: List<Double>, wValues: List<Int>): MixFarineResult {
        require(pesiG.size == wValues.size) { "pesiG e wValues devono avere la stessa lunghezza" }
        require(pesiG.size in 2..4) { "Serve un mix di 2-4 farine" }
        require(pesiG.all { it > 0 }) { "Tutti i pesi devono essere > 0" }

        val pesoTotale = pesiG.sum()
        val percentuali = pesiG.map { roundTo(it / pesoTotale * 100.0, 1) }

        // Media ponderata del W
        val wMedio = round(
            pesiG.zip(wValues).sumOf { (peso, w) -> peso * w } / pesoTotale
        ).toInt()

        val (sicura, avanzata) = idroMaxDaW(wMedio)

        return MixFarineResult(
            wMedio = wMedio,
            idroMaxSicura = sicura,
            idroMaxAvanzata = avanzata,
            pesoTotale = roundTo(pesoTotale, 1),
            percentuali = percentuali
        )
    }

    // ============================================================
    // FORZA DALLE PROTEINE
    // ============================================================

    /** W = 40 × P − 240 (lineare). Pensata per farine 0/00 con P >= 7-8%. */
    fun forzaDaProteine(pPct: Double): Int {
        require(pPct >= 6) { "Proteine < 6%: formula non sensata" }
        return round(40 * pPct - 240).toInt()
    }

    // ============================================================
    // CONTENITORE
    // ============================================================

    /** V = I × 2.4. Volume in ml consigliato per il contenitore di lievitazione. */
    fun contenitoreVolumeMl(pesoImpastoG: Double): Double {
        return roundTo(pesoImpastoG * CONTENITORE_MOLTIPLICATORE, 1)
    }

    // ============================================================
    // CONVERSIONE LIEVITO
    // ============================================================

    /** Converte LDB fresco in altri tipi di lievito, aggiustando farina/acqua. */
    fun conversioneLievito(l: Double, f: Double, a: Double): ConversioneLievito {
        val ldbs = l / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO
        val ldbc = l / LIEVITO_FRESCO_SECCO_CAPUTO_RATIO
        val lm = l * LM_LICOLI_FACTOR
        // LM solido = 1/3 farina + 2/3 acqua
        val farinaLm = f - lm / 3.0
        val acquaLm = a - 2.0 * lm / 3.0
        // LiCoLi = 50% farina + 50% acqua
        val licoli = l * LM_LICOLI_FACTOR
        val farinaLicoli = f - licoli / 2.0
        val acquaLicoli = a - licoli / 2.0
        return ConversioneLievito(
            ldbfInput = l,
            ldbs = roundTo(ldbs, 2),
            ldbc = roundTo(ldbc, 2),
            lm = roundTo(lm, 1),
            farinaLm = roundTo(farinaLm, 0),
            acquaLm = roundTo(acquaLm, 0),
            licoli = roundTo(licoli, 1),
            farinaLicoli = roundTo(farinaLicoli, 0),
            acquaLicoli = roundTo(acquaLicoli, 0)
        )
    }

    // ============================================================
    // CALCOLATORE BIGA (beta)
    // ============================================================

    /** Tabella idro biga in funzione di TA. */
    private fun idroBiga(ta: Int): Int {
        if (ta <= 18) return 45
        if (ta <= 22) return 44
        if (ta <= 25) return 43
        return 42
    }

    private fun forzaBiga(ta: Int): Int {
        return minOf(300 + 10 * (ta - 17), 350)
    }

    /** Calcolatore Biga (beta) — pre-fermento con maturazione TA-dipendente. */
    fun calcBiga(
        nPanetti: Int,
        pesoPanetto: Double,
        idroPct: Double,
        saleGPerL: Double,
        grassiGPerL: Double,
        ta: Int,
        bigaPct: Double
    ): RicettaBiga {
        require(ta in 17..28) { "TA biga deve essere tra 17 e 28" }
        require(bigaPct in 10.0..70.0) { "Biga % tra 10 e 70" }

        val m = nPanetti * pesoPanetto
        val i = idroPct / 100.0
        val s = saleGPerL
        val g = grassiGPerL

        // NOTA: nella Biga il lievito NON è incluso nel bilancio totale
        val farinaTot = m / (1 + i * (1 + (s + g) / 1000.0))
        val acquaTot = i * farinaTot
        val saleTot = s * acquaTot / 1000.0
        val grassiTot = g * acquaTot / 1000.0

        // Biga
        val b = bigaPct / 100.0
        val idroB = idroBiga(ta) / 100.0
        val farinaBiga = b * farinaTot
        val acquaBiga = idroB * farinaBiga
        val lievitoBiga = 0.01 * farinaBiga  // 1% sulla farina della biga

        // Rinfresco
        val farinaRinf = (1 - b) * farinaTot
        val acquaRinf = acquaTot - acquaBiga
        val saleRinf = saleTot
        val grassiRinf = grassiTot

        // Ore biga (formula decrescente con TA)
        val ore = 26.5 - 0.5 * ta
        val h = ore.toInt()
        val min = round((ore - h) * 60).toInt()
        val oreStr = "%02d:%02d".format(h, min)

        return RicettaBiga(
            farinaTot = roundTo(farinaTot, 1),
            acquaTot = roundTo(acquaTot, 1),
            saleTot = roundTo(saleTot, 2),
            grassiTot = roundTo(grassiTot, 2),
            farinaBiga = roundTo(farinaBiga, 1),
            acquaBiga = roundTo(acquaBiga, 1),
            lievitoBiga = roundTo(lievitoBiga, 2),
            farinaRinfresco = roundTo(farinaRinf, 1),
            acquaRinfresco = roundTo(acquaRinf, 1),
            saleRinfresco = roundTo(saleRinf, 2),
            grassiRinfresco = roundTo(grassiRinf, 2),
            oreBiga = roundTo(ore, 2),
            oreBigaStr = oreStr,
            idroBigaPct = idroBiga(ta),
            forzaBiga = forzaBiga(ta),
            forzaRinfresco = 300
        )
    }

    // ============================================================
    // CALCOLATORE BIGA FAST (beta) — preset rigido
    // ============================================================

    /** Biga Fast (preset Priore): biga 100%, idro biga 50%, 3h a 26-28°C. */
    fun calcBigaFast(
        nPanetti: Int,
        pesoPanetto: Double,
        idroPct: Double,
        saleGPerL: Double = 40.0,
        grassiGPerL: Double = 0.0
    ): RicettaBigaFast {
        require(idroPct in 70.0..80.0) { "Idratazione totale deve essere tra 70% e 80%" }

        val m = nPanetti * pesoPanetto
        val i = idroPct / 100.0
        val s = saleGPerL
        val g = grassiGPerL

        val farinaTot = m / (1 + i * (1 + (s + g) / 1000.0))
        val acquaTot = i * farinaTot
        val saleTot = s * acquaTot / 1000.0
        val grassiTot = g * acquaTot / 1000.0

        val acquaBiga = 0.50 * farinaTot
        val lievitoBiga = 0.01 * farinaTot
        val acquaRinf = acquaTot - acquaBiga

        return RicettaBigaFast(
            farinaTot = roundTo(farinaTot, 1),
            acquaTot = roundTo(acquaTot, 1),
            saleTot = roundTo(saleTot, 2),
            grassiTot = roundTo(grassiTot, 2),
            acquaBiga = roundTo(acquaBiga, 1),
            lievitoBiga = roundTo(lievitoBiga, 2),
            acquaRinfresco = roundTo(acquaRinf, 1),
            saleRinfresco = roundTo(saleTot, 2),
            grassiRinfresco = roundTo(grassiTot, 2)
        )
    }

    // ============================================================
    // UTILITA
    // ============================================================

    /** Formatta minuti in 'H:MM' (es. 390 -> '6:30'). */
    fun fmtMinutes(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return "$h:%02d".format(m)
    }
}
