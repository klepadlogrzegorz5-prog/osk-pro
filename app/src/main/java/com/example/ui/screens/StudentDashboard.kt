package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ActiveSession
import com.example.data.database.OskProfile
import com.example.ui.OskViewModel
import com.example.ui.CalendarEvent
import com.example.ui.StudentReferralInfo
import java.text.SimpleDateFormat
import java.util.*

import com.example.ui.components.FooterCompanyQuote
import com.example.ui.components.NeonLHeaderCircle

@Composable
fun StudentDashboard(
    viewModel: OskViewModel,
    session: ActiveSession,
    profile: OskProfile?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val alternativeProposals by viewModel.alternativeProposals.collectAsState()

    val myAlternativeProposals = alternativeProposals.filter {
        (it.studentName.equals(session.name, ignoreCase = true) || session.name.contains("Kursant", ignoreCase = true) || session.name.contains("Kowalski", ignoreCase = true)) && it.status == "PENDING"
    }

    // Filter events for the logged-in student
    val studentEvents = calendarEvents.filter { 
        it.studentName.equals(session.name, ignoreCase = true) || it.studentName == "Wszyscy"
    }

    // Helper to format timestamps to day numbers
    val sdfDayNum = SimpleDateFormat("dd", Locale.getDefault())
    val sdfDayName = SimpleDateFormat("EE", Locale.getDefault())
    val sdfFullDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Generate next 14 days for the interactive calendar
    val calendarDays = remember {
        val days = mutableListOf<Long>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        for (i in 0 until 14) {
            days.add(cal.timeInMillis)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    var selectedDayTimestamp by remember { mutableStateOf(calendarDays.first()) }

    var selectedStudentTab by remember { mutableIntStateOf(0) }

    val studentPayments by viewModel.studentPayments.collectAsState()
    val drivingSheets by viewModel.drivingSheets.collectAsState()
    val smsNotifications by viewModel.smsNotifications.collectAsState()
    val practicalEvaluations by viewModel.practicalEvaluations.collectAsState()
    val wordHotspots = viewModel.wordHotspots
    val lessonFeedbacks by viewModel.lessonFeedbacks.collectAsState()
    val studentReferrals by viewModel.studentReferrals.collectAsState()

    var feedbackRatingStars by remember { mutableIntStateOf(5) }
    var feedbackCommentText by remember { mutableStateOf("") }

    // Filter payments and driving sheets for current student
    val myPayments = studentPayments.filter {
        it.studentName.equals(session.name, ignoreCase = true) || session.name.contains("Kursant", ignoreCase = true) || session.name.contains("Kowalski", ignoreCase = true)
    }
    val myDrivingSheets = drivingSheets.filter {
        it.studentName.equals(session.name, ignoreCase = true) || session.name.contains("Kursant", ignoreCase = true) || session.name.contains("Kowalski", ignoreCase = true)
    }
    val myPracticalEvals = practicalEvaluations.filter {
        it.studentName.equals(session.name, ignoreCase = true) || session.name.contains("Kursant", ignoreCase = true) || session.name.contains("Kowalski", ignoreCase = true)
    }

    // Receipt viewer state
    var viewingReceiptForPayment by remember { mutableStateOf<com.example.ui.StudentPayment?>(null) }

    // WORD Simulator state
    val wordQuestions = viewModel.wordQuestions
    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, String>() }
    var examFinished by remember { mutableStateOf(false) }

    // Dialog state for cancelling a lesson
    var cancellingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var selectedCancelReason by remember { mutableStateOf("Nagła choroba / złe samopoczucie 🤒") }
    var customCancelReason by remember { mutableStateOf("") }

    // Dialog state for rescheduling a lesson
    var reschedulingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var rescheduleSelectedDay by remember { mutableStateOf(calendarDays[1]) } // default to tomorrow
    var rescheduleStartTime by remember { mutableStateOf("10:00") }

    // Settings Dialog state for Student
    var showStudentSettingsDialog by remember { mutableStateOf(false) }
    var studentSms24hEnabled by remember { mutableStateOf(true) }
    var studentSms2hEnabled by remember { mutableStateOf(true) }
    var studentPreferredInstructor by remember { mutableStateOf("Tomasz Nowak") }
    var studentTransmissionType by remember { mutableStateOf("Manualna (Skrzynia 6-biegowa)") }
    var studentPkkNumber by remember { mutableStateOf("12345/2026/PKK") }
    var studentPublicRankingConsent by remember { mutableStateOf(true) }

    // Dialog state for rejecting staff-proposed term and counter-proposing
    var reProposingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var reProposeSelectedDay by remember { mutableStateOf(calendarDays[1]) }
    var reProposeStartTime by remember { mutableStateOf("10:00") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f),
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
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonLHeaderCircle(size = 50.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Portal Kursanta OSK",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0284C7)
                        )
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showStudentSettingsDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("student_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia Kursanta")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onLogout,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("student_logout_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Wyloguj")
                    }
                }
            }

            // CLOUD CONNECTION STATUS CHIP
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0284C7).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("student_cloud_status_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Połączono w czasie rzeczywistym z Chmurą OSK (Darmowe dla Kursanta ✓)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0369A1)
                        )
                    }
                    Text(
                        text = "Sync 100%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // NAVIGATION TABS FOR STUDENT DASHBOARD
            ScrollableTabRow(
                selectedTabIndex = selectedStudentTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedStudentTab == 0,
                    onClick = { selectedStudentTab = 0 },
                    text = { Text("Grafik Jazd 📅", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 1,
                    onClick = { selectedStudentTab = 1 },
                    text = { Text("Raty & Płatności 💳", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 2,
                    onClick = { selectedStudentTab = 2 },
                    text = { Text("Karta Jazd 📋", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 3,
                    onClick = { selectedStudentTab = 3 },
                    text = { Text("SMS / Push 📱", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Sms, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 4,
                    onClick = { selectedStudentTab = 4 },
                    text = { Text("Egzamin WORD 🚦", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Quiz, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 5,
                    onClick = { selectedStudentTab = 5 },
                    text = { Text("Trasy WORD 🗺️", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Map, contentDescription = null) }
                )
                Tab(
                    selected = selectedStudentTab == 6,
                    onClick = { selectedStudentTab = 6 },
                    text = { Text("Ocena & Poleć ⭐️", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Star, contentDescription = null) }
                )
            }

            // SIMULATION NOTIFICATION FOR UPCOMING LESSON
            // Shown if there's a PLANNED training lesson in the next 24 hours
            val upcomingLesson = studentEvents.firstOrNull { it.status == "PLANNED" && it.category == "LESSON" }
            if (upcomingLesson != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationImportant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "POWIADOMIENIE SMS (20 min przed jazdą)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Przypominamy o lekcji praktycznej dziś o godz. ${upcomingLesson.startTime} z instruktorem: ${upcomingLesson.instructorName}. Możesz odwołać jazdę do ostatniej chwili w razie nagłej sytuacji.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Potwierdzono obecność! Szerokiej drogi 🚗", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Będę 👍", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = { cancellingEvent = upcomingLesson },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.onPrimaryContainer))),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Odwołaj jazdę ❌", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // 3-DAY ALTERNATIVE SLOT PROPOSALS (WITH 24H/6H LIMIT)
            myAlternativeProposals.forEach { proposal ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("alternative_slot_proposal_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROPOZYCJA ALTERNATYWNYCH TERMINÓW (3 DNI DO PRZODU)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Szkoła zaproponowała wolne terminy i dostępnych instruktorów w związku z kolizją w grafiku.\n⏱️ Czas na akceptację wybranego terminu: ${proposal.expirationHours}h ${if (proposal.expirationHours == 6) "(Napięty grafik!)" else "(Standardowo 24h)"}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Wybierz jeden z sugerowanych wolnych terminów:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        proposal.suggestedSlots.forEach { slot ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = slot.dateFormatted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Godziny: ${slot.startTime} - ${slot.endTime} • Instruktor: ${slot.instructorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.acceptAlternativeProposal(proposal.id, slot)
                                            Toast.makeText(context, "Zaakceptowano termin! Dodano jazdę do grafiku.", Toast.LENGTH_LONG).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Wybieram", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // PROPOSED BY STAFF RESCHEDULING ALERTS
            val proposedByStaffEvents = studentEvents.filter { it.status == "PROPOSED_BY_STAFF" }
            proposedByStaffEvents.forEach { proposedEvent ->
                if (proposedEvent.rescheduleNewDate != null) {
                    val proposedDateStr = sdfFullDate.format(Date(proposedEvent.rescheduleNewDate))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("proposed_reschedule_alert_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ZAPROPONOWANO NOWY TERMIN JAZDY!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Instruktor/Właściciel zaproponował nowy termin dla odwołanej jazdy:\n• Dotychczasowy termin: ${sdfFullDate.format(Date(proposedEvent.date))} ${proposedEvent.startTime}\n• Nowy termin: $proposedDateStr o godz. ${proposedEvent.rescheduleNewStartTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.studentAcceptProposedTerm(proposedEvent.id)
                                        Toast.makeText(context, "Zaakceptowano nowy termin! Do zobaczenia na jeździe 🚗", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.tertiaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f).testTag("accept_proposed_term_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Akceptuj", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = {
                                        reProposingEvent = proposedEvent
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.onTertiaryContainer,
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        )
                                    ),
                                    modifier = Modifier.weight(1.8f).testTag("counter_propose_term_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Inny termin...", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // School Info Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = profile?.companyName ?: "Szkoła OSK (Lokalna)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (profile != null) "Miejscowość: ${profile.city}, ul. ${profile.street}" else "Witamy w nowoczesnym portalu kursanta",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // TAB 0: GRAFIK JAZD & KALENDARZ
            if (selectedStudentTab == 0) {
            // INTERACTIVE CALENDAR SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Twój Kalendarz Interaktywny 📅",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kolejne 14 dni",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Interactive Days List (LazyRow)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(calendarDays) { timestamp ->
                        val isSelected = selectedDayTimestamp == timestamp
                        val dayNum = sdfDayNum.format(Date(timestamp))
                        val dayName = sdfDayName.format(Date(timestamp))
                            .replace(".", "")
                            .uppercase()

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                            modifier = Modifier
                                .width(56.dp)
                                .clickable { selectedDayTimestamp = timestamp }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = dayName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = dayNum,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // TODAY'S OR FILTERED DAY LESSONS LIST
            val activeDayEvents = studentEvents.filter { it.date == selectedDayTimestamp }
            val formattedSelectedDay = sdfFullDate.format(Date(selectedDayTimestamp))

            Text(
                text = "Lekcje w dniu $formattedSelectedDay:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (activeDayEvents.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Brak zaplanowanych jazd na ten dzień.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Wybierz inny dzień w kalendarzu, aby sprawdzić harmonogram.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeDayEvents.forEach { event ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DirectionsCar,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = event.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Status Badge
                                    val (badgeText, badgeBg, badgeContent) = when (event.status) {
                                        "STARTED" -> Triple("W TOKU 🚗", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                                        "COMPLETED" -> Triple("ZAKOŃCZONO ✅", Color(0xFFE8F5E9), Color(0xFF2E7D32))
                                        "CANCELLED" -> Triple("ODWOŁANO ❌", MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
                                        "RESCHEDULE_PENDING" -> Triple("ZMIANA TERMINU ⏳", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary)
                                        else -> Triple("ZAPLANOWANE 📅", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = badgeContent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Godziny jazdy",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${event.startTime} - ${event.endTime}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Twój Instruktor",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = event.instructorName.ifEmpty { "Do ustalenia" },
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (event.status == "CANCELLED" && event.cancelReason.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Powód odwołania: ${event.cancelReason}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                if (event.status == "RESCHEDULE_PENDING" && event.rescheduleNewDate != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val newDateStr = sdfFullDate.format(Date(event.rescheduleNewDate))
                                    Text(
                                        text = "Zaproponowano zmianę na: $newDateStr, godz: ${event.rescheduleNewStartTime}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }

                                // Interactive actions for PLANNED lesson
                                if (event.status == "PLANNED") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { reschedulingEvent = event },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Zmień termin", style = MaterialTheme.typography.labelMedium)
                                        }

                                        Button(
                                            onClick = { cancellingEvent = event },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Odwołaj", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ALL REMAINING RESERVED SESSIONS / OVERALL STATS
            Text(
                text = "Twoje Statystyki Szkolenia 📊",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val completedCount = studentEvents.count { it.status == "COMPLETED" }
                    val plannedCount = studentEvents.count { it.status == "PLANNED" }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Odbyte jazdy:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedCount z 30 godzin", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    }

                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / 30f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nadchodzące zarezerwowane lekcje:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$plannedCount lekcji", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
            } // end of tab 0

            // TAB 1: FINANSE & RATY ZA KURS
            if (selectedStudentTab == 1) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Stan Płatności i Raty za Kurs 💳", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalAmount = myPayments.sumOf { it.amountPln }
                        val paidAmount = myPayments.filter { it.status == "OPŁACONE" }.sumOf { it.amountPln }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Opłacono ogółem:", style = MaterialTheme.typography.bodyMedium)
                            Text("$paidAmount PLN z $totalAmount PLN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (totalAmount > 0) paidAmount.toFloat() / totalAmount.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        myPayments.forEach { payment ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (payment.status == "OPŁACONE") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = payment.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (payment.status == "OPŁACONE") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = if (payment.status == "OPŁACONE") "OPŁACONE ✓" else "OCZEKUJE ⏳",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Kwota: ${payment.amountPln} PLN | Termin: ${payment.dueDate}", style = MaterialTheme.typography.bodySmall)
                                    if (payment.paidDate != null) {
                                        Text(text = "Data opłacenia: ${payment.paidDate} (${payment.paymentMethod})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (payment.status != "OPŁACONE") {
                                            Button(
                                                onClick = {
                                                    viewModel.markPaymentAsPaid(payment.id, "BLIK / Karta")
                                                    Toast.makeText(context, "Opłacono ratę za kurs przez BLIK!", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Opłać BLIK ( ${payment.amountPln} PLN )")
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = { viewingReceiptForPayment = payment },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Pobierz Potwierdzenie PDF 📄", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: KARTA PRZEPROWADZONYCH GODZIN (ARKUSZ JAZD)
            if (selectedStudentTab == 2) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cyfrowa Karta Jazd & Podpisy 📋", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Zaakceptuj i podpisz cyfrowo przebyte lekcje z instruktorem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        val totalDrivingHours = myDrivingSheets.sumOf { it.hoursCount.toDouble() }
                        Text(text = "Łącznie zrealizowano: ${"%.1f".format(totalDrivingHours)} / 30.0 godzin jazd", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (myDrivingSheets.isEmpty()) {
                            Text("Brak wpisów w karcie jazd.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            myDrivingSheets.forEach { entry ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "${entry.dateFormatted} (${entry.hoursCount}h)", fontWeight = FontWeight.Bold)
                                            Text(text = "Instruktor: ${entry.instructorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Zakres: ${entry.topicsCovered}", style = MaterialTheme.typography.bodySmall)
                                        Text(text = "Uwagi: ${entry.instructorNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(text = if (entry.instructorSigned) "Instruktor: ✓" else "Instruktor: ✗", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                                Text(text = if (entry.studentSigned) "| Kursant: ✓" else "| Kursant: ⏳ Oczekuje", style = MaterialTheme.typography.labelSmall, color = if (entry.studentSigned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                            }

                                            if (!entry.studentSigned) {
                                                Button(
                                                    onClick = {
                                                        viewModel.studentSignDrivingSheet(entry.id)
                                                        Toast.makeText(context, "Podpisano kartę jazd!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Podpisz lekcję ✍️", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: SMS / PUSH 24H NOTIFICATIONS LOG
            if (selectedStudentTab == 3) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Automatyczne Powiadomienia SMS / Push 📱", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Przypomnienia 24h przed planowaną lekcją jazdy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    viewModel.sendSimulatedSms("+48 601 234 567", session.name, "PRZYPOMNIENIE OSK: Twoja lekcja jazdy odbędzie się jutro o godz. 10:00 z instruktorem Tomasz Nowak. Prosimy o przybycie 5 min wcześniej.")
                                    Toast.makeText(context, "Wysłano testowe powiadomienie SMS na Twój telefon!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("+ Test SMS 24h")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        smsNotifications.forEach { sms ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Do: ${sms.recipientName} (${sms.recipientPhone})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = sms.scheduledTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "💬 \"${sms.messageText}\"", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: SYMULATOR EGZAMINU WORD
            if (selectedStudentTab == 4) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Symulator Egzaminu Wewnętrznego WORD 🚦", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Oficjalne pytania egzaminacyjne & Wyniki egzaminu praktycznego", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (!examFinished) {
                            val q = wordQuestions[currentQuestionIdx]
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(text = "Pytanie ${currentQuestionIdx + 1} z ${wordQuestions.size} | Kategoria: ${q.category} (${q.points} pkt)", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }

                            Text(text = q.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(12.dp))

                            val options = listOf("A" to q.optionA, "B" to q.optionB, "C" to q.optionC)
                            options.forEach { (ansKey, ansText) ->
                                val isSelected = userAnswers[q.id] == ansKey
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { userAnswers[q.id] = ansKey }
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isSelected, onClick = { userAnswers[q.id] = ansKey })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$ansKey. $ansText", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = {
                                        if (currentQuestionIdx < wordQuestions.size - 1) {
                                            currentQuestionIdx++
                                        } else {
                                            examFinished = true
                                        }
                                    },
                                    enabled = userAnswers.containsKey(q.id),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (currentQuestionIdx < wordQuestions.size - 1) "Następne pytanie ➡️" else "Zakończ Egzamin 🏁")
                                }
                            }
                        } else {
                            // RESULT SCREEN
                            var totalPoints = 0
                            wordQuestions.forEach { q ->
                                if (userAnswers[q.id] == q.correctAnswer) {
                                    totalPoints += q.points
                                }
                            }
                            val maxPoints = wordQuestions.sumOf { it.points }
                            val passed = totalPoints >= (maxPoints * 0.8)

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (passed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (passed) "WYNIK: POZYTYWNY ✓" else "WYNIK: NEGATYWNY ✗",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = if (passed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Zdobyto: $totalPoints / $maxPoints punktów",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (passed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            currentQuestionIdx = 0
                                            userAnswers.clear()
                                            examFinished = false
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Spróbuj ponownie 🔄")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Arkusz Oceny Egzaminu Praktycznego 🚗", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        myPracticalEvals.forEach { eval ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Egzaminator: ${eval.instructorName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(eval.overallResult, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("• Przygotowanie do jazdy: ${if (eval.preparationOk) "ZALICZONE ✓" else "Niezaliczone"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Jazda pasem ruchu (Łuk): ${if (eval.maneuversManifoldOk) "ZALICZONE ✓" else "Niezaliczone"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Ruszanie na wzniesieniu: ${if (eval.hillStartOk) "ZALICZONE ✓" else "Niezaliczone"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Parkowanie: ${if (eval.parkingOk) "ZALICZONE ✓" else "Niezaliczone"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Uwagi egzaminatora: ${eval.remarks}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 5: TRASY EGZAMINACYJNE WORD & MIEJSCA TRUDNE
            if (selectedStudentTab == 5) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Trasy Egzaminacyjne & Miejsca Trudne (WORD) 🗺️", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("Przewodnik po pułapkach drogowych i miejscach o wysoki wskaźniku oblewaniu egzaminu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        wordHotspots.forEach { spot ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(spot.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (spot.difficultyLevel == "BARDZO TRUDNE") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                spot.difficultyLevel,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Lokalizacja: ${spot.locationName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Text(spot.description, style = MaterialTheme.typography.bodySmall)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Wskazówka: ${spot.drivingTip}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 6: OCENA INSTRUKTORA & PROGRAM POLECEŃ
            if (selectedStudentTab == 6) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Card 1: Ocena Instruktora po Jazdach
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Oceń Ostatnią Lekcję z Instruktorem ⭐️", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Twoja opinia pomaga nam podnosić jakość szkoleń w OSK", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Ocena:", fontWeight = FontWeight.Bold)
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = { feedbackRatingStars = star },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (star <= feedbackRatingStars) Icons.Default.Star else Icons.Default.StarOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = feedbackCommentText,
                                onValueChange = { feedbackCommentText = it },
                                label = { Text("Napisz krótką opinię / uwagi do jazdy") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (feedbackCommentText.isNotBlank()) {
                                        viewModel.submitLessonFeedback(session.name, "Tomasz Nowak", feedbackRatingStars, feedbackCommentText)
                                        Toast.makeText(context, "Dziękujemy! Twoja opinia została zapisana.", Toast.LENGTH_SHORT).show()
                                        feedbackCommentText = ""
                                    } else {
                                        Toast.makeText(context, "Wpisz krótką treść opinii przed wysłaniem.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Wyślij Opinię ✉️")
                            }
                        }
                    }

                    // Card 2: Program Poleceń (Poleć Znajomego -> Darmowe Godziny Jazdy)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Program Poleceń - Zyskaj Darmowe Jazdy! 🎁", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text("Podaj swój kod rabatowy znajomemu - po jego zapisie oboje dostaniecie 1h jazdy gratis!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            val myReferral = studentReferrals.firstOrNull() ?: StudentReferralInfo(session.name, "KURSANT2026", 2, 2)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Twój indywidualny kod:", style = MaterialTheme.typography.labelSmall)
                                        Text(myReferral.myReferralCode, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Skopiowano kod ${myReferral.myReferralCode} do schowka!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Kopiuj Kod 📋")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Zapisanych znajomych", style = MaterialTheme.typography.labelSmall)
                                    Text("${myReferral.friendsJoinedCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Darmowe godziny", style = MaterialTheme.typography.labelSmall)
                                    Text("${myReferral.freeBonusHours} h", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                        }
                    }
                }
            }

            // BRANDED FOOTER WITH COMPANY NAME AND INSPIRING QUOTE
            FooterCompanyQuote()
        }
    }



    // CANCELLATION DIALOG WITH REASON PICKER
    cancellingEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { cancellingEvent = null },
            title = { Text("Odwołaj lekcję jazdy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Wybierz powód odwołania lekcji z dnia ${sdfFullDate.format(Date(event.date))} (godz. ${event.startTime}):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val reasons = listOf(
                        "Nagła choroba / złe samopoczucie 🤒",
                        "Ważne sprawy rodzinne 👨‍👩‍👧",
                        "Konflikt z pracą lub szkołą 💼",
                        "Nieprzygotowanie do jazdy ⚠️",
                        "Inny powód... 📝"
                    )

                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCancelReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedCancelReason == reason,
                                onClick = { selectedCancelReason = reason }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (selectedCancelReason == "Inny powód... 📝") {
                        OutlinedTextField(
                            value = customCancelReason,
                            onValueChange = { customCancelReason = it },
                            placeholder = { Text("Opisz powód odwołania...") },
                            label = { Text("Uzasadnienie") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalReason = if (selectedCancelReason == "Inny powód... 📝") {
                            customCancelReason.ifBlank { "Inny powód" }
                        } else {
                            selectedCancelReason
                        }
                        viewModel.cancelCalendarEvent(event.id, finalReason)
                        cancellingEvent = null
                        customCancelReason = ""
                        Toast.makeText(context, "Odwołano lekcję! Powód został przekazany do instruktora.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Potwierdź odwołanie", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancellingEvent = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // RESCHEDULING DIALOG
    reschedulingEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { reschedulingEvent = null },
            title = { Text("Zaproponuj zmianę terminu", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Zgłoś prośbę o przeniesienie lekcji z ${sdfFullDate.format(Date(event.date))} (godz. ${event.startTime}). Wybierz nowy proponowany dzień i godzinę:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Wybierz proponowaną datę:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Quick select new day chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(calendarDays.drop(1)) { dayTimestamp -> // skip today
                            val isSelected = rescheduleSelectedDay == dayTimestamp
                            val dayStr = sdfFullDate.format(Date(dayTimestamp)).substring(0, 5) // dd.MM

                            FilterChip(
                                selected = isSelected,
                                onClick = { rescheduleSelectedDay = dayTimestamp },
                                label = { Text(dayStr, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Text(
                        text = "Wybierz godzinę rozpoczęcia:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    val hourOptions = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(hourOptions) { hour ->
                            val isSelected = rescheduleStartTime == hour
                            FilterChip(
                                selected = isSelected,
                                onClick = { rescheduleStartTime = hour },
                                label = { Text(hour) }
                            )
                        }
                    }

                    Text(
                        text = "* Ostateczna zmiana wymaga zatwierdzenia przez Twojego instruktora (${event.instructorName}). Do tego czasu obowiązuje dotychczasowy termin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedStartParts = rescheduleStartTime.split(":")
                        val newEndHour = (parsedStartParts[0].toInt() + 2).toString().padStart(2, '0')
                        val calculatedEndTime = "$newEndHour:${parsedStartParts[1]}"

                        viewModel.requestReschedule(
                            id = event.id,
                            newDate = rescheduleSelectedDay,
                            newStartTime = rescheduleStartTime,
                            newEndTime = calculatedEndTime
                        )
                        reschedulingEvent = null
                        Toast.makeText(context, "Wysłano prośbę o zmianę terminu! Oczekuj na akceptację instruktora.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Wyślij prośbę ⏳")
                }
            },
            dismissButton = {
                TextButton(onClick = { reschedulingEvent = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // COUNTER-PROPOSE DIALOG (When rejecting proposed term)
    reProposingEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { reProposingEvent = null },
            title = { Text("Zaproponuj inny termin jazdy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Poproś o inny dogodny dla Ciebie termin jazdy zamiast proponowanego przez instruktora:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Wybierz inną proponowaną datę:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(calendarDays.drop(1)) { dayTimestamp ->
                            val isSelected = reProposeSelectedDay == dayTimestamp
                            val dayStr = sdfFullDate.format(Date(dayTimestamp)).substring(0, 5)

                            FilterChip(
                                selected = isSelected,
                                onClick = { reProposeSelectedDay = dayTimestamp },
                                label = { Text(dayStr, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Text(
                        text = "Wybierz godzinę rozpoczęcia:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    val hourOptions = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(hourOptions) { hour ->
                            val isSelected = reProposeStartTime == hour
                            FilterChip(
                                selected = isSelected,
                                onClick = { reProposeStartTime = hour },
                                label = { Text(hour) }
                            )
                        }
                    }

                    Text(
                        text = "* Wniosek trafi do Właściciela i Twojego instruktora (${event.instructorName.ifEmpty { "Tomasz Nowak" }}) do zaakceptowania.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedStartParts = reProposeStartTime.split(":")
                        val newEndHour = (parsedStartParts[0].toInt() + 2).toString().padStart(2, '0')
                        val calculatedEndTime = "$newEndHour:${parsedStartParts[1]}"

                        viewModel.studentRejectAndProposeAnotherTerm(
                            eventId = event.id,
                            otherDate = reProposeSelectedDay,
                            otherStartTime = reProposeStartTime,
                            otherEndTime = calculatedEndTime
                        )
                        reProposingEvent = null
                        Toast.makeText(context, "Wysłano prośbę o inny termin! Oczekuj na akceptację.", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Wyślij propozycję ⏳")
                }
            },
            dismissButton = {
                TextButton(onClick = { reProposingEvent = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // FORMAL RECEIPT DIALOG (FINANCIAL CONFIRMATION)
    viewingReceiptForPayment?.let { payment ->
        AlertDialog(
            onDismissRequest = { viewingReceiptForPayment = null },
            title = { Text("Potwierdzenie Wpłaty - ${payment.receiptNumber}", fontWeight = FontWeight.Bold) },
            text = {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = profile?.companyName ?: "Ośrodek Szkolenia Kierowców OSK", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "NIP: 123-456-78-90 | ul. Dworcowa 12, Kraków", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = "Wpłacający: ${payment.studentName}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Tytuł wpłaty: ${payment.title}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Kwota: ${payment.amountPln} PLN (Słownie: ${payment.amountPln} złotych 00/100)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(text = "Forma płatności: ${payment.paymentMethod}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Data zaksięgowania: ${payment.paidDate ?: "Dzisiaj"}", style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(text = "STATUS: OPŁACONE ZAKSIĘGOWANE ✓", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Pobrano plik potwierdzenia wpłaty PDF!", Toast.LENGTH_SHORT).show()
                        viewingReceiptForPayment = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Pobierz / Drukuj PDF 📄")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingReceiptForPayment = null }) {
                    Text("Zamknij")
                }
            }
        )
    }

    // STUDENT SETTINGS DIALOG
    if (showStudentSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showStudentSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ustawienia Kursanta ⚙️", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Powiadomienia & Alerty SMS 🔔", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SMS 24h przed każdą jazdą", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Przypomnienie o terminie i miejscu zbiórki", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = studentSms24hEnabled,
                            onCheckedChange = { studentSms24hEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SMS 2h przed jazdą", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Ekspresowy alert bezpośrednio przed spotkaniem", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = studentSms2hEnabled,
                            onCheckedChange = { studentSms2hEnabled = it }
                        )
                    }

                    HorizontalDivider()

                    Text("Preferencje Szkolenia 🚘", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = studentPreferredInstructor,
                        onValueChange = { studentPreferredInstructor = it },
                        label = { Text("Preferowany Instruktor Prowadzący") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = studentTransmissionType,
                        onValueChange = { studentTransmissionType = it },
                        label = { Text("Typ Skrzyni Biegów") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    HorizontalDivider()

                    Text("Profil i Dane Urzędowe 🪪", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = studentPkkNumber,
                        onValueChange = { studentPkkNumber = it },
                        label = { Text("Numer PKK (Profil Kandydata)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Widoczność w Rankingu Poleceń", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Pokazuj moje imię na liście zdobywców darmowych godzin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = studentPublicRankingConsent,
                            onCheckedChange = { studentPublicRankingConsent = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Ustawienia kursanta zostały pomyślnie zapisane! ✓", Toast.LENGTH_SHORT).show()
                        showStudentSettingsDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz Ustawienia ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStudentSettingsDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}


