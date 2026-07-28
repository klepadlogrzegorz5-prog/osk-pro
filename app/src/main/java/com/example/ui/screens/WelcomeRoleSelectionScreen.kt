package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeRoleSelectionScreen(
    onBypassLogin: (String, String) -> Unit,
    onRoleSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HIGH END BRAND HEADER
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        Text(
                            text = "SYSTEM PRODRIVE OSK COMMAND",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF0284C7))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "OSK Steering",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "OSK ProDrive",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Platforma Szkoleń & Floty",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Kompleksowa obsługa Właściciela, Instruktora i Kursanta w jednym zintegrowanym systemie OSK",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // QUICK BYPASS LOGIN PANEL
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SZYBKIE WEJŚCIE (Szkolenie & Testy)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                                color = Color.White
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { onBypassLogin("OWNER", "Jan Kowalski (Właściciel)") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("bypass_owner"),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("Właściciel", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                            Button(
                                onClick = { onBypassLogin("INSTRUKTOR", "Tomasz Nowak") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("bypass_instructor"),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("Instruktor", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                            Button(
                                onClick = { onBypassLogin("KURSANT", "Jan Kowalski") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("bypass_student"),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Text("Kursant", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Role Selection Heading
                Text(
                    text = "Wybierz panel logowania:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                RoleCard(
                    title = "Panel Właściciela OSK 💼",
                    subtitle = "Zarządzanie kadrą, finansami, flotą i kodami",
                    description = "Pełny nadzór nad biurem, analizą paliwa i przychodami.",
                    icon = Icons.Default.BusinessCenter,
                    badgeColor = Color(0xFF1D4ED8),
                    testTag = "role_owner_card",
                    onClick = { onRoleSelected("OWNER") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                RoleCard(
                    title = "Panel Instruktora 🚗",
                    subtitle = "Dziennik jazd, rejestracja tankowań i usterki",
                    description = "Grafik, ocena kursantów oraz spisywanie licznika.",
                    icon = Icons.Default.DirectionsCar,
                    badgeColor = Color(0xFF0284C7),
                    testTag = "role_instructor_card",
                    onClick = { onRoleSelected("INSTRUKTOR") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                RoleCard(
                    title = "Panel Kursanta 🎓",
                    subtitle = "Rezerwacje jazd, postępy i polecenia",
                    description = "Śledzenie godzin, płatności i e-learning.",
                    icon = Icons.Default.School,
                    badgeColor = Color(0xFF059669),
                    testTag = "role_student_card",
                    onClick = { onRoleSelected("KURSANT") }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // REQUIRED FOOTER AT BOTTOM
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0F172A)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "powered by ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "gK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    badgeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = badgeColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

