// quaderno-screens-calc.jsx
// Splash screen + all 11 calculator screens.

// ════════════════ SPLASH ════════════════
function Q_Splash() {
  return (
    <div style={{
      height: '100%', background: Q.bg,
      display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'space-between',
      padding: '80px 0 60px', position: 'relative', overflow: 'hidden',
    }}>
      {/* Decorative dotted frame */}
      <div style={{
        position: 'absolute', inset: '40px 28px',
        border: '1px dashed ' + Q.ruleDots, borderRadius: 4,
        pointerEvents: 'none',
      }}/>
      {/* Corner marks */}
      {[
        { top: 32, left: 20, brT: true, brL: true },
        { top: 32, right: 20, brT: true, brR: true },
        { bottom: 52, left: 20, brB: true, brL: true },
        { bottom: 52, right: 20, brB: true, brR: true },
      ].map((p, i) => (
        <div key={i} style={{
          position: 'absolute', width: 22, height: 22,
          top: p.top, bottom: p.bottom, left: p.left, right: p.right,
          borderTop: p.brT ? '2px solid ' + Q.primary : 'none',
          borderBottom: p.brB ? '2px solid ' + Q.primary : 'none',
          borderLeft: p.brL ? '2px solid ' + Q.primary : 'none',
          borderRight: p.brR ? '2px solid ' + Q.primary : 'none',
        }}/>
      ))}

      <div style={{ textAlign: 'center', marginTop: 40, position: 'relative', zIndex: 1 }}>
        <div style={{
          fontFamily: Q.font, fontSize: 10.5, fontWeight: 700,
          letterSpacing: '0.32em', color: Q.primary, textTransform: 'uppercase',
        }}>Quaderno del pizzaiolo</div>
        <div style={{
          fontFamily: Q.font, fontSize: 14, fontStyle: 'italic',
          color: Q.ink3, marginTop: 6,
        }}>edizione 2026 · n° 1</div>
      </div>

      {/* Logo block — bowl/pizza mark drawn in SVG */}
      <div style={{
        position: 'relative', zIndex: 1,
        display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24,
      }}>
        <svg width="220" height="220" viewBox="0 0 220 220">
          {/* outer dotted ring */}
          <circle cx="110" cy="110" r="100" fill="none" stroke={Q.ruleDots} strokeWidth="1" strokeDasharray="2 4"/>
          {/* crust ring */}
          <circle cx="110" cy="110" r="84" fill="#E8D9B8" stroke={Q.ink} strokeWidth="2"/>
          {/* sauce */}
          <circle cx="110" cy="110" r="68" fill={Q.primary}/>
          {/* mozzarella dots */}
          {[[80,80,12],[140,90,11],[100,135,13],[150,140,10],[75,128,9],[125,72,10]].map(([cx,cy,r],i) => (
            <circle key={i} cx={cx} cy={cy} r={r} fill={Q.paper} stroke="#E8D9B8" strokeWidth="1"/>
          ))}
          {/* basil leaves */}
          <ellipse cx="92" cy="110" rx="6" ry="10" fill={Q.olive} transform="rotate(-20 92 110)"/>
          <ellipse cx="128" cy="118" rx="6" ry="10" fill={Q.olive} transform="rotate(30 128 118)"/>
          <ellipse cx="115" cy="92" rx="5" ry="8" fill={Q.oliveDk} transform="rotate(50 115 92)"/>
        </svg>

        <div style={{ textAlign: 'center' }}>
          <div style={{
            fontFamily: Q.font, fontSize: 56, fontWeight: 900,
            color: Q.ink, letterSpacing: '-0.04em', lineHeight: 1,
          }}>PizzaLab</div>
          <div style={{
            fontFamily: Q.font, fontSize: 15, fontStyle: 'italic',
            color: Q.ink2, marginTop: 8, letterSpacing: '0.02em',
          }}>l'arte dell'impasto, a portata di mano</div>
        </div>
      </div>

      {/* Footer */}
      <div style={{ position: 'relative', zIndex: 1, textAlign: 'center' }}>
        <div style={{
          fontFamily: Q.font, fontSize: 10, fontStyle: 'italic',
          color: Q.ink3, letterSpacing: '0.18em', textTransform: 'uppercase',
        }}>· farina · acqua · sale · tempo ·</div>
        <div style={{ marginTop: 14 }}>
          {/* loader: 3 tally dots */}
          <div style={{ display: 'inline-flex', gap: 6 }}>
            {[0,1,2].map(i => (
              <div key={i} style={{
                width: 6, height: 6, borderRadius: 999,
                background: Q.primary, opacity: i === 0 ? 1 : 0.35,
              }}/>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ════════════════ CALC TAB STRIP ════════════════
// Renders the row of categories above the tab bar + the tab pills themselves.
function Q_CalcTabStrip({ tab, onChange }) {
  const cats = [
    { name: 'Impasto', color: Q.primary, tabs: ['Facile', 'Avanzato', 'PRO', 'Teglia', 'Biga'] },
    { name: 'Farine',  color: Q.olive,   tabs: ['Mix Farine', 'W·Proteine'] },
    { name: 'Utility', color: '#7A6043', tabs: ['Temp.', 'Contenitore', 'Conversione'] },
  ];
  const allTabs = cats.flatMap(c => c.tabs.map(t => ({ name: t, color: c.color })));

  return (
    <div style={{ padding: '4px 14px 0' }}>
      <div style={{ display: 'flex', gap: 16, padding: '2px 8px 6px', overflow: 'hidden' }}>
        {cats.map(c => (
          <div key={c.name} style={{
            fontFamily: Q.font, fontSize: 9.5, fontWeight: 700,
            letterSpacing: '0.22em', textTransform: 'uppercase',
            color: c.color,
            display: 'flex', alignItems: 'center', gap: 6,
          }}>
            <div style={{ width: 5, height: 5, borderRadius: 1, background: c.color }}/>
            {c.name}
          </div>
        ))}
      </div>
      <div style={{
        overflowX: 'auto', padding: '0 8px',
        borderTop: '1px solid ' + Q.ink, borderBottom: '3px double ' + Q.ink,
      }}>
        <div style={{ display: 'flex', gap: 4, padding: '8px 0' }}>
          {allTabs.map(({ name, color }) => {
            const on = name === tab;
            return (
              <div key={name} onClick={() => onChange && onChange(name)} style={{
                padding: '4px 10px', borderRadius: 4,
                fontFamily: Q.font, fontSize: 12.5,
                fontWeight: on ? 700 : 500,
                fontStyle: on ? 'normal' : 'italic',
                color: on ? Q.paper : color,
                background: on ? color : 'transparent',
                whiteSpace: 'nowrap', cursor: 'pointer', flexShrink: 0,
              }}>{name}</div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ════════════════ Calcolatore — wrapper ════════════════
function Q_Calculator({ defaultTab = 'Facile' }) {
  const [tab, setTab] = React.useState(defaultTab);
  const Screens = {
    'Facile': Q_CalcFacile,
    'Avanzato': Q_CalcAvanzato,
    'PRO': Q_CalcPRO,
    'Teglia': Q_CalcTeglia,
    'Biga': Q_CalcBiga,
    'Mix Farine': Q_CalcMixFarine,
    'W·Proteine': Q_CalcWProteine,
    'Temp.': Q_CalcTempForno,
    'Contenitore': Q_CalcContenitore,
    'Conversione': Q_CalcConversione,
  };
  const Body = Screens[tab] || Q_CalcFacile;

  // Header text varies per tab
  const headers = {
    'Facile':       { title: 'Facile',       italic: 'per panetti' },
    'Avanzato':     { title: 'Avanzato',     italic: 'pieno controllo' },
    'PRO':          { title: 'PRO',          italic: 'percentuali farina' },
    'Teglia':       { title: 'Teglia',       italic: 'al taglio' },
    'Biga':         { title: 'Biga',         italic: 'prefermento' },
    'Mix Farine':   { title: 'Mix Farine',   italic: 'forza media' },
    'W·Proteine':   { title: 'W',            italic: 'dalle proteine' },
    'Temp.':        { title: 'Temperature', italic: 'di cottura' },
    'Contenitore':  { title: 'Contenitore', italic: 'volume utile' },
    'Conversione':  { title: 'Conversione', italic: 'dei lieviti' },
  };
  const h = headers[tab] || headers['Facile'];

  return (
    <div style={{ background: Q.bg, minHeight: '100%' }}>
      <Q_Header kicker={`Calcolo · ${tab}`} title={h.title} italic={h.italic} right={
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontFamily: Q.font, fontStyle: 'italic', fontSize: 11, color: Q.ink3 }}>oggi · 21 mag</div>
          <div style={{ fontFamily: Q.font, fontSize: 12, color: Q.ink2, fontVariantNumeric: 'tabular-nums', fontWeight: 600 }}>22 °C</div>
        </div>
      }/>
      <Q_CalcTabStrip tab={tab} onChange={setTab}/>
      <Body />
    </div>
  );
}

// ════════════════ FACILE / Per Panetti ════════════════
function Q_CalcFacile() {
  const [mode, setMode] = React.useState('Per Panetti');
  const [nP, setNP]   = React.useState(4);
  const [pP, setPP]   = React.useState(250);
  const [ore, setOre] = React.useState(24);
  const [t, setT]     = React.useState(22);
  const [idro, setIdro] = React.useState(63);

  if (mode === 'Da Farina') return <Q_CalcDaFarinaInner onModeChange={setMode} />;

  const totale = nP * pP;
  const farina = Math.round(totale / (1 + idro/100 + 0.018 + 0.0017));
  const acqua  = Math.round(farina * (idro/100));
  const sale   = Math.round(farina * 0.018);
  const ldbf   = (farina * 0.0017 * (24/ore) * (22/t)).toFixed(2);

  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '12px 22px 4px' }}>
        <Q_Segmented items={['Per Panetti', 'Da Farina']} value={mode} onChange={setMode}/>
      </div>
      <Q_Field label="Panetti" hint="quante palline" value={nP} suffix="" onMinus={() => setNP(Math.max(1, nP-1))} onPlus={() => setNP(Math.min(12, nP+1))} presets={[2,4,6,8]} selected={nP} onPresetSelect={setNP}/>
      <Q_Field label="Peso panetto" value={pP} suffix=" g" onMinus={() => setPP(Math.max(180, pP-10))} onPlus={() => setPP(Math.min(350, pP+10))} presets={[180,220,250,280]} selected={pP} onPresetSelect={setPP}/>
      <Q_Field label="Lievitazione" value={ore} suffix=" h" onMinus={() => setOre(Math.max(1, ore-1))} onPlus={() => setOre(Math.min(48, ore+1))} presets={[6,8,12,24,48]} selected={ore} onPresetSelect={setOre}/>
      <Q_Field label="Temperatura" hint="ambiente" value={t} suffix=" °C" onMinus={() => setT(Math.max(18, t-1))} onPlus={() => setT(Math.min(30, t+1))}/>
      <Q_Field label="Idratazione" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(55, idro-1))} onPlus={() => setIdro(Math.min(80, idro+1))} presets={[60,63,65,70]} selected={idro} onPresetSelect={setIdro}/>

      <Q_Card kicker="Ricetta" title={`per ${nP} × ${pP} g`} meta={`tot. ${totale} g`}>
        <Q_LeaderRow label="Farina" value={farina} unit="g"/>
        <Q_LeaderRow label="Acqua" value={acqua} unit="g"/>
        <Q_LeaderRow label="Sale" value={sale} unit="g"/>
        <Q_LeaderRow label="Lievito fresco" value={ldbf} unit="g"/>

        <div style={{
          marginTop: 12, paddingTop: 10, borderTop: '1px dashed ' + Q.ruleDots,
          display: 'flex', justifyContent: 'space-between',
        }}>
          <div>
            <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>forza consigliata</div>
            <div style={{ fontFamily: Q.font, fontSize: 16, fontWeight: 700, color: Q.olive }}>W {Math.round(180 + ore*4)}</div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>pronti alle</div>
            <div style={{ fontFamily: Q.font, fontSize: 22, fontWeight: 800, color: Q.primary, fontVariantNumeric: 'tabular-nums', letterSpacing: '-0.02em' }}>18:00</div>
          </div>
        </div>
      </Q_Card>

      <div style={{ padding: '16px 22px 12px' }}>
        <Q_PrimaryBtn icon={IconArrowRight}>Avvia processo</Q_PrimaryBtn>
      </div>
    </div>
  );
}

// ════════════════ FACILE / Da Farina ════════════════
function Q_CalcDaFarinaInner({ onModeChange }) {
  const [farinaG, setFarinaG] = React.useState(500);
  const [subMode, setSubMode] = React.useState('Fisso peso');
  const [pP, setPP] = React.useState(250);
  const [nP, setNP] = React.useState(4);
  const [ore, setOre] = React.useState(24);
  const [t, setT] = React.useState(22);
  const [idro, setIdro] = React.useState(63);

  const totale = Math.round(farinaG * (1 + idro/100 + 0.018 + 0.0017));
  const acqua  = Math.round(farinaG * (idro/100));
  const sale   = Math.round(farinaG * 0.018);
  const ldbf   = (farinaG * 0.0017 * (24/ore) * (22/t)).toFixed(2);
  const computedN = subMode === 'Fisso peso' ? Math.max(1, Math.round(totale / pP)) : nP;
  const computedP = subMode === 'Fisso peso' ? pP : Math.round(totale / Math.max(1, nP));

  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '12px 22px 4px' }}>
        <Q_Segmented items={['Per Panetti', 'Da Farina']} value="Da Farina" onChange={onModeChange}/>
      </div>
      <Q_Field label="Farina" value={farinaG} suffix=" g" onMinus={() => setFarinaG(Math.max(200, farinaG-50))} onPlus={() => setFarinaG(Math.min(2000, farinaG+50))} presets={[300,500,1000,1500]} selected={farinaG} onPresetSelect={setFarinaG}/>

      <div style={{ padding: '12px 22px 4px' }}>
        <Q_Segmented items={['Fisso peso', 'Fisso numero']} value={subMode} onChange={setSubMode}/>
      </div>

      {subMode === 'Fisso peso' ? (
        <Q_Field label="Peso panetto" value={pP} suffix=" g" onMinus={() => setPP(Math.max(180, pP-10))} onPlus={() => setPP(Math.min(350, pP+10))} presets={[180,220,250,280]} selected={pP} onPresetSelect={setPP}/>
      ) : (
        <Q_Field label="Numero panetti" value={nP} suffix="" onMinus={() => setNP(Math.max(1, nP-1))} onPlus={() => setNP(Math.min(12, nP+1))} presets={[2,4,6,8]} selected={nP} onPresetSelect={setNP}/>
      )}
      <Q_Field label="Lievitazione" value={ore} suffix=" h" onMinus={() => setOre(Math.max(1, ore-1))} onPlus={() => setOre(Math.min(48, ore+1))} presets={[6,8,12,24,48]} selected={ore} onPresetSelect={setOre}/>
      <Q_Field label="Temperatura" hint="ambiente" value={t} suffix=" °C" onMinus={() => setT(Math.max(18, t-1))} onPlus={() => setT(Math.min(30, t+1))}/>
      <Q_Field label="Idratazione" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(55, idro-1))} onPlus={() => setIdro(Math.min(80, idro+1))}/>

      <Q_Card kicker="Ricetta" title={`${computedN} × ${computedP} g`} meta={`da ${farinaG} g farina`}>
        <Q_LeaderRow label="Farina"          value={farinaG} unit="g"/>
        <Q_LeaderRow label="Acqua"           value={acqua} unit="g"/>
        <Q_LeaderRow label="Sale"            value={sale} unit="g"/>
        <Q_LeaderRow label="Lievito fresco"  value={ldbf} unit="g"/>
        <div style={{
          marginTop: 8, paddingTop: 8, borderTop: '1px dashed ' + Q.ruleDots,
        }}>
          <Q_LeaderRow label="Totale impasto" value={totale} unit="g" strong/>
        </div>
      </Q_Card>

      <div style={{ padding: '16px 22px 12px' }}>
        <Q_PrimaryBtn icon={IconArrowRight}>Avvia processo</Q_PrimaryBtn>
      </div>
    </div>
  );
}

// ════════════════ AVANZATO ════════════════
// Same shape as Facile but with extra inputs: sale g/L, grassi g/L, malto %,
// plus separated puntata/frigo/appretto controls.
function Q_CalcAvanzato() {
  const [nP, setNP] = React.useState(4);
  const [pP, setPP] = React.useState(250);
  const [idro, setIdro] = React.useState(63);
  const [saleGL, setSaleGL] = React.useState(40);
  const [grassiGL, setGrassiGL] = React.useState(0);
  const [malto, setMalto] = React.useState(0.0);
  const [ore, setOre] = React.useState(24);
  const [t, setT] = React.useState(22);
  const [puntata, setPuntata] = React.useState(120);
  const [frigo, setFrigo] = React.useState(16);
  const [appretto, setAppretto] = React.useState(330);

  const totale = nP * pP;
  const farina = Math.round(totale / (1 + idro/100 + 0.022 + 0.0017));
  const acqua  = Math.round(farina * (idro/100));
  const sale   = Math.round(acqua * saleGL/1000);
  const olio   = Math.round(acqua * grassiGL/1000);
  const ldbf   = (farina * 0.0017 * (24/ore)).toFixed(2);

  return (
    <div style={{ paddingBottom: 12 }}>
      <Q_Field label="Panetti" value={nP} suffix="" onMinus={() => setNP(Math.max(1, nP-1))} onPlus={() => setNP(Math.min(12, nP+1))} presets={[2,4,6,8]} selected={nP} onPresetSelect={setNP} dense/>
      <Q_Field label="Peso panetto" value={pP} suffix=" g" onMinus={() => setPP(Math.max(180, pP-10))} onPlus={() => setPP(Math.min(350, pP+10))} dense/>
      <Q_Field label="Idratazione" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(59, idro-1))} onPlus={() => setIdro(Math.min(75, idro+1))} dense/>
      <Q_Field label="Sale" hint="sull'acqua" value={saleGL} suffix=" g/L" onMinus={() => setSaleGL(Math.max(20, saleGL-2))} onPlus={() => setSaleGL(Math.min(60, saleGL+2))} dense/>
      <Q_Field label="Grassi" hint="olio" value={grassiGL} suffix=" g/L" onMinus={() => setGrassiGL(Math.max(0, grassiGL-5))} onPlus={() => setGrassiGL(Math.min(50, grassiGL+5))} dense/>
      <Q_Field label="Malto" value={malto.toFixed(1)} suffix=" %" onMinus={() => setMalto(Math.max(0, +(malto-0.1).toFixed(1)))} onPlus={() => setMalto(Math.min(3, +(malto+0.1).toFixed(1)))} dense/>
      <Q_Field label="Lievitazione totale" value={ore} suffix=" h" onMinus={() => setOre(Math.max(8, ore-2))} onPlus={() => setOre(Math.min(48, ore+2))} presets={[8,12,24,48]} selected={ore} onPresetSelect={setOre} dense/>
      <Q_Field label="Temperatura" value={t} suffix=" °C" onMinus={() => setT(Math.max(18, t-1))} onPlus={() => setT(Math.min(30, t+1))} dense/>
      <Q_Field label="Puntata" value={puntata} suffix=" min" onMinus={() => setPuntata(Math.max(30, puntata-15))} onPlus={() => setPuntata(Math.min(300, puntata+15))} dense/>
      <Q_Field label="Frigo" value={frigo} suffix=" h" onMinus={() => setFrigo(Math.max(0, frigo-1))} onPlus={() => setFrigo(Math.min(48, frigo+1))} dense/>
      <Q_Field label="Appretto" value={appretto} suffix=" min" onMinus={() => setAppretto(Math.max(60, appretto-30))} onPlus={() => setAppretto(Math.min(600, appretto+30))} dense/>

      <Q_Card kicker="Ricetta avanzata" title={`${nP} × ${pP} g`} meta={`tot. ${totale} g`}>
        <Q_LeaderRow label="Farina"         value={farina} unit="g"/>
        <Q_LeaderRow label="Acqua"          value={acqua}  unit="g"/>
        <Q_LeaderRow label="Sale"           value={sale}   unit="g"/>
        <Q_LeaderRow label="Olio"           value={olio}   unit="g"/>
        <Q_LeaderRow label="Malto"          value={(farina*malto/100).toFixed(1)} unit="g"/>
        <Q_LeaderRow label="Lievito fresco" value={ldbf}   unit="g"/>
        <div style={{
          marginTop: 10, paddingTop: 10, borderTop: '1px dashed ' + Q.ruleDots,
          display: 'flex', justifyContent: 'space-between', gap: 16,
        }}>
          <div>
            <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>W consigliato</div>
            <div style={{ fontFamily: Q.font, fontSize: 16, fontWeight: 700, color: Q.olive }}>W {Math.round(180 + ore*4)}</div>
          </div>
          <div>
            <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>ciclo</div>
            <div style={{ fontFamily: Q.font, fontSize: 13, fontWeight: 600, color: Q.ink2 }}>{frigo > 0 ? 'TA + Frigo + Appretto' : 'TA + Appretto'}</div>
          </div>
        </div>
      </Q_Card>

      <div style={{ padding: '16px 22px 12px' }}>
        <Q_PrimaryBtn icon={IconArrowRight}>Avvia processo</Q_PrimaryBtn>
      </div>
    </div>
  );
}

// ════════════════ PRO (% farina) ════════════════
function Q_CalcPRO() {
  const [nP, setNP] = React.useState(4);
  const [pP, setPP] = React.useState(250);
  const [idro, setIdro] = React.useState(63);
  const [salePct, setSalePct] = React.useState(2.5);
  const [grassiPct, setGrassiPct] = React.useState(0);
  const [lievPct, setLievPct] = React.useState(0.2);
  const [maltoPct, setMaltoPct] = React.useState(0);

  const totale = nP * pP;
  const farina = Math.round(totale / (1 + idro/100 + salePct/100 + grassiPct/100 + lievPct/100 + maltoPct/100));
  const acqua = Math.round(farina * idro/100);

  return (
    <div style={{ paddingBottom: 12 }}>
      <Q_Field label="Panetti" value={nP} suffix="" onMinus={() => setNP(Math.max(1, nP-1))} onPlus={() => setNP(Math.min(20, nP+1))} dense/>
      <Q_Field label="Peso panetto" value={pP} suffix=" g" onMinus={() => setPP(Math.max(150, pP-10))} onPlus={() => setPP(Math.min(500, pP+10))} dense/>
      <Q_Field label="Idratazione" hint="% farina" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(50, idro-1))} onPlus={() => setIdro(Math.min(85, idro+1))} dense/>
      <Q_Field label="Sale" hint="% farina" value={salePct.toFixed(1)} suffix=" %" onMinus={() => setSalePct(Math.max(0, +(salePct-0.1).toFixed(1)))} onPlus={() => setSalePct(Math.min(5, +(salePct+0.1).toFixed(1)))} dense/>
      <Q_Field label="Grassi" hint="% farina" value={grassiPct.toFixed(1)} suffix=" %" onMinus={() => setGrassiPct(Math.max(0, +(grassiPct-0.1).toFixed(1)))} onPlus={() => setGrassiPct(Math.min(5, +(grassiPct+0.1).toFixed(1)))} dense/>
      <Q_Field label="Lievito" hint="% farina" value={lievPct.toFixed(2)} suffix=" %" onMinus={() => setLievPct(Math.max(0, +(lievPct-0.05).toFixed(2)))} onPlus={() => setLievPct(Math.min(2, +(lievPct+0.05).toFixed(2)))} dense/>
      <Q_Field label="Malto" hint="% farina" value={maltoPct.toFixed(1)} suffix=" %" onMinus={() => setMaltoPct(Math.max(0, +(maltoPct-0.1).toFixed(1)))} onPlus={() => setMaltoPct(Math.min(3, +(maltoPct+0.1).toFixed(1)))} dense/>

      <Q_Card kicker="Ricetta PRO" title={`${nP} × ${pP} g`} meta={`tot. ${totale} g`}>
        <Q_LeaderRow label="Farina"        value={farina} unit="g"/>
        <Q_LeaderRow label="Acqua"         value={acqua}  unit="g"/>
        <Q_LeaderRow label="Sale"          value={Math.round(farina*salePct/100)} unit="g"/>
        <Q_LeaderRow label="Olio"          value={Math.round(farina*grassiPct/100)} unit="g"/>
        <Q_LeaderRow label="Lievito"       value={(farina*lievPct/100).toFixed(2)} unit="g"/>
        <Q_LeaderRow label="Malto"         value={(farina*maltoPct/100).toFixed(1)} unit="g"/>
      </Q_Card>

      <div style={{ padding: '14px 22px 8px', fontFamily: Q.font, fontSize: 11.5, fontStyle: 'italic', color: Q.ink3, textAlign: 'center' }}>
        nessuna timeline · gestisci tu tempi e fasi
      </div>
    </div>
  );
}

// ════════════════ TEGLIA ════════════════
function Q_CalcTeglia() {
  const [forma, setForma] = React.useState('Rotonda');
  const [diam, setDiam] = React.useState(32);
  const [lato, setLato] = React.useState(40);
  const [nTeg, setNTeg] = React.useState(1);
  const [idro, setIdro] = React.useState(68);
  const [saleGL, setSaleGL] = React.useState(40);
  const [grassiGL, setGrassiGL] = React.useState(30);

  const area = forma === 'Rotonda' ? Math.round(Math.PI * (diam/2)**2)
             : forma === 'Quadrata' ? lato * lato
             : 40 * 30;
  const pesoPanetto = Math.round(area * 0.58);
  const totale = pesoPanetto * nTeg;
  const farina = Math.round(totale / (1 + idro/100 + 0.022 + grassiGL/1000 + 0.0017));
  const acqua = Math.round(farina * idro/100);

  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '12px 22px 8px' }}>
        <Q_Segmented items={['Rotonda', 'Quadrata', 'Rettangolare']} value={forma} onChange={setForma}/>
      </div>

      {forma === 'Rotonda' && (
        <Q_Field label="Diametro" value={diam} suffix=" cm" onMinus={() => setDiam(Math.max(20, diam-2))} onPlus={() => setDiam(Math.min(50, diam+2))} presets={[26,28,30,32,36]} selected={diam} onPresetSelect={setDiam}/>
      )}
      {forma === 'Quadrata' && (
        <Q_Field label="Lato" value={lato} suffix=" cm" onMinus={() => setLato(Math.max(20, lato-2))} onPlus={() => setLato(Math.min(60, lato+2))}/>
      )}
      {forma === 'Rettangolare' && (
        <>
          <Q_Field label="Lato 1" value={40} suffix=" cm" onMinus={()=>{}} onPlus={()=>{}}/>
          <Q_Field label="Lato 2" value={30} suffix=" cm" onMinus={()=>{}} onPlus={()=>{}}/>
        </>
      )}

      <Q_Field label="Teglie" value={nTeg} suffix="" onMinus={() => setNTeg(Math.max(1, nTeg-1))} onPlus={() => setNTeg(Math.min(6, nTeg+1))} dense/>
      <Q_Field label="Idratazione" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(60, idro-1))} onPlus={() => setIdro(Math.min(80, idro+1))} dense/>
      <Q_Field label="Sale" value={saleGL} suffix=" g/L" onMinus={() => setSaleGL(Math.max(20, saleGL-2))} onPlus={() => setSaleGL(Math.min(60, saleGL+2))} dense/>
      <Q_Field label="Grassi" hint="olio" value={grassiGL} suffix=" g/L" onMinus={() => setGrassiGL(Math.max(0, grassiGL-5))} onPlus={() => setGrassiGL(Math.min(80, grassiGL+5))} dense/>

      <Q_Card kicker={`Teglia · ${forma.toLowerCase()}`} title={`${pesoPanetto} g`} titleSuffix={`× ${nTeg}`} meta={`area ${area} cm²`}>
        <Q_LeaderRow label="Farina" value={farina} unit="g"/>
        <Q_LeaderRow label="Acqua"  value={acqua}  unit="g"/>
        <Q_LeaderRow label="Sale"   value={Math.round(acqua*saleGL/1000)} unit="g"/>
        <Q_LeaderRow label="Olio"   value={Math.round(acqua*grassiGL/1000)} unit="g"/>
        <Q_LeaderRow label="Lievito fresco" value={(farina*0.0017).toFixed(2)} unit="g"/>
        <div style={{
          marginTop: 10, paddingTop: 10, borderTop: '1px dashed ' + Q.ruleDots,
          display: 'flex', justifyContent: 'space-between',
        }}>
          <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink3 }}>forza consigliata</div>
          <div style={{ fontFamily: Q.font, fontSize: 18, fontWeight: 800, color: Q.olive, letterSpacing: '-0.02em' }}>W 300</div>
        </div>
      </Q_Card>
    </div>
  );
}

// ════════════════ BIGA ════════════════
function Q_CalcBiga() {
  const [nP, setNP] = React.useState(4);
  const [pP, setPP] = React.useState(250);
  const [idro, setIdro] = React.useState(65);
  const [saleGL, setSaleGL] = React.useState(40);
  const [bigaPct, setBigaPct] = React.useState(50);

  const totale = nP * pP;
  const farinaTot = Math.round(totale / (1 + idro/100 + 0.022 + 0.0017));
  const acquaTot = Math.round(farinaTot * idro/100);
  const farinaBiga = Math.round(farinaTot * bigaPct/100);
  const acquaBiga = Math.round(farinaBiga * 0.45);

  return (
    <div style={{ paddingBottom: 12 }}>
      <Q_Field label="Panetti" value={nP} suffix="" onMinus={() => setNP(Math.max(1, nP-1))} onPlus={() => setNP(Math.min(12, nP+1))} presets={[2,4,6,8]} selected={nP} onPresetSelect={setNP}/>
      <Q_Field label="Peso panetto" value={pP} suffix=" g" onMinus={() => setPP(Math.max(180, pP-10))} onPlus={() => setPP(Math.min(350, pP+10))}/>
      <Q_Field label="Idratazione totale" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(55, idro-1))} onPlus={() => setIdro(Math.min(75, idro+1))}/>
      <Q_Field label="Sale" value={saleGL} suffix=" g/L" onMinus={() => setSaleGL(Math.max(20, saleGL-2))} onPlus={() => setSaleGL(Math.min(60, saleGL+2))}/>
      <Q_Field label="Biga" hint="% farina" value={bigaPct} suffix=" %" onMinus={() => setBigaPct(Math.max(10, bigaPct-5))} onPlus={() => setBigaPct(Math.min(70, bigaPct+5))} presets={[30,50,70]} selected={bigaPct} onPresetSelect={setBigaPct}/>

      {/* Biga */}
      <Q_Card kicker="Biga" title="16–24 h" meta="idro 45 %" accent={Q.olive}>
        <Q_LeaderRow label="Farina biga"      value={farinaBiga} unit="g"/>
        <Q_LeaderRow label="Acqua biga"       value={acquaBiga}  unit="g"/>
        <Q_LeaderRow label="Lievito biga"     value={(farinaBiga*0.01).toFixed(1)} unit="g"/>
        <div style={{ marginTop: 8, paddingTop: 8, borderTop: '1px dashed ' + Q.ruleDots, fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink2 }}>
          maturazione a 18 °C · forza biga: W 320+
        </div>
      </Q_Card>

      {/* Rinfresco */}
      <Q_Card kicker="Rinfresco" title="al taglio">
        <Q_LeaderRow label="Farina rinfresco" value={farinaTot - farinaBiga} unit="g"/>
        <Q_LeaderRow label="Acqua rinfresco"  value={acquaTot - acquaBiga}   unit="g"/>
        <Q_LeaderRow label="Sale rinfresco"   value={Math.round(acquaTot*saleGL/1000)} unit="g"/>
        <Q_LeaderRow label="Forza rinfresco"  value="W 280" unit=""/>
      </Q_Card>
    </div>
  );
}

// ════════════════ MIX FARINE ════════════════
function Q_CalcMixFarine() {
  const farine = [
    { nome: 'Caputo Pizzeria', w: 265, peso: 600 },
    { nome: 'Caputo Manitoba', w: 380, peso: 400 },
  ];
  const tot = farine.reduce((s,f) => s + f.peso, 0);
  const wMedio = Math.round(farine.reduce((s,f) => s + f.w * f.peso, 0) / tot);
  const idroSic = Math.min(85, Math.round(wMedio * 0.18 + 30));
  const idroAvz = Math.min(95, Math.round(wMedio * 0.22 + 32));

  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '12px 22px 4px', fontFamily: Q.font, fontSize: 12.5, fontStyle: 'italic', color: Q.ink2 }}>
        Calcola la forza media del mix e l'idratazione massima consigliata.
      </div>

      {farine.map((f, i) => (
        <div key={i} style={{
          margin: '10px 22px', padding: '12px 14px',
          background: Q.paper, border: '1px solid ' + Q.rule, borderRadius: 2,
        }}>
          <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
            <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase' }}>Farina {i+1}</div>
            <div style={{ display: 'flex', gap: 6 }}>
              <div style={{ fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.olive, padding: '1px 6px', border: '1px solid ' + Q.olive, borderRadius: 2 }}>preset</div>
              {i > 1 && <div style={{ color: Q.primary }}><IconClose size={14}/></div>}
            </div>
          </div>
          <div style={{ fontFamily: Q.font, fontSize: 16, fontWeight: 700, color: Q.ink, marginTop: 4 }}>{f.nome}</div>
          <div style={{ display: 'flex', gap: 14, marginTop: 8 }}>
            <div style={{ flex: 1 }}>
              <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>peso</div>
              <div style={{ fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.ink, fontVariantNumeric: 'tabular-nums', borderBottom: '2px solid ' + Q.ink, paddingBottom: 1 }}>{f.peso} <span style={{ fontSize: 11, fontWeight: 500, color: Q.ink2 }}>g</span></div>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontFamily: Q.font, fontSize: 10, fontStyle: 'italic', color: Q.ink3 }}>forza</div>
              <div style={{ fontFamily: Q.font, fontSize: 18, fontWeight: 700, color: Q.olive, fontVariantNumeric: 'tabular-nums', borderBottom: '2px solid ' + Q.olive, paddingBottom: 1 }}>W {f.w}</div>
            </div>
          </div>
        </div>
      ))}

      <div style={{ padding: '4px 22px 0' }}>
        <div style={{
          padding: '10px', textAlign: 'center', cursor: 'pointer',
          fontFamily: Q.font, fontSize: 12.5, fontWeight: 600, color: Q.ink2,
          border: '1px dashed ' + Q.ink2, borderRadius: 4,
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 6,
          fontStyle: 'italic',
        }}>
          <IconPlus size={14}/> aggiungi una farina
        </div>
      </div>

      <Q_Card kicker="Risultato mix" title={`W ${wMedio}`} titleSuffix={`· ${tot} g totali`}>
        {farine.map((f,i) => (
          <Q_LeaderRow key={i} label={`${f.nome} (W${f.w})`} value={`${Math.round(f.peso/tot*100)}`} unit="%"/>
        ))}
        <div style={{ marginTop: 10, paddingTop: 10, borderTop: '1px dashed ' + Q.ruleDots }}>
          <div style={{ fontFamily: Q.font, fontSize: 10, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase', marginBottom: 6 }}>Idratazione max</div>
          <Q_BarRow label="sicura (a mano)"     value={idroSic} color={Q.primary}/>
          <Q_BarRow label="avanzata (planetaria)" value={idroAvz} color={Q.olive}/>
        </div>
        <div style={{ marginTop: 8, fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink2 }}>
          farina forte: ideale per lievitazioni lunghe (24h+), buona struttura.
        </div>
      </Q_Card>
    </div>
  );
}
function Q_BarRow({ label, value, color }) {
  return (
    <div style={{ marginTop: 6 }}>
      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
        <div style={{ fontFamily: Q.font, fontSize: 12, color: Q.ink }}>{label}</div>
        <div style={{ fontFamily: Q.font, fontSize: 14, fontWeight: 700, color, fontVariantNumeric: 'tabular-nums' }}>{value} %</div>
      </div>
      <div style={{ height: 4, background: Q.bgWarmer, borderRadius: 2, marginTop: 2, overflow: 'hidden' }}>
        <div style={{ width: value + '%', height: '100%', background: color, borderRadius: 2 }}/>
      </div>
    </div>
  );
}

// ════════════════ W DA PROTEINE ════════════════
function Q_CalcWProteine() {
  const [prot, setProt] = React.useState(12.5);
  const w = Math.round(prot * 27.5 - 60);
  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '16px 22px 4px', fontFamily: Q.font, fontSize: 12.5, fontStyle: 'italic', color: Q.ink2 }}>
        Sul pacco della farina raramente trovi il W, ma quasi sempre le proteine. Stima il W dalle proteine in etichetta.
      </div>

      <Q_Field label="Proteine" hint="in etichetta" value={prot.toFixed(1)} suffix=" %" onMinus={() => setProt(Math.max(7, +(prot-0.1).toFixed(1)))} onPlus={() => setProt(Math.min(17, +(prot+0.1).toFixed(1)))}/>

      <Q_Card kicker="W stimato" title={`W ${w}`} accent={Q.olive}>
        <div style={{ fontFamily: Q.font, fontSize: 12.5, color: Q.ink2, lineHeight: 1.6 }}>
          {w < 200 && 'Farina debole. Adatta a lievitazioni brevi (1–4 h). Impasto croccante, struttura modesta.'}
          {w >= 200 && w < 280 && 'Farina media. Buona per pizze classiche con lievitazione di 8–12 h.'}
          {w >= 280 && w < 340 && 'Farina forte. Ideale per lievitazioni lunghe (24 h+), buona struttura.'}
          {w >= 340 && 'Farina molto forte. Perfetta per lievitazioni molto lunghe (48 h+) o impasti ad alta idratazione.'}
        </div>
      </Q_Card>

      <div style={{ padding: '14px 22px', fontFamily: Q.font, fontSize: 11, color: Q.ink3, fontStyle: 'italic', textAlign: 'center' }}>
        formula: W ≈ proteine × 27.5 − 60
      </div>
    </div>
  );
}

// ════════════════ TEMPERATURE FORNO ════════════════
function Q_CalcTempForno() {
  const [pizza, setPizza] = React.useState('Napoletana');
  const [forno, setForno] = React.useState('Legna');
  const [idro, setIdro] = React.useState(68);

  // matrix lookup (simplified)
  const ranges = {
    'Napoletana':    { c: [430,480], p: [380,430], t: '60–90 sec' },
    'Contemporanea': { c: [400,450], p: [350,400], t: '90 sec – 2:30' },
    'Teglia':        { c: [250,300], p: [280,320], t: '10–20 min' },
    'Pala':          { c: [300,350], p: [320,370], t: '5–8 min' },
  };
  const fornoCap = { 'Legna': 999, 'Gas': 450, 'Elettrico': 400, 'Domestico': 280 };
  let r = ranges[pizza];
  const cap = fornoCap[forno];
  const c = [Math.min(r.c[0], cap), Math.min(r.c[1], cap)];
  const p = [Math.min(r.p[0], cap-20), Math.min(r.p[1], cap-10)];

  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '12px 22px 8px' }}>
        <div style={{ fontFamily: Q.font, fontSize: 11, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase', marginBottom: 6 }}>Tipo di pizza</div>
        <Q_ChipRow items={['Napoletana','Contemporanea','Teglia','Pala']} value={pizza} onChange={setPizza} wrap/>
      </div>
      <div style={{ padding: '4px 22px 8px' }}>
        <div style={{ fontFamily: Q.font, fontSize: 11, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase', marginBottom: 6 }}>Tipo di forno</div>
        <Q_ChipRow items={['Legna','Gas','Elettrico','Domestico']} value={forno} onChange={setForno} wrap/>
      </div>

      <Q_Field label="Idratazione" value={idro} suffix=" %" onMinus={() => setIdro(Math.max(55, idro-1))} onPlus={() => setIdro(Math.min(85, idro+1))} dense/>

      <Q_Card kicker="Temperature consigliate" title={`${c[0]}–${c[1]} °C`} titleSuffix="cielo" accent={Q.primary}>
        <Q_LeaderRow label="Platea (base)" value={`${p[0]}–${p[1]}`} unit="°C"/>
        <Q_LeaderRow label="Tempo cottura" value={r.t} unit=""/>
      </Q_Card>

      <div style={{
        margin: '14px 22px 8px', padding: '12px 14px',
        background: Q.bgWarmer, borderLeft: '3px solid ' + Q.olive, borderRadius: 2,
      }}>
        <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.18em', color: Q.olive, textTransform: 'uppercase', marginBottom: 6 }}>Consigli</div>
        {[
          'Base brucia / sopra crudo: riduci platea, aumenta cielo',
          'Sopra brucia / base pallida: riduci cielo, aumenta platea',
          forno === 'Domestico' && 'Usa una pietra refrattaria preriscaldata per la platea',
          idro >= 75 && 'Alta idratazione: serve cielo più forte per asciugare la superficie',
        ].filter(Boolean).map((c,i) => (
          <div key={i} style={{ fontFamily: Q.font, fontSize: 12, color: Q.ink2, lineHeight: 1.55, paddingLeft: 12, position: 'relative' }}>
            <span style={{ position: 'absolute', left: 0, color: Q.olive }}>·</span>{c}
          </div>
        ))}
      </div>
    </div>
  );
}

// ════════════════ CONTENITORE ════════════════
function Q_CalcContenitore() {
  const [peso, setPeso] = React.useState(1000);
  const vol = Math.round(peso * 2.4);
  return (
    <div style={{ paddingBottom: 12 }}>
      <div style={{ padding: '16px 22px 4px', fontFamily: Q.font, fontSize: 12.5, fontStyle: 'italic', color: Q.ink2 }}>
        Il volume utile per la lievitazione: peso × 2.4. Esempio: 1 kg di impasto chiede almeno 2.4 L.
      </div>

      <Q_Field label="Peso impasto" value={peso} suffix=" g" onMinus={() => setPeso(Math.max(100, peso-100))} onPlus={() => setPeso(Math.min(5000, peso+100))} presets={[500,1000,1500,2000]} selected={peso} onPresetSelect={setPeso}/>

      <Q_Card kicker="Contenitore" title={`${vol} ml`} titleSuffix={`· ${(vol/1000).toFixed(1)} L`} accent={Q.olive}>
        <div style={{
          marginTop: 4, display: 'flex', alignItems: 'flex-end', justifyContent: 'center', gap: 8,
          padding: '8px 0 4px',
        }}>
          {/* visual: container + dough fill */}
          <svg width="100" height="100" viewBox="0 0 100 100">
            {/* container */}
            <path d="M 22 18 L 22 86 Q 22 92 28 92 L 72 92 Q 78 92 78 86 L 78 18" fill="none" stroke={Q.ink} strokeWidth="2" strokeLinecap="round"/>
            <ellipse cx="50" cy="18" rx="28" ry="4" fill="none" stroke={Q.ink} strokeWidth="2"/>
            {/* dough fill — 1/2.4 = ~42% */}
            <rect x="24" y="55" width="52" height="35" fill={Q.primary} opacity="0.85"/>
            {/* dough top wavy */}
            <ellipse cx="50" cy="55" rx="26" ry="3" fill={Q.primaryHi}/>
          </svg>
          <div style={{ flex: 1 }}>
            <div style={{ fontFamily: Q.font, fontSize: 10.5, fontWeight: 700, letterSpacing: '0.22em', color: Q.primary, textTransform: 'uppercase' }}>volume utile</div>
            <Q_LeaderRow label="Impasto" value={peso} unit="g"/>
            <Q_LeaderRow label="Spazio raddoppio" value={Math.round(peso * 1.4)} unit="ml"/>
            <Q_LeaderRow label="Volume totale" value={vol} unit="ml" strong/>
          </div>
        </div>
      </Q_Card>
    </div>
  );
}

// ════════════════ CONVERSIONE LIEVITO ════════════════
function Q_CalcConversione() {
  const [ldbf, setLdbf] = React.useState(3);
  const farinaR = 600, acquaR = 380;

  // Conversioni standard
  const ldbs = (ldbf * 0.33).toFixed(2);
  const ldbc = (ldbf * 0.5).toFixed(2);
  const lm = Math.round(ldbf * 53);
  const farinaLm = Math.round(farinaR - lm/2);
  const acquaLm  = Math.round(acquaR - lm/2);
  const licoli = Math.round(ldbf * 30);
  const farinaLi = Math.round(farinaR - licoli/2);
  const acquaLi  = Math.round(acquaR - licoli/2);

  return (
    <div style={{ paddingBottom: 12 }}>
      <Q_Field label="Lievito fresco" hint="LDB" value={ldbf} suffix=" g" onMinus={() => setLdbf(Math.max(0.5, +(ldbf-0.5).toFixed(1)))} onPlus={() => setLdbf(+(ldbf+0.5).toFixed(1))} presets={[1,2,3,5]} selected={ldbf} onPresetSelect={setLdbf}/>
      <Q_Field label="Farina ricetta" value={farinaR} suffix=" g" onMinus={()=>{}} onPlus={()=>{}} dense/>
      <Q_Field label="Acqua ricetta" value={acquaR} suffix=" g" onMinus={()=>{}} onPlus={()=>{}} dense/>

      <Q_Card kicker="Lieviti secchi" title="equivalenti">
        <Q_LeaderRow label="LDB secco attivo" value={ldbs} unit="g"/>
        <Q_LeaderRow label="LDB secco Caputo" value={ldbc} unit="g"/>
        <div style={{ marginTop: 6, fontFamily: Q.font, fontSize: 11, fontStyle: 'italic', color: Q.ink2 }}>
          regola: 1 g fresco = 0.33 g secco attivo = 0.5 g secco Caputo
        </div>
      </Q_Card>

      <Q_Card kicker="Lievito madre" title="solido" accent={Q.olive}>
        <Q_LeaderRow label="Lievito madre" value={lm} unit="g"/>
        <Q_LeaderRow label="Farina corretta" value={farinaLm} unit="g"/>
        <Q_LeaderRow label="Acqua corretta" value={acquaLm} unit="g"/>
      </Q_Card>

      <Q_Card kicker="Li.Co.Li." title="idro 100 %" accent={Q.olive}>
        <Q_LeaderRow label="Li.Co.Li." value={licoli} unit="g"/>
        <Q_LeaderRow label="Farina corretta" value={farinaLi} unit="g"/>
        <Q_LeaderRow label="Acqua corretta" value={acquaLi} unit="g"/>
      </Q_Card>
    </div>
  );
}

Object.assign(window, {
  Q_Splash, Q_Calculator, Q_CalcTabStrip,
  Q_CalcFacile, Q_CalcDaFarinaInner, Q_CalcAvanzato, Q_CalcPRO, Q_CalcTeglia,
  Q_CalcBiga, Q_CalcMixFarine, Q_CalcWProteine, Q_CalcTempForno,
  Q_CalcContenitore, Q_CalcConversione,
});
