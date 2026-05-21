# PizzaLab · Quaderno — Pacchetto di consegna

> Proposta di redesign completa per **PizzaLab**. Stile editoriale-quaderno:
> tipografia Inter, palette terra di Siena + oliva + carta, niente Material 3
> generico. Da integrare nella app Android Compose esistente.

---

## Cosa c'è qui dentro

| File / cartella           | Cos'è                                                  |
|---------------------------|--------------------------------------------------------|
| **`index.html`**          | Showcase interattiva con **tutte e 22 le schermate**. Apri in un browser. |
| `HANDOFF.md`              | Guida operativa per Cowork: mappatura componenti Compose, regole di traduzione, gotchas. **Leggere prima di integrare.** |
| `tokens.css`              | Design tokens (colori, type, spazi, radii, motion) come variabili CSS. |
| `tokens.kt`               | Stessi token già scritti come `ColorScheme` + `Typography` di Material 3. **Drop-in replacement** di `Theme.kt`. |
| `quaderno-core.jsx`       | Primitive di layout (Header, BottomNav, Field, Card, Dialog…). Codice di riferimento per i componenti Compose. |
| `quaderno-calc.jsx`       | Tutte e 11 le schermate dei calcolatori. |
| `quaderno-rest.jsx`       | Processo, Cottura, Aiuto + tutti i dialog. |
| `shared.jsx`              | Icone, dati di esempio. |
| `android-frame.jsx`       | Cornice Pixel-like usata nello showcase (solo per anteprima). |
| `assets/`                 | Launcher icon, materiali grafici. |

---

## Le 22 schermate

| # | Schermata | File |
|---|-----------|------|
| **00.1** | Splash all'avvio | `Q_Splash` in `quaderno-calc.jsx` |
| **01.1** | Calcolatore · Facile (Per Panetti) | `Q_CalcFacile` |
| **01.2** | Calcolatore · Facile (Da Farina) | `Q_CalcDaFarinaInner` |
| **01.3** | Calcolatore · Avanzato | `Q_CalcAvanzato` |
| **01.4** | Calcolatore · PRO | `Q_CalcPRO` |
| **01.5** | Calcolatore · Teglia (3 forme) | `Q_CalcTeglia` |
| **01.6** | Calcolatore · Biga | `Q_CalcBiga` |
| **01.7** | Calcolatore · Mix Farine | `Q_CalcMixFarine` |
| **01.8** | Calcolatore · W da Proteine | `Q_CalcWProteine` |
| **01.9** | Calcolatore · Temperature Forno | `Q_CalcTempForno` |
| **01.10** | Calcolatore · Contenitore | `Q_CalcContenitore` |
| **01.11** | Calcolatore · Conversione Lievito | `Q_CalcConversione` |
| **02.1** | Processo · Template selector | `Q_ProcessTemplates` |
| **02.2** | Processo · Attivo (running) | `Q_ProcessActive` |
| **02.3** | Processo · Long-press menu | `Q_ProcessContextMenu` |
| **02.4** | Processo · Dialog "Nuovo" | `Q_ProcessNewDialog` |
| **02.5** | Processo · Dialog "Nuova fase" | `Q_ProcessAddPhaseDialog` |
| **02.6** | Processo · Completato | `Q_ProcessCompleted` |
| **03.1** | Cottura · Timer principale | `Q_Cooking state="running"` |
| **03.2** | Cottura · Custom timer | `Q_CookingCustom` |
| **03.3** | Cottura · Multi-timer | `Q_CookingMultiTimer` |
| **03.4** | Cottura · "Pronta!" | `Q_Cooking state="complete"` |
| **03.5** | Cottura · Sound picker dialog | `Q_CookingSoundPicker` |
| **04.1** | Aiuto · Glossario completo | `Q_Help` |

---

## Cambiamenti chiave rispetto all'app attuale

1. **Niente più slider Material 3.** Sostituiti da **stepper +/− con preset
   chip** (Q_Field). Più preciso, più tattile, più "ricettario".
2. **Tab calcolatore ridisegnate.** Categoria su etichetta minuscola in alto
   (puntino + nome), tab sottostanti su una riga sola con stile editoriale
   (filetto sopra + doppio filetto sotto).
3. **Card alleggerite.** Niente più `OutlinedCard` generico. Card con
   **corner-mark** terracotta nei due angoli opposti, niente bordo continuo
   per dare il feel "scheda strappata da un blocco".
4. **Leader-dots** (`Farina ⋯⋯⋯ 614 g`) per ogni riga ingrediente, allinea
   visivamente label e numero senza tabella.
5. **Tipografia stratificata.** 4 livelli netti:
   tutto-MAIUSCOLO terracotta (kicker) · bold grandissimo (titolo) ·
   italic oliva (sottotitolo) · sans regolare (corpo).
6. **Stato "Pronta!"** invece di un overlay generico Material — il
   quadrante intero diventa rosso pomodoro pieno, con numerali bianchi.
7. **Bottom nav** con sottolineatura terracotta + label in italic per le
   tab inattive (subtle, da "menu del giorno").
8. **Splash branded**: logo pizza disegnato + wordmark + decorazione
   tipografica "edizione 2026 · n° 1" che richiama il nome "Quaderno".

---

## Come consegnare a Cowork

1. **Mostra `index.html`** in un browser per il walkthrough visivo.
2. **Manda l'intera cartella `pizzalab-quaderno/`** (oppure lo zip).
3. Indica `HANDOFF.md` come primo file da leggere per l'integrazione.
4. Il file `tokens.kt` è **drop-in** in `app/src/main/java/com/pizzalab/ui/theme/`
   e sostituisce `Theme.kt`. Tutto il resto è layout: nessuna modifica alla
   logica dei calcolatori, alle formule (`PizzaFormulas.kt`), ai viewmodels,
   al servizio timer.

---

## Cose ancora aperte

- **Font Inter** è caricato da Google Fonts nei mock; per la app Android conviene
  includere i `.ttf` in `app/src/main/res/font/` e dichiararli in una `FontFamily`.
- **L'icona del notification** (`ic_notification_pizza.xml`) è quella esistente
  ed è già compatibile.
- **Il launcher icon** rimane il PNG esistente; il design del Quaderno include
  un logo SVG nello splash che richiama la stessa pizza.
- **Dark mode**: non disegnata (l'app è light-only). Se Cowork la vuole
  introdurre, parlarne — la palette ha bisogno di una mappatura dedicata, non
  basta invertire i colori.
- **Animazioni**: non specificate per ogni transizione. Convenzione: tutto
  `tween(240ms, ease-out)` salvo il completamento del timer che fa una
  pulsazione lenta sul quadrante.
