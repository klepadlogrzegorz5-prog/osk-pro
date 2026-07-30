package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LearnerBlue
import com.example.ui.theme.LearnerYellow
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onDismiss: () -> Unit
) {
    // 12 Seconds Auto Timer
    val durationSeconds = 12
    var remainingSeconds by remember { mutableIntStateOf(durationSeconds) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        onDismiss()
    }

    val progressAnimated by animateFloatAsState(
        targetValue = (durationSeconds - remainingSeconds) / durationSeconds.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "ProgressAnimation"
    )

    val progressPercent = (progressAnimated * 100).toInt()

    // Dynamic loading stage text based on seconds
    val loadingStageText = when {
        remainingSeconds > 9 -> "⚡ Inicjalizacja rdzenia OSK-PRO..."
        remainingSeconds > 6 -> "🔄 Synchronizacja bazy pytań PWPW..."
        remainingSeconds > 3 -> "🛡️ Weryfikacja kluczy szyfrujących..."
        else -> "✨ System gotowy do pracy!"
    }

    // Pulsing & Rotation animations
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimations")
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbitalRotation"
    )

    val reverseRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ReverseRotation"
    )

    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BadgePulse"
    )

    // Glitch State & Shutter Strobe animations
    val shutterFlicker by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShutterFlicker"
    )

    var isSecureRevealed by remember { mutableStateOf(false) }
    var glitchTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2200) // Glitch phase
        glitchTrigger = true
        delay(350)
        isSecureRevealed = true // Explosive reveal of "GrzesKlep secure your app"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF030712),
                        Color(0xFF020617)
                    ),
                    radius = 1300f
                )
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar with Skip Button & Live Countdown Chip (No Bar Loader)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF0284C7).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "START W: ${remainingSeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("skip_splash_button")
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "POMIŃ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Branding & STUNNING QUANTUM CIRCULAR LOADING HERO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // QUANTUM LOADING ARC GAUGES AROUND HERO BADGE
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    // Canvas Quantum Arc Gauge & Circular Progress Ring
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidthPx = 6.dp.toPx()
                        val diameter = size.minDimension - strokeWidthPx * 2

                        // 1. Outer Dark Rail Track
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = diameter / 2,
                            style = Stroke(width = strokeWidthPx)
                        )

                        // 2. Quantum Progress Sweep Arc based on progressAnimated (0% to 360%)
                        val sweepAngleProgress = progressAnimated * 360f
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF06B6D4), // Cyan
                                    Color(0xFF3B82F6), // Blue
                                    Color(0xFFA855F7), // Purple
                                    Color(0xFFEC4899), // Pink
                                    Color(0xFF06B6D4)
                                )
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngleProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )

                        // 3. Fast Rotating Orbital Laser Segment 1
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFACC15),
                                    Color(0x00FACC15)
                                )
                            ),
                            startAngle = rotationAngle,
                            sweepAngle = 120f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                            size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx())
                        )

                        // 4. Reverse Rotating Laser Segment 2
                        drawArc(
                            color = Color(0xFF38BDF8),
                            startAngle = reverseRotation,
                            sweepAngle = 80f,
                            useCenter = false,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(20.dp.toPx(), 20.dp.toPx()),
                            size = Size(size.width - 40.dp.toPx(), size.height - 40.dp.toPx())
                        )
                    }

                    // Central Cookie Peel Badge
                    LearnerCookiePeelBadge(
                        modifier = Modifier
                            .scale(badgePulse)
                            .size(145.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QUANTUM PERCENTAGE COUNTER & LOADING STAGE TEXT
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFA855F7))
                        )
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$progressPercent%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = loadingStageText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Title with Neon Glow Style
                Text(
                    text = "OSK-PRO",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Przyszłość Zarządzania Nauką Jazdy",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF38BDF8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Kompleksowa Platforma: Menedżer • Instruktor • Kursant",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Value Proposition Highlights (Why OSK-PRO?)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "KLUCZOWE ATUTY SYSTEMU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.5.sp
                )

                ValueCard(
                    icon = Icons.Default.AutoGraph,
                    title = "99.4% Zdawalności Egzaminów",
                    description = "Baza pytań PWPW i inteligentny asystent postępów",
                    color = Color(0xFF10B981)
                )

                ValueCard(
                    icon = Icons.Default.CalendarMonth,
                    title = "Smart Grafik Jazd 24/7",
                    description = "Błyskawiczne rezerwacje i synchronizacja live",
                    color = Color(0xFF8B5CF6)
                )

                ValueCard(
                    icon = Icons.Default.DirectionsCar,
                    title = "Zarządzanie Flotą i Bilans",
                    description = "Kontrola tankowań, kosztów i dostępności aut",
                    color = Color(0xFF0EA5E9)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main CTA Button
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "PRZEJDŹ DO WYBORU ROLI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FOOTER: GLITCH "Powered by gK" -> EXPLOSIVE REVEAL "GrzesKlep secure your app"
            GlitchSecurityFooter(
                isSecureRevealed = isSecureRevealed,
                glitchActive = glitchTrigger
            )
        }
    }
}

/**
 * Custom Canvas & Layer Badge producing a HUGE yellow "L" square
 * with a realistic 3D Cookie Peel / Sticker Curl Corner effect.
 */
@Composable
private fun LearnerCookiePeelBadge(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cornerRadius = 32.dp.toPx()
            val peelSize = 42.dp.toPx() // Size of the peeled corner

            // Base Yellow Square Path with Top-Right Peeled Cutout
            val basePath = Path().apply {
                moveTo(cornerRadius, 0f)
                // Top edge leading up to peel cutout
                lineTo(w - peelSize, 0f)
                // Diagonal inner cut for peeled corner
                lineTo(w, peelSize)
                // Right edge
                lineTo(w, h - cornerRadius)
                quadraticBezierTo(w, h, w - cornerRadius, h)
                // Bottom edge
                lineTo(cornerRadius, h)
                quadraticBezierTo(0f, h, 0f, h - cornerRadius)
                // Left edge
                lineTo(0f, cornerRadius)
                quadraticBezierTo(0f, 0f, cornerRadius, 0f)
                close()
            }

            // Draw Base Red Sticker
            drawPath(
                path = basePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF87171), // Bright Red top
                        Color(0xFFDC2626), // Core Crimson Red
                        Color(0xFF991B1B)  // Deep Red bottom
                    )
                )
            )

            // Outer white outline border
            drawPath(
                path = basePath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 4.dp.toPx())
            )

            // Draw Peeled Cookie Corner Fold (Top-Right 3D Curl)
            val foldPath = Path().apply {
                moveTo(w - peelSize, 0f)
                lineTo(w, peelSize)
                // Curl back point
                quadraticBezierTo(w - peelSize * 0.4f, peelSize * 0.4f, w - peelSize, 0f)
                close()
            }

            // Shadow under peeled curl
            drawPath(
                path = Path().apply {
                    moveTo(w - peelSize - 4.dp.toPx(), 0f)
                    lineTo(w, peelSize + 4.dp.toPx())
                    lineTo(w - peelSize, peelSize)
                    close()
                },
                color = Color.Black.copy(alpha = 0.45f)
            )

            // Folded back surface (Metallic Dark Crimson Underside)
            drawPath(
                path = foldPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF991B1B),
                        Color(0xFF7F1D1D),
                        Color(0xFFFCA5A5)
                    ),
                    start = Offset(w - peelSize, 0f),
                    end = Offset(w, peelSize)
                )
            )

            // Highlight line on fold edge
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = Offset(w - peelSize, 0f),
                end = Offset(w, peelSize),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Center "L" Letter (Big Bold Display Typography in White)
        Text(
            text = "L",
            fontSize = 92.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        )
    }
}

/**
 * Footer with Cyber Glitch Effect and text "😈 GrzesKlep secure your App 24/h"
 */
@Composable
private fun GlitchSecurityFooter(
    isSecureRevealed: Boolean,
    glitchActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchAnimations")
    
    // Random jitter offset for glitch effect
    val glitchX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "JitterX"
    )

    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "JitterAlpha"
    )

    // Explosive reveal scale and glow animation
    val revealScale by animateFloatAsState(
        targetValue = if (isSecureRevealed) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "RevealScale"
    )

    val revealAlpha by animateFloatAsState(
        targetValue = if (isSecureRevealed) 1f else 0f,
        animationSpec = tween(400),
        label = "RevealAlpha"
    )

    val footerText = "😈 GrzesKlep secure your App 24/h"

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (!isSecureRevealed) {
            // PHASE 1: Footer with Glitch Effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.offset(x = if (glitchActive) glitchX.dp else 0.dp)
            ) {
                // Chromatic Aberration Shadow (Cyan)
                Text(
                    text = footerText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF06B6D4).copy(alpha = if (glitchActive) 0.85f else 0f),
                    modifier = Modifier.offset(x = 2.dp, y = (-1).dp)
                )
                // Chromatic Aberration Shadow (Magenta)
                Text(
                    text = footerText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEC4899).copy(alpha = if (glitchActive) 0.85f else 0f),
                    modifier = Modifier.offset(x = (-2).dp, y = 1.dp)
                )
                // Main Text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = footerText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White.copy(alpha = if (glitchActive) glitchAlpha else 0.9f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        } else {
            // PHASE 2: EXPLOSIVE CYBERPUNK GLITCH REVEAL: "😈 GrzesKlep secure your App 24/h"
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFEF4444),
                            Color(0xFF06B6D4),
                            Color(0xFFA855F7),
                            Color(0xFFEC4899)
                        )
                    )
                ),
                modifier = Modifier
                    .scale(revealScale)
                    .graphicsLayer(alpha = revealAlpha)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Glitch Offset Cyan
                    Text(
                        text = footerText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF06B6D4).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .offset(x = glitchX.dp, y = (-1).dp)
                    )
                    // Glitch Offset Magenta
                    Text(
                        text = footerText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEC4899).copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .offset(x = (-glitchX).dp, y = 1.dp)
                    )
                    // Main White Text
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFDC2626).copy(alpha = 0.3f),
                                        Color(0xFF7C3AED).copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = footerText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ValueCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        color = Color(0xFF1E293B).copy(alpha = 0.75f),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}


