package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.NotificationEntity
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    currentRole: UserRole,
    notifications: List<NotificationEntity>,
    onSendNotification: (String, String, String) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    onMarkAllAsRead: () -> Unit,
    onDeleteNotification: (Long) -> Unit,
    onClearAll: () -> Unit,
    onWishLuckClick: (String, String, Long) -> Unit = { _, _, _ -> }
) {
    var titleText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var selectedTarget by remember { mutableStateOf("ALL") } // "ALL", "KURSANT", "INSTRUKTOR"
    var showSuccessToast by remember { mutableStateOf(false) }

    // Filter notifications based on role
    val filteredNotifications = notifications.filter { notification ->
        when (currentRole) {
            UserRole.STUDENT -> notification.targetGroup == "KURSANT" || notification.targetGroup == "ALL"
            UserRole.INSTRUCTOR -> notification.targetGroup == "INSTRUKTOR" || notification.targetGroup == "ALL"
            UserRole.MANAGER -> true // Manager sees everything
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Broadcast panel for Manager and Instructor
        if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nadaj nowe powiadomienie",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Target group label helper
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Panel Nadawcy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        // Target Group Picker (Do kursantów, Do instruktorów, Do wszystkich)
                        Text(
                            text = "Grupa docelowa:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val targets = listOf(
                                "ALL" to "Do wszystkich",
                                "KURSANT" to "Do kursantów",
                                "INSTRUKTOR" to "Do instruktorów"
                            )

                            targets.forEach { (key, label) ->
                                val isSelected = selectedTarget == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTarget = key },
                                    label = { Text(label, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).testTag("chip_target_$key")
                                )
                            }
                        }

                        // Input fields
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { titleText = it },
                            label = { Text("Tytuł powiadomienia") },
                            placeholder = { Text("np. Przerwa świąteczna, Zmiana grafiku") },
                            modifier = Modifier.fillMaxWidth().testTag("notif_title_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            label = { Text("Treść wiadomości") },
                            placeholder = { Text("Wpisz treść komunikatu...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("notif_message_input")
                        )

                        Button(
                            onClick = {
                                if (titleText.isNotBlank() && messageText.isNotBlank()) {
                                    onSendNotification(selectedTarget, titleText, messageText)
                                    titleText = ""
                                    messageText = ""
                                    showSuccessToast = true
                                }
                            },
                            enabled = titleText.isNotBlank() && messageText.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_send_notif")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wyślij powiadomienie", fontWeight = FontWeight.Bold)
                        }

                        if (showSuccessToast) {
                            Text(
                                text = "✓ Powiadomienie zostało wysłane pomyślnie!",
                                color = Color(0xFF10B981),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Header and management actions for notifications list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Skrzynka odbiorcza",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Liczba wiadomości: ${filteredNotifications.size}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (filteredNotifications.any { !it.isRead }) {
                        TextButton(
                            onClick = onMarkAllAsRead,
                            modifier = Modifier.testTag("btn_mark_all_read")
                        ) {
                            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Odczytaj wszystko", fontSize = 12.sp)
                        }
                    }

                    if (currentRole == UserRole.MANAGER && notifications.isNotEmpty()) {
                        IconButton(
                            onClick = onClearAll,
                            modifier = Modifier.testTag("btn_clear_all_notifs")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Wyczyść wszystko",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Notifications List
        if (filteredNotifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Brak powiadomień",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nie masz obecnie żadnych nowych komunikatów.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredNotifications) { item ->
                NotificationItemRow(
                    notification = item,
                    currentRole = currentRole,
                    onMarkAsRead = { onMarkAsRead(item.id) },
                    onDelete = { onDeleteNotification(item.id) },
                    onWishLuckClick = onWishLuckClick
                )
            }
        }
    }
}

@Composable
private fun NotificationItemRow(
    notification: NotificationEntity,
    currentRole: UserRole,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onWishLuckClick: (String, String, Long) -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedTime = sdf.format(Date(notification.timestamp))

    // Determine color codes based on priority or content triggers
    val isSystem = notification.senderRole == "System"
    val isMissingDocs = notification.title.contains("dokumenty", ignoreCase = true) || notification.title.contains("Brakujące", ignoreCase = true)
    val isUnpaid = notification.title.contains("płatność", ignoreCase = true) || notification.title.contains("Rozliczenie", ignoreCase = true)
    val isVehicleIssue = notification.title.contains("usterkę", ignoreCase = true) || notification.title.contains("awarię", ignoreCase = true) || notification.title.contains("badania", ignoreCase = true)

    val cardBorderColor = when {
        !notification.isRead -> MaterialTheme.colorScheme.primary
        isMissingDocs -> Color(0xFFEF4444) // Red alert
        isUnpaid -> Color(0xFFF59E0B) // Orange warning
        isVehicleIssue -> Color(0xFF3B82F6) // Blue notice
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val cardBgColor = when {
        !notification.isRead -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        isMissingDocs -> Color(0xFFEF4444).copy(alpha = 0.03f)
        isUnpaid -> Color(0xFFF59E0B).copy(alpha = 0.03f)
        isVehicleIssue -> Color(0xFF3B82F6).copy(alpha = 0.03f)
        else -> MaterialTheme.colorScheme.surface
    }

    val targetBadgeText = when (notification.targetGroup) {
        "ALL" -> "Wszyscy"
        "KURSANT" -> "Kursanci"
        "INSTRUKTOR" -> "Instruktorzy"
        else -> notification.targetGroup
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (!notification.isRead) 1.5.dp else 1.dp,
            cardBorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_item_${notification.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Icon + Sender Name + Target Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar/Icon placeholder
                    Surface(
                        color = when {
                            isSystem -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            notification.senderRole == "Zarządca OSK" -> Color(0xFF10B981).copy(alpha = 0.15f)
                            else -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isMissingDocs -> Icons.Default.Description
                                    isUnpaid -> Icons.Default.AttachMoney
                                    isVehicleIssue -> Icons.Default.Build
                                    else -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = when {
                                    isMissingDocs -> Color(0xFFEF4444)
                                    isUnpaid -> Color(0xFFF59E0B)
                                    isVehicleIssue -> Color(0xFF3B82F6)
                                    notification.senderRole == "Zarządca OSK" -> Color(0xFF10B981)
                                    else -> Color(0xFF38BDF8)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = notification.senderName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isSystem) "System automatyczny" else notification.senderRole,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Target Group Badge
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Do: $targetBadgeText",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Unread blue dot indicator
                    if (!notification.isRead) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body text
            Text(
                text = notification.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            if (notification.isWishReminder && notification.relatedStudentName != null && notification.relatedExamType != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        onWishLuckClick(notification.relatedStudentName, notification.relatedExamType, notification.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_wish_luck_${notification.id}")
                ) {
                    Icon(imageVector = Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Życz powodzenia 🤞", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Timestamp + Quick Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!notification.isRead) {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(32.dp).testTag("btn_read_${notification.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Oznacz jako przeczytane",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Allow delete if role is Manager or if it's their own notification
                    if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("btn_delete_${notification.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Usuń powiadomienie",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
