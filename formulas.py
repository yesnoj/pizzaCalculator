"""
formulas.py — Implementazione delle formule pizzaiole utilizzate da Pizza Calc.

Tutte le funzioni sono pure: non fanno I/O, non hanno side-effects,
sono facilmente testabili e riutilizzabili.

================================================================
Note sulle scelte di calibrazione (bug-fix interni)
================================================================
Le tabelle di lookup per il Calcolatore Facile presentavano alcune
incoerenze nella versione iniziale che sono state corrette:

  1. Facile 8H, T 28-30°C: la distribuzione (puntata + frigo + apretto)
     sommava 7h invece delle 8h prescritte. Fix: apretto esteso da
     4:00 a 5:00, totale ripristinato a 8h.

  2. Facile 12H, T=21°C: la tabella del lievito fresco aveva un
     valore (1.32 g) che rompeva la monotonicità decrescente del
     regime no-frigo (T 18-21). Fix: valore corretto a 1.05 g,
     coerente con il decay osservato a T 18-20 (~-0.18 g/°C).

  3. Teglia quadrata: la formula corretta `peso = 0.58 × lato²` è
     applicata; nessun valore fisso.

Moduli coperti:
- Calcolatori pizza: Facile, Avanzato, PRO, Teglia
- Strumenti farina: Mix Farine, Forza da Proteine
- Altri: Contenitore, Conversione Lievito
- Pre-fermenti: Biga, Biga Fast
"""

from __future__ import annotations
from dataclasses import dataclass, field
from datetime import datetime, timedelta
from typing import Optional
import math


# ============================================================
# COSTANTI
# ============================================================
SALE_PER_LITRO_NAPOLETANO = 40.0       # g/L acqua (4% acqua)
TEGLIA_GR_PER_CM2 = 0.58                # peso impasto per cm² di teglia
CONTENITORE_MOLTIPLICATORE = 2.4        # volume contenitore = peso × 2.4
LM_LICOLI_FACTOR = 53                   # convenzione "ricca di LM" (vedi README)
LIEVITO_FRESCO_SECCO_ATTIVO_RATIO = 3   # LDBS = LDB / 3
LIEVITO_FRESCO_SECCO_CAPUTO_RATIO = 2   # LDBC = LDB / 2


# ============================================================
# CALCOLATORE FACILE — tabelle di lookup
# ============================================================

# Lievito fresco (g) di riferimento per ~604g di farina (4 panetti × 250g,
# idro 63%, default). Per altri pesi il valore scala linearmente con la farina.
LDBF_TABLE_FACILE = {
    8: {18: 2.54, 19: 2.22, 20: 1.95, 21: 1.73, 22: 1.54, 23: 1.38,
        24: 1.24, 25: 1.12, 26: 1.01, 27: 0.92, 28: 0.84, 29: 0.77, 30: 0.71},
    # 12H: bug-fix a T=21 (sito: 1.32, rompe la monotonicità del regime no-frigo).
    # In regime no-frigo (T 18-21) i valori 1.56, 1.38, 1.20 hanno decay -0.18 per +1°C.
    # L'estrapolazione naturale a T=21 è ~1.05 (lineare 1.02, esponenziale 1.04).
    # Sostituiamo 1.32 → 1.05 per ripristinare il decay monotono. A T=22 entra il
    # frigo e il valore risale legittimamente (cambio di regime).
    12: {18: 1.56, 19: 1.38, 20: 1.20, 21: 1.05, 22: 1.45, 23: 1.40,
         24: 1.34, 25: 1.31, 26: 1.28, 27: 1.27, 28: 1.26, 29: 1.16, 30: 1.06},
    24: {18: 1.83, 19: 1.70, 20: 1.57, 21: 1.44, 22: 1.31, 23: 1.25,
         24: 1.19, 25: 1.15, 26: 1.12, 27: 1.06, 28: 1.01, 29: 0.93, 30: 0.85},
    48: {18: 1.43, 19: 1.32, 20: 1.20, 21: 1.10, 22: 0.99, 23: 0.94,
         24: 0.88, 25: 0.84, 26: 0.80, 27: 0.75, 28: 0.70, 29: 0.65, 30: 0.59},
}

# Forza farina (W) consigliata
def forza_facile(liev: int, T: int) -> int:
    if liev == 8:
        return 260 if T >= 25 else 240
    if liev == 12:
        if T <= 21: return 260
        if T <= 25: return 290
        return 330
    if liev == 24:
        if T <= 23: return 250
        if T <= 25: return 240
        return 260
    if liev == 48:
        if T <= 21: return 300
        if T <= 25: return 320
        return 330
    return 250


def temp_chiusura_facile(liev: int, T: int) -> int:
    """Temperatura target dell'impasto a fine impastatura."""
    if liev == 8 and T >= 25:
        return 22
    return 24


def get_distribution_facile(liev: int, T: int) -> tuple[int, int, int]:
    """Restituisce (puntata_min, frigo_min, apretto_min) per liev/T date."""
    if liev == 8:
        if T <= 23: return (30, 0, 450)
        if T == 24: return (60, 0, 420)
        if T == 25: return (90, 0, 390)
        if T == 26: return (105, 0, 375)
        if T == 27: return (120, 0, 360)
        # Bug-fix: la calibrazione originaria per T 28-30 restituiva (180, 0, 240),
        # somma 7h invece di 8h come per le altre temperature. Estendiamo
        # l'apretto a 300 min per ripristinare il totale di 8h.
        return (180, 0, 300)
    if liev == 12:
        if T <= 21: return (180, 0, 540)
        if T <= 23: return (120, 240, 360)
        if T <= 25: return (120, 300, 300)
        if T <= 27: return (60, 360, 300)
        return (60, 420, 240)
    if liev == 24:
        if T <= 20: return (150, 900, 390)
        if T <= 23: return (120, 990, 330)
        if T <= 25: return (120, 1050, 270)
        if T <= 27: return (60, 1110, 270)
        return (60, 1140, 240)
    if liev == 48:
        if T <= 20: return (150, 2340, 390)
        if T <= 23: return (120, 2430, 330)
        if T <= 25: return (120, 2490, 270)
        if T <= 27: return (60, 2550, 270)
        return (60, 2580, 240)
    raise ValueError(f"liev non valido: {liev}")


@dataclass
class RicettaFacile:
    farina: float
    acqua: float
    sale: float
    ldbf: float
    ldbs: float
    totale: float
    forza: int
    temp_chiusura: int
    puntata_min: int
    frigo_min: int
    apretto_min: int
    inizio: datetime
    inizio_apretto: datetime
    pronti: datetime


def calc_facile(
    n_panetti: int,
    peso_panetto: float,
    liev: int,
    T: int,
    idro_pct: float,
    inizio: Optional[datetime] = None,
) -> RicettaFacile:
    """Calcolatore Facile — modalità guidata con scelte automatiche di tempi e lievito."""
    if liev not in (8, 12, 24, 48):
        raise ValueError("liev deve essere 8, 12, 24 o 48")
    if not 18 <= T <= 30:
        raise ValueError("Temperatura ambiente deve essere tra 18 e 30 °C")
    if not 59 <= idro_pct <= 70:
        raise ValueError("Idratazione deve essere tra 59 e 70 %")
    if inizio is None:
        inizio = datetime.now().replace(second=0, microsecond=0)

    totale_target = n_panetti * peso_panetto + n_panetti * 0.5  # margine
    idro = idro_pct / 100.0

    # ldbf per 604g di farina (riferimento empirico)
    ldbf_ref = LDBF_TABLE_FACILE[liev][T]
    ldbf_pct = ldbf_ref / 604.0  # frazione fissa di farina
    sale_pct = idro * 0.04        # sale = 4% acqua = 4% × idro × farina

    # totale = farina × (1 + idro + sale_pct + ldbf_pct)
    farina = totale_target / (1 + idro + sale_pct + ldbf_pct)
    acqua = idro * farina
    sale = sale_pct * farina
    ldbf = ldbf_pct * farina
    ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

    p, f, a = get_distribution_facile(liev, T)
    inizio_apretto = inizio + timedelta(minutes=p + f)
    pronti = inizio + timedelta(minutes=p + f + a)

    return RicettaFacile(
        farina=round(farina, 1), acqua=round(acqua, 1),
        sale=round(sale, 2), ldbf=round(ldbf, 2), ldbs=round(ldbs, 2),
        totale=round(farina + acqua + sale + ldbf, 1),
        forza=forza_facile(liev, T),
        temp_chiusura=temp_chiusura_facile(liev, T),
        puntata_min=p, frigo_min=f, apretto_min=a,
        inizio=inizio, inizio_apretto=inizio_apretto, pronti=pronti,
    )


# ============================================================
# CALCOLATORE AVANZATO
# ============================================================
# Stessa logica del Facile ma con sale/grassi/malto e tempi manuali.
# Per il lievito riusiamo la tabella Facile (approssimazione ragionevole)
# bucketizzando il liev totale nel valore standard più vicino.

@dataclass
class RicettaAvanzata:
    farina: float
    acqua: float
    sale: float
    olio: float
    malto: float
    ldbf: float
    ldbs: float
    totale: float
    forza: int
    temp_chiusura: int
    puntata_min: int
    frigo_min: int
    apretto_min: int
    ciclo: str
    inizio: datetime
    inizio_apretto: datetime
    pronti: datetime


def _ciclo_avanzato(liev: int, frigo: int, T: int) -> str:
    """Classificazione dei cicli osservata empiricamente."""
    if frigo == 0:
        return "CICLO2" if T >= 25 else "CICLO1"
    if liev <= 24: return "CICLO3"
    if liev <= 36: return "CICLO4"
    return "CICLO5"


def _ldbf_avanzato_ref(liev: int, T: int) -> float:
    """ldbf di riferimento per ~530g farina (default Avanzato 4×220, idro 63%)."""
    bucket = min((8, 12, 24, 48), key=lambda b: abs(b - liev))
    base = LDBF_TABLE_FACILE[bucket][T]
    # rapporto su farina: ldbf/604 nella tabella → applichiamo su 530
    return base * 530 / 604


def calc_avanzato(
    n_panetti: int,
    peso_panetto: float,
    idro_pct: float,
    sale_g_per_l: float,
    grassi_g_per_l: float,
    malto_pct: float,
    liev: int,
    frigo_h: int,
    puntata_min: int,
    apretto_min: int,
    T: int,
    inizio: Optional[datetime] = None,
) -> RicettaAvanzata:
    """Calcolatore Avanzato — controllo manuale di sale, grassi, tempi."""
    if inizio is None:
        inizio = datetime.now().replace(second=0, microsecond=0)

    totale_target = n_panetti * peso_panetto + n_panetti * 0.5
    idro = idro_pct / 100.0

    # ldbf empirico (% sulla farina costante per tabella facile)
    ldbf_ref = _ldbf_avanzato_ref(liev, T)
    ldbf_pct = ldbf_ref / 530.0
    sale_pct = idro * sale_g_per_l / 1000.0
    olio_pct = idro * grassi_g_per_l / 1000.0
    malto_pct_frac = malto_pct / 100.0

    farina = totale_target / (1 + idro + sale_pct + olio_pct + ldbf_pct + malto_pct_frac)
    acqua = idro * farina
    sale = sale_pct * farina
    olio = olio_pct * farina
    malto = malto_pct_frac * farina
    ldbf = ldbf_pct * farina
    ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

    inizio_apretto = inizio + timedelta(minutes=puntata_min + frigo_h * 60)
    pronti = inizio_apretto + timedelta(minutes=apretto_min)

    return RicettaAvanzata(
        farina=round(farina, 1), acqua=round(acqua, 1),
        sale=round(sale, 2), olio=round(olio, 2), malto=round(malto, 2),
        ldbf=round(ldbf, 2), ldbs=round(ldbs, 2),
        totale=round(farina + acqua + sale + olio + malto + ldbf, 1),
        forza=forza_facile(min((8, 12, 24, 48), key=lambda b: abs(b - liev)), T),
        temp_chiusura=24 if T < 25 else 22,
        puntata_min=puntata_min, frigo_min=frigo_h * 60, apretto_min=apretto_min,
        ciclo=_ciclo_avanzato(liev, frigo_h, T),
        inizio=inizio, inizio_apretto=inizio_apretto, pronti=pronti,
    )


# ============================================================
# CALCOLATORE PRO — aritmetica pura
# ============================================================
@dataclass
class RicettaPro:
    farina: float
    acqua: float
    sale: float
    olio: float
    malto: float
    ldbf: float
    ldbs: float
    totale: float


def calc_pro(
    n_panetti: int,
    peso_panetto: float,
    idro_pct: float,
    sale_pct_farina: float,
    grassi_pct_farina: float,
    lievito_pct_farina: float,
    malto_pct_farina: float,
) -> RicettaPro:
    """Calcolatore PRO — tutto in % sulla farina, niente logica di lievitazione."""
    if sale_pct_farina > 5:
        raise ValueError("Sale max 5% nel PRO")
    if grassi_pct_farina > 5:
        raise ValueError("Grassi max 5% nel PRO")

    totale_target = n_panetti * peso_panetto
    i = idro_pct / 100.0
    s = sale_pct_farina / 100.0
    g = grassi_pct_farina / 100.0
    l = lievito_pct_farina / 100.0
    m = malto_pct_farina / 100.0

    farina = totale_target / (1 + i + s + g + l + m)
    acqua = i * farina
    sale = s * farina
    olio = g * farina
    malto = m * farina
    ldbf = l * farina
    ldbs = ldbf / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO

    return RicettaPro(
        farina=round(farina, 1), acqua=round(acqua, 1),
        sale=round(sale, 2), olio=round(olio, 2), malto=round(malto, 2),
        ldbf=round(ldbf, 2), ldbs=round(ldbs, 2),
        totale=round(farina + acqua + sale + olio + malto + ldbf, 1),
    )


# ============================================================
# CALCOLATORE TEGLIA
# ============================================================
@dataclass
class RicettaTeglia:
    peso_panetto: float
    area_cm2: float
    farina: float
    acqua: float
    sale: float
    olio: float
    ldbf: float
    ldbs: float
    totale: float
    forza: int


def calcola_peso_teglia(forma: str, **dimensioni) -> tuple[float, float]:
    """Calcola peso impasto e area dalla geometria della teglia.
    
    forma: 'rotonda' (d=diametro), 'rettangolare' (rl1, rl2), 'quadrata' (rl1)
    Restituisce (peso_g, area_cm2).
    """
    if forma == "rotonda":
        d = dimensioni["d"]
        area = math.pi * (d / 2) ** 2
    elif forma == "quadrata":
        l = dimensioni["rl1"]
        area = l * l
    elif forma == "rettangolare":
        l1, l2 = dimensioni["rl1"], dimensioni["rl2"]
        area = l1 * l2
    else:
        raise ValueError(f"Forma sconosciuta: {forma}")
    peso = TEGLIA_GR_PER_CM2 * area
    return peso, area


def calc_teglia(
    forma: str,
    n_teglia: int,
    idro_pct: float,
    sale_g_per_l: float,
    grassi_g_per_l: float,
    liev: int,
    frigo_h: int,
    puntata_min: int,
    apretto_min: int,
    T: int,
    inizio: Optional[datetime] = None,
    **dimensioni,
) -> RicettaTeglia:
    """Calcolatore Teglia — peso panetto dall'area teglia, idro libera."""
    peso, area = calcola_peso_teglia(forma, **dimensioni)
    avanzata = calc_avanzato(
        n_panetti=n_teglia, peso_panetto=peso,
        idro_pct=idro_pct, sale_g_per_l=sale_g_per_l,
        grassi_g_per_l=grassi_g_per_l, malto_pct=0,
        liev=liev, frigo_h=frigo_h, puntata_min=puntata_min,
        apretto_min=apretto_min, T=T, inizio=inizio,
    )
    return RicettaTeglia(
        peso_panetto=round(peso, 1), area_cm2=round(area, 1),
        farina=avanzata.farina, acqua=avanzata.acqua,
        sale=avanzata.sale, olio=avanzata.olio,
        ldbf=avanzata.ldbf, ldbs=avanzata.ldbs,
        totale=avanzata.totale, forza=avanzata.forza,
    )


# ============================================================
# MIX FARINE — regola dell'alligation
# ============================================================
def mix_farine(T_g: float, W1: float, W2: float, Wmix: float) -> tuple[float, float]:
    """Restituisce (farina_1_g, farina_2_g) per ottenere Wmix da W1+W2."""
    if W1 == W2:
        raise ValueError("W1 e W2 devono essere diversi")
    lo, hi = sorted((W1, W2))
    if not lo <= Wmix <= hi:
        raise ValueError(f"Wmix={Wmix} deve essere tra {lo} e {hi}")
    f1 = T_g * (W2 - Wmix) / (W2 - W1)
    f2 = T_g - f1
    return round(f1, 1), round(f2, 1)


# ============================================================
# FORZA DALLE PROTEINE
# ============================================================
def forza_da_proteine(P_pct: float) -> int:
    """W = 40 × P − 240 (lineare). Pensata per farine 0/00 con P ≥ 7-8%."""
    if P_pct < 6:
        raise ValueError("Proteine < 6%: formula non sensata")
    return int(round(40 * P_pct - 240))


# ============================================================
# CONTENITORE
# ============================================================
def contenitore_volume_ml(peso_impasto_g: float) -> float:
    """V = I × 2.4. Volume in ml consigliato per il contenitore di lievitazione."""
    return round(peso_impasto_g * CONTENITORE_MOLTIPLICATORE, 1)


# ============================================================
# CONVERSIONE LIEVITO
# ============================================================
@dataclass
class ConversioneLievito:
    ldbf_input: float       # input: lievito di birra fresco
    ldbs: float             # lievito secco attivo
    ldbc: float             # lievito secco Caputo
    lm: float               # lievito madre solido (idro 50%)
    farina_lm: float        # farina aggiustata per LM
    acqua_lm: float         # acqua aggiustata per LM
    licoli: float           # lievito madre Li.Co.Li (idro 100%)
    farina_licoli: float
    acqua_licoli: float


def conversione_lievito(L: float, F: float, A: float) -> ConversioneLievito:
    """Converte LDB fresco in altri tipi di lievito, aggiustando farina/acqua."""
    ldbs = L / LIEVITO_FRESCO_SECCO_ATTIVO_RATIO
    ldbc = L / LIEVITO_FRESCO_SECCO_CAPUTO_RATIO
    lm = L * LM_LICOLI_FACTOR
    # LM solido = 1/3 farina + 2/3 acqua
    farina_lm = F - lm / 3
    acqua_lm = A - 2 * lm / 3
    # LiCoLi = 50% farina + 50% acqua
    licoli = L * LM_LICOLI_FACTOR
    farina_licoli = F - licoli / 2
    acqua_licoli = A - licoli / 2
    return ConversioneLievito(
        ldbf_input=L,
        ldbs=round(ldbs, 2), ldbc=round(ldbc, 2),
        lm=round(lm, 1), farina_lm=round(farina_lm, 0), acqua_lm=round(acqua_lm, 0),
        licoli=round(licoli, 1), farina_licoli=round(farina_licoli, 0),
        acqua_licoli=round(acqua_licoli, 0),
    )


# ============================================================
# CALCOLATORE BIGA (beta)
# ============================================================
@dataclass
class RicettaBiga:
    farina_tot: float
    acqua_tot: float
    sale_tot: float
    grassi_tot: float
    farina_biga: float
    acqua_biga: float
    lievito_biga: float
    farina_rinfresco: float
    acqua_rinfresco: float
    sale_rinfresco: float
    grassi_rinfresco: float
    ore_biga: float
    ore_biga_str: str
    idro_biga_pct: int
    forza_biga: int
    forza_rinfresco: int


def _idro_biga(TA: int) -> int:
    """Tabella idro biga in funzione di TA."""
    if TA <= 18: return 45
    if TA <= 22: return 44
    if TA <= 25: return 43
    return 42


def _forza_biga(TA: int) -> int:
    return min(300 + 10 * (TA - 17), 350)


def calc_biga(
    n_panetti: int,
    peso_panetto: float,
    idro_pct: float,
    sale_g_per_l: float,
    grassi_g_per_l: float,
    TA: int,
    biga_pct: float,
) -> RicettaBiga:
    """Calcolatore Biga (beta) — pre-fermento con maturazione TA-dipendente."""
    if not 17 <= TA <= 28:
        raise ValueError("TA biga deve essere tra 17 e 28")
    if not 10 <= biga_pct <= 70:
        raise ValueError("Biga % tra 10 e 70")

    M = n_panetti * peso_panetto
    I = idro_pct / 100.0
    S = sale_g_per_l
    G = grassi_g_per_l

    # NOTA: nella Biga il lievito NON è incluso nel bilancio totale
    farina_tot = M / (1 + I * (1 + (S + G) / 1000))
    acqua_tot = I * farina_tot
    sale_tot = S * acqua_tot / 1000
    grassi_tot = G * acqua_tot / 1000

    # Biga
    B = biga_pct / 100.0
    idro_b = _idro_biga(TA) / 100.0
    farina_biga = B * farina_tot
    acqua_biga = idro_b * farina_biga
    lievito_biga = 0.01 * farina_biga  # 1% sulla farina della biga

    # Rinfresco
    farina_rinf = (1 - B) * farina_tot
    acqua_rinf = acqua_tot - acqua_biga
    sale_rinf = sale_tot
    grassi_rinf = grassi_tot

    # Ore biga (formula decrescente con TA)
    ore = 26.5 - 0.5 * TA
    h = int(ore)
    m = int(round((ore - h) * 60))
    ore_str = f"{h:02d}:{m:02d}"

    return RicettaBiga(
        farina_tot=round(farina_tot, 1), acqua_tot=round(acqua_tot, 1),
        sale_tot=round(sale_tot, 2), grassi_tot=round(grassi_tot, 2),
        farina_biga=round(farina_biga, 1), acqua_biga=round(acqua_biga, 1),
        lievito_biga=round(lievito_biga, 2),
        farina_rinfresco=round(farina_rinf, 1), acqua_rinfresco=round(acqua_rinf, 1),
        sale_rinfresco=round(sale_rinf, 2), grassi_rinfresco=round(grassi_rinf, 2),
        ore_biga=round(ore, 2), ore_biga_str=ore_str,
        idro_biga_pct=_idro_biga(TA), forza_biga=_forza_biga(TA),
        forza_rinfresco=300,
    )


# ============================================================
# CALCOLATORE BIGA FAST (beta) — preset rigido
# ============================================================
@dataclass
class RicettaBigaFast:
    farina_tot: float
    acqua_tot: float
    sale_tot: float
    grassi_tot: float
    acqua_biga: float
    lievito_biga: float
    acqua_rinfresco: float
    sale_rinfresco: float
    grassi_rinfresco: float
    ore_biga_str: str = "03:00"
    idro_biga_pct: int = 50
    forza_biga: int = 300


def calc_biga_fast(
    n_panetti: int,
    peso_panetto: float,
    idro_pct: float,
    sale_g_per_l: float = 40,
    grassi_g_per_l: float = 0,
) -> RicettaBigaFast:
    """Biga Fast (preset Priore): biga 100%, idro biga 50%, 3h a 26-28°C."""
    if not 70 <= idro_pct <= 80:
        raise ValueError("Idratazione totale deve essere tra 70% e 80%")

    M = n_panetti * peso_panetto
    I = idro_pct / 100.0
    S = sale_g_per_l
    G = grassi_g_per_l

    farina_tot = M / (1 + I * (1 + (S + G) / 1000))
    acqua_tot = I * farina_tot
    sale_tot = S * acqua_tot / 1000
    grassi_tot = G * acqua_tot / 1000

    acqua_biga = 0.50 * farina_tot
    lievito_biga = 0.01 * farina_tot
    acqua_rinf = acqua_tot - acqua_biga

    return RicettaBigaFast(
        farina_tot=round(farina_tot, 1), acqua_tot=round(acqua_tot, 1),
        sale_tot=round(sale_tot, 2), grassi_tot=round(grassi_tot, 2),
        acqua_biga=round(acqua_biga, 1), lievito_biga=round(lievito_biga, 2),
        acqua_rinfresco=round(acqua_rinf, 1),
        sale_rinfresco=round(sale_tot, 2), grassi_rinfresco=round(grassi_tot, 2),
    )


# ============================================================
# UTILITÀ
# ============================================================
def fmt_minutes(minutes: int) -> str:
    """Formatta minuti in 'H:MM' (es. 390 → '6:30')."""
    h, m = divmod(minutes, 60)
    return f"{h}:{m:02d}"
