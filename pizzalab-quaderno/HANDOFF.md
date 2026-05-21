# HANDOFF — PizzaLab · Quaderno → Compose

Guida operativa per il team **Cowork** che porterà questi mockup
nell'app Android reale. Letta prima di iniziare.

L'app esistente è già in Jetpack Compose / Material 3, quindi non si
tratta di una riscrittura: è un **restyling completo del Theme + dei
componenti di UI custom**. Le formule (`PizzaFormulas.kt`), i ViewModel
e il `TimerService` non si toccano.

---

## 1. Sostituire `Theme.kt`

Il file `tokens.kt` di questo pacchetto è un drop-in replacement di
`app/src/main/java/com/pizzalab/ui/theme/Theme.kt`.

1. Copia `tokens.kt` come `Theme.kt` nella stessa cartella.
2. In `MainActivity.kt`, sostituisci `PizzaLabTheme { … }` con
   `QuadernoTheme { … }`.
3. In `themes.xml`, cambia lo status bar color:
   ```xml
   <item name="android:statusBarColor">@color/pizza_paper</item>
   <item name="android:windowLightStatusBar">true</item>
   ```
   e aggiungi `<color name="pizza_paper">#FDF9EE</color>` in `colors.xml`.

A questo punto tutto Material 3 (Button, Card, FilterChip, etc.) eredita
la nuova palette. Le schermate vanno aggiornate sotto.

---

## 2. Famiglia di font

I mock usano **Inter** caricato da Google Fonts. Per la app:

1. Scarica i `.ttf` di Inter (400, 500, 600, 700, 800, 900) da
   <https://rsms.me/inter/> e mettili in `app/src/main/res/font/`.
2. In `Theme.kt`, dopo aver definito `Typography`:
   ```kotlin
   val InterFamily = FontFamily(
       Font(R.font.inter_regular,   FontWeight.Normal),
       Font(R.font.inter_medium,    FontWeight.Medium),
       Font(R.font.inter_semibold,  FontWeight.SemiBold),
       Font(R.font.inter_bold,      FontWeight.Bold),
       Font(R.font.inter_extrabold, FontWeight.ExtraBold),
       Font(R.font.inter_black,     FontWeight.Black),
   )
   ```
3. Applica `fontFamily = InterFamily` ad ogni `TextStyle` di
   `QuadernoTypography`.

---

## 3. Mappatura componenti — JSX (mock) → Compose (codebase reale)

| JSX nel mock           | Compose equivalente nella app             | Note |
|------------------------|--------------------------------------------|------|
| `Q_Header`             | `Column { Text(kicker); Row { Text(title); Text(italic, FontStyle.Italic) } }` | Inline, no AppBar |
| `Q_BottomNav`          | `NavigationBar` con `NavigationBarItem`    | Già esiste in `Navigation.kt`. Cambia solo l'aspetto degli `Icon` (1.6dp stroke, no fill) e aggiungi sottolineatura su tab attiva |
| `Q_Field` (stepper)    | **Nuovo componente.** Vedi §4. Sostituisce ogni `Slider` del codebase |
| `Q_LeaderRow`          | `Row` con `Text` + `Spacer(weight=1f).background(dottedBrush)` + `Text` | Per i dotted leader, vedi §5 |
| `Q_Card` (corner-mark) | **Nuovo componente.** Box con 4 `Canvas` agli angoli. Sostituisce `OutlinedCard` |
| `Q_ChipRow`            | `FlowRow` di `FilterChip` con `colors = filterChipColors(...)` custom |
| `Q_Segmented`          | `SingleChoiceSegmentedButtonRow` di Material 3 (1.2+) |
| `Q_Dialog`             | `AlertDialog` di M3 ma con `shape = RoundedCornerShape(4dp)` e corner-mark custom |
| `Q_PrimaryBtn`         | `Button(colors = ButtonDefaults.buttonColors(containerColor = Primary))` |
| `Q_DarkBtn`            | `Button(colors = ButtonDefaults.buttonColors(containerColor = Ink))` |
| `Q_SecondaryBtn`       | `OutlinedButton` |
| `Q_ClockDial`          | **Nuovo componente.** `Canvas` per quadrante + arc + lancetta. Vedi §6 |

---

## 4. `Q_Field` — il nostro stepper (sostituisce ogni `Slider`)

Lo `Slider` di Material non basta più: serve un input preciso, numerico,
con preset rapidi. Il pattern:

```
┌─ label ─────────────────────────  (−)  18.5  (+) ─┐
│                                                   │
│  preset · preset · preset · preset                 │
└────────────────────────────────────────────────────┘
─ (dashed hairline) ─
```

**Comportamento:**
- Pulsanti `−` / `+` con `IconButton` cerchiati 26 dp.
- Numero al centro: `Text` con `FontWeight.Bold`, `fontFamily = InterFamily`,
  `fontFeatureSettings = "tnum"`, **underline 2 dp Ink** sotto il numero.
- Preset: lista di valori cliccabili. Sfondo `BgWarmer` se selezionato,
  trasparente altrimenti. `FontWeight.Bold` + `Color = Primary` se selezionato.
- Underline tratteggiato tra un Q_Field e il successivo (vedi §5).

**Firma proposta:**
```kotlin
@Composable
fun QField(
    label: String,
    value: Int,
    suffix: String,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1,
    presets: List<Int>? = null,
    hint: String? = null,
    dense: Boolean = false,
)
```

Per i `Float` (idratazione frazionaria, percentuale grassi), una variante
`QFieldDecimal` con stessa firma ma valori `Double` e `decimals: Int`.

---

## 5. Hairlines tratteggiati e leader-dots

Compose non ha una `dashedBorder` nativa. Crea un brush:

```kotlin
val DashedRule = remember {
    object : Shape {
        override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
            Outline.Generic(Path().apply {
                val dashLen = 4f; val gapLen = 4f
                var x = 0f
                while (x < size.width) {
                    addRect(Rect(x, 0f, x + dashLen, size.height))
                    x += dashLen + gapLen
                }
            })
    }
}
```

Per i **leader-dots** (`Farina ⋯⋯⋯ 614 g`):

```kotlin
Row(verticalAlignment = Alignment.Bottom) {
    Text("Farina")
    DottedSpacer(modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
    Text("614 g", style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"))
}

@Composable
fun DottedSpacer(modifier: Modifier = Modifier, color: Color = QuadernoColors.RuleDots) {
    Canvas(modifier.height(1.dp).fillMaxWidth()) {
        val r = 0.6.dp.toPx()
        val gap = 4.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawCircle(color, r, Offset(x, size.height / 2))
            x += gap
        }
    }
}
```

---

## 6. `Q_ClockDial` — quadrante del timer di Cottura

Sostituisce il `CircularTimerDisplay` esistente in `CookingScreen.kt`.

**Costruzione (Canvas):**
1. Cerchio Paper Ø 264 dp con bordo Ink 1.5 dp.
2. 60 tick marks: lunghi 14 dp (ogni 5) o 6 dp, stroke Ink (i lunghi) / Ink3.
3. Arco di progresso: stroke Primary 3 dp con `alpha = 0.6f`,
   `strokeCap = StrokeCap.Butt`, `sweepAngle = 360 * fraction`.
4. Hub centrale: cerchio Ø 12 dp Ink.
5. Lancetta: linea da centro a tick top, ruotata di `(1 - fraction) * 360°`,
   stroke Primary 3 dp con `StrokeCap.Round`, pallino Ø 8 dp Primary in
   cima alla lancetta.
6. Animazione: `animateFloatAsState(fraction, tween(800ms, FastOutSlowInEasing))`.

**Stato "Pronta!":** cerchio pieno Primary, ticks invertiti in Paper a
`alpha 0.85f / 0.5f`, scritta **"Pronta!"** in `displayMedium` Paper al
centro. Pulsazione lenta `infiniteRepeatable` scale 1.0 → 1.02.

---

## 7. Bottom navigation

Già strutturato bene in `Navigation.kt`. Modifiche:

```kotlin
NavigationBar(
    containerColor = QuadernoColors.Paper,
    contentColor   = QuadernoColors.Ink3,
    tonalElevation = 0.dp,
    modifier = Modifier.drawBehind {
        // dashed top border
        drawDashedLine(QuadernoColors.RuleDots)
    }
) { … }
```

L'indicatore della tab attiva non è il classico pillolone Material:
è una **sottolineatura 24 × 2 dp Primary** centrata sotto label/icona.
Usa `NavigationBarItemColors` con `indicatorColor = Color.Transparent`
e disegna la sottolineatura a mano con `Box`.

Label tab non-attiva: `FontStyle.Italic`, `FontWeight.Medium`.
Label tab attiva: `FontStyle.Normal`, `FontWeight.Bold`.

---

## 8. Calculator tab strip

Pattern attuale (`CalculatorScreen.kt` lines 86–119): chip colorate con
sfondo pieno. Da sostituire con:

1. Riga superiore con i 3 nomi categoria (Impasto / Farine / Utility) in
   `labelSmall` con letter-spacing aumentato, preceduti da un quadratino
   5×5 dp pieno del colore categoria.
2. Filetto Ink 1 dp + doppio filetto Ink 3 dp sotto.
3. `LazyRow` di tab: `Modifier.padding(horizontal = 10dp, vertical = 4dp)`,
   `RoundedCornerShape(4dp)`. Attiva: background colore categoria, testo
   Paper. Inattiva: testo nel colore categoria, sfondo trasparente, italic.

---

## 9. Dialog

Sostituisce `AlertDialog` standard. Pattern:

- `shape = RoundedCornerShape(4.dp)` (non i 28 dp di default).
- Header con kicker + titolo (`displayMedium`).
- Body con padding 20 dp.
- Footer separato da una `DashedDivider`. Bottoni come testo
  cliccabile (`TextButton`): "Annulla" italic Ink2, "Conferma"
  uppercase bold Primary.
- 4 angoli con corner-mark Primary 18 dp.

---

## 10. Mapping schermo → file Kotlin esistente

| Schermata Quaderno   | File esistente da modificare                                          |
|----------------------|------------------------------------------------------------------------|
| 00.1 Splash          | **Nuovo** `SplashActivity` o `composable("splash")` route             |
| 01.1–01.2 Facile     | `ui/calculator/FacileCalculator.kt` + `DaFarinaCalculator.kt`         |
| 01.3 Avanzato        | `ui/calculator/AvanzatoCalculator.kt`                                 |
| 01.4 PRO             | `ui/calculator/ProCalculator.kt`                                      |
| 01.5 Teglia          | `ui/calculator/TegliaCalculator.kt`                                   |
| 01.6 Biga            | `ui/calculator/BigaCalculator.kt`                                     |
| 01.7 Mix Farine      | `ui/mixfarine/MixFarineScreen.kt`                                     |
| 01.8 W·Proteine      | `ui/calculator/UtilityCalculators.kt :: ForzaProteineCalculator`      |
| 01.9 Temperature     | `ui/calculator/UtilityCalculators.kt :: TemperatureFornoCalculator`   |
| 01.10 Contenitore    | `ui/calculator/UtilityCalculators.kt :: ContenitoreCalculator`        |
| 01.11 Conversione    | `ui/calculator/UtilityCalculators.kt :: ConversioneLievitoCalculator` |
| 01.– tab strip       | `ui/calculator/CalculatorScreen.kt` (lines 86–119)                    |
| 02.1 Template        | `ui/process/ProcessScreen.kt :: TemplateSelector`                     |
| 02.2 Attivo          | `ui/process/ProcessScreen.kt :: ActiveProcessView + PhaseCard`        |
| 02.3 Long-press menu | `ui/process/ProcessScreen.kt :: PhaseCard :: DropdownMenu`            |
| 02.4 Nuovo processo  | `ui/process/ProcessScreen.kt :: NewProcessNameDialog`                 |
| 02.5 Nuova fase      | `ui/process/AddEditPhaseDialog.kt`                                    |
| 02.6 Completato      | Nuovo stato in `ActiveProcessView` (mostrato quando `allCompleted`)   |
| 03.1–03.4 Cottura    | `ui/cooking/CookingScreen.kt`                                         |
| 03.5 Sound picker    | `ui/cooking/CookingScreen.kt :: SoundPickerDialog`                    |
| 04.1 Glossario       | `ui/help/HelpScreen.kt` — sostituire l'attuale `GlossaryEntry` con la versione a due colonne |

---

## 11. Note finali

- **Nessuna modifica a `PizzaFormulas.kt`.** I numeri nei mock sono indicativi;
  in app verranno dalle stesse formule attuali.
- **Nessuna modifica al `TimerService`** o ai `ViewModel`.
- **`themes.xml`** va aggiornato per il colore della status bar.
- L'icona di lancio `ic_launcher` resta quella esistente.
- L'icona di notifica `ic_notification_pizza` resta quella esistente.
- **Dark mode**: non disegnata. L'app è già light-only; se serve aggiungere
  dark mode, va disegnata da capo (la palette warm-paper non si inverte
  bene).
- **Accessibilità**: tutti i contrasti foreground/background passano AA
  (Primary `#A8392B` su Paper `#FDF9EE` = 5.6:1; Ink su Bg = 13:1).
  Verifica i tap target: i Q_RoundBtn da 26 dp sono sotto il minimo di
  48 dp Material — circondali con un `Box(modifier = Modifier.size(48.dp))`
  che funga da hit-area.

In caso di dubbi, scrivere a chi ha curato il design.
