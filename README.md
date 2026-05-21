# PizzaLab

App Android nativa per pizzaioli casalinghi e professionisti: calcolatori di impasto, gestione del processo di lievitazione con timer per ogni fase, e timer di cottura con orologio visuale.

Tutto offline, nessuna dipendenza da servizi esterni. Formule verificate e calibrate.

Costruita con Kotlin, Jetpack Compose e Material 3, con un design system custom "Quaderno" che richiama l'estetica di un quaderno da cucina.

---

## Funzionalità

### Calcolatori impasto

Sei calcolatori in tab con swipe orizzontale, ciascuno pensato per un livello di controllo diverso:

- **Facile** — Ricetta guidata: scegli panetti, peso, ore di lievitazione (da 1 a 48h) e temperatura. Il sistema calcola automaticamente lievito, distribuzione dei tempi (puntata, frigo, appretto) e forza farina consigliata. Disponibile anche in modalità "Per farina" (fissi la farina e ottieni il resto) e "Fisso numero" (fissi il numero di panetti).
- **Avanzato** — Stesso motore del Facile, ma con controllo manuale su sale (g/L), grassi (g/L), malto (%), puntata, ore frigo e appretto.
- **PRO** — Aritmetica pura: tutto in percentuale sulla farina, senza logica di lievitazione.
- **Teglia** — Come l'Avanzato, ma il peso panetto è calcolato dalla superficie della teglia (rotonda, quadrata o rettangolare, coefficiente 0.58 g/cm²).
- **Biga** — Pre-fermento classico con maturazione temperatura-dipendente (12–18h). Percentuale di farina in biga configurabile (10–70%).
- **Biga Fast** — Preset rigido per biga veloce: 100% farina in biga, idratazione biga 50%, 3h a 26–28°C.

### Utilità

- **Mix Farine** — Regola dell'alligation per ottenere una W target mescolando due farine.
- **W da Proteine** — Stima la forza W dal contenuto proteico (W = 40×P − 240).
- **Contenitore** — Volume consigliato per il contenitore di lievitazione (peso × 2.4).
- **Conversione Lievito** — LDB fresco verso secco attivo, secco Caputo, Lievito Madre solido e Li.Co.Li.

### Processo di lievitazione

Gestione completa del processo dall'impasto alla cottura:

- Creazione processo da template predefiniti (Napoletana classica, Teglia romana) o da zero.
- Fasi personalizzabili con preset rapidi (Autolisi, Impasto, Puntata, Pieghe, Staglio, Frigo, Appretto, Rinfresco) o completamente custom.
- Timer per ogni fase con notifica al completamento.
- Drag & drop per riordinare le fasi (long press).
- Vista riepilogativa al completamento con navigazione diretta alla cottura.

### Timer di cottura

- Orologio visuale a quadrante con arco di progresso e lancetta.
- Preset rapidi: Napoletana (90s, forno a legna ~450°C), Teglia (4min, forno elettrico ~300°C), Pala (3min, ~350°C), Padellino (5min, ~250°C).
- Timer personalizzato con durata libera e preset rapidi (1–8 minuti).
- Contatore pizze cotte nella sessione.
- Suono e vibrazione al completamento, con suono configurabile.

### Glossario

Dizionario dei termini della panificazione, organizzato in colonne con definizioni rapide consultabili durante il lavoro.

---

## Stack tecnico

- **Linguaggio**: Kotlin
- **UI**: Jetpack Compose + Material 3 (BOM 2024.01.00)
- **Architettura**: MVVM con ViewModel e StateFlow
- **Design system**: "Quaderno" — palette carta/inchiostro, bordi tratteggiati, corner mark rossi, tipografia bold/italic
- **Target**: Android API 26+ (Android 8.0)
- **Dipendenze esterne**: nessuna (tutto calcolato localmente)

---

## Struttura del progetto

```
PizzaLab/
├── app/src/main/java/com/pizzalab/
│   ├── domain/
│   │   └── PizzaFormulas.kt          # Tutte le formule come funzioni pure
│   ├── data/
│   │   └── model/
│   │       └── ProcessModels.kt      # DoughProcess, DoughPhase, CookingPreset, CookingTimer
│   ├── ui/
│   │   ├── theme/
│   │   │   └── Theme.kt              # QuadernoTheme, QuadernoColors, QuadernoTypo
│   │   ├── components/
│   │   │   └── QuadernoComponents.kt # QCard, QField, QHeader, QPrimaryButton, ...
│   │   ├── calculator/
│   │   │   ├── CalculatorScreen.kt   # Tab strip con swipe
│   │   │   ├── FacileCalculator.kt
│   │   │   ├── DaFarinaCalculator.kt
│   │   │   ├── AvanzatoCalculator.kt
│   │   │   ├── ProCalculator.kt
│   │   │   ├── TegliaCalculator.kt
│   │   │   ├── BigaCalculator.kt
│   │   │   └── UtilityCalculators.kt # Mix Farine, W, Contenitore, Conversione
│   │   ├── process/
│   │   │   ├── ProcessScreen.kt      # Template, processo attivo, completamento
│   │   │   ├── ProcessViewModel.kt
│   │   │   └── AddEditPhaseDialog.kt
│   │   ├── cooking/
│   │   │   ├── CookingScreen.kt      # Quadrante orologio e timer
│   │   │   └── CookingViewModel.kt
│   │   ├── help/
│   │   │   └── HelpScreen.kt         # Glossario
│   │   └── navigation/
│   │       └── Navigation.kt         # Bottom nav a 4 tab
│   ├── service/
│   │   └── TimerService.kt           # Foreground service per timer
│   └── MainActivity.kt
└── build.gradle.kts
```

`PizzaFormulas.kt` è completamente disaccoppiato dalla UI: contiene solo funzioni pure con data class tipizzate.

---

## Formule principali

Tutte le formule sono documentate in dettaglio in `PizzaFormulas.kt`. Ecco le costanti chiave:

- **40 g/L** = sale per litro d'acqua (riferimento napoletano)
- **0.58 g/cm²** = peso impasto per area di teglia
- **2.4×** = moltiplicatore peso impasto verso volume contenitore
- **3:1** = rapporto lievito fresco : secco attivo
- **2:1** = rapporto lievito fresco : secco Caputo
- **53×** = fattore conversione LDB verso LM solido / Li.Co.Li
- **1%** = lievito sulla farina della biga
- **W = 40×P − 240** = stima forza W da proteine

---

## Build

```bash
# Clona il repository
git clone https://github.com/user/pizzaCalculator.git
cd pizzaCalculator/PizzaLab

# Build con Gradle
./gradlew assembleDebug

# Installa su dispositivo connesso
./gradlew installDebug
```

Richiede Android Studio Hedgehog o superiore con JDK 17.
