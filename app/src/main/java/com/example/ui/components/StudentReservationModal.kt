package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.CategoryPriceEntity

@Composable
fun StudentReservationModal(
    defaultStudentName: String = "",
    categoryPrices: List<CategoryPriceEntity> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, dateOfBirth: String, pesel: String, phone: String, email: String, selectedDate: String, selectedTimeSlots: String, category: String, pkkNumber: String?, documentPhotoPath: String?) -> Unit
) {
    var fullName by remember { mutableStateOf(defaultStudentName.ifBlank { "Anna Wiśniewska" }) }
    var dateOfBirth by remember { mutableStateOf("2004-05-14") }
    var pesel by remember { mutableStateOf("04251408921") }
    var phone by remember { mutableStateOf("+48 601 234 567") }
    var email by remember { mutableStateOf("anna.wisniewska@osk-pro.pl") }

    // Category Choice, PKK Profile & Document Photo state
    var selectedCategory by remember { mutableStateOf("Kat. B") }
    var pkkNumber by remember { mutableStateOf("") }
    var documentPhotoPath by remember { mutableStateOf<String?>(null) }
    val categoriesList = remember { listOf("Kat. A", "Kat. B", "Kat. C", "Kat. D") }

    // Date & Time slot state
    var selectedMonthName by remember { mutableStateOf("Sierpień 2026") }
    var showMonthlyGridDialog by remember { mutableStateOf(false) }

    // Map of selected day number (1..31) -> Set of selected time slots for that day
    var selectedDaySlotsMap by remember {
        mutableStateOf(
            mapOf(
                8 to setOf("10:00", "11:00")
            )
        )
    }

    // Active day selected in calendar dialog
    var activeDayForSlots by remember { mutableStateOf(8) }

    val allTimeSlots = remember {
        listOf(
            "08:00", "09:00", "10:00", "11:00",
            "12:00", "13:00", "14:00", "15:00",
            "16:00", "17:00", "18:00", "19:00", "20:00"
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Helper functions
    fun validatePesel(p: String): Boolean = p.filter { it.isDigit() }.length == 11
    fun validateEmail(e: String): Boolean = e.contains("@") && e.contains(".")

    // Format final selected text
    val formattedDatesSummary = remember(selectedDaySlotsMap) {
        if (selectedDaySlotsMap.isEmpty()) {
            "Brak wybranego terminu"
        } else {
            selectedDaySlotsMap.entries.sortedBy { it.key }.joinToString("; ") { (day, slots) ->
                val dayStr = if (day < 10) "0$day" else "$day"
                "$dayStr Sierpnia 2026 (${slots.sorted().joinToString(", ")})"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0B0F19), // Dark Obsidian Canvas
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                2.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFFEC4899))
                )
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EventAvailable,
                                    contentDescription = "Rezerwacja",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "FORMULARZ REZERWACJI JAZD",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Wybór terminu & rejestracja zgłoszenia",
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Zamknij", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Student Details Form
                Surface(
                    color = Color(0xFF131B2E).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. DANE OSOBOWE I KONTAKTOWE KURSANTA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA855F7),
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Imię i nazwisko", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Badge, null, tint = Color(0xFF38BDF8)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reservation_input_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Date of Birth
                            OutlinedTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = { Text("Data urodzenia", color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Cake, null, tint = Color(0xFF38BDF8)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reservation_input_dob")
                            )

                            // PESEL
                            val isPeselValid = validatePesel(pesel)
                            OutlinedTextField(
                                value = pesel,
                                onValueChange = { if (it.length <= 11) pesel = it },
                                label = { Text("PESEL", color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = if (isPeselValid) Color(0xFF10B981) else Color(0xFFF87171)) },
                                trailingIcon = {
                                    if (isPeselValid) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981))
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = if (isPeselValid) Color(0xFF10B981) else Color(0xFFF87171),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reservation_input_pesel")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Phone
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Nr telefonu", color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF38BDF8)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reservation_input_phone")
                            )

                            // Email
                            val isEmailValid = validateEmail(email)
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("E-mail", color = Color.White.copy(alpha = 0.7f)) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF38BDF8)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = if (isEmailValid) Color(0xFF38BDF8) else Color(0xFFF87171),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedContainerColor = Color(0xFF0F172A),
                                    unfocusedContainerColor = Color(0xFF0F172A)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reservation_input_email")
                            )
                        }

                        // Category Selection Selector
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "WYBÓR KATEGORII PRAWA JAZDY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categoriesList.forEach { cat ->
                                val isCatSelected = selectedCategory == cat
                                val matchedPrice = categoryPrices.find { it.category == cat }
                                val priceText = matchedPrice?.let {
                                    "Kurs: ${String.format("%.0f", it.priceCoursePln)} / 1h: ${String.format("%.0f", it.priceHourPln)}"
                                } ?: "Zapytać"
                                val isCatActive = matchedPrice?.isActive ?: true

                                val catBgColor by animateColorAsState(
                                    targetValue = if (isCatSelected) Color(0xFF8B5CF6).copy(alpha = 0.3f) else Color(0xFF0F172A),
                                    label = "CatBg"
                                )
                                Surface(
                                    color = catBgColor,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (isCatSelected) 2.dp else 1.dp,
                                        color = if (isCatSelected) Color(0xFFA855F7) else Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedCategory = cat }
                                        .testTag("category_chip_$cat")
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = cat,
                                                fontWeight = if (isCatSelected) FontWeight.Black else FontWeight.Bold,
                                                color = if (isCatSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = if (isCatActive) priceText else "Pominięta",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCatActive) Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1.5: PKK & Identity Document (Optional)
                Surface(
                    color = Color(0xFF131B2E).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROFIL PKK LUB ZDJĘCIE DOKUMENTU TOŻSAMOŚCI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 0.8.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Dodaj numer PKK lub prześlij zdjęcie dokumentu. Dane te zostaną automatycznie przekazane do bazy dokumentów zarządcy OSK.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // PKK Number field
                        OutlinedTextField(
                            value = pkkNumber,
                            onValueChange = { pkkNumber = it },
                            label = { Text("Numer PKK (Wpisz 20 cyfr)", color = Color.White.copy(alpha = 0.7f)) },
                            leadingIcon = { Icon(Icons.Default.Pin, null, tint = Color(0xFF38BDF8)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reservation_input_pkk")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Camera / Photo Upload simulation
                        Text(
                            text = "Zdjęcie dokumentu (np. dowód osobisty, wniosek):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (documentPhotoPath == null) {
                            // Upload simulation button
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Simulate picking a photo
                                        documentPhotoPath = "pkk_doc_upload_${(1000..9999).random()}.jpg"
                                    }
                                    .testTag("upload_photo_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF38BDF8))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Zrób lub prześlij zdjęcie dokumentu",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        } else {
                            // Preview of simulated uploaded photo
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Dodano zdjęcie dokumentu!",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = documentPhotoPath!!,
                                                fontSize = 11.sp,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }

                                    // Remove Photo
                                    IconButton(
                                        onClick = { documentPhotoPath = null },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń zdjęcie", tint = Color(0xFFF87171))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Terminarz & Interactive Calendar Display Trigger
                Surface(
                    color = Color(0xFF131B2E).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFF87171).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Terminarz",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "2. TERMINARZ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFF87171),
                                    letterSpacing = 0.8.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            // Trigger Button for Interactive Calendar Grid
                            Button(
                                onClick = { showMonthlyGridDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("open_calendar_grid_button")
                            ) {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Otwórz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Monthly Calendar Grid embedded preview & trigger banner
                        Surface(
                            onClick = { showMonthlyGridDialog = true },
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calendar_trigger_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = null,
                                                tint = Color(0xFFF87171),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = selectedMonthName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Kliknij ikonę 'Terminarz', aby wybrać dni i godziny (08:00-20:00)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8)
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Time Slots Picker for active selected day
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GODZINY DLA DNI ($activeDayForSlots Sierpnia 2026):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Text(
                                text = "Wybrane: ${selectedDaySlotsMap[activeDayForSlots]?.size ?: 0} godz.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val activeDaySlots = selectedDaySlotsMap[activeDayForSlots] ?: emptySet()

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val chunkedSlots = allTimeSlots.chunked(4)
                            chunkedSlots.forEach { rowSlots ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowSlots.forEach { timeSlot ->
                                        val isSelected = activeDaySlots.contains(timeSlot)
                                        val slotBgColor by animateColorAsState(
                                            targetValue = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFF0F172A),
                                            animationSpec = tween(150),
                                            label = "SlotBg"
                                        )

                                        Surface(
                                            color = slotBgColor,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFF87171) else Color.White.copy(alpha = 0.15f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    val currentSet = selectedDaySlotsMap[activeDayForSlots] ?: emptySet()
                                                    val updatedSet = if (isSelected) currentSet - timeSlot else currentSet + timeSlot
                                                    selectedDaySlotsMap = if (updatedSet.isEmpty()) {
                                                        selectedDaySlotsMap - activeDayForSlots
                                                    } else {
                                                        selectedDaySlotsMap + (activeDayForSlots to updatedSet)
                                                    }
                                                }
                                                .testTag("timeslot_$timeSlot")
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 12.dp)
                                            ) {
                                                Text(
                                                    text = timeSlot,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                    
                                    val emptySlots = 4 - rowSlots.size
                                    repeat(emptySlots) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selected Summary Badge (Pale Red)
                        Surface(
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Podsumowanie wybranych terminów:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = formattedDatesSummary,
                                        fontSize = 11.sp,
                                        color = Color(0xFFFCA5A5)
                                    )
                                }
                            }
                        }
                    }
                }

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⚠️ ${validationError!!}",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button: "Zapytaj o termin"
                Surface(
                    color = Color(0xFFDC2626),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        2.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF87171), Color(0xFFEC4899), Color(0xFFA855F7))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (fullName.isBlank()) {
                                validationError = "Wpisz imię i nazwisko kursanta!"
                                return@clickable
                            }
                            if (!validatePesel(pesel)) {
                                validationError = "Nieprawidłowy numer PESEL! Wpisz 11 cyfr."
                                return@clickable
                            }
                            if (selectedDaySlotsMap.isEmpty()) {
                                validationError = "Wybierz przynajmniej jeden dzień i godzinę w terminarzu!"
                                return@clickable
                            }

                            validationError = null

                            // Package selected dates & times
                            val isMultiDay = selectedDaySlotsMap.size > 1
                            val primaryDate = if (isMultiDay) {
                                "Wiele terminów (${selectedDaySlotsMap.size} dni)"
                            } else {
                                val primaryEntry = selectedDaySlotsMap.entries.first()
                                "${if (primaryEntry.key < 10) "0${primaryEntry.key}" else primaryEntry.key}-08-2026"
                            }
                            
                            val primarySlots = if (isMultiDay) {
                                formattedDatesSummary
                            } else {
                                selectedDaySlotsMap.entries.first().value.sorted().joinToString(", ")
                            }

                            onConfirm(
                                fullName,
                                dateOfBirth,
                                pesel,
                                phone,
                                email,
                                primaryDate,
                                primarySlots,
                                selectedCategory,
                                pkkNumber.ifBlank { null },
                                documentPhotoPath
                            )
                            onDismiss()
                        }
                        .testTag("submit_reservation_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Zapytaj o termin",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ZAPYTAJ O TERMIN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )
                    }
                }
            }
        }
    }

    // Interactive Monthly Grid Dialog ("Terminarz")
    if (showMonthlyGridDialog) {
        InteractiveMonthlyGridDialog(
            monthTitle = selectedMonthName,
            selectedDaySlotsMap = selectedDaySlotsMap,
            activeDay = activeDayForSlots,
            onDayClick = { day ->
                activeDayForSlots = day
                showMonthlyGridDialog = false
            },
            onDismiss = { showMonthlyGridDialog = false }
        )
    }
}

@Composable
fun InteractiveMonthlyGridDialog(
    monthTitle: String,
    selectedDaySlotsMap: Map<Int, Set<String>>,
    activeDay: Int,
    onDayClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val weekDays = remember { listOf("Pn", "Wt", "Śr", "Czw", "Pt", "Sob", "Niedz") }
    // August 2026 starts on Saturday (5 empty slots before 1st)
    val totalDaysInMonth = 31
    val startOffsetDays = 5 // Sat is 6th column (index 5)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFFF87171)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TERMINARZ - SIATKA MIESIĘCZNA",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = monthTitle,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Zamknij", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weekday Headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekDays.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calendar Grid (7 columns)
                val totalCells = startOffsetDays + totalDaysInMonth

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    items(totalCells) { index ->
                        if (index < startOffsetDays) {
                            // Blank offset cell
                            Box(modifier = Modifier.size(38.dp))
                        } else {
                            val dayNumber = index - startOffsetDays + 1
                            val hasSlots = selectedDaySlotsMap.containsKey(dayNumber) && selectedDaySlotsMap[dayNumber]!!.isNotEmpty()
                            val isActiveDay = dayNumber == activeDay

                            // Soft Pale Red background for selected day square
                            val tileBgColor = when {
                                hasSlots -> Color(0xFFEF4444).copy(alpha = 0.35f) // Blada czerwień (pale red)
                                isActiveDay -> Color(0xFF38BDF8).copy(alpha = 0.25f)
                                else -> Color(0xFF1E293B)
                            }

                            val borderColor = when {
                                hasSlots -> Color(0xFFF87171)
                                isActiveDay -> Color(0xFF38BDF8)
                                else -> Color.White.copy(alpha = 0.1f)
                            }

                            Surface(
                                color = tileBgColor,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(if (hasSlots || isActiveDay) 2.dp else 1.dp, borderColor),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { onDayClick(dayNumber) }
                                    .testTag("calendar_day_$dayNumber")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNumber",
                                            fontSize = 13.sp,
                                            fontWeight = if (hasSlots || isActiveDay) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (hasSlots) Color.White else if (isActiveDay) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.8f)
                                        )

                                        if (hasSlots) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(Color(0xFFF87171), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFFEF4444).copy(alpha = 0.35f), RoundedCornerShape(3.dp))
                                .border(1.dp, Color(0xFFF87171), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Wybrany termin (Blada czerwień)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF1E293B), RoundedCornerShape(3.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dostępny dzień", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_calendar_grid_button")
                ) {
                    Text("Zatwierdź Wybór Dni", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}
