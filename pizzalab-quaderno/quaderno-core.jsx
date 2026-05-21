// quaderno-core.jsx
// Design palette + shared layout primitives for the Quaderno direction.
// Everything that's reused across screens lives here.

// ════════════════ PALETTE ════════════════
const Q = {
  bg:        '#F5EEDC',  // aged paper — app background
  bgWarmer:  '#EFE7D2',  // sunken / pressed states
  paper:     '#FDF9EE',  // surface highlight (cards, dialogs)
  ink:       '#2B1F12',  // primary text
  ink2:      '#65553E',  // secondary text
  ink3:      '#9D8B6E',  // tertiary / meta
  rule:      '#D9CEB3',  // solid hairline
  ruleDots:  '#C5B591',  // dotted hairline
  primary:   '#A8392B',  // deep tomato — single accent
  primaryHi: '#C04A2A',  // hover/active variant
  olive:     '#5E6B3D',  // olive green — secondary accent (W, durations)
  oliveDk:   '#3F4A28',
  font:      '"Inter", system-ui, sans-serif',
};

// ════════════════ HEADER ════════════════
// Used at the top of every screen. Kicker (uppercase small) + bold title +
// optional italic subtitle on the same line + right slot.
function Q_Header({ kicker, title, italic, right, tight }) {
  return (
    <div style={{ padding: tight ? '14px 22px 4px' : '20px 22px 8px' }}>
      {kicker && (
        <div style={{
          fontFamily: Q.font, fontSize: 10.5, fontWeight: 600,
          letterSpacing: '0.24em', color: Q.primary, textTransform: 'uppercase',
        }}>{kicker}</div>
      )}
      <div style={{
        display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
        marginTop: 4, gap: 12,
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, flexWrap: 'wrap' }}>
          <div style={{
            fontFamily: Q.font, fontSize: 28, fontWeight: 800,
            color: Q.ink, letterSpacing: '-0.025em', lineHeight: 1,
          }}>{title}</div>
          {italic && (
            <div style={{
              fontFamily: Q.font, fontStyle: 'italic', fontWeight: 400,
              fontSize: 18, color: Q.olive,
            }}>{italic}</div>
          )}
        </div>
        {right}
      </div>
    </div>
  );
}

// ════════════════ BOTTOM NAV ════════════════
function Q_BottomNav({ active, onChange }) {
  const items = [
    { key: 'calc', label: 'Calcolo',   icon: IconCalculator },
    { key: 'proc', label: 'Processo',  icon: IconTimer },
    { key: 'cook', label: 'Cottura',   icon: IconFlame },
    { key: 'help', label: 'Glossario', icon: IconBook },
  ];
  return (
    <div style={{
      background: Q.paper,
      borderTop: '1px dashed ' + Q.ruleDots,
      display: 'flex', padding: '6px 4px 4px',
      flexShrink: 0,
    }}>
      {items.map(it => {
        const on = it.key === active;
        const I = it.icon;
        return (
          <div key={it.key} onClick={() => onChange && onChange(it.key)} style={{
            flex: 1, padding: '6px 4px 6px',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
            color: on ? Q.primary : Q.ink3,
            position: 'relative', cursor: onChange ? 'pointer' : 'default',
          }}>
            <I size={20} stroke={1.6}/>
            <div style={{
              fontFamily: Q.font, fontSize: 10, fontWeight: on ? 700 : 500,
              fontStyle: on ? 'normal' : 'italic',
            }}>{it.label}</div>
            {on && <div style={{
              position: 'absolute', bottom: -2, width: 24, height: 2,
              background: Q.primary, borderRadius: 1,
            }}/>}
          </div>
        );
      })}
    </div>
  );
}

// ════════════════ FIELD (stepper + presets) ════════════════
// Big tabular number with stepper buttons on the sides, optional preset chips
// underneath that snap to common values. Replaces all the M3 sliders.
function Q_Field({ label, hint, value, suffix, onMinus, onPlus, presets, onPresetSelect, selected, dense }) {
  return (
    <div style={{ padding: dense ? '10px 22px 12px' : '12px 22px 14px', borderBottom: '1px dashed ' + Q.ruleDots }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 8 }}>
        <div style={{
          fontFamily: Q.font, fontSize: 13.5, color: Q.ink, fontWeight: 600,
          flex: 1, minWidth: 0,
        }}>
          {label}{' '}
          {hint && <span style={{ fontStyle: 'italic', color: Q.ink3, fontWeight: 400 }}>{hint}</span>}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <button onClick={onMinus} style={Q_RoundBtn}><IconMinus size={14} stroke={2.2}/></button>
          <div style={{
            fontFamily: Q.font, fontSize: 22, fontWeight: 700, color: Q.ink,
            fontVariantNumeric: 'tabular-nums', minWidth: 56, textAlign: 'center',
            letterSpacing: '-0.01em',
            borderBottom: '2px solid ' + Q.ink, paddingBottom: 1,
          }}>
            {value}<span style={{ fontSize: 12, fontWeight: 500, color: Q.ink2, marginLeft: 2 }}>{suffix}</span>
          </div>
          <button onClick={onPlus} style={Q_RoundBtn}><IconPlus size={14} stroke={2.2}/></button>
        </div>
      </div>
      {presets && presets.length > 0 && (
        <div style={{ marginTop: 8, display: 'flex', gap: 4, flexWrap: 'wrap' }}>
          {presets.map(p => {
            const on = String(p) === String(selected);
            return (
              <div key={p} onClick={() => onPresetSelect && onPresetSelect(p)} style={{
                fontFamily: Q.font, fontSize: 11, fontWeight: on ? 700 : 500,
                color: on ? Q.primary : Q.ink2,
                padding: '2px 8px', borderRadius: 3,
                background: on ? Q.bgWarmer : 'transparent',
                fontVariantNumeric: 'tabular-nums',
                cursor: 'pointer',
              }}>{p}</div>
            );
          })}
        </div>
      )}
    </div>
  );
}
const Q_RoundBtn = {
  width: 26, height: 26, borderRadius: 999,
  background: 'transparent', border: '1px solid ' + Q.ink2, color: Q.ink2,
  display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
  padding: 0, flexShrink: 0,
};

// ════════════════ NUMERIC TEXT FIELD ════════════════
// For pure text input (Mix Farine W values, Contenitore peso, etc.)
function Q_TextField({ label, value, placeholder, suffix }) {
  return (
    <div style={{ padding: '10px 22px 12px', borderBottom: '1px dashed ' + Q.ruleDots }}>
      <div style={{
        fontFamily: Q.font, fontSize: 11, fontStyle: 'italic',
        color: Q.ink3, marginBottom: 4, letterSpacing: '0.02em',
      }}>{label}</div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
        <div style={{
          flex: 1, padding: '0 0 4px',
          borderBottom: '2px solid ' + Q.ink,
          fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.ink,
          fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em',
        }}>{value || <span style={{ color: Q.ink3, fontWeight: 400, fontStyle: 'italic' }}>{placeholder}</span>}</div>
        {suffix && <div style={{ fontFamily: Q.font, fontSize: 12, color: Q.ink2 }}>{suffix}</div>}
      </div>
    </div>
  );
}

// ════════════════ SEGMENTED PICKER ════════════════
// For "Per Panetti / Da Farina", "Forza / Proteine", "Fisso peso / Fisso numero"
function Q_Segmented({ items, value, onChange }) {
  return (
    <div style={{
      display: 'flex', border: '1px solid ' + Q.ink2, borderRadius: 4,
      background: Q.paper, overflow: 'hidden',
    }}>
      {items.map((it, i) => {
        const on = it === value;
        return (
          <div key={it} onClick={() => onChange && onChange(it)} style={{
            flex: 1, padding: '8px 10px', textAlign: 'center',
            fontFamily: Q.font, fontSize: 12.5,
            fontWeight: on ? 700 : 500, fontStyle: on ? 'normal' : 'italic',
            background: on ? Q.ink : 'transparent',
            color: on ? Q.paper : Q.ink2,
            cursor: 'pointer',
            borderRight: i < items.length - 1 ? '1px solid ' + Q.ink2 : 'none',
          }}>{it}</div>
        );
      })}
    </div>
  );
}

// ════════════════ CHIP ROW ════════════════
function Q_ChipRow({ items, value, onChange, wrap }) {
  return (
    <div style={{
      display: 'flex', gap: 6, flexWrap: wrap ? 'wrap' : 'nowrap',
      overflowX: wrap ? 'visible' : 'auto',
    }}>
      {items.map(it => {
        const on = it === value || (it.value !== undefined && it.value === value);
        const label = it.label || it;
        return (
          <div key={label} onClick={() => onChange && onChange(it.value !== undefined ? it.value : it)} style={{
            padding: '4px 10px', borderRadius: 3,
            border: '1px solid ' + (on ? Q.ink : Q.rule),
            background: on ? Q.ink : Q.paper,
            fontFamily: Q.font, fontSize: 11.5, fontWeight: on ? 700 : 500,
            color: on ? Q.paper : Q.ink2, cursor: 'pointer',
            fontStyle: on ? 'normal' : 'italic',
            whiteSpace: 'nowrap', flexShrink: 0,
          }}>{label}</div>
        );
      })}
    </div>
  );
}

// ════════════════ LEADER ROW (label ⋯ value) ════════════════
function Q_LeaderRow({ label, value, unit, strong }) {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline', padding: '4px 0', gap: 6 }}>
      <div style={{ fontFamily: Q.font, fontSize: 14, color: Q.ink, fontWeight: strong ? 700 : 400 }}>{label}</div>
      <div style={{
        flex: 1, borderBottom: '1px dotted ' + Q.ruleDots, height: 1,
        transform: 'translateY(-3px)',
      }}/>
      <div style={{
        fontFamily: Q.font, fontSize: strong ? 18 : 16, fontWeight: 700, color: Q.ink,
        fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.01em',
      }}>{value} {unit && <span style={{ fontSize: 11, fontWeight: 500, color: Q.ink2 }}>{unit}</span>}</div>
    </div>
  );
}

// ════════════════ RECIPE CARD (corner marks) ════════════════
function Q_Card({ kicker, title, titleSuffix, meta, children, accent }) {
  const c = accent || Q.primary;
  return (
    <div style={{
      margin: '14px 22px 0', padding: '16px 18px',
      background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 2,
      position: 'relative',
    }}>
      <div style={{
        position: 'absolute', top: -1, left: -1, width: 18, height: 18,
        borderTop: '2px solid ' + c, borderLeft: '2px solid ' + c, pointerEvents: 'none',
      }}/>
      <div style={{
        position: 'absolute', bottom: -1, right: -1, width: 18, height: 18,
        borderBottom: '2px solid ' + c, borderRight: '2px solid ' + c, pointerEvents: 'none',
      }}/>

      {kicker && (
        <div style={{
          fontFamily: Q.font, fontSize: 10, fontWeight: 700,
          letterSpacing: '0.24em', textTransform: 'uppercase', color: c,
        }}>{kicker}</div>
      )}
      {title && (
        <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginTop: 2 }}>
          <div style={{
            fontFamily: Q.font, fontSize: 20, fontWeight: 800, color: Q.ink,
            letterSpacing: '-0.02em',
          }}>{title} {titleSuffix && <span style={{ fontSize: 13, color: Q.ink2, fontWeight: 500 }}>{titleSuffix}</span>}</div>
          {meta && <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3 }}>{meta}</div>}
        </div>
      )}
      <div style={{ marginTop: title ? 10 : 0 }}>{children}</div>
    </div>
  );
}

// ════════════════ BUTTONS ════════════════
function Q_PrimaryBtn({ children, onClick, icon: Icon }) {
  return (
    <button onClick={onClick} style={{
      width: '100%', padding: '12px 16px',
      background: Q.primary, color: Q.paper, border: 'none', borderRadius: 4,
      fontFamily: Q.font, fontSize: 13, fontWeight: 700,
      letterSpacing: '0.04em', cursor: 'pointer',
      display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
    }}>
      {children}
      {Icon && <Icon size={14} stroke={2.2}/>}
    </button>
  );
}
function Q_SecondaryBtn({ children, onClick, italic }) {
  return (
    <button onClick={onClick} style={{
      padding: '10px 16px',
      background: 'transparent', color: Q.ink2, border: '1px solid ' + Q.ink2, borderRadius: 4,
      fontFamily: Q.font, fontSize: 13, fontWeight: 600, cursor: 'pointer',
      fontStyle: italic ? 'italic' : 'normal',
    }}>{children}</button>
  );
}
function Q_DarkBtn({ children, onClick }) {
  return (
    <button onClick={onClick} style={{
      width: '100%', padding: '12px 16px',
      background: Q.ink, color: Q.paper, border: 'none', borderRadius: 4,
      fontFamily: Q.font, fontSize: 13, fontWeight: 700, cursor: 'pointer',
    }}>{children}</button>
  );
}

// ════════════════ DIALOG OVERLAY ════════════════
// Renders centered modal with backdrop, used for "Nuovo Processo", "Aggiungi Fase",
// "Suono allarme". The backdrop is the warm paper tint, not black.
function Q_Dialog({ title, kicker, children, onConfirm, onDismiss, confirmLabel = 'Conferma', dismissLabel = 'Annulla', confirmDisabled }) {
  return (
    <div style={{
      position: 'absolute', inset: 0,
      background: 'rgba(43, 31, 18, 0.55)', backdropFilter: 'blur(2px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      padding: 16, zIndex: 10,
    }}>
      <div style={{
        width: '100%', maxWidth: 340,
        background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 4,
        boxShadow: '0 12px 32px rgba(43,31,18,0.20)',
        position: 'relative', overflow: 'hidden',
      }}>
        {/* corner mark */}
        <div style={{
          position: 'absolute', top: -1, left: -1, width: 18, height: 18,
          borderTop: '2px solid ' + Q.primary, borderLeft: '2px solid ' + Q.primary,
        }}/>
        <div style={{
          position: 'absolute', bottom: -1, right: -1, width: 18, height: 18,
          borderBottom: '2px solid ' + Q.primary, borderRight: '2px solid ' + Q.primary,
        }}/>

        <div style={{ padding: '18px 20px 4px' }}>
          {kicker && (
            <div style={{
              fontFamily: Q.font, fontSize: 10, fontWeight: 700,
              letterSpacing: '0.22em', textTransform: 'uppercase', color: Q.primary,
            }}>{kicker}</div>
          )}
          <div style={{
            fontFamily: Q.font, fontSize: 20, fontWeight: 800, color: Q.ink,
            letterSpacing: '-0.02em', marginTop: 2,
          }}>{title}</div>
        </div>

        <div style={{ padding: '8px 20px 16px' }}>{children}</div>

        <div style={{
          display: 'flex', gap: 10, padding: '12px 20px 18px',
          borderTop: '1px dashed ' + Q.ruleDots,
          justifyContent: 'flex-end', alignItems: 'center',
        }}>
          {onDismiss && (
            <div onClick={onDismiss} style={{
              fontFamily: Q.font, fontSize: 12.5, fontWeight: 600,
              color: Q.ink2, cursor: 'pointer', letterSpacing: '0.04em',
              padding: '6px 12px', fontStyle: 'italic',
            }}>{dismissLabel}</div>
          )}
          {onConfirm && (
            <div onClick={confirmDisabled ? undefined : onConfirm} style={{
              fontFamily: Q.font, fontSize: 12.5, fontWeight: 700,
              color: confirmDisabled ? Q.ink3 : Q.primary, cursor: confirmDisabled ? 'default' : 'pointer',
              letterSpacing: '0.04em', textTransform: 'uppercase',
              padding: '6px 12px',
            }}>{confirmLabel}</div>
          )}
        </div>
      </div>
    </div>
  );
}

// ════════════════ SECTION DIVIDER ════════════════
// Used in Help/Glossary screen: kicker + dashed rule + count badge.
function Q_SectionDivider({ label, count, accent }) {
  const c = accent || Q.primary;
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 10,
      margin: '18px 22px 10px',
    }}>
      <div style={{
        fontFamily: Q.font, fontSize: 11, fontWeight: 700,
        letterSpacing: '0.22em', color: c, textTransform: 'uppercase',
      }}>§ {label}</div>
      <div style={{ flex: 1, height: 1, background: Q.rule }}/>
      {count != null && (
        <div style={{
          fontFamily: Q.font, fontSize: 11, color: Q.ink3,
          fontVariantNumeric: 'tabular-nums', fontStyle: 'italic',
        }}>{String(count).padStart(2, '0')}</div>
      )}
    </div>
  );
}

// ════════════════ SHELL — wraps screen content + bottom nav ════════════════
function Q_Shell({ active, onTabChange, children, overlay }) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: Q.bg, position: 'relative' }}>
      <div style={{ flex: 1, overflow: 'auto' }}>{children}</div>
      <Q_BottomNav active={active} onChange={onTabChange}/>
      {overlay}
    </div>
  );
}

Object.assign(window, {
  Q, Q_Header, Q_BottomNav, Q_Field, Q_TextField, Q_Segmented, Q_ChipRow,
  Q_LeaderRow, Q_Card, Q_PrimaryBtn, Q_SecondaryBtn, Q_DarkBtn, Q_Dialog,
  Q_SectionDivider, Q_Shell, Q_RoundBtn,
});
