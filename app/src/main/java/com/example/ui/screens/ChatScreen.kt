package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ChatMessageEntity
import com.example.data.db.UserKeyEntity
import com.example.ui.components.EmptyState
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Utility to clean role suffixes from usernames
fun cleanName(fullName: String): String {
    return fullName.replace(" (Zarządca)", "")
        .replace(" (Instruktor)", "")
        .replace(" (Kursant)", "")
        .trim()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    userKeys: List<UserKeyEntity>,
    currentUserRole: UserRole,
    currentUserName: String,
    onSendMessage: (String, String) -> Unit,
    onUpdateBlockStatus: (Long, Boolean) -> Unit,
    onUpdateRemoveStatus: (Long, Boolean) -> Unit
) {
    val context = LocalContext.current
    val cleanedCurrentName = remember(currentUserName) { cleanName(currentUserName) }
    
    // Find if the current user has a corresponding UserKey and check their flags
    val currentUserKey = userKeys.find { cleanName(it.assignedName) == cleanedCurrentName }
    val isUserBlocked = currentUserKey?.isBlocked == true
    val isUserRemoved = currentUserKey?.isRemovedFromChat == true

    var activeChannelId by remember { mutableStateOf("general") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Czat, 1: Kanały i Kontakty, 2: Moderacja (Zarządca only)
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupNameInput by remember { mutableStateOf("") }
    
    // For search/filtering
    var userSearchQuery by remember { mutableStateOf("") }

    // Scan all messages to discover custom groups dynamically
    val groupChannels = remember(messages) {
        messages.map { it.channelId }
            .filter { it.startsWith("group_") }
            .distinct()
            .map { it.removePrefix("group_") }
    }

    if (isUserRemoved) {
        // Red overlay card if user was banned/removed from chat by the manager
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Zablokowany",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Dostęp Zablokowany",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Zarządca OSK całkowicie usunął Twój profil z komunikatora szkolnego.\n\nSkontaktuj się bezpośrednio z biurem OSK, aby wyjaśnić sytuację lub odzyskać uprawnienia.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Komunikator OSK-PRO Live",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Zalogowany jako: $currentUserName",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (currentUserRole == UserRole.MANAGER || currentUserRole == UserRole.INSTRUCTOR) {
                IconButton(
                    onClick = { showCreateGroupDialog = true },
                    modifier = Modifier.testTag("create_group_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "Utwórz nową grupę",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Navigation row
        val tabCount = if (currentUserRole == UserRole.MANAGER) 3 else 2
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Czat", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Kontakty & Grupy", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            if (currentUserRole == UserRole.MANAGER) {
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Moderacja", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // VIEW CHAT ROOM
                val displayedChannelTitle = when {
                    activeChannelId == "general" -> "📢 Kanał Ogólny (Wszyscy)"
                    activeChannelId == "instructors" -> "🚗 Kanał: Instruktorzy & Zarząd"
                    activeChannelId.startsWith("group_") -> "👥 Grupa: " + activeChannelId.removePrefix("group_")
                    activeChannelId.startsWith("priv_") -> {
                        val other = activeChannelId.removePrefix("priv_").split("___")
                            .firstOrNull { cleanName(it) != cleanedCurrentName } ?: "Czat prywatny"
                        "💬 Prywatny: $other"
                    }
                    else -> "Rozmowa"
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = displayedChannelTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (activeChannelId != "general") {
                            TextButton(
                                onClick = { activeChannelId = "general" },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Wróć do ogólnego", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Filter messages belonging to this activeChannelId
                val filteredMessages = messages.filter { it.channelId == activeChannelId }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (filteredMessages.isEmpty()) {
                        EmptyState(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            title = "Brak wiadomości w tym pokoju",
                            description = "Napisz pierwszą wiadomość poniżej, aby rozpocząć konwersację na tym kanale.",
                            actionButtonText = null,
                            onActionClick = null,
                            testTagPrefix = "room_chat"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredMessages) { msg ->
                                val isMe = cleanName(msg.senderName) == cleanedCurrentName
                                ChatBubbleItem(message = msg, isMe = isMe)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input field bar with lock check
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var textInput by remember { mutableStateOf("") }
                        
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { 
                                Text(
                                    if (isUserBlocked) "🔒 Twoje pisanie zostało zablokowane" 
                                    else "Napisz wiadomość..."
                                ) 
                            },
                            enabled = !isUserBlocked,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent
                            ),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank() && !isUserBlocked) {
                                    onSendMessage(textInput.trim(), activeChannelId)
                                    textInput = ""
                                }
                            },
                            enabled = !isUserBlocked && textInput.isNotBlank(),
                            modifier = Modifier.testTag("send_chat_button")
                        ) {
                            Surface(
                                color = if (isUserBlocked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Wyślij",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // CHANNELS AND CONTACTS DIRECTORY
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // GROUPS SECTION
                    item {
                        Text(
                            text = "Pokoje i Grupy",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 1. General channel
                    item {
                        ChannelRowItem(
                            title = "📢 Ogólny",
                            description = "Kompilowany czat dla wszystkich kursantów, instruktorów i zarządu.",
                            isActive = activeChannelId == "general",
                            onClick = {
                                activeChannelId = "general"
                                selectedTab = 0
                            }
                        )
                    }

                    // 2. Instructors channel (Zarządca + Instruktor only!)
                    if (currentUserRole == UserRole.MANAGER || currentUserRole == UserRole.INSTRUCTOR) {
                        item {
                            ChannelRowItem(
                                title = "🚗 Grupa: Instruktorzy",
                                description = "Tylko dla kadry szkoleniowej oraz zarządcy OSK.",
                                isActive = activeChannelId == "instructors",
                                onClick = {
                                    activeChannelId = "instructors"
                                    selectedTab = 0
                                }
                            )
                        }
                    }

                    // 3. Custom group channels
                    if (groupChannels.isNotEmpty()) {
                        items(groupChannels) { name ->
                            ChannelRowItem(
                                title = "👥 Grupa: $name",
                                description = "Czat grupowy utworzony przez kadrę.",
                                isActive = activeChannelId == "group_$name",
                                onClick = {
                                    activeChannelId = "group_$name"
                                    selectedTab = 0
                                }
                            )
                        }
                    }

                    // PRIVATE CONTACTS SECTION
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rozmowy Prywatne (Kontakty)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Contact Search
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Szukaj kontaktu...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Filter userKeys list based on roles and search query
                    val availableContacts = userKeys.filter { key ->
                        val cleanAssigned = cleanName(key.assignedName)
                        val nameMatches = cleanAssigned.contains(userSearchQuery, ignoreCase = true)
                        val isNotMe = cleanAssigned != cleanedCurrentName
                        val isNotRemoved = !key.isRemovedFromChat

                        if (currentUserRole == UserRole.STUDENT) {
                            // Students can ONLY see instructors and managers
                            isNotMe && isNotRemoved && nameMatches && (key.role == "Instruktor" || key.role == "Zarządca")
                        } else {
                            // Instructors and managers see everyone
                            isNotMe && isNotRemoved && nameMatches
                        }
                    }

                    if (availableContacts.isEmpty()) {
                        item {
                            Text(
                                text = "Brak dostępnych kontaktów.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Separate into sections for beautiful Material feel
                        val studentsList = availableContacts.filter { it.role == "Kursant" }
                        val staffList = availableContacts.filter { it.role != "Kursant" }

                        if (staffList.isNotEmpty()) {
                            item {
                                Text("KADRA OSK & ZARZĄD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            items(staffList) { userKey ->
                                ContactRowItem(
                                    userKey = userKey,
                                    onClick = {
                                        // Generate sorted private channel ID
                                        val sortedNames = listOf(cleanedCurrentName, cleanName(userKey.assignedName)).sorted()
                                        activeChannelId = "priv_${sortedNames[0]}___${sortedNames[1]}"
                                        selectedTab = 0
                                    }
                                )
                            }
                        }

                        if (studentsList.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("AKTYWNI KURSANCI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            items(studentsList) { userKey ->
                                ContactRowItem(
                                    userKey = userKey,
                                    onClick = {
                                        // Generate sorted private channel ID
                                        val sortedNames = listOf(cleanedCurrentName, cleanName(userKey.assignedName)).sorted()
                                        activeChannelId = "priv_${sortedNames[0]}___${sortedNames[1]}"
                                        selectedTab = 0
                                    }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // MODERATION PANEL (MANAGER ONLY)
                if (currentUserRole == UserRole.MANAGER) {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Text(
                            text = "Zarządzanie Uczestnikami Komunikatora",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Blokuj prawa wysyłania wiadomości lub usuwaj użytkowników z widoku czatów.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        var modSearchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = modSearchQuery,
                            onValueChange = { modSearchQuery = it },
                            placeholder = { Text("Filtruj użytkowników do moderacji...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        val moderateUsers = userKeys.filter {
                            cleanName(it.assignedName) != cleanedCurrentName &&
                            cleanName(it.assignedName).contains(modSearchQuery, ignoreCase = true)
                        }

                        if (moderateUsers.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Brak użytkowników do moderacji.", color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(moderateUsers) { user ->
                                    ModerationUserCard(
                                        user = user,
                                        onBlockToggle = { onUpdateBlockStatus(user.id, !user.isBlocked) },
                                        onRemoveToggle = { onUpdateRemoveStatus(user.id, !user.isRemovedFromChat) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE GROUP DIALOG
    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Utwórz nowy czat grupowy", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Wpisz nazwę grupy szkoleniowej lub tematycznej. Wszyscy uprawnieni użytkownicy będą widzieć tę grupę.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = newGroupNameInput,
                        onValueChange = { newGroupNameInput = it },
                        label = { Text("Nazwa grupy") },
                        placeholder = { Text("np. Szkolenie Teoria Kat. A") },
                        modifier = Modifier.fillMaxWidth().testTag("new_group_name_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newGroupNameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            // Send custom group initialization message
                            onSendMessage("📢 Utworzono nową grupę: $trimmed przez $currentUserName", "group_$trimmed")
                            activeChannelId = "group_$trimmed"
                            newGroupNameInput = ""
                            showCreateGroupDialog = false
                            selectedTab = 0 // Switch to Chat directly
                        }
                    },
                    enabled = newGroupNameInput.isNotBlank()
                ) {
                    Text("Utwórz")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    newGroupNameInput = ""
                    showCreateGroupDialog = false
                }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
private fun ChannelRowItem(
    title: String,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ContactRowItem(
    userKey: UserKeyEntity,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = when (userKey.role) {
                    "Zarządca" -> MaterialTheme.colorScheme.primaryContainer
                    "Instruktor" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (userKey.role == "Kursant") Icons.Default.School else Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cleanName(userKey.assignedName),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = userKey.role,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (userKey.isBlocked) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "Zablokowany",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text("Czat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ModerationUserCard(
    user: UserKeyEntity,
    onBlockToggle: () -> Unit,
    onRemoveToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = cleanName(user.assignedName),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.role,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Klucz: ${user.code}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (user.isBlocked) {
                        Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text("PISANIE BLOK", color = MaterialTheme.colorScheme.error, fontSize = 8.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                    if (user.isRemovedFromChat) {
                        Badge(containerColor = Color.Red.copy(alpha = 0.2f)) {
                            Text("BANNED", color = Color.Red, fontSize = 8.sp, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Block Button
                Button(
                    onClick = onBlockToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (user.isBlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(
                        imageVector = if (user.isBlocked) Icons.Default.CheckCircle else Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (user.isBlocked) "Odblokuj" else "Zablokuj", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Remove Button
                Button(
                    onClick = onRemoveToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isRemovedFromChat) MaterialTheme.colorScheme.secondaryContainer else Color.Red,
                        contentColor = if (user.isRemovedFromChat) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(
                        imageVector = if (user.isRemovedFromChat) Icons.Default.Refresh else Icons.Default.PersonRemove,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (user.isRemovedFromChat) "Przywróć" else "Usuń z czatu", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageEntity,
    isMe: Boolean
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    val isSystemMsg = message.senderName.startsWith("SYSTEM")

    if (isSystemMsg) {
        // Render beautiful, notice-styled notification banner for system auto-notifications
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = message.message,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = message.senderRole,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                )
            ) {
                Text(
                    text = message.message,
                    color = textColor,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
