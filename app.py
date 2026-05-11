"""
app.py — Pizza Calc PRO: 10 calcolatori pizza in un'unica app Streamlit.

Avvio:
    streamlit run app.py

Dipendenze (vedi requirements.txt):
    streamlit >= 1.30
"""
import streamlit as st
from datetime import datetime, time, date
import formulas as f


# ============================================================
# CONFIGURAZIONE PAGINA
# ============================================================
st.set_page_config(
    page_title="🍕 Pizza Calc",
    page_icon="🍕",
    layout="wide",
    initial_sidebar_state="collapsed",
)

# CSS custom: palette caldo/rosso pizza, card pulite, font leggermente più grande
st.markdown("""
<style>
    /* Header con sfondo a striscia */
    .main-title {
        font-size: 2.5rem;
        font-weight: 800;
        background: linear-gradient(90deg, #c1272d 0%, #f15a24 60%, #f7931e 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        margin-bottom: 0;
    }
    .main-sub {
        color: #888;
        font-size: 1rem;
        margin-top: 0;
        margin-bottom: 1.5rem;
    }
    /* Tab più alti e leggibili */
    .stTabs [data-baseweb="tab-list"] button [data-testid="stMarkdownContainer"] p {
        font-size: 1rem;
        font-weight: 600;
    }
    .stTabs [aria-selected="true"] {
        background-color: rgba(241, 90, 36, 0.10) !important;
    }
    /* Card output */
    .output-card {
        background: rgba(241, 90, 36, 0.05);
        border-left: 4px solid #f15a24;
        padding: 1rem 1.2rem;
        border-radius: 6px;
        margin: 0.5rem 0;
    }
    /* Riga ingrediente */
    .ingr {
        display: flex; justify-content: space-between;
        padding: 0.35rem 0; border-bottom: 1px solid rgba(0,0,0,0.06);
    }
    .ingr:last-child { border-bottom: none; }
    .ingr .label { color: #555; }
    .ingr .val { font-weight: 700; color: #c1272d; }
    /* Pillola tempo */
    .pill {
        display: inline-block; padding: 0.2rem 0.6rem;
        background: #fef3e2; color: #c1272d; border-radius: 12px;
        font-size: 0.85rem; margin-right: 0.4rem;
    }
</style>
""", unsafe_allow_html=True)

st.markdown('<p class="main-title">🍕 Pizza Calc</p>', unsafe_allow_html=True)
st.markdown(
    '<p class="main-sub">Tutto quel che serve per la tua pizza, in un\'unica app · '
    'formule verificate empiricamente</p>',
    unsafe_allow_html=True,
)


# ============================================================
# HELPER UI
# ============================================================
def ingr_row(label: str, value, unit: str = "g"):
    st.markdown(
        f'<div class="ingr"><span class="label">{label}</span>'
        f'<span class="val">{value} {unit}</span></div>',
        unsafe_allow_html=True,
    )


def output_card(title: str, content_html: str):
    st.markdown(
        f'<div class="output-card"><b>{title}</b><br>{content_html}</div>',
        unsafe_allow_html=True,
    )


# ============================================================
# TABS
# ============================================================
tabs = st.tabs([
    "🍕 Facile", "⚙️ Avanzato", "🔧 PRO", "🍞 Teglia",
    "🌾 Mix Farine", "💪 W da Proteine", "🫙 Contenitore",
    "🔄 Conversione Lievito", "🧪 Biga", "⚡ Biga Fast",
])


# ------------------------------------------------------------
# 1. FACILE
# ------------------------------------------------------------
with tabs[0]:
    st.subheader("Calcolatore Pizza Facile")
    st.caption("La modalità guidata: scegli ore, temperatura, idratazione — al resto pensa l'app.")
    with st.expander("🔧 Note sulle tabelle di calibrazione"):
        st.markdown(
            "Per il calcolatore Facile sono state corrette alcune incoerenze:\n"
            "- **8H, T ≥ 28°C**: somma puntata+appretto portata a 8 h (apretto 5:00).\n"
            "- **12H, T = 21°C**: lievito ricalibrato a ~1,05 g per "
            "preservare la monotonicità decrescente con T nel regime no-frigo."
        )
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        c1, c2 = st.columns(2)
        n_panetti = c1.number_input("N° panetti", 1, 20, 4, key="f_n")
        peso = c2.number_input("Peso panetto (g)", 100, 400, 250, step=10, key="f_p")

        liev = st.radio("Ore lievitazione totali", [8, 12, 24, 48], horizontal=True, key="f_l")

        c3, c4 = st.columns(2)
        T = c3.slider("Temperatura ambiente (°C)", 18, 30, 20, key="f_T")
        idro = c4.slider("Idratazione (%)", 59, 70, 63, key="f_i")

        c5, c6 = st.columns(2)
        d_inizio = c5.date_input("Data inizio", value=date.today(), key="f_d")
        h_inizio = c6.time_input("Ora inizio", value=time(13, 50), key="f_h")

    with col_out:
        try:
            inizio = datetime.combine(d_inizio, h_inizio)
            r = f.calc_facile(n_panetti, peso, liev, T, idro, inizio)

            st.markdown("**Ingredienti**")
            ingr_row("Farina", r.farina)
            ingr_row("Acqua", r.acqua)
            ingr_row("Sale", r.sale)
            ingr_row("Lievito fresco (LDB)", r.ldbf)
            ingr_row("Lievito secco", r.ldbs)
            ingr_row("**Totale**", f"<b>{r.totale}</b>")

            st.markdown(
                f'<br><span class="pill">Forza farina: {r.forza} W</span>'
                f'<span class="pill">T chiusura: {r.temp_chiusura} °C</span>',
                unsafe_allow_html=True,
            )

            st.markdown("**Timeline**")
            output_card(
                "Tempi di lievitazione",
                f"Puntata: <b>{f.fmt_minutes(r.puntata_min)}</b> · "
                f"Frigo: <b>{f.fmt_minutes(r.frigo_min)}</b> · "
                f"Appretto: <b>{f.fmt_minutes(r.apretto_min)}</b>",
            )
            output_card(
                "Promemoria",
                f"Inizio impasto: <b>{r.inizio:%d/%m/%Y %H:%M}</b><br>"
                f"Inizio appretto: <b>{r.inizio_apretto:%d/%m/%Y %H:%M}</b><br>"
                f"Pronti per la cottura: <b>{r.pronti:%d/%m/%Y %H:%M}</b>",
            )
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 2. AVANZATO
# ------------------------------------------------------------
with tabs[1]:
    st.subheader("Calcolatore Pizza Avanzato")
    st.caption("Controllo manuale di sale, grassi, malto e ripartizione tempi.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        c1, c2 = st.columns(2)
        n_panetti = c1.number_input("N° panetti", 1, 30, 4, key="a_n")
        peso = c2.number_input("Peso panetto (g)", 100, 400, 220, step=10, key="a_p")

        c3, c4 = st.columns(2)
        liev = c3.number_input("Ore tot.", 8, 72, 24, key="a_l")
        T = c4.slider("Temperatura ambiente (°C)", 18, 28, 21, key="a_T")

        idro = st.slider("Idratazione (%)", 50, 85, 63, key="a_i")

        c5, c6, c7 = st.columns(3)
        sale = c5.number_input("Sale (g/L acqua)", 0, 70, 48, key="a_s")
        grassi = c6.number_input("Grassi (g/L acqua)", 0, 70, 0, key="a_g")
        malto = c7.number_input("Malto (% farina)", 0.0, 1.0, 0.0, step=0.1, key="a_m")

        c8, c9, c10 = st.columns(3)
        puntata_h = c8.number_input("Puntata (ore)", 0.0, 24.0, 3.0, step=0.5, key="a_pu")
        frigo_h = c9.number_input("Frigo (ore)", 0, 60, 15, key="a_fr")
        apretto_h = c10.number_input("Appretto (ore)", 0.0, 24.0, 6.0, step=0.5, key="a_ap")

        c11, c12 = st.columns(2)
        d_inizio = c11.date_input("Data inizio", value=date.today(), key="a_d")
        h_inizio = c12.time_input("Ora inizio", value=time(13, 57), key="a_h")

    with col_out:
        try:
            inizio = datetime.combine(d_inizio, h_inizio)
            r = f.calc_avanzato(
                n_panetti=n_panetti, peso_panetto=peso, idro_pct=idro,
                sale_g_per_l=sale, grassi_g_per_l=grassi, malto_pct=malto,
                liev=liev, frigo_h=frigo_h,
                puntata_min=int(puntata_h * 60), apretto_min=int(apretto_h * 60),
                T=T, inizio=inizio,
            )

            st.markdown("**Ingredienti**")
            ingr_row("Farina", r.farina)
            ingr_row("Acqua", r.acqua)
            ingr_row("Sale", r.sale)
            if r.olio > 0: ingr_row("Olio", r.olio)
            if r.malto > 0: ingr_row("Malto", r.malto)
            ingr_row("Lievito fresco", r.ldbf)
            ingr_row("Lievito secco", r.ldbs)
            ingr_row("**Totale**", f"<b>{r.totale}</b>")

            st.markdown(
                f'<br><span class="pill">Forza: {r.forza} W</span>'
                f'<span class="pill">T chiusura: {r.temp_chiusura}°C</span>'
                f'<span class="pill">{r.ciclo}</span>',
                unsafe_allow_html=True,
            )
            output_card(
                "Promemoria",
                f"Inizio impasto: <b>{r.inizio:%d/%m %H:%M}</b><br>"
                f"Inizio appretto: <b>{r.inizio_apretto:%d/%m %H:%M}</b><br>"
                f"Pronti: <b>{r.pronti:%d/%m %H:%M}</b>",
            )
            st.info(
                "ℹ️ La stima del lievito usa la tabella del Facile come approssimazione. "
                "Per cicli lunghi con frigo i valori possono divergere leggermente dal "
                "calcolatore originale."
            )
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 3. PRO
# ------------------------------------------------------------
with tabs[2]:
    st.subheader("Calcolatore Pizza PRO")
    st.caption("Aritmetica pura: tutto in % sulla farina. Per chi sa già cosa vuole.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        c1, c2 = st.columns(2)
        n_panetti = c1.number_input("N° panetti", 1, 30, 4, key="p_n")
        peso = c2.number_input("Peso panetto (g)", 100, 400, 220, step=10, key="p_p")

        idro = st.slider("Idratazione (%)", 40, 100, 63, key="p_i")

        c3, c4 = st.columns(2)
        sale = c3.number_input("Sale (% farina)", 0.0, 5.0, 3.0, step=0.1, key="p_s")
        grassi = c4.number_input("Grassi (% farina)", 0.0, 5.0, 0.0, step=0.1, key="p_g")

        c5, c6 = st.columns(2)
        lievito = c5.number_input("Lievito (% farina)", 0.0, 5.0, 0.10, step=0.05, format="%.2f", key="p_l")
        malto = c6.number_input("Malto (% farina)", 0.0, 2.0, 0.0, step=0.1, key="p_m")

    with col_out:
        try:
            r = f.calc_pro(n_panetti, peso, idro, sale, grassi, lievito, malto)
            st.markdown("**Ingredienti**")
            ingr_row("Farina", r.farina)
            ingr_row("Acqua", r.acqua)
            ingr_row("Sale", r.sale)
            if r.olio > 0: ingr_row("Olio", r.olio)
            if r.malto > 0: ingr_row("Malto", r.malto)
            ingr_row("Lievito fresco", r.ldbf)
            ingr_row("Lievito secco", r.ldbs)
            ingr_row("**Totale**", f"<b>{r.totale}</b>")
            st.success("✅ Modalità aritmetica pura — nessuna stima sulla lievitazione.")
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 4. TEGLIA
# ------------------------------------------------------------
with tabs[3]:
    st.subheader("Calcolatore Pizza in Teglia")
    st.caption("Il peso panetto viene calcolato dall'area teglia (58 g/dm²).")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        forma = st.radio("Forma teglia", ["rotonda", "quadrata", "rettangolare"],
                         horizontal=True, key="t_forma")

        dims = {}
        if forma == "rotonda":
            dims["d"] = st.number_input("Diametro (cm)", 10, 80, 28, key="t_d")
        elif forma == "quadrata":
            dims["rl1"] = st.number_input("Lato (cm)", 10, 80, 30, key="t_l")
        else:
            c1, c2 = st.columns(2)
            dims["rl1"] = c1.number_input("Lato 1 (cm)", 10, 80, 30, key="t_l1")
            dims["rl2"] = c2.number_input("Lato 2 (cm)", 10, 80, 40, key="t_l2")

        c3, c4 = st.columns(2)
        n_teglia = c3.number_input("N° teglie", 1, 10, 1, key="t_n")
        idro = c4.slider("Idratazione (%)", 50, 90, 67, key="t_i")

        c5, c6 = st.columns(2)
        sale = c5.number_input("Sale (g/L acqua)", 0, 70, 30, key="t_s")
        grassi = c6.number_input("Grassi (g/L acqua)", 0, 70, 30, key="t_g")

        c7, c8, c9 = st.columns(3)
        liev = c7.number_input("Ore tot.", 4, 72, 8, key="t_liev")
        T = c8.slider("T (°C)", 18, 30, 21, key="t_T")
        frigo_h = c9.number_input("Frigo (ore)", 0, 60, 0, key="t_fr")

    with col_out:
        try:
            r = f.calc_teglia(
                forma=forma, n_teglia=n_teglia, idro_pct=idro,
                sale_g_per_l=sale, grassi_g_per_l=grassi,
                liev=liev, frigo_h=frigo_h,
                puntata_min=120, apretto_min=360, T=T, **dims,
            )
            st.markdown("**Teglia**")
            ingr_row("Peso panetto", r.peso_panetto)
            ingr_row("Area teglia", r.area_cm2, "cm²")

            st.markdown("**Ingredienti**")
            ingr_row("Farina", r.farina)
            ingr_row("Acqua", r.acqua)
            ingr_row("Sale", r.sale)
            if r.olio > 0: ingr_row("Olio", r.olio)
            ingr_row("Lievito fresco", r.ldbf)
            ingr_row("Lievito secco", r.ldbs)
            ingr_row("**Totale**", f"<b>{r.totale}</b>")
            st.markdown(f'<br><span class="pill">Forza farina: {r.forza} W</span>',
                       unsafe_allow_html=True)
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 5. MIX FARINE
# ------------------------------------------------------------
with tabs[4]:
    st.subheader("Mix Farine")
    st.caption("Regola dell'alligation: ottieni la W desiderata mescolando due farine.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        T_g = st.number_input("Farina totale (g)", 100, 5000, 500, step=50, key="mx_T")
        c1, c2 = st.columns(2)
        W1 = c1.number_input("Farina 1: forza W", 80, 500, 200, step=10, key="mx_W1")
        W2 = c2.number_input("Farina 2: forza W", 80, 500, 350, step=10, key="mx_W2")
        Wmix = st.slider("Forza W desiderata", min(W1, W2), max(W1, W2),
                         (min(W1, W2) + max(W1, W2)) // 2, key="mx_Wmix")

    with col_out:
        try:
            f1, f2 = f.mix_farine(T_g, W1, W2, Wmix)
            st.markdown("**Proporzioni**")
            ingr_row(f"Farina 1 (W={W1})", f1)
            ingr_row(f"Farina 2 (W={W2})", f2)
            ingr_row("**Totale**", f"<b>{round(f1+f2,1)}</b>")
            st.info(f"📊 Farina 1: **{100*f1/T_g:.1f}%** · Farina 2: **{100*f2/T_g:.1f}%**")
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 6. FORZA DA PROTEINE
# ------------------------------------------------------------
with tabs[5]:
    st.subheader("Calcola W dalle Proteine")
    st.caption("Stima la forza W di una farina 0/00 dal contenuto proteico.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        P = st.slider("Proteine (g / 100 g farina)", 7.0, 17.0, 12.0, step=0.1, key="fp_P")
        st.caption("Esempi tipici: 9% = pasta frolla, 11% = pane, 13% = pizza, 15% = panettone")

    with col_out:
        try:
            W = f.forza_da_proteine(P)
            st.metric("Forza W stimata", f"{W} W")
            st.markdown(
                '<div class="output-card"><b>Formula</b><br>'
                f'W = 40 × P − 240 = 40 × {P} − 240 = <b>{W}</b></div>',
                unsafe_allow_html=True,
            )
            st.warning(
                "⚠️ Stima approssimata: la W reale dipende anche dalla qualità "
                "delle proteine (rapporto gliadine/glutenine), non solo dalla quantità."
            )
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 7. CONTENITORE
# ------------------------------------------------------------
with tabs[6]:
    st.subheader("Contenitore Lievitazione")
    st.caption("Quanto deve essere capiente il contenitore per evitare fuoriuscite.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        impasto = st.number_input("Peso impasto (g)", 50, 5000, 1000, step=50, key="cn_I")
        st.caption(f"Moltiplicatore: × {f.CONTENITORE_MOLTIPLICATORE}")

    with col_out:
        V = f.contenitore_volume_ml(impasto)
        st.metric("Volume contenitore consigliato", f"{V:.0f} ml")
        st.markdown(
            f'<div class="output-card"><b>Conversioni</b><br>'
            f'• {V/1000:.2f} litri<br>'
            f'• {V:.0f} ml<br>'
            f'• Il contenitore ha un volume {f.CONTENITORE_MOLTIPLICATORE}× il peso dell\'impasto, '
            f'così l\'impasto può raddoppiare senza fuoriuscire.</div>',
            unsafe_allow_html=True,
        )


# ------------------------------------------------------------
# 8. CONVERSIONE LIEVITO
# ------------------------------------------------------------
with tabs[7]:
    st.subheader("Conversione Lievito")
    st.caption("Converti il lievito di birra fresco in altri tipi, aggiustando farina e acqua.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        L = st.number_input("Lievito di birra fresco (g)", 0.01, 100.0, 1.06, step=0.1, key="cl_L")
        c1, c2 = st.columns(2)
        F_in = c1.number_input("Farina ricetta (g)", 50, 5000, 530, step=10, key="cl_F")
        A_in = c2.number_input("Acqua ricetta (g)", 50, 5000, 334, step=10, key="cl_A")

    with col_out:
        c = f.conversione_lievito(L, F_in, A_in)

        st.markdown("**Lievito di birra secco**")
        ingr_row(f"Secco attivo (LDB÷3)", c.ldbs)
        ingr_row(f"Secco Caputo (LDB÷2)", c.ldbc)

        st.markdown("**Lievito Madre solido (50% idro)**")
        ingr_row("Lievito madre", c.lm)
        ingr_row("→ Farina aggiustata", c.farina_lm)
        ingr_row("→ Acqua aggiustata", c.acqua_lm)

        st.markdown("**LiCoLi (100% idro)**")
        ingr_row("LiCoLi", c.licoli)
        ingr_row("→ Farina aggiustata", c.farina_licoli)
        ingr_row("→ Acqua aggiustata", c.acqua_licoli)

        with st.expander("ℹ️ Nota sul fattore ×53"):
            st.write(
                "L'app usa un fattore di conversione LDB→LM/LiCoLi di **×53**, "
                "più alto dei tipici 25-30× della letteratura panificatoria. È una "
                "scelta 'ricca di LM' che compensa per lieviti madre giovani o poco attivi. "
                "Se hai un LM molto vigoroso puoi ridurre il valore di conseguenza."
            )


# ------------------------------------------------------------
# 9. BIGA
# ------------------------------------------------------------
with tabs[8]:
    st.subheader("Calcolatore Biga (beta)")
    st.caption("Pre-fermento classico: maturazione 12-18 h a temperatura controllata.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        c1, c2 = st.columns(2)
        n_panetti = c1.number_input("N° panetti", 1, 30, 4, key="b_n")
        peso = c2.number_input("Peso panetto (g)", 100, 400, 250, step=10, key="b_p")

        c3, c4 = st.columns(2)
        idro = c3.slider("Idratazione totale (%)", 55, 80, 65, key="b_i")
        TA = c4.slider("T ambiente (°C)", 17, 28, 20, key="b_T")

        c5, c6, c7 = st.columns(3)
        sale = c5.number_input("Sale (g/L)", 0, 70, 50, key="b_s")
        grassi = c6.number_input("Grassi (g/L)", 0, 70, 0, key="b_g")
        biga_pct = c7.slider("Biga (%)", 10, 70, 30, key="b_b")

    with col_out:
        try:
            r = f.calc_biga(n_panetti, peso, idro, sale, grassi, TA, biga_pct)

            st.markdown("**Biga**")
            ingr_row("Farina biga", r.farina_biga)
            ingr_row("Acqua biga", r.acqua_biga)
            ingr_row("Lievito fresco biga", r.lievito_biga)
            st.markdown(
                f'<span class="pill">Idro biga: {r.idro_biga_pct}%</span>'
                f'<span class="pill">Maturazione: {r.ore_biga_str}</span>'
                f'<span class="pill">Farina consigliata: {r.forza_biga} W</span>',
                unsafe_allow_html=True,
            )

            st.markdown("<br>**Rinfresco**", unsafe_allow_html=True)
            ingr_row("Farina rinfresco", r.farina_rinfresco)
            ingr_row("Acqua rinfresco", r.acqua_rinfresco)
            ingr_row("Sale", r.sale_rinfresco)
            if r.grassi_rinfresco > 0:
                ingr_row("Grassi", r.grassi_rinfresco)
            st.markdown(
                f'<span class="pill">Forza rinfresco: {r.forza_rinfresco} W</span>',
                unsafe_allow_html=True,
            )

            with st.expander("📊 Dosi totali (riepilogo)"):
                ingr_row("Farina totale", r.farina_tot)
                ingr_row("Acqua totale", r.acqua_tot)
                ingr_row("Sale totale", r.sale_tot)
        except Exception as e:
            st.error(f"❌ {e}")


# ------------------------------------------------------------
# 10. BIGA FAST
# ------------------------------------------------------------
with tabs[9]:
    st.subheader("Calcolatore Biga Fast (beta)")
    st.caption("Preset 'biga Priore': 100% farina in biga al 50% idro, 3h a 26-28°C.")
    col_in, col_out = st.columns([1, 1], gap="large")

    with col_in:
        c1, c2 = st.columns(2)
        n_panetti = c1.number_input("N° panetti", 1, 30, 4, key="bf_n")
        peso = c2.number_input("Peso panetto (g)", 100, 400, 250, step=10, key="bf_p")

        idro = st.slider("Idratazione totale (%)", 70, 80, 80, key="bf_i")

        c3, c4 = st.columns(2)
        sale = c3.number_input("Sale (g/L)", 0, 70, 40, key="bf_s")
        grassi = c4.number_input("Grassi (g/L)", 0, 70, 0, key="bf_g")

        st.info("⚙️ Parametri fissi: biga 100%, idro biga 50%, 3h a 26-28°C, W=300")

    with col_out:
        try:
            r = f.calc_biga_fast(n_panetti, peso, idro, sale, grassi)

            st.markdown("**Biga (100% della farina)**")
            ingr_row("Farina biga", r.farina_tot)
            ingr_row("Acqua biga", r.acqua_biga)
            ingr_row("Lievito fresco", r.lievito_biga)
            st.markdown(
                f'<span class="pill">Idro biga: 50%</span>'
                f'<span class="pill">Maturazione: 3:00 a 26-28°C</span>'
                f'<span class="pill">Farina consigliata: 300 W</span>',
                unsafe_allow_html=True,
            )

            st.markdown("<br>**Rinfresco**", unsafe_allow_html=True)
            st.caption("L'acqua del rinfresco va aggiunta tipicamente 50% subito + 50% a filo.")
            ingr_row("Acqua rinfresco", r.acqua_rinfresco)
            ingr_row("Sale", r.sale_rinfresco)
            if r.grassi_rinfresco > 0:
                ingr_row("Grassi", r.grassi_rinfresco)

            with st.expander("📊 Dosi totali (riepilogo)"):
                ingr_row("Farina totale", r.farina_tot)
                ingr_row("Acqua totale", r.acqua_tot)
                ingr_row("Sale totale", r.sale_tot)
        except Exception as e:
            st.error(f"❌ {e}")


# ============================================================
# FOOTER
# ============================================================
st.markdown("---")
st.caption("🍕 Pizza Calc — per uso personale.")
