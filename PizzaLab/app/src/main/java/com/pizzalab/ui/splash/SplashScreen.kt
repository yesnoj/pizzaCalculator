package com.pizzalab.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pizzalab.ui.theme.QuadernoColors
import kotlinx.coroutines.delay

/**
 * Quaderno-style splash screen.
 * Shows a simple editorial layout with the app name, tagline,
 * corner marks, dotted frame and animated loader dots.
 *
 * @param onSplashComplete Called when the splash animation finishes.
 */
@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(400))
        delay(1200)
        alpha.animateTo(0f, animationSpec = tween(300))
        onSplashComplete()
    }

    val inkColor = QuadernoColors.Ink
    val ruleColor = QuadernoColors.Rule
    val primaryColor = QuadernoColors.Primary
    val ruleDotsColor = QuadernoColors.RuleDots

    // Loader dots animation
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val loaderPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loaderDots",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QuadernoColors.Paper),
        contentAlignment = Alignment.Center,
    ) {
        // Dotted frame with corner marks
        Canvas(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            val cornerLen = 30.dp.toPx()
            val sw = 2.dp.toPx()

            // Top-left corner mark
            drawLine(primaryColor, Offset(0f, sw / 2), Offset(cornerLen, sw / 2), sw)
            drawLine(primaryColor, Offset(sw / 2, 0f), Offset(sw / 2, cornerLen), sw)

            // Top-right corner mark
            drawLine(primaryColor, Offset(size.width - cornerLen, sw / 2), Offset(size.width, sw / 2), sw)
            drawLine(primaryColor, Offset(size.width - sw / 2, 0f), Offset(size.width - sw / 2, cornerLen), sw)

            // Bottom-left corner mark
            drawLine(primaryColor, Offset(0f, size.height - sw / 2), Offset(cornerLen, size.height - sw / 2), sw)
            drawLine(primaryColor, Offset(sw / 2, size.height - cornerLen), Offset(sw / 2, size.height), sw)

            // Bottom-right corner mark
            drawLine(primaryColor, Offset(size.width - cornerLen, size.height - sw / 2), Offset(size.width, size.height - sw / 2), sw)
            drawLine(primaryColor, Offset(size.width - sw / 2, size.height - cornerLen), Offset(size.width - sw / 2, size.height), sw)

            // Dashed border connecting corners
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
            val thinStroke = 1.dp.toPx()

            // Top edge
            drawLine(ruleDotsColor, Offset(cornerLen, sw / 2), Offset(size.width - cornerLen, sw / 2), thinStroke, pathEffect = dashEffect)
            // Bottom edge
            drawLine(ruleDotsColor, Offset(cornerLen, size.height - sw / 2), Offset(size.width - cornerLen, size.height - sw / 2), thinStroke, pathEffect = dashEffect)
            // Left edge
            drawLine(ruleDotsColor, Offset(sw / 2, cornerLen), Offset(sw / 2, size.height - cornerLen), thinStroke, pathEffect = dashEffect)
            // Right edge
            drawLine(ruleDotsColor, Offset(size.width - sw / 2, cornerLen), Offset(size.width - sw / 2, size.height - cornerLen), thinStroke, pathEffect = dashEffect)
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Pizza icon — simple circle with slice marks drawn in Canvas
            Canvas(modifier = Modifier.size(80.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 4.dp.toPx()

                // Outer circle
                drawCircle(
                    color = primaryColor,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 3.dp.toPx()),
                )

                // Inner circle (crust line)
                drawCircle(
                    color = primaryColor,
                    radius = radius * 0.75f,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )

                // Slice lines (3 slices)
                for (i in 0 until 3) {
                    val angle = (i * 120f - 90f) * (Math.PI / 180f).toFloat()
                    drawLine(
                        color = primaryColor,
                        start = center,
                        end = Offset(
                            center.x + radius * kotlin.math.cos(angle),
                            center.y + radius * kotlin.math.sin(angle),
                        ),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PizzaLab",
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = QuadernoColors.Ink,
                    letterSpacing = (-0.03).sp,
                ),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "il quaderno del pizzaiolo",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = QuadernoColors.Olive,
                ),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated loader dots
            Canvas(modifier = Modifier.size(width = 40.dp, height = 8.dp)) {
                val dotRadius = 3.dp.toPx()
                val gap = 14.dp.toPx()
                val startX = (size.width - 2 * gap) / 2

                for (i in 0 until 3) {
                    val x = startX + i * gap
                    val isActive = loaderPhase.toInt() == i
                    drawCircle(
                        color = if (isActive) primaryColor else ruleDotsColor,
                        radius = if (isActive) dotRadius * 1.2f else dotRadius,
                        center = Offset(x, size.height / 2),
                    )
                }
            }
        }
    }
}
