package com.pizzalab.ui.calculator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.ui.components.QHeader
import com.pizzalab.ui.mixfarine.MixFarineScreen
import com.pizzalab.ui.theme.QuadernoColors

enum class TabCategory { IMPASTO, FARINE, UTILITY }

enum class CalculatorTab(val label: String, val category: TabCategory) {
    FACILE("Facile", TabCategory.IMPASTO),
    AVANZATO("Avanzato", TabCategory.IMPASTO),
    PRO("PRO", TabCategory.IMPASTO),
    TEGLIA("Teglia", TabCategory.IMPASTO),
    BIGA("Biga", TabCategory.IMPASTO),
    MIX_FARINE("Mix Farine", TabCategory.FARINE),
    W_PROTEINE("W·Proteine", TabCategory.FARINE),
    TEMPERATURE("Temp.", TabCategory.UTILITY),
    CONTENITORE("Contenitore", TabCategory.UTILITY),
    CONVERSIONE("Conversione", TabCategory.UTILITY)
}

private data class CategoryInfo(val name: String, val color: Color, val tabs: List<CalculatorTab>)

private val categories = listOf(
    CategoryInfo("Impasto", QuadernoColors.CatImpasto, CalculatorTab.entries.filter { it.category == TabCategory.IMPASTO }),
    CategoryInfo("Farine", QuadernoColors.CatFarine, CalculatorTab.entries.filter { it.category == TabCategory.FARINE }),
    CategoryInfo("Utility", QuadernoColors.CatUtility, CalculatorTab.entries.filter { it.category == TabCategory.UTILITY }),
)

private val headerInfo = mapOf(
    CalculatorTab.FACILE to ("Facile" to "per panetti"),
    CalculatorTab.AVANZATO to ("Avanzato" to "pieno controllo"),
    CalculatorTab.PRO to ("PRO" to "percentuali farina"),
    CalculatorTab.TEGLIA to ("Teglia" to "al taglio"),
    CalculatorTab.BIGA to ("Biga" to "prefermento"),
    CalculatorTab.MIX_FARINE to ("Mix Farine" to "forza media"),
    CalculatorTab.W_PROTEINE to ("W" to "dalle proteine"),
    CalculatorTab.TEMPERATURE to ("Temperature" to "di cottura"),
    CalculatorTab.CONTENITORE to ("Contenitore" to "volume utile"),
    CalculatorTab.CONVERSIONE to ("Conversione" to "dei lieviti"),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorScreen(onStartProcess: () -> Unit = {}, modifier: Modifier = Modifier) {
    val tabs = CalculatorTab.entries
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex, pageCount = { tabs.size })

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page -> selectedTabIndex = page }
    }
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) pagerState.animateScrollToPage(selectedTabIndex)
    }

    val currentTab = tabs[selectedTabIndex]
    val (headerTitle, headerItalic) = headerInfo[currentTab] ?: ("" to "")

    Column(modifier = modifier.fillMaxSize().background(QuadernoColors.Bg)) {
        QHeader(
            kicker = "Calcolo · ${currentTab.label}",
            title = headerTitle,
            italic = headerItalic,
            tight = true,
        )

        QCalcTabStrip(
            selectedTab = currentTab,
            onTabSelect = { tab -> selectedTabIndex = tabs.indexOf(tab) },
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            beyondBoundsPageCount = 1,
        ) { pageIndex ->
            when (tabs[pageIndex]) {
                CalculatorTab.FACILE -> FacileTabContent(onStartProcess = onStartProcess)
                CalculatorTab.AVANZATO -> AvanzatoCalculator(onStartProcess = onStartProcess)
                CalculatorTab.PRO -> ProCalculator()
                CalculatorTab.TEGLIA -> TegliaCalculator()
                CalculatorTab.BIGA -> BigaCalculator()
                CalculatorTab.MIX_FARINE -> MixFarineScreen()
                CalculatorTab.W_PROTEINE -> ForzaProteineCalculator()
                CalculatorTab.TEMPERATURE -> TemperatureFornoCalculator()
                CalculatorTab.CONTENITORE -> ContenitoreCalculator()
                CalculatorTab.CONVERSIONE -> ConversioneLievitoCalculator()
            }
        }
    }
}

@Composable
private fun QCalcTabStrip(selectedTab: CalculatorTab, onTabSelect: (CalculatorTab) -> Unit) {
    val tabs = CalculatorTab.entries
    val scrollState = rememberScrollState()

    // Auto-scroll the tab strip so the selected tab is visible
    LaunchedEffect(selectedTab) {
        val selectedIndex = tabs.indexOf(selectedTab)
        // Approximate position: each chip ~70dp wide on average
        val approxOffset = (selectedIndex * 74).coerceAtLeast(0)
        scrollState.animateScrollTo(
            (approxOffset - 60).coerceAtLeast(0)
        )
    }

    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            categories.forEach { cat ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(cat.color),
                    )
                    Text(
                        text = cat.name.uppercase(),
                        style = TextStyle(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.2.sp,
                            color = cat.color,
                        ),
                    )
                }
            }
        }

        val inkColor = QuadernoColors.Ink
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(inkColor, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    val bottomY = size.height
                    drawLine(inkColor, Offset(0f, bottomY - 1.dp.toPx()), Offset(size.width, bottomY - 1.dp.toPx()), 1.dp.toPx())
                    drawLine(inkColor, Offset(0f, bottomY - 4.dp.toPx()), Offset(size.width, bottomY - 4.dp.toPx()), 1.dp.toPx())
                }
                .padding(vertical = 8.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEach { tab ->
                val isSelected = tab == selectedTab
                val catColor = when (tab.category) {
                    TabCategory.IMPASTO -> QuadernoColors.CatImpasto
                    TabCategory.FARINE -> QuadernoColors.CatFarine
                    TabCategory.UTILITY -> QuadernoColors.CatUtility
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) catColor else Color.Transparent)
                        .clickable { onTabSelect(tab) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = tab.label,
                        style = TextStyle(
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontStyle = if (isSelected) FontStyle.Normal else FontStyle.Italic,
                            color = if (isSelected) QuadernoColors.Paper else catColor,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FacileTabContent(onStartProcess: () -> Unit) {
    var daFarinaMode by rememberSaveable { mutableStateOf(false) }
    if (daFarinaMode) {
        DaFarinaCalculator(onStartProcess = onStartProcess, onModeChange = { daFarinaMode = false })
    } else {
        FacileCalculator(onStartProcess = onStartProcess, onModeChange = { daFarinaMode = true })
    }
}
