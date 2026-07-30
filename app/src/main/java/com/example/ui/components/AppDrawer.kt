package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.viewmodel.UserRole

enum class ScreenRoute(val title: String, val icon: ImageVector) {
    DASHBOARD("Pulpit Główny", Icons.Default.Dashboard),
    NOTIFICATIONS("Powiadomienia", Icons.Default.Notifications),
    EXAMS("Egzaminy & PKK", Icons.Default.AssignmentTurnedIn),
    RESERVATIONS("Rezerwacje", Icons.Default.EventAvailable),
    DOCUMENTS("Dokumenty", Icons.Default.Description),
    FLEET("Flota Pojazdów", Icons.Default.DirectionsCar),
    REPORTS("Raporty Floty", Icons.Default.Assessment),
    SCHEDULE("Grafik i Rezerwacje", Icons.Default.CalendarMonth),
    FINANCE("Finanse i Rozliczenia", Icons.Default.AccountBalanceWallet),
    USER_KEYS("Kody Dostępu", Icons.Default.Key),
    SICK_LEAVE("Zwolnienia L4", Icons.Default.MedicalServices),
    CHAT("Komunikator Live", Icons.AutoMirrored.Filled.Chat),
    DRIVING_TESTS("Testy na prawo jazdy", Icons.Default.Assignment),
    OPTIONS("Opcje", Icons.Default.Settings)
}

@Composable
fun AppDrawerContent(
    currentRoute: ScreenRoute,
    currentRole: UserRole,
    companyName: String,
    companyNip: String,
    userName: String,
    onRoleSelect: (UserRole) -> Unit,
    onRouteSelect: (ScreenRoute) -> Unit,
    onCloseDrawer: () -> Unit,
    onLogout: () -> Unit = {}
) {
    var showCreatorDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0B0F19), // Deep Cyber Background
        drawerContentColor = Color.White,
        modifier = Modifier.width(330.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Banner with Red Cookie Peel Sticker
            Surface(
                color = Color(0xFF131B2E),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0xFFA855F7), Color(0xFF38BDF8))
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Enlarged Red Cookie Peel Sticker Icon
                    RedLearnerStickerIcon(
                        modifier = Modifier.size(54.dp),
                        fontSize = 32.sp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "OSK-PRO",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = companyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "NIP: $companyNip",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Role Switcher Card in Selection Screen Style
            if (currentRole == UserRole.MANAGER) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.5.dp,
                        Color(0xFFA855F7).copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "AKTYWNA ROLA W SYSTEMIE:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFA855F7),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentRole.icon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentRole.label,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = userName,
                                        fontSize = 12.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Static and clean user info badge with no switcher/options for Student and Instructor
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.4f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.12f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = currentRole.icon, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = currentRole.label,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 14.dp),
                color = Color.White.copy(alpha = 0.12f)
            )

            // Scrollable Menu Items in Selection Screen Style with Neon Purple Borders
            val availableRoutes = getRoutesForRole(currentRole)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                availableRoutes.forEach { route ->
                    val isSelected = currentRoute == route
                    val animatedBgColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF0F172A).copy(alpha = 0.8f),
                        animationSpec = tween(200),
                        label = "ItemBgAnim"
                    )

                    Surface(
                        color = animatedBgColor,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            brush = if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFA855F7), Color(0xFF38BDF8), Color(0xFFEC4899))
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B5CF6).copy(alpha = 0.5f), Color(0xFF8B5CF6).copy(alpha = 0.3f))
                                )
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onRouteSelect(route)
                                onCloseDrawer()
                            }
                            .testTag("nav_item_${route.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = route.icon,
                                    contentDescription = route.title,
                                    tint = if (isSelected) Color(0xFF38BDF8) else Color(0xFFA855F7),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = route.title,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // INTERACTIVE RED LOGO / LOGOUT BUTTON ("WYLOGUJ")
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val logoutBgColor by animateColorAsState(
                targetValue = if (isPressed) Color(0xFFDC2626) else Color(0xFF1E1B4B),
                animationSpec = tween(150),
                label = "LogoutBg"
            )

            Surface(
                color = logoutBgColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.5.dp,
                    if (isPressed) Color.White else Color(0xFFEF4444)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            onLogout()
                            onCloseDrawer()
                        }
                    )
                    .testTag("logout_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Wyloguj",
                        tint = if (isPressed) Color.White else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WYLOGUJ Z SYSTEMU",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer info
            Text(
                text = "OSK-PRO v1.0 • Cyberpunk Edition",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

fun getRoutesForRole(role: UserRole): List<ScreenRoute> {
    return when (role) {
        UserRole.MANAGER -> listOf(
            ScreenRoute.DASHBOARD,
            ScreenRoute.NOTIFICATIONS,
            ScreenRoute.EXAMS,
            ScreenRoute.RESERVATIONS,
            ScreenRoute.DOCUMENTS,
            ScreenRoute.FLEET,
            ScreenRoute.REPORTS,
            ScreenRoute.SCHEDULE,
            ScreenRoute.FINANCE,
            ScreenRoute.USER_KEYS,
            ScreenRoute.SICK_LEAVE,
            ScreenRoute.CHAT,
            ScreenRoute.DRIVING_TESTS,
            ScreenRoute.OPTIONS
        )
        UserRole.INSTRUCTOR -> listOf(
            ScreenRoute.DASHBOARD,
            ScreenRoute.NOTIFICATIONS,
            ScreenRoute.EXAMS,
            ScreenRoute.RESERVATIONS,
            ScreenRoute.DOCUMENTS,
            ScreenRoute.SCHEDULE,
            ScreenRoute.FLEET,
            ScreenRoute.REPORTS,
            ScreenRoute.SICK_LEAVE,
            ScreenRoute.CHAT,
            ScreenRoute.DRIVING_TESTS,
            ScreenRoute.OPTIONS
        )
        UserRole.STUDENT -> listOf(
            ScreenRoute.DASHBOARD,
            ScreenRoute.NOTIFICATIONS,
            ScreenRoute.RESERVATIONS,
            ScreenRoute.DOCUMENTS,
            ScreenRoute.SCHEDULE,
            ScreenRoute.CHAT,
            ScreenRoute.DRIVING_TESTS,
            ScreenRoute.OPTIONS
        )
    }
}

