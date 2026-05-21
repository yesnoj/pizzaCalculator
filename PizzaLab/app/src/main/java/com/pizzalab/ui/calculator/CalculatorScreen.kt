package com.pizzalab.ui.calculator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pizzalab.ui.mixfarine.MixFarineScreen

/**
 * Category colors for grouping related calculator tabs.
 * - IMPASTO: dough calculators (primary)
 * - FARINE: flour-related tools
 * - UTILITY: conversion and container tools
 */
enum class TabCategory { IMPASTO, FARINE, UTILITY }

enum class CalculatorTab(val label: String, val category: TabCategory) {
    FACILE("Facile", TabCategory.IMPASTO),
    AVANZATO("Avanzato", TabCategory.IMPASTO),
    PRO("PRO", TabCategory.IMPASTO),
    TEGLIA("Teglia", TabCategory.IMPASTO),
    BIGA("Biga", TabCategory.IMPASTO),
    MIX_FARINE("Mix Farine", TabCategory.FARINE),
    W_PROTEINE("W da Proteine", TabCategory.FARINE),
    TEMPERATURE("Temperature", TabCategory.UTILITY),
    CONTENITORE("Contenitore", TabCategory.UTILITY),
    CONVERSIONE("Conversione", TabCategory.UTILITY)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(
    onStartProcess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tabs = CalculatorTab.entries
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size }
    )

    // Sync pager -> tab indicator
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            selectedTabIndex = page
        }
    }

    // Sync tab click -> pager
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    // Colori per categoria
    val impastoColor = Color(0xFFD84315)    // arancione/pizza
    val farineColor = Color(0xFF8B6914)     // marrone/grano
    val utilityColor = Color(0xFF2E7D32)    // verde

    Column(modifier = modifier.fillMaxSize()) {
        // Tab personalizzate con sfondo colorato e bordi arrotondati
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(tabs) { index, tab ->
                val baseColor = when (tab.category) {
                    TabCategory.IMPASTO -> impastoColor
                    TabCategory.FARINE -> farineColor
                    TabCategory.UTILITY -> utilityColor
                }
                val isSelected = selectedTabIndex == index
                val bgColor = if (isSelected) baseColor else baseColor.copy(alpha = 0.55f)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(bgColor)
                        .clickable { selectedTabIndex = index }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val padModifier = Modifier.padding(16.dp)
            when (tabs[pageIndex]) {
                CalculatorTab.FACILE -> FacileTabContent(
                    onStartProcess = onStartProcess,
                    modifier = padModifier
                )
                CalculatorTab.AVANZATO -> AvanzatoCalculator(
                    onStartProcess = onStartProcess,
                    modifier = padModifier
                )
                CalculatorTab.PRO -> ProCalculator(modifier = padModifier)
                CalculatorTab.TEGLIA -> TegliaCalculator(modifier = padModifier)
                CalculatorTab.BIGA -> BigaCalculator(modifier = padModifier)
                CalculatorTab.MIX_FARINE -> MixFarineScreen(modifier = padModifier)
                CalculatorTab.W_PROTEINE -> ForzaProteineCalculator(modifier = padModifier)
                CalculatorTab.TEMPERATURE -> TemperatureFornoCalculator(modifier = padModifier)
                CalculatorTab.CONTENITORE -> ContenitoreCalculator(modifier = padModifier)
                CalculatorTab.CONVERSIONE -> ConversioneLievitoCalculator(modifier = padModifier)
            }
        }
    }
}

/**
 * Contenitore per la tab "Facile" con due sotto-modalità:
 * "Per Panetti" (calcolatore classico) e "Da Farina" (partendo dalla farina).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FacileTabContent(
    onStartProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // false = Per Panetti, true = Da Farina
    var daFarinaMode by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Titolo
        Text(
            text = "Calcolatore Facile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Chip di selezione modalità
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !daFarinaMode,
                onClick = { daFarinaMode = false },
                label = { Text("Per Panetti") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = daFarinaMode,
                onClick = { daFarinaMode = true },
                label = { Text("Da Farina") },
                modifier = Modifier.weight(1f)
            )
        }

        // Contenuto — weight(1f) per dare tutto lo spazio rimanente al calcolatore
        if (daFarinaMode) {
            DaFarinaCalculator(
                onStartProcess = onStartProcess,
                modifier = Modifier.weight(1f)
            )
        } else {
            FacileCalculator(
                onStartProcess = onStartProcess,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
