// shared.jsx — utilities shared across the 3 redesign variants.
// Icons are simple inline SVGs (Material/Lucide stroke style).
// Each variant exports its own screens; this file only exports primitives.

// ─── Icons ────────────────────────────────────────────────────────────
// All icons are stroked, 24×24 viewBox, currentColor. Pass {size, stroke}.
// Match Lucide/Material Symbols proportions.

const Icon = ({ children, size = 24, stroke = 2, fill = 'none', style }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill={fill}
    stroke="currentColor"
    strokeWidth={stroke}
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ flexShrink: 0, ...style }}
  >
    {children}
  </svg>
);

const IconCalculator   = (p) => <Icon {...p}><rect x="4" y="2" width="16" height="20" rx="2"/><line x1="8" y1="6" x2="16" y2="6"/><line x1="16" y1="14" x2="16" y2="18"/><path d="M16 10h.01M12 10h.01M8 10h.01M12 14h.01M8 14h.01M12 18h.01M8 18h.01"/></Icon>;
const IconTimer        = (p) => <Icon {...p}><line x1="10" y1="2" x2="14" y2="2"/><line x1="12" y1="14" x2="15" y2="11"/><circle cx="12" cy="14" r="8"/></Icon>;
const IconFlame        = (p) => <Icon {...p}><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></Icon>;
const IconHelp         = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></Icon>;
const IconBook         = (p) => <Icon {...p}><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/></Icon>;
const IconPlay         = (p) => <Icon {...p} fill="currentColor"><polygon points="6 3 20 12 6 21 6 3"/></Icon>;
const IconPause        = (p) => <Icon {...p} fill="currentColor"><rect x="6" y="4" width="4" height="16"/><rect x="14" y="4" width="4" height="16"/></Icon>;
const IconReplay       = (p) => <Icon {...p}><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></Icon>;
const IconPlus         = (p) => <Icon {...p}><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></Icon>;
const IconMinus        = (p) => <Icon {...p}><line x1="5" y1="12" x2="19" y2="12"/></Icon>;
const IconClose        = (p) => <Icon {...p}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></Icon>;
const IconCheck        = (p) => <Icon {...p}><polyline points="20 6 9 17 4 12"/></Icon>;
const IconCheckCircle  = (p) => <Icon {...p}><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></Icon>;
const IconCircle       = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/></Icon>;
const IconHourglass    = (p) => <Icon {...p}><path d="M6 2h12M6 22h12M6 2v6a6 6 0 0 0 12 0V2M6 22v-6a6 6 0 0 1 12 0v6"/></Icon>;
const IconGrip         = (p) => <Icon {...p}><circle cx="9" cy="6" r="1" fill="currentColor"/><circle cx="9" cy="12" r="1" fill="currentColor"/><circle cx="9" cy="18" r="1" fill="currentColor"/><circle cx="15" cy="6" r="1" fill="currentColor"/><circle cx="15" cy="12" r="1" fill="currentColor"/><circle cx="15" cy="18" r="1" fill="currentColor"/></Icon>;
const IconPizza        = (p) => <Icon {...p}><path d="M15 11h.01M11 15h.01M16 16h.01M2 16l20 6-6-20A20 20 0 0 0 2 16"/><path d="M5.71 17.11a17.04 17.04 0 0 1 11.4-11.4"/></Icon>;
const IconChevronDown  = (p) => <Icon {...p}><polyline points="6 9 12 15 18 9"/></Icon>;
const IconChevronRight = (p) => <Icon {...p}><polyline points="9 18 15 12 9 6"/></Icon>;
const IconChevronLeft  = (p) => <Icon {...p}><polyline points="15 18 9 12 15 6"/></Icon>;
const IconSearch       = (p) => <Icon {...p}><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></Icon>;
const IconMusic        = (p) => <Icon {...p}><path d="M9 18V5l12-2v13"/><circle cx="6" cy="18" r="3"/><circle cx="18" cy="16" r="3"/></Icon>;
const IconThermometer  = (p) => <Icon {...p}><path d="M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"/></Icon>;
const IconDroplet      = (p) => <Icon {...p}><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></Icon>;
const IconScale        = (p) => <Icon {...p}><path d="M3 6l5-2 4 1 4-1 5 2-3 6h-4l-2-4-2 4H6z"/><circle cx="12" cy="17" r="3"/></Icon>;
const IconWheat        = (p) => <Icon {...p}><path d="M2 22 16 8M3.47 12.53 5 11l1.53 1.53a3.5 3.5 0 0 1 0 4.94L5 19l-1.53-1.53a3.5 3.5 0 0 1 0-4.94zM7.47 8.53 9 7l1.53 1.53a3.5 3.5 0 0 1 0 4.94L9 15l-1.53-1.53a3.5 3.5 0 0 1 0-4.94zM11.47 4.53 13 3l1.53 1.53a3.5 3.5 0 0 1 0 4.94L13 11l-1.53-1.53a3.5 3.5 0 0 1 0-4.94zM20 2h2v2a4 4 0 0 1-4 4h-2V6a4 4 0 0 1 4-4zM11.47 17.47 13 19l-1.53 1.53a3.5 3.5 0 0 1-4.94 0L5 19l1.53-1.53a3.5 3.5 0 0 1 4.94 0zM15.47 13.47 17 15l-1.53 1.53a3.5 3.5 0 0 1-4.94 0L9 15l1.53-1.53a3.5 3.5 0 0 1 4.94 0zM19.47 9.47 21 11l-1.53 1.53a3.5 3.5 0 0 1-4.94 0L13 11l1.53-1.53a3.5 3.5 0 0 1 4.94 0z"/></Icon>;
const IconClock        = (p) => <Icon {...p}><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></Icon>;
const IconArrowRight   = (p) => <Icon {...p}><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></Icon>;
const IconMoreH        = (p) => <Icon {...p}><circle cx="12" cy="12" r="1" fill="currentColor"/><circle cx="19" cy="12" r="1" fill="currentColor"/><circle cx="5" cy="12" r="1" fill="currentColor"/></Icon>;

// ─── Sample data shared by all variants ───────────────────────────────
// Values consistent with PizzaFormulas output for ~4 panetti / 250g / 24h / 22°C / 63%.

const RECIPE = {
  nPanetti: 4,
  pesoPanetto: 250,
  oreLievitazione: 24,
  temperatura: 22,
  idratazione: 63,
  // results
  farina: 614,
  acqua: 387,
  sale: 18,
  ldbf: 1.05,
  ldbs: 0.35,
  totale: 1020,
  forza: 280,
  tempChiusura: 24,
  puntataMin: 120,
  frigoMin: 1080,
  aprettoMin: 240,
  inizioApretto: '14:00',
  pronti: '18:00',
};

const PROCESS_PHASES = [
  { name: 'Autolisi', desc: 'Riposo farina + acqua',           min: 30,    state: 'done' },
  { name: 'Impasto',  desc: 'Sale + lievito',                   min: 20,    state: 'done' },
  { name: 'Puntata',  desc: 'Prima lievitazione',               min: 120,   state: 'active', elapsed: 47 },
  { name: 'Frigo',    desc: 'Maturazione 4 °C',                 min: 1440,  state: 'pending' },
  { name: 'Appretto', desc: 'Lievitazione finale dei panetti',  min: 120,   state: 'pending' },
];

const COOKING_PRESETS = [
  { name: 'Napoletana', sec: 90,  hint: 'Forno a legna ~450°C' },
  { name: 'Teglia',     sec: 240, hint: 'Elettrico ~300°C' },
  { name: 'Pala',       sec: 180, hint: 'Elettrico ~350°C' },
  { name: 'Padellino',  sec: 300, hint: 'Elettrico ~250°C' },
];

const GLOSSARY = [
  { section: 'Ingredienti', items: [
    { term: 'Idratazione',  def: 'La percentuale di acqua sulla farina. 63 % significa 630 g di acqua per 1 kg di farina.' },
    { term: 'W',            def: 'La forza della farina: capacità di assorbire acqua e trattenere i gas. W 280 per lunghe lievitazioni.' },
    { term: 'Sale',         def: 'Rafforza il glutine, rallenta la lievitazione. Circa il 2.5 % sulla farina.' },
    { term: 'Lievito (LDB)',def: 'Cubetto di lievito di birra fresco. Sciogli in acqua tiepida.' },
    { term: 'Malto',        def: 'Zucchero naturale che nutre il lievito e colora la crosta. 0.1–0.5 %.' },
  ]},
  { section: 'Lievitazione', items: [
    { term: 'Autolisi', def: 'Riposo di farina e acqua, senza sale né lievito. Idrata la farina e pre-forma il glutine.' },
    { term: 'Puntata',  def: 'Prima lievitazione in massa, subito dopo l\u2019impasto. Sviluppa aromi.' },
    { term: 'Frigo',    def: 'Maturazione lenta a 4 °C. Allunga la conservazione e migliora la digeribilità.' },
    { term: 'Appretto', def: 'Lievitazione finale dei singoli panetti a temperatura ambiente.' },
    { term: 'Staglio',  def: 'Divisione dell\u2019impasto in panetti del peso desiderato.' },
  ]},
  { section: 'Cottura', items: [
    { term: 'Napoletana', def: 'Cottura in forno a legna a ~450 °C, 60–90 secondi. Cornicione alto e leopardato.' },
    { term: 'Teglia',     def: 'Pizza al taglio in forno elettrico a 250–300 °C, 4–6 minuti. Base croccante.' },
    { term: 'Pala',       def: 'Pizza lunga su pala romana, ~350 °C per 3 minuti. Alta idratazione.' },
  ]},
];

// ─── Helpers ───────────────────────────────────────────────────────────
function fmtMin(min) {
  if (min < 60) return min + ' min';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return m === 0 ? h + ' h' : h + ' h ' + m + ' min';
}
function fmtSec(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return String(m).padStart(1, '0') + ':' + String(s).padStart(2, '0');
}

// ─── Variant label tag (rendered above each phone in the canvas) ─────
function VariantTag({ letter, name, subtitle, accent }) {
  return (
    <div style={{
      fontFamily: 'Inter, system-ui',
      display: 'flex', alignItems: 'baseline', gap: 10,
      padding: '0 4px 8px',
    }}>
      <div style={{
        fontSize: 13, fontWeight: 700, color: accent, letterSpacing: '0.08em',
      }}>{letter}</div>
      <div style={{ fontSize: 15, fontWeight: 600, color: '#2a1f17' }}>{name}</div>
      <div style={{ fontSize: 12, color: 'rgba(60,40,30,0.6)' }}>{subtitle}</div>
    </div>
  );
}

Object.assign(window, {
  Icon,
  IconCalculator, IconTimer, IconFlame, IconHelp, IconBook,
  IconPlay, IconPause, IconReplay, IconPlus, IconMinus,
  IconClose, IconCheck, IconCheckCircle, IconCircle, IconHourglass,
  IconGrip, IconPizza, IconChevronDown, IconChevronRight, IconChevronLeft,
  IconSearch, IconMusic, IconThermometer, IconDroplet, IconScale, IconWheat,
  IconClock, IconArrowRight, IconMoreH,
  RECIPE, PROCESS_PHASES, COOKING_PRESETS, GLOSSARY,
  fmtMin, fmtSec, VariantTag,
});
