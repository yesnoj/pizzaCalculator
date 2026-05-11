# 🍕 Pizza Calc

App Streamlit con dieci calcolatori pizzaioli in un'unica interfaccia
multi-tab: dosi degli impasti, miscele di farine, conversioni di lievito,
contenitori di lievitazione, e pre-fermenti (biga e biga veloce).

Tutto offline, niente dipendenze da servizi esterni, formule verificate
e calibrate.

---

## Indice

1. [Cosa contiene](#cosa-contiene)
2. [Setup e avvio](#setup-e-avvio)
3. [Hosting sul NAS](#hosting-sul-nas)
4. [Documentazione delle formule](#documentazione-delle-formule)
5. [Riepilogo trasversale](#riepilogo-trasversale)
6. [Note di calibrazione (bug-fix interni)](#note-di-calibrazione-bug-fix-interni)
7. [Struttura del codice](#struttura-del-codice)
8. [Roadmap](#roadmap)

---

## Cosa contiene

10 calcolatori in tab separati:

| # | Tab | Scopo |
|---|---|---|
| 1 | **Facile** | Ricetta pizza guidata: scelte automatiche dei tempi e del lievito |
| 2 | **Avanzato** | Stessa logica, ma con controllo manuale di tempi, sale, grassi, malto |
| 3 | **PRO** | Aritmetica pura: tutto in % sulla farina, niente logica di lievitazione |
| 4 | **Teglia** | Pizza in teglia, peso panetto calcolato dall'area |
| 5 | **Mix Farine** | Regola dell'alligation per ottenere una W target da due farine |
| 6 | **W da Proteine** | Stima della forza W di una farina dal contenuto proteico |
| 7 | **Contenitore** | Volume del contenitore di lievitazione consigliato |
| 8 | **Conversione Lievito** | LDB fresco → secco / Caputo / Lievito Madre / LiCoLi |
| 9 | **Biga** | Pre-fermento classico (12-18 h) con maturazione TA-dipendente |
| 10 | **Biga Fast** | Preset "biga veloce" 100%, idro biga 50%, 3 h a 26-28°C |

---

## Setup e avvio

```bash
# 1. crea un virtual env (opzionale ma consigliato)
python3 -m venv venv
source venv/bin/activate    # Linux/macOS
# venv\Scripts\activate     # Windows

# 2. installa la dipendenza
pip install -r requirements.txt

# 3. avvia
streamlit run app.py
```

L'app si apre nel browser su `http://localhost:8501`.

---

## Hosting sul NAS

Due opzioni rapide per averla sempre a portata di telefono in cucina.

**(a) Container Station con immagine Python ufficiale:**

```bash
docker run -d \
  --name pizza-calc \
  -p 8501:8501 \
  -v /share/Container/pizza-calc:/app \
  -w /app \
  python:3.11-slim \
  bash -c "pip install -r requirements.txt && streamlit run app.py --server.address 0.0.0.0"
```

**(b) Direttamente in SSH** (se Python è già installato sul NAS):

```bash
cd /share/homes/admin/pizza-calc
pip install --user streamlit
nohup streamlit run app.py --server.port 8501 --server.address 0.0.0.0 > pizza.log 2>&1 &
```

Poi accessibile da telefono in cucina su `http://<ip-nas>:8501`.

---

## Documentazione delle formule

Tutte le formule sono implementate in `formulas.py` come funzioni pure
con dataclass tipizzate. Questa è la specifica matematica di ognuna.

### 1. Calcolatore Pizza Facile

**Input**: numero panetti, peso panetto, ore totali (8/12/24/48), TA in °C
(18-30), idratazione (59-70%), data/ora inizio.

**Formule**:

- **Sale = 4% dell'acqua** (= 40 g/L, riferimento napoletano fisso)
- **Lievito secco = Lievito fresco / 3**
- **Idratazione = acqua / farina** (input diretto)
- **Bilancio massa**:
  ```
  farina = peso_totale / (1 + idro% + sale% + ldbf%)
  acqua  = farina × idro%
  ```

**Tabelle di lookup**:

- **Lievito fresco** (g) per ~604 g di farina (4×250g, idro 63%, default).
  Per altri pesi scala linearmente. Esempio per 8H/TA:

  | T (°C) | 18 | 20 | 22 | 24 | 26 | 28 | 30 |
  |---|---|---|---|---|---|---|---|
  | ldbf (g) | 2,54 | 1,95 | 1,54 | 1,24 | 1,01 | 0,84 | 0,71 |

  Decadimento quasi esponenziale con T. Per cicli con frigo (24/48 H)
  il lievito dipende anche dalla ripartizione interna puntata/frigo/appretto.

- **Distribuzione puntata + frigo + appretto** (somma = ore totali):

  | liev | T=18-22 | T=24-25 | T=26-27 | T=28-30 |
  |---|---|---|---|---|
  | 8 H | 0:30+0+7:30 | 1:00/1:30+0+7/6:30 | 1:45/2:00+0+6:15/6:00 | 3:00+0+5:00 |
  | 12 H | 3:00+0+9:00 (T<22) / 2:00+4+6:00 (T≥22) | 2:00+5+5:00 | 1:00+6+5:00 | 1:00+7+4:00 |
  | 24 H | 2:30+15+6:30 | 2:00+17:30+4:30 | 1:00+18:30+4:30 | 1:00+19+4:00 |
  | 48 H | 2:30+39+6:30 | 2:00+41:30+4:30 | 1:00+42:30+4:30 | 1:00+43+4:00 |

  Regola implicita: a TA più alta più ore in frigo, meno puntata e appretto.

- **Forza farina (W) consigliata**: tabella manuale, range 240-330 W.
  In generale liev più lunghe → W più alti.

- **Temperatura chiusura impasto**: 24 °C di default, scende a 22 °C
  quando TA ≥ 25 °C (per evitare di surriscaldare l'impasto durante la fermentazione).

### 2. Calcolatore Pizza Avanzato

Stesso engine del Facile, ma l'utente controlla manualmente tutto.

**Input aggiuntivi**:
- `salepl` = grammi di sale **per litro d'acqua** (g/L)
- `grassipl` = grammi di olio **per litro d'acqua** (g/L)
- `malto` = malto in % sulla farina (0-1%)
- `puntata`, `frigo`, `apretto` = ore impostate manualmente

**Differenze chiave rispetto al Facile**:
- **Idratazione libera** (non più 59-70%)
- `sale = salepl × acqua / 1000` (es. 48 g/L × 0,334 L = 16,0 g)
- `olio = grassipl × acqua / 1000` (stessa logica del sale)
- `malto_g = malto% × farina` (non influenza farina/forza/lievito)

**Output aggiuntivo**: campo `ciclo` (classificazione interna):
- **CICLO1**: lievitazione diretta, T ≤ 22 °C, senza frigo
- **CICLO2**: lievitazione diretta a T alta (es. 8 h a 25 °C+)
- **CICLO3**: con frigo, fino a 24 h totali
- **CICLO4**: con frigo, ~36 h
- **CICLO5**: con frigo lungo (~48 h)

### 3. Calcolatore Pizza PRO

Aritmetica pura: nessuna gestione del tempo, della temperatura o della
lievitazione. L'utente specifica direttamente tutte le percentuali e
il sistema risolve il bilancio.

**Input**: panetti, peso, idro %, sale % farina, grassi % farina,
lievito % farina, malto % farina.

**Formule** (tutto in % sulla farina):

```
farina = peso_totale / (1 + idro% + sale% + grassi% + lievito% + malto%)
acqua  = farina × idro%
sale   = farina × sale%
olio   = farina × grassi%
ldbf   = farina × lievito%
ldbs   = ldbf / 3
```

Massimi: sale 5%, grassi 5%. Nessuna `forza` (W) restituita.

### 4. Calcolatore Pizza in Teglia

Come l'Avanzato, ma il peso panetto è **calcolato** dalla superficie della teglia.

**Input specifici**:
- Forma: `rotonda` (diametro d), `quadrata` (lato), `rettangolare` (l1 × l2)
- `n_teglia` = numero teglie
- `idro` libera (testata fino al 90% per pinsa)

**Formule del peso panetto**:

- Rotonda: `peso = 0,58 × π × (d/2)²`
- Quadrata: `peso = 0,58 × lato²`
- Rettangolare: `peso = 0,58 × l1 × l2`

Il coefficiente **0,58 g/cm² = 58 g/dm²** è lo standard per teglia e pinsa.

### 5. Mix Farine

**Scopo**: date due farine con forza W diversa e una W target, calcola
le proporzioni con cui mescolarle (regola dell'alligation).

**Input**: `T` farina totale (g), `W1`, `W2`, `Wmix` (vincolo: W1 ≤ Wmix ≤ W2).

**Formule**:

```
Farina1 = T × (W2 − Wmix) / (W2 − W1)
Farina2 = T − Farina1
```

Derivazione dal sistema:

```
F1 + F2 = T
(F1·W1 + F2·W2) / T = Wmix
```

**Verifica**:
- T=500, W1=200, W2=350, Wmix=300 → F1=166,67 g, F2=333,33 g ✓
- T=1000, W1=200, W2=400, Wmix=260 → F1=700 g, F2=300 g ✓

### 6. Calcola W dalle Proteine

**Scopo**: stima la forza W di una farina tipo 0/00 dal contenuto
proteico dichiarato in etichetta.

**Input**: `P` proteine in g per 100 g di farina.

**Formula**:

```
W = 40 × P − 240
```

equivalente a `W = 40 × (P − 6)`.

| P (%) | 8,5 | 9 | 10 | 11 | 12 | 13,5 | 15 |
|---|---|---|---|---|---|---|---|
| W | 100 | 120 | 160 | 200 | 240 | 300 | 360 |

**Note**:
- Stima semplificata: la W reale dipende anche dalla qualità delle
  proteine (rapporto gliadine/glutenine), non solo dalla quantità.
- Per `P ≤ 6` darebbe W ≤ 0: pensata per farine 0/00 con almeno ~7-8% di proteine.

### 7. Calcola Contenitore Impasto

**Scopo**: dato un peso d'impasto, suggerisce il volume del contenitore
di lievitazione, con margine per evitare fuoriuscite.

**Input**: `I` peso impasto in grammi.

**Formula**:

```
V = I × 2,4
```

Verifica: 250 g → 600 ml ; 1000 g → 2400 ml.

Il moltiplicatore 2,4 tiene conto dell'espansione dell'impasto durante
la lievitazione, con margine di sicurezza.

### 8. Conversione Lievito

**Scopo**: dato un quantitativo di lievito di birra fresco (LDB) di una
ricetta, calcola gli equivalenti negli altri tipi di lievito, aggiustando
farina e acqua per i lieviti che apportano massa.

**Input**: `L` (LDB fresco g), `F` (farina ricetta g), `A` (acqua ricetta g).

**Formule**:

**Lievito secco attivo (LDBS)**:
```
LDBS = L / 3
```

**Lievito secco Caputo (LDBC)**:
```
LDBC = L / 2
```

**Lievito Madre solido (idro 50%) — LM**:
```
LM   = L × 53
F1   = F − LM / 3                  (LM apporta 1/3 farina)
A1   = A − 2 × LM / 3              (LM apporta 2/3 acqua)
```

**Lievito Madre Li.Co.Li. (idro 100%)**:
```
LiCoLi = L × 53
F2     = F − LiCoLi / 2            (LiCoLi 50/50 farina/acqua)
A2     = A − LiCoLi / 2
```

> ⚠️ Il fattore **×53** per LM/LiCoLi è una scelta "ricca di LM"
> rispetto ai tipici 25-30× della letteratura panificatoria.
> Adatto a lieviti madre giovani o poco vigorosi; con LM molto attivi
> conviene ridurre.

**Verifica con L=1,06 ; F=530 ; A=334**:
- LDBS = 0,35 ✓ ; LDBC = 0,53 ✓
- LM = 56,2 ; F1 = 511 ; A1 = 297 ✓
- LiCoLi = 56,2 ; F2 = 502 ; A2 = 306 ✓

### 9. Calcolatore Biga (beta)

Ricetta con pre-fermento "biga" classico, maturazione 12-18 h a TA
controllata.

**Input**: panetti, peso, idro totale, sale g/L, grassi g/L, TA biga
(17-28 °C), % di farina che va nella biga (10-70%).

**Dosi totali** (nota: il lievito **non** è incluso nel bilancio):

```
M     = N × P
F_tot = M / (1 + I × (1 + (S + G)/1000))
A_tot = I × F_tot
Sale  = S × A_tot / 1000
Olio  = G × A_tot / 1000
```

**Maturazione biga** (dipende solo dalla TA):

```
Ore_biga = 26,5 − 0,5 × TA   (formato hh:mm)
```

- **Idratazione biga** (tabella a gradini):
  - TA 17-18 → 45%
  - TA 19-22 → 44%
  - TA 23-25 → 43%
  - TA 26+ → 42%

- **Forza W consigliata biga**: `W_biga ≈ 300 + 10 × (TA − 17)`, satura a 350.

**Ingredienti biga**:

```
Farina_biga  = B × F_tot
Acqua_biga   = idro_biga(TA) × Farina_biga
Lievito_biga = 0,01 × Farina_biga          (1% sulla farina della biga)
```

**Ingredienti rinfresco** (tutto ciò che resta):

```
Farina_rinfresco = (1 − B) × F_tot
Acqua_rinfresco  = A_tot − Acqua_biga
Sale_rinfresco   = Sale_tot
Grassi_rinfresco = Grassi_tot
```

### 10. Calcolatore Biga Fast (beta)

Preset rigido per la "biga veloce": tutta la farina in biga ad alta
idratazione, maturazione corta in ambiente caldo.

**Vincoli**:
- Idratazione totale: 70% ≤ I ≤ 80%
- Biga: bloccata a 100% della farina
- Ore biga: 3:00 (fisse)
- Idratazione biga: 50% (fissa)
- W biga consigliata: 300

**Dosi totali**: stesse formule della Biga classica.

**Ingredienti biga** (biga = 100% della farina):

```
Farina_biga  = F_tot
Acqua_biga   = 0,50 × F_tot
Lievito_biga = 0,01 × F_tot
```

**Ingredienti rinfresco** (solo acqua, sale, grassi residui):

```
Farina_rinfresco = 0
Acqua_rinfresco  = (I − 0,50) × F_tot
Sale_rinfresco   = Sale_tot
Grassi_rinfresco = Grassi_tot
```

**Promemoria orari**:
- Totale = 3 h (biga) + 0,5 h (pieghe) + 2,5 h (appretto) = **6 h**

---

## Riepilogo trasversale

### Convenzioni "sale" e "grassi" — variano fra calcolatori!

| Calcolatore | Sale | Grassi |
|---|---|---|
| Facile | 4% acqua (fisso) | — |
| Avanzato | g/L acqua (`salepl`) | g/L acqua (`grassipl`) |
| PRO | **% farina** (max 5) | **% farina** (max 5) |
| Teglia | g/L acqua | g/L acqua |
| Biga / Biga Fast | g/L acqua | g/L acqua |

### Convenzioni "lievito"

| Calcolatore | Modello lievito |
|---|---|
| Facile / Avanzato / Teglia | Calcolato automaticamente da tabella interna (T, ore, distribuzione) |
| PRO | Input diretto in % sulla farina |
| Biga / Biga Fast | Fisso al **1%** della farina della biga |

In tutti i calcolatori che producono `ldbf` e `ldbs`:
**lievito secco = lievito fresco / 3**.

### Bilancio di massa: lievito incluso o no?

| Calcolatore | Lievito incluso nel bilancio? |
|---|---|
| Facile, Avanzato, PRO, Teglia | **Sì** (totale = farina + acqua + sale + lievito + grassi + malto) |
| Biga, Biga Fast | **No** (totale = farina + acqua + sale + grassi; lievito aggiunto extra) |

### Costanti notevoli

- **40 g/L** = sale per litro d'acqua (riferimento napoletano)
- **2,4** = moltiplicatore peso impasto → volume contenitore
- **0,58 g/cm² (58 g/dm²)** = peso impasto per area di teglia
- **53×** = fattore di conversione LDB → LM solido / LiCoLi
- **1%** = lievito sulla farina della biga
- **3:1** = rapporto lievito fresco : lievito secco attivo
- **2:1** = rapporto lievito fresco : lievito secco Caputo

---

## Note di calibrazione (bug-fix interni)

Le tabelle di lookup originarie del Calcolatore Facile presentavano due
incoerenze interne che sono state corrette in questa implementazione:

**1. Facile 8H / T ≥ 28°C** — la distribuzione `3:00 + 0 + 4:00` sommava
7 h, mentre tutte le altre temperature sommavano 8 h come da specifica.
**Fix**: appretto esteso da 4 h a 5 h, totale ripristinato a 8 h.

**2. Facile 12H / T = 21°C** — il valore `ldbf = 1,32 g` rompeva la
monotonicità del regime no-frigo (T 18-21°C): a parità di regime, ldbf
deve diminuire con T crescente. **Fix**: valore corretto a ~1,05 g,
coerente con il decay osservato a T 18-20 (-0,18 g/°C). A T = 22°C il
valore risale legittimamente perché entra in gioco il frigo (cambio di
regime).

**3. Teglia quadrata** — formula corretta `peso = 0,58 × lato²` applicata
uniformemente, niente valori fissi.

Entrambi i fix delle tabelle del Facile sono documentati nei commenti
in `formulas.py`. Se per qualche motivo vuoi tornare ai valori di
calibrazione originali, basta annullare gli `if` corrispondenti.

---

## Struttura del codice

```
pizza_app/
├── app.py            # UI Streamlit (10 tab)
├── formulas.py       # Tutte le formule come funzioni pure
├── requirements.txt
└── README.md
```

`formulas.py` è completamente disaccoppiato dalla UI: puoi importarlo
e usarlo dentro un altro script (CLI, Flask, script di automazione,
bot Telegram, ecc.) senza tirarci dietro Streamlit.

Esempio di uso da CLI/script:

```python
import formulas as f
from datetime import datetime

# Ricetta Facile classica
r = f.calc_facile(
    n_panetti=4, peso_panetto=250,
    liev=24, T=20, idro_pct=63,
    inizio=datetime(2026, 5, 11, 13, 50),
)
print(f"Farina: {r.farina} g, lievito: {r.ldbf} g")
print(f"Pronta per la cottura: {r.pronti}")

# Mix di due farine
f1, f2 = f.mix_farine(500, 200, 350, 300)
print(f"Farina 1: {f1} g, Farina 2: {f2} g")
```

---

## Roadmap

Idee per evolverla:

- **Salvataggio ricette preferite** (JSON locale) — le tue 4-5 ricette
  standard sempre a portata di click
- **Calcolatore temperatura acqua di impasto**
  ```
  T_acqua = 3 × T_chiusura − T_farina − T_ambiente − T_attrito_impastatrice
  ```
  Davvero utile per calibrare a seconda della stagione
- **Export PDF della ricetta** con timeline e shopping list
- **Notifiche promemoria** via Telegram bot ("tra 30 min metti in frigo",
  "tra 1 h staglia i panetti")
- **Storico delle pizze fatte** con foto e voto — integrazione naturale
  con Piwigo sul NAS
- **API REST** (Flask/FastAPI) esposta sul NAS, con frontend Streamlit
  che ci si appoggia — utile per build mobile native o widget iOS
