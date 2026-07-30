package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.UserRole

@Composable
fun RedLearnerStickerIcon(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cornerRadius = w * 0.22f
            val peelSize = w * 0.28f

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

            // Outer White Border
            drawPath(
                path = basePath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = (w * 0.05f).coerceAtLeast(2f))
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
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(top = 1.dp, end = 1.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OskTopBar(
    currentRoute: ScreenRoute,
    currentRole: UserRole,
    isDarkMode: Boolean,
    onOpenDrawer: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadCount: Int = 0
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ENLARGED RED LEARNER COOKIE PEEL ICON
                RedLearnerStickerIcon(
                    modifier = Modifier.size(42.dp),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // MAIN TITLE IS "OSK-PRO" AS REQUESTED
                    Text(
                        text = "OSK-PRO",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${currentRoute.title} • ${currentRole.label}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onOpenDrawer,
                modifier = Modifier.testTag("open_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Otwórz menu nawigacji"
                )
            }
        },
        actions = {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_top_bar_button")
            ) {
                if (unreadCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    modifier = Modifier.testTag("unread_notifications_badge")
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Powiadomienia"
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = "Powiadomienia"
                    )
                }
            }

            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Przełącz motyw"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

