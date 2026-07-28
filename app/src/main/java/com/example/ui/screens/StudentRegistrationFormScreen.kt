package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ActiveSession
import com.example.ui.OskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationFormScreen(
    viewModel: OskViewModel,
    session: ActiveSession,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(session.name) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pkkNumber by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("B") }
    var preferredInstructor by remember { mutableStateOf("") } // "" = Any
    var slotChoice by remember { mutableStateOf("EXISTING") } // "EXISTING", "CUSTOM", "TIMEFRAME"
    
    // Price list from ViewModel
    val coursePrices by viewModel.coursePrices.collectAsState()
    val availableInstructors by viewModel.instructorCars.collectAsState()
    
    // For EXISTING choice: list of free events filtered by preferred instructor
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val freeEvents = calendarEvents.filter { evt ->
        evt.studentName.isEmpty() && evt.status == "PLANNED" &&
        (preferredInstructor.isBlank() || evt.instructorName.equals(preferredInstructor, ignoreCase = true))
    }
    var selectedEventId by remember { mutableStateOf<String?>(null) }
    
    // For CUSTOM choice:
    var customDateString by remember { mutableStateOf("") }
    var customTime by remember { mutableStateOf("10:00") }

    // For TIMEFRAME choice:
    var timeframeStartStr by remember { mutableStateOf("01.10.2026") }
    var timeframeEndStr by remember { mutableStateOf("17.10.2026") }
    var timeframeNote by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    // Set first available free event as default selection if any
    LaunchedEffect(freeEvents) {
        if (selectedEventId == null && freeEvents.isNotEmpty()) {
            selectedEventId = freeEvents.first().id
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Wyloguj", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Karta Rejestracji Kursanta",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Witaj w OSK! 👋",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Aby rozpocząć szkolenie praktyczne, wypełnij poniższy formularz i wybierz termin pierwszej jazdy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Card / Guide
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Twój kod dostępu: ${session.codeUsed}. Zostanie on na stałe przypisany do Twojego profilu.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Personal info
            Text(
                text = "1. Dane Osobowe 👤",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Imię i Nazwisko") },
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Adres E-mail") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Numer Telefonu") },
                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pkkNumber,
                onValueChange = { pkkNumber = it },
                label = { Text("Numer PKK / Profil Kierowcy (np. 12345/67890/12345)") },
                leadingIcon = { Icon(imageVector = Icons.Default.Badge, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step 2: Cennik kursów OSK
            Text(
                text = "2. Cennik Kursów i Oferta OSK 🏷️",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    coursePrices.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.categoryName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.theoryHours}h teoria • ${item.practiceHours}h praktyka • ${item.description}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${item.pricePln} PLN",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 3: Category
            Text(
                text = "3. Wybierz Kategorię Prawa Jazdy 🚗",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("A", "B", "C", "D").forEach { category ->
                    val selected = selectedCategory == category
                    Card(
                        onClick = { selectedCategory = category },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Kat. $category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 4: Dedykowany Instruktor
            Text(
                text = "4. Preferowany Instruktor (Jazda z jednym instruktorem) 👨‍🏫",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val instructorsList = listOf("" to "Bez preferencji (Dowolny)") + availableInstructors.keys.map { it to it }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                instructorsList.forEach { (instKey, instLabel) ->
                    val selected = preferredInstructor == instKey
                    FilterChip(
                        selected = selected,
                        onClick = { preferredInstructor = instKey },
                        label = { Text(text = instLabel, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (selected) { { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) } } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 5: First lesson slot choice
            Text(
                text = "5. Wybór Pierwszej Jazdy / Ramy Czasowe 📅",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Choice mode chips (3 options: EXISTING, CUSTOM, TIMEFRAME)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    onClick = { slotChoice = "EXISTING" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (slotChoice == "EXISTING") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = if (slotChoice == "EXISTING") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Wolne terminy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text("Instruktora", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Card(
                    onClick = { slotChoice = "CUSTOM" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (slotChoice == "CUSTOM") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, tint = if (slotChoice == "CUSTOM") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Pojedynczy termin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text("Własna data", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Card(
                    onClick = { slotChoice = "TIMEFRAME" },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (slotChoice == "TIMEFRAME") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = if (slotChoice == "TIMEFRAME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ramy czasowe", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        Text("Zakres dat", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (slotChoice == "EXISTING") {
                Text(
                    text = if (preferredInstructor.isNotBlank()) "Wolne terminy instruktora: $preferredInstructor" else "Wszystkie wolne terminy w kalendarzu:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (freeEvents.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Brak wolnych terminów dla wybranego instruktora. Wybierz opcję 'Ramy czasowe' lub 'Pojedynczy termin'.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    freeEvents.forEach { event ->
                        val selected = selectedEventId == event.id
                        Card(
                            onClick = { selectedEventId = event.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { selectedEventId = event.id })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = event.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = sdf.format(Date(event.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${event.startTime} - ${event.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(text = "Instruktor: ${event.instructorName}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            } else if (slotChoice == "CUSTOM") {
                Text(
                    text = "Zaproponuj własną datę i godzinę (wymaga potwierdzenia):",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = customDateString,
                    onValueChange = { customDateString = it },
                    label = { Text("Data jazdy (np. 15.08.2026)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customTime,
                    onValueChange = { customTime = it },
                    label = { Text("Godzina rozpoczęcia (np. 10:00)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.AccessTime, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // TIMEFRAME RANGE REQUEST
                Text(
                    text = "Podaj ramy czasowe na dopasowanie jazd przez szkołę:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeframeStartStr,
                        onValueChange = { timeframeStartStr = it },
                        label = { Text("Od daty (np. 01.10.2026)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeframeEndStr,
                        onValueChange = { timeframeEndStr = it },
                        label = { Text("Do daty (np. 17.10.2026)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.DateRange, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = timeframeNote,
                    onValueChange = { timeframeNote = it },
                    label = { Text("Preferowane godziny / uwagi (np. Po 16:00, weekendy)") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Notes, contentDescription = null) },
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "ℹ️ Właściciel lub instruktor mają 3 dni robocze na zaakceptowanie, nadanie poprawek lub Zaproponowanie innego rozwiązania.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || phone.isBlank()) return@Button
                    
                    var customDateMs: Long? = null
                    var customEndTime: String? = null
                    var tfStartMs: Long? = null
                    var tfEndMs: Long? = null
                    
                    if (slotChoice == "CUSTOM") {
                        try {
                            customDateMs = sdf.parse(customDateString.trim())?.time
                        } catch (e: Exception) {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, 2)
                            customDateMs = cal.timeInMillis
                        }
                        
                        val startHourStr = customTime.substringBefore(":")
                        val startMinStr = customTime.substringAfter(":", "00")
                        val startHour = startHourStr.toIntOrNull() ?: 10
                        val endHour = (startHour + 2) % 24
                        customEndTime = String.format(Locale.getDefault(), "%02d:%s", endHour, startMinStr)
                    } else if (slotChoice == "TIMEFRAME") {
                        try {
                            tfStartMs = sdf.parse(timeframeStartStr.trim())?.time
                            tfEndMs = sdf.parse(timeframeEndStr.trim())?.time
                        } catch (e: Exception) {
                            tfStartMs = System.currentTimeMillis()
                        }
                    }

                    viewModel.registerStudentSelf(
                        name = name,
                        email = email,
                        phone = phone,
                        category = selectedCategory,
                        pkkNumber = pkkNumber,
                        slotChoice = slotChoice,
                        selectedEventId = if (slotChoice == "EXISTING") selectedEventId else null,
                        customDate = customDateMs,
                        customStartTime = if (slotChoice == "CUSTOM") customTime else null,
                        customEndTime = customEndTime,
                        timeframeStart = tfStartMs,
                        timeframeEnd = tfEndMs,
                        timeframeNote = timeframeNote,
                        preferredInstructor = preferredInstructor,
                        accessCode = session.codeUsed ?: "BYPASS"
                    )
                },
                enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() &&
                        (slotChoice == "EXISTING" && selectedEventId != null || slotChoice == "CUSTOM" && customDateString.isNotBlank() && customTime.isNotBlank() || slotChoice == "TIMEFRAME" && timeframeStartStr.isNotBlank()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_registration_button")
            ) {
                Icon(imageVector = Icons.Default.HowToReg, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zatwierdź i Wyślij Zgłoszenie", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
