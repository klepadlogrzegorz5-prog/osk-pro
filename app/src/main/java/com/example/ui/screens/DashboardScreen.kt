package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScreenRoute
import com.example.ui.theme.LearnerBlue
import com.example.ui.theme.LearnerYellow
import com.example.ui.viewmodel.UserRole

@Composable
fun DashboardScreen(
    currentRole: UserRole,
    companyName: String,
    companyNip: String,
    userName: String,
    vehicleCount: Int,
    lessonCount: Int,
    balancePln: Double,
    keyCount: Int,
    sickLeaveCount: Int,
    onNavigate: (ScreenRoute) -> Unit,
    onAddVehicleClick: () -> Unit,
    onBookLessonClick: () -> Unit,
    onAddFinanceClick: () -> Unit,
    onGenerateKeyClick: () -> Unit,
    onStudentReservationClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Hero Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF0284C7)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            color = LearnerYellow,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "L",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LearnerBlue
                                )
                            }
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${currentRole.icon} ${currentRole.label}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Witaj, $userName!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = "System OSK-PRO • $companyName (NIP: $companyNip)",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    if (currentRole == UserRole.STUDENT) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Prominent Cyberpunk Glassmorphism Button: "Zarezerwuj termin"
                        Surface(
                            onClick = onStudentReservationClick,
                            color = Color(0xFFEF4444).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF87171), Color(0xFFEC4899), Color(0xFFA855F7))
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("student_hero_reserve_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFEF4444),
                                        shape = CircleShape,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.EventAvailable,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "ZAREZERWUJ TERMIN",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = "Formularz rejestracyjny & terminarz jazd",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFCA5A5)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid
        item {
            Text(
                text = "PODSUMOWANIE SYSTMEOWE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Flota Pojazdów",
                    value = "$vehicleCount aut",
                    icon = Icons.Default.DirectionsCar,
                    color = Color(0xFF0EA5E9),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenRoute.FLEET) }
                )
                StatCard(
                    title = "Zaplanowane Jazdy",
                    value = "$lessonCount lekcji",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenRoute.SCHEDULE) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Bilans Finansowy",
                    value = String.format("%.2f zł", balancePln),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenRoute.FINANCE) }
                )
                StatCard(
                    title = "Kody Dostępu / L4",
                    value = "$keyCount kluczy / $sickLeaveCount L4",
                    icon = Icons.Default.Key,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(ScreenRoute.USER_KEYS) }
                )
            }
        }

        // Quick Actions Section
        item {
            Text(
                text = "SZYBKIE AKCJE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (currentRole) {
                    UserRole.MANAGER -> {
                        QuickActionButton(
                            text = "Dodaj Nowy Pojazd do Floty",
                            icon = Icons.Default.Add,
                            testTag = "quick_add_vehicle",
                            onClick = onAddVehicleClick
                        )
                        QuickActionButton(
                            text = "Wygeneruj Kod Dostępu dla Instruktorów/Kursantów",
                            icon = Icons.Default.Key,
                            testTag = "quick_gen_key",
                            onClick = onGenerateKeyClick
                        )
                        QuickActionButton(
                            text = "Zapisz Nową Operację Finansową",
                            icon = Icons.Default.AttachMoney,
                            testTag = "quick_add_finance",
                            onClick = onAddFinanceClick
                        )
                    }
                    UserRole.INSTRUCTOR -> {
                        QuickActionButton(
                            text = "Zaplanuj Jazdę z Kursantem",
                            icon = Icons.Default.CalendarMonth,
                            testTag = "quick_book_lesson",
                            onClick = onBookLessonClick
                        )
                        QuickActionButton(
                            text = "Przejdź do Komunikatora Live",
                            icon = Icons.Default.Chat,
                            testTag = "quick_open_chat",
                            onClick = { onNavigate(ScreenRoute.CHAT) }
                        )
                    }
                    UserRole.STUDENT -> {
                        QuickActionButton(
                            text = "Zarezerwuj Termin Jazdy (Formularz Online)",
                            icon = Icons.Default.EventAvailable,
                            testTag = "quick_propose_lesson",
                            onClick = onStudentReservationClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    text: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
