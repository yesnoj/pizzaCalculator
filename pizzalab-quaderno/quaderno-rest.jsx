// quaderno-screens-rest.jsx
// Process screens (templates, active, dialogs), Cooking screens,
// and the full Help/Glossary screen.

// ════════════════════════════════════════════════════════════════
// PROCESSO
// ════════════════════════════════════════════════════════════════

// ─── Templates (empty state) ─────────────────────────────────────
function Q_ProcessTemplates({ onSelectTemplate, onCreateNew }) {
  const templates = [
    {
      name: 'Napoletana Classica',
      summary: 'Autolisi → Impasto → Puntata → Frigo → Appretto',
      totalH: 28,
      phases: 5,
    },
    {
      name: 'Veloce 8h',
      summary: 'Impasto → Puntata → Lievitazione → Appretto',
      totalH: 8,
      phases: 4,
    },
    {
      name: 'Teglia 24h',
      summary: 'Autolisi → Impasto → Pieghe → Frigo → Appretto',
      totalH: 24,
      phases: 5,
    },
  ];

  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Processo · Quaderno di lievitazione" title="Processi" italic="di lavorazione"/>

      <div style={{
        padding: '4px 22px 16px',
        fontFamily: Q.font, fontSize: 13, fontStyle: 'italic', color: Q.ink2,
      }}>
        Scegli un template oppure crea un nuovo processo da zero.
      </div>

      <div style={{ padding: '0 22px' }}>
        {templates.map((t, i) => (
          <div key={i} onClick={() => onSelectTemplate && onSelectTemplate(t)} style={{
            padding: '14px 16px', marginBottom: 10,
            background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 4,
            cursor: 'pointer', position: 'relative',
          }}>
            <div style={{
              position: 'absolute', top: -1, left: -1, width: 14, height: 14,
              borderTop: '2px solid ' + Q.primary, borderLeft: '2px solid ' + Q.primary,
            }}/>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
              <div style={{ fontFamily: Q.font, fontSize: 17, fontWeight: 800, color: Q.ink, letterSpacing: '-0.02em' }}>{t.name}</div>
              <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3 }}>nº 0{i+1}</div>
            </div>
            <div style={{ fontFamily: Q.font, fontSize: 12.5, color: Q.ink2, marginTop: 4, lineHeight: 1.4 }}>
              {t.summary}
            </div>
            <div style={{
              display: 'flex', alignItems: 'center', gap: 12, marginTop: 8,
              paddingTop: 8, borderTop: '1px dashed ' + Q.ruleDots,
            }}>
              <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3 }}>{t.phases} fasi</div>
              <div style={{ flex: 1 }}/>
              <div style={{ fontFamily: Q.font, fontSize: 13, fontWeight: 700, color: Q.primary, fontVariantNumeric: 'tabular-nums' }}>{t.totalH} h totali</div>
              <IconArrowRight size={14} stroke={2} style={{ color: Q.ink2 }}/>
            </div>
          </div>
        ))}
      </div>

      <div style={{ padding: '8px 22px 22px' }}>
        <div onClick={onCreateNew} style={{
          padding: '14px', textAlign: 'center', cursor: 'pointer',
          fontFamily: Q.font, fontSize: 13, fontWeight: 700, color: Q.ink,
          border: '1px dashed ' + Q.ink2, borderRadius: 4,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          fontStyle: 'italic',
        }}>
          <IconPlus size={14} stroke={2.2}/> crea un nuovo processo
        </div>
      </div>
    </div>
  );
}

// ─── Active process (running) ────────────────────────────────────
function Q_ProcessActive() {
  const phases = PROCESS_PHASES;
  const startHour = 10;
  let cum = startHour;
  const decorated = phases.map(p => {
    const start = cum;
    cum += p.min/60;
    return { ...p, start, end: cum };
  });
  const fmtHour = (h) => {
    const hh = Math.floor(h) % 24, mm = Math.round((h - Math.floor(h)) * 60);
    return String(hh).padStart(2,'0') + ':' + String(mm).padStart(2,'0');
  };

  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Processo · Quaderno di lievitazione" title="Napoletana" italic="classica" right={
        <div style={{
          padding: '4px 10px', borderRadius: 999, border: '1px solid ' + Q.primary,
          fontFamily: Q.font, fontSize: 11, fontWeight: 700, color: Q.primary,
          letterSpacing: '0.06em', textTransform: 'uppercase',
        }}>in corso</div>
      }/>

      <div style={{
        padding: '6px 22px 12px',
        fontFamily: Q.font, fontSize: 12, fontStyle: 'italic', color: Q.ink2,
        borderBottom: '1px dashed ' + Q.ruleDots,
      }}>
        partito alle 10:00 · pronti alle <span style={{ fontStyle: 'normal', fontWeight: 700, color: Q.ink }}>18:00</span> di domani
      </div>

      <div style={{ padding: '8px 22px' }}>
        {decorated.map((p, i) => {
          const done = p.state === 'done', active = p.state === 'active';
          return (
            <div key={i} style={{
              display: 'grid', gridTemplateColumns: '56px 1fr', columnGap: 14,
              padding: '12px 0',
              borderBottom: i < decorated.length - 1 ? '1px solid ' + Q.rule : 'none',
              opacity: done ? 0.6 : 1, position: 'relative',
            }}>
              <div style={{ paddingTop: 4 }}>
                <div style={{
                  fontFamily: Q.font, fontSize: 14, fontWeight: 700,
                  color: active ? Q.primary : Q.ink2,
                  fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em',
                }}>{fmtHour(p.start)}</div>
                <div style={{
                  fontFamily: Q.font, fontSize: 9.5, fontStyle: 'italic',
                  color: Q.ink3, marginTop: 2,
                }}>nº {String(i+1).padStart(2,'0')}</div>
              </div>

              <div>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                  <div style={{
                    fontFamily: Q.font, fontSize: 18, fontWeight: 800,
                    color: Q.ink, letterSpacing: '-0.02em',
                  }}>{p.name}</div>
                  <div style={{
                    fontFamily: Q.font, fontSize: 11.5, fontStyle: 'italic',
                    color: Q.ink2,
                  }}>{fmtMin(p.min)}</div>
                  {!done && !active && (
                    <div style={{ marginLeft: 'auto', color: Q.ink3 }}>
                      <IconGrip size={16}/>
                    </div>
                  )}
                </div>
                <div style={{ fontFamily: Q.font, fontSize: 12.5, color: Q.ink2, marginTop: 2 }}>
                  {p.desc}
                </div>

                {active && (
                  <div style={{ marginTop: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ flex: 1, height: 4, background: Q.bgWarmer, borderRadius: 2, overflow: 'hidden' }}>
                      <div style={{ width: (p.elapsed/p.min)*100 + '%', height: '100%', background: Q.primary, borderRadius: 2 }}/>
                    </div>
                    <div style={{ fontFamily: Q.font, fontSize: 11, color: Q.primary, fontWeight: 700, fontVariantNumeric: 'tabular-nums' }}>
                      {fmtMin(p.elapsed)} / {fmtMin(p.min)}
                    </div>
                  </div>
                )}

                {done && (
                  <div style={{
                    marginTop: 4, fontFamily: Q.font, fontSize: 11,
                    fontStyle: 'italic', color: Q.olive,
                    display: 'flex', alignItems: 'center', gap: 4,
                  }}>
                    <IconCheck size={12} stroke={2}/> fatta
                  </div>
                )}
              </div>
            </div>
          );
        })}

        {/* add phase button */}
        <div style={{
          margin: '12px 0 4px', padding: '10px',
          textAlign: 'center', cursor: 'pointer',
          fontFamily: Q.font, fontSize: 12.5, fontWeight: 600, color: Q.ink2,
          border: '1px dashed ' + Q.ruleDots, borderRadius: 4,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
          fontStyle: 'italic',
        }}>
          <IconPlus size={13}/> aggiungi una fase
        </div>
      </div>

      <div style={{ padding: '12px 22px 18px', display: 'flex', gap: 8 }}>
        <div style={{ flex: 1 }}><Q_DarkBtn>Completa fase</Q_DarkBtn></div>
        <Q_SecondaryBtn italic>pausa</Q_SecondaryBtn>
      </div>
    </div>
  );
}

// ─── Active process — COMPLETED ──────────────────────────────────
function Q_ProcessCompleted() {
  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Processo · Napoletana Classica" title="Completato" italic="ben fatto" right={
        <div style={{
          padding: '4px 10px', borderRadius: 999, background: Q.olive, border: '1px solid ' + Q.olive,
          fontFamily: Q.font, fontSize: 11, fontWeight: 700, color: Q.paper,
          letterSpacing: '0.06em', textTransform: 'uppercase',
        }}>fatto</div>
      }/>

      <Q_Card kicker="Riepilogo" title="28 h 10 min" titleSuffix="totali" meta="21–22 maggio">
        <Q_LeaderRow label="Autolisi"  value="30 min" unit=""/>
        <Q_LeaderRow label="Impasto"   value="20 min" unit=""/>
        <Q_LeaderRow label="Puntata"   value="2 h"    unit=""/>
        <Q_LeaderRow label="Frigo"     value="24 h"   unit=""/>
        <Q_LeaderRow label="Appretto"  value="2 h"    unit=""/>

        <div style={{
          marginTop: 14, padding: '12px 0 0', borderTop: '1px dashed ' + Q.ruleDots,
          textAlign: 'center',
        }}>
          <div style={{ fontFamily: Q.font, fontSize: 32, fontWeight: 900, color: Q.primary, letterSpacing: '-0.03em' }}>«</div>
          <div style={{ fontFamily: Q.font, fontSize: 13, fontStyle: 'italic', color: Q.ink2, padding: '0 8px', lineHeight: 1.5 }}>
            Tutte le fasi sono completate. I panetti sono pronti per la stesura e la cottura.
          </div>
          <div style={{ fontFamily: Q.font, fontSize: 32, fontWeight: 900, color: Q.primary, letterSpacing: '-0.03em', marginTop: -8 }}>»</div>
        </div>
      </Q_Card>

      <div style={{ padding: '16px 22px 22px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <Q_PrimaryBtn icon={IconFlame}>Passa alla cottura</Q_PrimaryBtn>
        <Q_SecondaryBtn italic>nuovo processo</Q_SecondaryBtn>
      </div>
    </div>
  );
}

// ─── Dialog: Nuovo processo (name input) ─────────────────────────
function Q_ProcessNewDialog({ onConfirm, onDismiss }) {
  return (
    <Q_Dialog kicker="Nuovo" title="Processo" onConfirm={onConfirm} onDismiss={onDismiss} confirmLabel="Crea">
      <div style={{ fontFamily: Q.font, fontSize: 12.5, color: Q.ink2, marginBottom: 12, fontStyle: 'italic' }}>
        Dai un nome al tuo nuovo processo di lievitazione.
      </div>
      <div>
        <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 4 }}>nome del processo</div>
        <div style={{
          padding: '0 0 6px', borderBottom: '2px solid ' + Q.ink,
          fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.ink,
        }}>
          Domenica in famiglia<span style={{ animation: 'blink 1s infinite', color: Q.primary, fontWeight: 400, marginLeft: 1 }}>|</span>
        </div>
      </div>
    </Q_Dialog>
  );
}

// ─── Dialog: Aggiungi / Modifica fase ────────────────────────────
function Q_ProcessAddPhaseDialog({ onConfirm, onDismiss }) {
  const presets = ['Autolisi','Impasto','Puntata','Pieghe','Staglio','Frigo','Appretto','Rinfresco','Personalizzata'];
  const [sel, setSel] = React.useState('Frigo');
  const data = {
    'Autolisi':       { name: 'Autolisi',       desc: 'Riposo farina e acqua',    h: 0, m: 30 },
    'Impasto':        { name: 'Impasto',        desc: 'Impastamento ingredienti', h: 0, m: 20 },
    'Puntata':        { name: 'Puntata',        desc: 'Prima lievitazione in massa', h: 2, m: 0 },
    'Pieghe':         { name: 'Pieghe',         desc: 'Pieghe di rinforzo',       h: 0, m: 5 },
    'Staglio':        { name: 'Staglio',        desc: 'Formatura dei panetti',    h: 0, m: 15 },
    'Frigo':          { name: 'Frigo',          desc: 'Maturazione in frigorifero', h: 24, m: 0 },
    'Appretto':       { name: 'Appretto',       desc: 'Lievitazione finale a temperatura', h: 2, m: 0 },
    'Rinfresco':      { name: 'Rinfresco',      desc: 'Aggiunta ingredienti al pre-fermento', h: 0, m: 10 },
    'Personalizzata': { name: '',               desc: '',                          h: 0, m: 0 },
  }[sel];

  return (
    <Q_Dialog kicker="Nuova" title="Fase di lievitazione" onConfirm={onConfirm} onDismiss={onDismiss} confirmLabel="Aggiungi">
      <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 8 }}>fase predefinita</div>
      <Q_ChipRow items={presets} value={sel} onChange={setSel} wrap/>

      <div style={{ marginTop: 12 }}>
        <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 4 }}>nome</div>
        <div style={{ padding: '0 0 4px', borderBottom: '2px solid ' + Q.ink, fontFamily: Q.font, fontSize: 16, fontWeight: 700, color: Q.ink }}>
          {data.name || <span style={{ color: Q.ink3, fontWeight: 400, fontStyle: 'italic' }}>il nome della fase</span>}
        </div>
      </div>

      <div style={{ marginTop: 10 }}>
        <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 4 }}>descrizione</div>
        <div style={{ padding: '0 0 4px', borderBottom: '1px dotted ' + Q.ruleDots, fontFamily: Q.font, fontSize: 13, color: Q.ink2 }}>
          {data.desc || '—'}
        </div>
      </div>

      <div style={{ marginTop: 12, display: 'flex', gap: 12 }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 4 }}>ore</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <button style={Q_RoundBtn}><IconMinus size={12} stroke={2.2}/></button>
            <div style={{ flex: 1, textAlign: 'center', fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.ink, borderBottom: '2px solid ' + Q.ink, paddingBottom: 1, fontVariantNumeric: 'tabular-nums' }}>{data.h}</div>
            <button style={Q_RoundBtn}><IconPlus size={12} stroke={2.2}/></button>
          </div>
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginBottom: 4 }}>minuti</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
            <button style={Q_RoundBtn}><IconMinus size={12} stroke={2.2}/></button>
            <div style={{ flex: 1, textAlign: 'center', fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.ink, borderBottom: '2px solid ' + Q.ink, paddingBottom: 1, fontVariantNumeric: 'tabular-nums' }}>{data.m}</div>
            <button style={Q_RoundBtn}><IconPlus size={12} stroke={2.2}/></button>
          </div>
        </div>
      </div>
    </Q_Dialog>
  );
}

// ─── Context menu (long-press on a phase) ────────────────────────
function Q_ProcessContextMenu() {
  return (
    <div style={{ background: Q.bg, minHeight: '100%', position: 'relative' }}>
      {/* render a single phase entry, with menu over it */}
      <Q_Header kicker="Processo · Napoletana classica" title="Napoletana" italic="classica"/>
      <div style={{ padding: '8px 22px' }}>
        {PROCESS_PHASES.slice(0,3).map((p, i) => {
          const done = p.state === 'done', active = p.state === 'active';
          const highlighted = i === 2;
          return (
            <div key={i} style={{
              padding: '12px 12px',
              marginBottom: 8, borderRadius: 4,
              background: highlighted ? Q.paper : 'transparent',
              border: highlighted ? '2px solid ' + Q.primary : '1px solid ' + Q.rule,
              opacity: done ? 0.6 : 1,
              boxShadow: highlighted ? '0 8px 24px rgba(43,31,18,0.16)' : 'none',
              position: 'relative',
            }}>
              <div style={{ fontFamily: Q.font, fontSize: 16, fontWeight: 800, color: Q.ink }}>{p.name}</div>
              <div style={{ fontFamily: Q.font, fontSize: 12, color: Q.ink2, marginTop: 2 }}>{p.desc} · {fmtMin(p.min)}</div>
            </div>
          );
        })}
      </div>

      {/* floating menu */}
      <div style={{
        position: 'absolute', top: 240, right: 36, zIndex: 5,
        background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 4,
        boxShadow: '0 12px 32px rgba(43,31,18,0.18)',
        minWidth: 180, padding: '4px 0',
      }}>
        <div style={{
          position: 'absolute', top: -1, left: -1, width: 12, height: 12,
          borderTop: '2px solid ' + Q.primary, borderLeft: '2px solid ' + Q.primary,
        }}/>
        {[
          { icon: IconEdit, label: 'Modifica', color: Q.ink },
          { icon: IconTrash, label: 'Elimina', color: Q.primary, italic: true },
        ].map((m, i) => (
          <div key={i} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '10px 16px', cursor: 'pointer',
            fontFamily: Q.font, fontSize: 13, color: m.color, fontWeight: 600,
            fontStyle: m.italic ? 'italic' : 'normal',
          }}>
            <m.icon size={16} stroke={1.8}/> {m.label}
          </div>
        ))}
      </div>

      {/* hint */}
      <div style={{
        position: 'absolute', bottom: 24, left: 22, right: 22,
        padding: '10px 14px', background: Q.bgWarmer, borderRadius: 4,
        fontFamily: Q.font, fontSize: 12, fontStyle: 'italic', color: Q.ink2,
        textAlign: 'center', borderLeft: '3px solid ' + Q.olive,
      }}>
        Tieni premuto su una fase per modificarla o eliminarla.
      </div>
    </div>
  );
}

// Need IconEdit / IconTrash – use IconClose for delete, draw a pencil for edit.
const IconEdit  = (p) => <Icon {...p}><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></Icon>;
const IconTrash = (p) => <Icon {...p}><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1.5 14a2 2 0 0 1-2 2h-7a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v2"/></Icon>;

// ════════════════════════════════════════════════════════════════
// COTTURA
// ════════════════════════════════════════════════════════════════

// ─── Helper: clock face dial ─────────────────────────────────────
function Q_ClockDial({ presetName, remaining, total }) {
  const pct = total > 0 ? remaining / total : 0;
  const handAngle = (1 - pct) * 360;
  const C = 2 * Math.PI * 122;

  return (
    <div style={{ position: 'relative', width: 280, height: 280 }}>
      <svg width="280" height="280" viewBox="0 0 280 280">
        <circle cx="140" cy="140" r="132" fill={Q.paper} stroke={Q.ink} strokeWidth="1.5"/>
        {Array.from({length: 60}).map((_, i) => {
          const a = (i / 60) * 2 * Math.PI - Math.PI/2;
          const r1 = 122, r2 = i % 5 === 0 ? 108 : 116;
          const x1 = 140 + Math.cos(a)*r1, y1 = 140 + Math.sin(a)*r1;
          const x2 = 140 + Math.cos(a)*r2, y2 = 140 + Math.sin(a)*r2;
          return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2}
            stroke={i % 5 === 0 ? Q.ink : Q.ink3} strokeWidth={i % 5 === 0 ? 1.5 : 1}/>;
        })}
        <circle cx="140" cy="140" r="122" fill="none"
          stroke={Q.primary} strokeWidth="3"
          strokeDasharray={C} strokeDashoffset={C * (1 - pct)}
          transform="rotate(-90 140 140)" opacity="0.6"/>
        <circle cx="140" cy="140" r="6" fill={Q.ink}/>
        <g transform={`rotate(${handAngle} 140 140)`}>
          <line x1="140" y1="140" x2="140" y2="32" stroke={Q.primary} strokeWidth="3" strokeLinecap="round"/>
          <circle cx="140" cy="32" r="4" fill={Q.primary}/>
        </g>
      </svg>
      <div style={{ position: 'absolute', bottom: 64, left: 0, right: 0, textAlign: 'center' }}>
        <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.22em', textTransform: 'uppercase', color: Q.primary }}>{presetName}</div>
        <div style={{ fontFamily: Q.font, fontSize: 38, fontWeight: 800, color: Q.ink, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.04em', lineHeight: 1, marginTop: 4 }}>{fmtSec(remaining)}</div>
        <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 11, color: Q.ink3, marginTop: 4 }}>su {fmtSec(total)} totali</div>
      </div>
    </div>
  );
}

// ─── Helper: clock face — COMPLETE state ─────────────────────────
function Q_ClockDialComplete({ presetName }) {
  return (
    <div style={{ position: 'relative', width: 280, height: 280 }}>
      <svg width="280" height="280" viewBox="0 0 280 280">
        <circle cx="140" cy="140" r="132" fill={Q.primary} stroke={Q.ink} strokeWidth="1.5"/>
        {Array.from({length: 60}).map((_, i) => {
          const a = (i / 60) * 2 * Math.PI - Math.PI/2;
          const r1 = 122, r2 = i % 5 === 0 ? 108 : 116;
          const x1 = 140 + Math.cos(a)*r1, y1 = 140 + Math.sin(a)*r1;
          const x2 = 140 + Math.cos(a)*r2, y2 = 140 + Math.sin(a)*r2;
          return <line key={i} x1={x1} y1={y1} x2={x2} y2={y2}
            stroke={Q.paper} opacity={i % 5 === 0 ? 0.9 : 0.5}
            strokeWidth={i % 5 === 0 ? 1.5 : 1}/>;
        })}
      </svg>
      <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
        <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.22em', textTransform: 'uppercase', color: Q.paper, opacity: 0.85 }}>{presetName}</div>
        <div style={{ fontFamily: Q.font, fontSize: 56, fontWeight: 900, color: Q.paper, letterSpacing: '-0.04em', lineHeight: 1, marginTop: 8 }}>Pronta!</div>
        <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 13, color: Q.paper, opacity: 0.85, marginTop: 10 }}>0:00 alla cottura</div>
      </div>
    </div>
  );
}

// ─── Cottura — main running timer ────────────────────────────────
function Q_Cooking({ state }) {
  // state: 'idle' | 'running' | 'complete' | 'custom' | 'multi'
  const [preset, setPreset] = React.useState(COOKING_PRESETS[0]);
  const remaining = state === 'complete' ? 0 : 67;

  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Cottura" title="Forno" italic={state === 'complete' ? 'pizza pronta' : 'al lavoro'}/>

      <div style={{ padding: '0 22px 14px' }}>
        <div style={{ fontFamily: Q.font, fontSize: 12, fontStyle: 'italic', color: Q.ink2 }}>{preset.hint}</div>
      </div>

      <div style={{
        display: 'flex', gap: 6, padding: '0 22px 14px', flexWrap: 'wrap',
        borderBottom: '1px dashed ' + Q.ruleDots,
      }}>
        {COOKING_PRESETS.map(p => {
          const on = p.name === preset.name;
          return (
            <div key={p.name} onClick={() => setPreset(p)} style={{
              padding: '4px 10px', borderRadius: 3,
              border: '1px solid ' + (on ? Q.ink : Q.rule),
              background: on ? Q.ink : Q.paper,
              fontFamily: Q.font, fontSize: 11.5, fontWeight: on ? 700 : 500,
              color: on ? Q.paper : Q.ink2, cursor: 'pointer',
              fontStyle: on ? 'normal' : 'italic',
            }}>{p.name}</div>
          );
        })}
        <div style={{
          padding: '4px 10px', borderRadius: 3, border: '1px dashed ' + Q.ink2,
          background: 'transparent',
          fontFamily: Q.font, fontSize: 11.5, fontWeight: 500, fontStyle: 'italic',
          color: Q.ink2, cursor: 'pointer',
          display: 'flex', alignItems: 'center', gap: 4,
        }}>
          <IconPlus size={10}/> personalizzato
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', padding: '20px 0 12px' }}>
        {state === 'complete'
          ? <Q_ClockDialComplete presetName={preset.name}/>
          : <Q_ClockDial presetName={preset.name} remaining={remaining} total={preset.sec}/>}
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', gap: 12, padding: '0 0 14px' }}>
        {state === 'complete' ? (
          <button style={{ ...Q_CtrlBtn, width: 64, height: 56, background: Q.olive, color: Q.paper, borderColor: Q.olive, padding: '0 16px', borderRadius: 999, fontFamily: Q.font, fontSize: 12, fontWeight: 700, letterSpacing: '0.06em', textTransform: 'uppercase' }}>
            Sfornata!
          </button>
        ) : (
          <>
            <button style={Q_CtrlBtn}><IconReplay size={18} stroke={1.8}/></button>
            <button style={{ ...Q_CtrlBtn, width: 56, height: 56, background: Q.primary, color: Q.paper, borderColor: Q.primary }}>
              <IconPause size={22}/>
            </button>
            <button style={Q_CtrlBtn}><IconPlus size={18} stroke={2}/></button>
          </>
        )}
      </div>

      {/* Sound + pizze counter */}
      <div style={{
        marginTop: 'auto', padding: '12px 22px 16px',
        borderTop: '1px dashed ' + Q.ruleDots,
        background: Q.paper,
      }}>
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '6px 0',
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <IconMusic size={14} stroke={1.8} style={{ color: Q.ink2 }}/>
            <div style={{ fontFamily: Q.font, fontSize: 12.5, color: Q.ink }}>
              suono: <span style={{ fontStyle: 'italic', color: Q.primary, fontWeight: 600 }}>Campanella</span>
            </div>
          </div>
          <IconChevronRight size={14} stroke={1.8} style={{ color: Q.ink2 }}/>
        </div>
        <div style={{ height: 1, background: Q.ruleDots, margin: '6px 0' }}/>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 11, color: Q.ink3 }}>pizze sfornate · oggi</div>
            <div style={{ fontFamily: Q.font, fontSize: 22, fontWeight: 800, color: Q.ink, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.02em' }}>3</div>
          </div>
          <div style={{ display: 'flex', alignItems: 'flex-end', gap: 8 }}>
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: 3 }}>
              <div style={Q_Tally}/><div style={Q_Tally}/><div style={Q_Tally}/>
            </div>
            <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 11, color: Q.ink3 }}>iii</div>
          </div>
        </div>
      </div>
    </div>
  );
}
const Q_CtrlBtn = {
  width: 44, height: 44, borderRadius: 999,
  background: Q.paper, border: '1px solid ' + Q.ink2, color: Q.ink,
  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
  padding: 0,
};
const Q_Tally = { width: 3, height: 24, background: Q.primary, borderRadius: 1 };

// ─── Cottura — custom timer input ────────────────────────────────
function Q_CookingCustom() {
  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Cottura · personalizzato" title="Tempo libero" italic="quanto serve?"/>

      <div style={{ padding: '8px 22px 14px', borderBottom: '1px dashed ' + Q.ruleDots }}>
        <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
          {COOKING_PRESETS.map(p => (
            <div key={p.name} style={{
              padding: '4px 10px', borderRadius: 3, border: '1px solid ' + Q.rule,
              background: Q.paper, fontFamily: Q.font, fontSize: 11.5, fontStyle: 'italic',
              color: Q.ink2,
            }}>{p.name}</div>
          ))}
          <div style={{
            padding: '4px 10px', borderRadius: 3, background: Q.ink,
            fontFamily: Q.font, fontSize: 11.5, fontWeight: 700, color: Q.paper,
            display: 'flex', alignItems: 'center', gap: 4,
          }}>
            <IconCheck size={10} stroke={2.5}/> personalizzato
          </div>
        </div>
      </div>

      <div style={{
        margin: '20px 22px', padding: '18px 20px',
        background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 2,
        position: 'relative',
      }}>
        <div style={{
          position: 'absolute', top: -1, left: -1, width: 16, height: 16,
          borderTop: '2px solid ' + Q.primary, borderLeft: '2px solid ' + Q.primary,
        }}/>
        <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase', marginBottom: 14 }}>
          Imposta il tempo
        </div>

        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'center', gap: 12 }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{
              fontFamily: Q.font, fontSize: 64, fontWeight: 900, color: Q.ink,
              fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.04em', lineHeight: 1,
              borderBottom: '2px solid ' + Q.ink, padding: '0 16px 4px',
            }}>03</div>
            <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginTop: 6 }}>minuti</div>
          </div>
          <div style={{ fontFamily: Q.font, fontSize: 56, fontWeight: 900, color: Q.primary, paddingBottom: 24 }}>:</div>
          <div style={{ textAlign: 'center' }}>
            <div style={{
              fontFamily: Q.font, fontSize: 64, fontWeight: 900, color: Q.ink,
              fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.04em', lineHeight: 1,
              borderBottom: '2px solid ' + Q.ink, padding: '0 16px 4px',
            }}>30</div>
            <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3, marginTop: 6 }}>secondi</div>
          </div>
        </div>

        {/* keypad-ish presets */}
        <div style={{ marginTop: 20, display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 6 }}>
          {['30 s','1:00','2:00','2:30','3:00','5:00'].map(t => (
            <div key={t} style={{
              padding: '8px', textAlign: 'center', cursor: 'pointer',
              fontFamily: Q.font, fontSize: 12.5, fontWeight: 600, color: Q.ink2,
              border: '1px solid ' + Q.rule, borderRadius: 3, background: Q.paper,
              fontVariantNumeric: 'tabular-nums',
            }}>{t}</div>
          ))}
        </div>
      </div>

      <div style={{ padding: '0 22px 16px' }}>
        <Q_PrimaryBtn icon={IconPlay}>Avvia timer</Q_PrimaryBtn>
      </div>
    </div>
  );
}

// ─── Cottura — Sound picker dialog ───────────────────────────────
function Q_CookingSoundPicker({ onConfirm, onDismiss }) {
  const sounds = [
    { key: 'bell',  name: 'Campanella',     selected: true },
    { key: 'beep',  name: 'Bip elettronico' },
    { key: 'ding',  name: 'Ding forno' },
    { key: 'trill', name: 'Sveglia' },
    { key: 'chime', name: 'Carillon' },
  ];
  const [sel, setSel] = React.useState('bell');
  return (
    <Q_Dialog kicker="Cottura" title="Suono allarme" onConfirm={onConfirm} onDismiss={onDismiss} confirmLabel="Conferma">
      <div style={{ fontFamily: Q.font, fontSize: 12, fontStyle: 'italic', color: Q.ink2, marginBottom: 8 }}>
        Tocca per provarlo · scegli quello che ti piace.
      </div>
      {sounds.map(s => {
        const on = s.key === sel;
        return (
          <div key={s.key} onClick={() => setSel(s.key)} style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '10px 4px', borderBottom: '1px dotted ' + Q.ruleDots,
            cursor: 'pointer',
          }}>
            <div style={{
              width: 16, height: 16, borderRadius: 999,
              border: '1.5px solid ' + (on ? Q.primary : Q.ink2),
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}>
              {on && <div style={{ width: 8, height: 8, borderRadius: 999, background: Q.primary }}/>}
            </div>
            <div style={{
              flex: 1, fontFamily: Q.font, fontSize: 14, color: Q.ink,
              fontWeight: on ? 700 : 500,
            }}>{s.name}</div>
            <div style={{ color: Q.primary, cursor: 'pointer' }}>
              <IconVolume size={18} stroke={1.6}/>
            </div>
          </div>
        );
      })}
    </Q_Dialog>
  );
}
const IconVolume = (p) => <Icon {...p}><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"/><path d="M15.54 8.46a5 5 0 0 1 0 7.07"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14"/></Icon>;

// ─── Cottura — multi-timer list ──────────────────────────────────
function Q_CookingMultiTimer() {
  const timers = [
    { n: 4, name: 'Napoletana', remaining: 67, total: 90, running: true },
    { n: 3, name: 'Napoletana', remaining: 12, total: 90, running: true },
    { n: 2, name: 'Teglia',     remaining: 0,  total: 240, running: false, done: true },
    { n: 1, name: 'Napoletana', remaining: 0,  total: 90,  running: false, done: true },
  ];

  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Cottura" title="Forno" italic="4 in turno" right={
        <div style={{ padding: '4px 10px', borderRadius: 999, background: Q.primary, fontFamily: Q.font, fontSize: 11, fontWeight: 700, color: Q.paper, letterSpacing: '0.06em', textTransform: 'uppercase' }}>· · ·</div>
      }/>

      <div style={{ padding: '0 22px 12px', borderBottom: '1px dashed ' + Q.ruleDots }}>
        <div style={{ fontFamily: Q.font, fontSize: 12.5, fontStyle: 'italic', color: Q.ink2 }}>
          Più timer attivi in parallelo. Il principale è in alto.
        </div>
      </div>

      <div style={{ padding: '12px 22px' }}>
        {timers.map((t, i) => {
          const pct = t.total > 0 ? t.remaining / t.total : 0;
          return (
            <div key={i} style={{
              padding: '12px 14px', marginBottom: 8,
              background: t.done ? 'transparent' : Q.paper,
              border: '1px solid ' + (t.running ? Q.primary : Q.rule),
              borderRadius: 3,
              opacity: t.done ? 0.55 : 1,
              position: 'relative',
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                {/* mini dial */}
                <svg width="44" height="44" viewBox="0 0 44 44">
                  <circle cx="22" cy="22" r="18" fill="none" stroke={Q.rule} strokeWidth="2"/>
                  <circle cx="22" cy="22" r="18" fill="none" stroke={t.done ? Q.olive : Q.primary} strokeWidth="2.5"
                    strokeDasharray={2 * Math.PI * 18}
                    strokeDashoffset={2 * Math.PI * 18 * (1 - (t.done ? 1 : pct))}
                    transform="rotate(-90 22 22)" strokeLinecap="round"/>
                  {t.done && (
                    <g transform="translate(14 14)">
                      <path d="M2 8 L6 12 L14 4" fill="none" stroke={Q.olive} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"/>
                    </g>
                  )}
                </svg>

                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
                    <div style={{ fontFamily: Q.font, fontSize: 10, fontWeight: 700, letterSpacing: '0.18em', color: Q.ink3 }}>Nº {String(t.n).padStart(2,'0')}</div>
                    <div style={{ fontFamily: Q.font, fontSize: 15, fontWeight: 700, color: Q.ink, letterSpacing: '-0.01em' }}>{t.name}</div>
                  </div>
                  <div style={{
                    fontFamily: Q.font, fontSize: 22, fontWeight: 800,
                    color: t.done ? Q.olive : Q.primary,
                    fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.02em',
                    marginTop: 2,
                  }}>{t.done ? 'Pronta!' : fmtSec(t.remaining)}</div>
                </div>

                {!t.done && (
                  <div style={{ display: 'flex', gap: 4 }}>
                    <button style={{ ...Q_CtrlBtn, width: 32, height: 32 }}><IconPause size={14}/></button>
                    <button style={{ ...Q_CtrlBtn, width: 32, height: 32 }}><IconReplay size={14} stroke={1.8}/></button>
                  </div>
                )}
                {t.done && (
                  <div style={{ color: Q.ink3, cursor: 'pointer' }}><IconClose size={18} stroke={1.8}/></div>
                )}
              </div>
            </div>
          );
        })}

        <div style={{
          padding: '12px', marginTop: 6, textAlign: 'center', cursor: 'pointer',
          fontFamily: Q.font, fontSize: 13, fontWeight: 700, color: Q.primary,
          border: '1px dashed ' + Q.primary, borderRadius: 3,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
        }}>
          <IconPlus size={14} stroke={2.2}/> aggiungi pizza
        </div>
      </div>

      <div style={{
        padding: '14px 22px 18px', borderTop: '1px dashed ' + Q.ruleDots,
        background: Q.paper,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      }}>
        <div>
          <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 11, color: Q.ink3 }}>pizze sfornate · stasera</div>
          <div style={{ fontFamily: Q.font, fontSize: 24, fontWeight: 800, color: Q.ink, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.02em' }}>2</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 3 }}>
          <div style={Q_Tally}/><div style={Q_Tally}/>
        </div>
      </div>
    </div>
  );
}

// ════════════════════════════════════════════════════════════════
// AIUTO / GLOSSARIO — full version
// ════════════════════════════════════════════════════════════════

const FULL_GLOSSARY = [
  { section: 'Ingredienti e misure', items: [
    { term: 'Idratazione',         def: 'La percentuale di acqua sulla farina. 63 % significa 630 g di acqua per 1 kg di farina. Più alta è l\u2019idratazione, più morbido l\u2019impasto.' },
    { term: 'Forza della farina (W)', def: 'Capacità della farina di assorbire acqua e trattenere i gas. Deboli (W 170–200) per lievitazioni brevi, forti (W 280–350) per lunghe.' },
    { term: 'Baker\u2019s %',      def: 'Sistema in cui ogni ingrediente è una percentuale sul peso della farina. La farina è sempre 100 %.' },
    { term: 'Lievito fresco (LDB)', def: 'Cubetto di lievito di birra in frigo al supermercato. Va sciolto in acqua tiepida.' },
    { term: 'Lievito secco',       def: 'Lievito disidratato in granuli. Serve circa 1/3 rispetto al fresco. L\u2019istantaneo si aggiunge direttamente alla farina.' },
    { term: 'Sale',                def: 'Rafforza il glutine, rallenta la lievitazione. Dose classica: 40 g per litro di acqua (~2.5 % sulla farina).' },
    { term: 'Malto',               def: 'Zucchero naturale che nutre il lievito e colora la crosta. Si usa 0.1–0.5 % sulla farina.' },
  ]},
  { section: 'Fasi della lievitazione', items: [
    { term: 'Impasto',             def: 'Si mescolano gli ingredienti e si lavora fino a sviluppare il glutine (la maglia glutinica). Un buon impasto è liscio ed elastico.' },
    { term: 'Autolisi',            def: 'Riposo di farina e acqua, senza sale né lievito, per 20–60 minuti prima di impastare. Pre-forma il glutine.' },
    { term: 'Puntata',             def: 'Prima lievitazione dell\u2019impasto intero, subito dopo l\u2019impastamento. Sviluppa aromi.' },
    { term: 'Pieghe (folding)',    def: 'Piegature dell\u2019impasto durante la puntata, per rafforzarlo senza re-impastare.' },
    { term: 'Staglio',             def: 'Divisione dell\u2019impasto in panetti del peso desiderato, arrotondati in palline lisce e tese.' },
    { term: 'Appretto',            def: 'Seconda lievitazione dei panetti, dopo lo staglio. L\u2019impasto è pronto quando, premuto con un dito, torna su lentamente.' },
    { term: 'Frigo (retarding)',   def: 'Lievitazione in frigo (4–6 °C) che rallenta il lievito e favorisce la maturazione. Aromi complessi, impasto più digeribile.' },
    { term: 'Maturazione',         def: 'Gli enzimi scompongono amidi e proteine. Un impasto può essere lievitato ma non ancora maturo.' },
    { term: 'Temp. di chiusura',   def: 'La temperatura dell\u2019impasto alla fine dell\u2019impastamento. Idealmente 22–24 °C.' },
  ]},
  { section: 'Tecniche e prefermenti', items: [
    { term: 'Biga',                def: 'Pre-impasto asciutto (44–50 % idro) con farina, acqua e poco lievito, che fermenta 16–24 ore. Dà struttura e aromi più complessi.' },
    { term: 'Rinfresco',           def: 'Aggiunta di farina, acqua e altri ingredienti alla biga matura per completare l\u2019impasto.' },
    { term: 'Lievito madre',       def: 'Impasto di farina e acqua fermentato con lieviti e batteri naturali. Sapori complessi ma richiede rinfreschi regolari.' },
    { term: 'Li.Co.Li.',           def: 'Lievito in Coltura Liquida: versione del lievito madre al 100 % di idratazione. Più gestibile della pasta madre solida.' },
    { term: 'Mix farine',          def: 'Miscela di farine con W diverso per ottenere una forza intermedia. Il W risultante è la media ponderata.' },
  ]},
  { section: 'Cottura e stesura', items: [
    { term: 'Stesura',             def: 'Allargamento del panetto a disco. La napoletana si stende a mano, mai col mattarello.' },
    { term: 'Cornicione',          def: 'Bordo rialzato della pizza. Si forma se durante la stesura si lascia l\u2019aria nel bordo.' },
    { term: 'Pala',                def: 'Pizza stesa su teglia o pietra, tipicamente rettangolare, cotta a 280–320 °C per 3–5 minuti.' },
    { term: 'Teglia',              def: 'Pizza cotta direttamente in teglia, idratazione alta (70–80 %). Peso impasto: 0.58 g/cm² (bianca), 0.50 (spessa), 0.375 (fina).' },
    { term: 'Tonda Romana',        def: 'Pizza tonda romana al piatto, croccante e sottile. Panetti da 180 g, W 300–320, semola e tipo 1. Cottura ~290 °C, 2 minuti.' },
  ]},
  { section: 'Strumenti', items: [
    { term: 'Contenitore',         def: 'Volume del contenitore = peso impasto × 2.4. Per 1 kg, almeno 2.4 L.' },
    { term: 'Conversione lievito', def: 'Regola: 1 g fresco = 0.33 g secco attivo = 0.5 g secco Caputo. Il lievito madre richiede ~53× il peso del fresco equivalente.' },
  ]},
];

function Q_Help() {
  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker="Aiuto" title="Glossario" italic="dei pizzaioli"/>

      <div style={{ padding: '0 22px 12px' }}>
        <div style={{ fontFamily: Q.font, fontSize: 12.5, fontStyle: 'italic', color: Q.ink2 }}>
          Tutti i termini dell'app, spiegati come fareste in cucina.
        </div>
      </div>

      <div style={{ padding: '0 22px 8px' }}>
        <div style={{
          display: 'flex', gap: 4, padding: '8px 0 10px',
          borderTop: '1px solid ' + Q.ink, borderBottom: '1px solid ' + Q.ink,
          fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink2,
        }}>
          <div style={{ flex: 1 }}>
            {FULL_GLOSSARY.reduce((s,sec) => s + sec.items.length, 0)} voci · {FULL_GLOSSARY.length} sezioni
          </div>
          <div style={{ color: Q.primary, fontWeight: 700, fontStyle: 'normal' }}>cerca →</div>
        </div>
      </div>

      <div style={{ padding: '0 0 22px' }}>
        {FULL_GLOSSARY.map((sec, si) => (
          <div key={si}>
            <Q_SectionDivider label={sec.section} count={sec.items.length}/>
            <div style={{ padding: '0 22px' }}>
              {sec.items.map((it, ii) => (
                <div key={ii} style={{
                  display: 'grid', gridTemplateColumns: '110px 1fr', columnGap: 12,
                  padding: '8px 0',
                  borderBottom: ii < sec.items.length - 1 ? '1px dotted ' + Q.ruleDots : '1px solid ' + Q.rule,
                }}>
                  <div style={{
                    fontFamily: Q.font, fontSize: 13, fontWeight: 800,
                    color: Q.ink, letterSpacing: '-0.01em', paddingTop: 1,
                  }}>{it.term}</div>
                  <div style={{
                    fontFamily: Q.font, fontSize: 12.5, color: Q.ink2, lineHeight: 1.55,
                  }}>{it.def}</div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

Object.assign(window, {
  Q_ProcessTemplates, Q_ProcessActive, Q_ProcessCompleted,
  Q_ProcessNewDialog, Q_ProcessAddPhaseDialog, Q_ProcessContextMenu,
  Q_Cooking, Q_CookingCustom, Q_CookingSoundPicker, Q_CookingMultiTimer,
  Q_Help, FULL_GLOSSARY, IconEdit, IconTrash, IconVolume,
});
