package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LearnerBlue
import com.example.ui.theme.LearnerYellow
import com.example.ui.viewmodel.UserRole

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (UserRole) -> Unit
) {
    var showCreatorDialog by remember { mutableStateOf(false) }
    // Pulse animation for header logo
    val infiniteTransition = rememberInfiniteTransition(label = "HeaderPulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    // Glitch jitter effect for header title
    val glitchJitterX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlitchJitter"
    )

    // Shutter / Migawka strobe flicker effect
    val shutterFlicker by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShutterFlicker"
    )

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
                    radius = 1200f
                )
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ENLARGED PULSING RED LOGO WITH COOKIE PEEL EFFECT
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale)
                    .size(86.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cornerRadius = 20.dp.toPx()
                    val peelSize = 24.dp.toPx()

                    val basePath = Path().apply {
                        moveTo(cornerRadius, 0f)
                        lineTo(w - peelSize, 0f)
                        lineTo(w, peelSize)
                        lineTo(w, h - cornerRadius)
                        quadraticBezierTo(w, h, w - cornerRadius, h)
                        lineTo(cornerRadius, h)
                        quadraticBezierTo(0f, h, 0f, h - cornerRadius)
                        lineTo(0f, cornerRadius)
                        quadraticBezierTo(0f, 0f, cornerRadius, 0f)
                        close()
                    }

                    // Base Red Gradient
                    drawPath(
                        path = basePath,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))
                        )
                    )

                    // Outer Border
                    drawPath(
                        path = basePath,
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // 3D Peel Corner
                    val foldPath = Path().apply {
                        moveTo(w - peelSize, 0f)
                        lineTo(w, peelSize)
                        quadraticBezierTo(w - peelSize * 0.4f, peelSize * 0.4f, w - peelSize, 0f)
                        close()
                    }

                    drawPath(
                        path = foldPath,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF991B1B), Color(0xFF7F1D1D), Color(0xFFFCA5A5)),
                            start = Offset(w - peelSize, 0f),
                            end = Offset(w, peelSize)
                        )
                    )
                }

                Text(
                    text = "L",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp, end = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SHUTTER FLICKER (MIGAWKA) & GLITCH EFFECT TITLE "WITAJ W OSK-PRO"
            Box(contentAlignment = Alignment.Center) {
                // Chromatic Magenta Glitch Shadow
                Text(
                    text = "WITAJ W OSK-PRO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEC4899).copy(alpha = 0.85f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.offset(x = glitchJitterX.dp, y = (-1.5).dp)
                )
                // Chromatic Cyan Glitch Shadow
                Text(
                    text = "WITAJ W OSK-PRO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF06B6D4).copy(alpha = 0.85f),
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.offset(x = (-glitchJitterX).dp, y = 1.5.dp)
                )
                // Main Strobe / Shutter Flicker Pure White Title
                Text(
                    text = "WITAJ W OSK-PRO",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = shutterFlicker),
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HIGH-CONTRAST VIVID SUBTITLE PILL CONTAINER
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF10B981))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYSTEM SPORZĄDZONY DLA OSK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Wybierz profil logowania poniżej. Interfejs, uprawnienia oraz dane dopasują się automatycznie.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role selection cards
            RoleCard(
                title = "Zarządzanie OSK",
                roleBadge = "Właściciel / Menedżer",
                description = "Pełny dostęp zarządczy do finansów, floty pojazdów i kadry.",
                features = listOf("Klucze dostępu & NIP firmy", "Ewidencja floty & przeglądów", "Kasa OSK i bilans finansowy"),
                icon = Icons.Default.AdminPanelSettings,
                accentColor = Color(0xFF0284C7),
                testTag = "role_card_manager",
                onClick = { onRoleSelected(UserRole.MANAGER) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RoleCard(
                title = "Instruktor",
                roleBadge = "Pracownik Szkoleniowy",
                description = "Narzędzia pracy codziennej z kursantami i harmonogram jazd.",
                features = listOf("Smart Grafik i rezerwacja jazd", "Komunikator Live i czat", "Zgłaszanie L4 i zastępstw"),
                icon = Icons.Default.Badge,
                accentColor = Color(0xFF8B5CF6),
                testTag = "role_card_instructor",
                onClick = { onRoleSelected(UserRole.INSTRUCTOR) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            RoleCard(
                title = "Kursant Kat. B",
                roleBadge = "Uczestnik Kursu",
                description = "Cyfrowy portfel kursanta, rezerwacja jazd oraz baza pytań.",
                features = listOf("Proponowanie terminów jazd", "Baza wiedzy i kodeks drogowy", "Testy egzaminacyjne PWPW"),
                icon = Icons.Default.School,
                accentColor = Color(0xFF10B981),
                testTag = "role_card_student",
                onClick = { onRoleSelected(UserRole.STUDENT) }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // CYBERPUNK GLITCH SECURITY FOOTER
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFEF4444), Color(0xFF06B6D4), Color(0xFFA855F7), Color(0xFFEC4899))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { showCreatorDialog = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Glitch Magenta Layer
                    Text(
                        text = "😈 GrzesKlep secure your App 24/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEC4899).copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .offset(x = glitchJitterX.dp, y = (-1).dp)
                    )
                    // Glitch Cyan Layer
                    Text(
                        text = "😈 GrzesKlep secure your App 24/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF06B6D4).copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                            .offset(x = (-glitchJitterX).dp, y = 1.dp)
                    )
                    // Main Front Layer
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFDC2626).copy(alpha = 0.25f),
                                        Color(0xFF7C3AED).copy(alpha = 0.25f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Shield",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "😈 GrzesKlep secure your App 24/h",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        if (showCreatorDialog) {
            com.example.ui.components.CreatorInfoDialog(onDismiss = { showCreatorDialog = false })
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    roleBadge: String,
    description: String,
    features: List<String>,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF1E293B).copy(alpha = 0.8f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(accentColor, accentColor.copy(alpha = 0.3f))
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            color = accentColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = roleBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = accentColor,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Zaloguj",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Bullet points
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                features.forEach { feature ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feature,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

