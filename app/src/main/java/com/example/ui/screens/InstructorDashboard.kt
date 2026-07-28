package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ActiveSession
import com.example.data.database.OskProfile
import com.example.ui.OskViewModel
import com.example.ui.components.FooterCompanyQuote
import com.example.ui.components.Icon3DBadge
import com.example.ui.components.NeonLHeaderCircle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InstructorDashboard(
    viewModel: OskViewModel,
    session: ActiveSession,
    profile: OskProfile?,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val reminders by viewModel.reminders.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val groupSchedules by viewModel.groupSchedules.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    var showAddGroupScheduleDialog by remember { mutableStateOf(false) }
    var newGroupTitle by remember { mutableStateOf("") }
    var newGroupCategory by remember { mutableStateOf("B") }
    var newGroupDatesDesc by remember { mutableStateOf("Środy i Piątki w godz. 16:00 - 19:00") }
    var newGroupLocation by remember { mutableStateOf("Główna Sala Wykładowa OSK (Sala 1)") }

    // Collect dynamic instructor stats
    val instructorCars by viewModel.instructorCars.collectAsState()
    val instructorRates by viewModel.instructorRates.collectAsState()
    val instructorHours by viewModel.instructorHours.collectAsState()
    val fleetVehicles by viewModel.fleetVehicles.collectAsState()
    val vehicleFaults by viewModel.vehicleFaults.collectAsState()
    val fuelRefuels by viewModel.fuelRefuels.collectAsState()
    val odometerLogs by viewModel.odometerLogs.collectAsState()

    var faultPlateNumber by remember { mutableStateOf("KR 12345") }
    var faultDesc by remember { mutableStateOf("") }
    var faultPriority by remember { mutableStateOf("ŚREDNI") }

    // Refuel form states
    var fuelPlateNumber by remember { mutableStateOf("KR 12345") }
    var fuelLitersStr by remember { mutableStateOf("38.5") }
    var fuelCostStr by remember { mutableStateOf("249.90") }
    var fuelKmStr by remember { mutableStateOf("42550") }
    var fuelStationStr by remember { mutableStateOf("Orlen") }
    var fuelReceiptStr by remember { mutableStateOf("FV/2026/07/140") }
    var fuelTypeStr by remember { mutableStateOf("Pb95") }

    // Odometer form states
    var odoPlateNumber by remember { mutableStateOf("KR 12345") }
    var odoStartKmStr by remember { mutableStateOf("42420") }
    var odoEndKmStr by remember { mutableStateOf("42500") }
    var odoNotesStr by remember { mutableStateOf("Jazdy z kursantem 8h") }

    // Settings Dialog state for Instructor
    var showInstructorSettingsDialog by remember { mutableStateOf(false) }
    var instWorkHoursStart by remember { mutableStateOf("08:00") }
    var instWorkHoursEnd by remember { mutableStateOf("18:00") }
    var instAutoConfirmBookings by remember { mutableStateOf(true) }
    var instWeekendWorkEnabled by remember { mutableStateOf(true) }
    var instHideRatesMode by remember { mutableStateOf(false) }
    var instSmsNewBookingAlert by remember { mutableStateOf(true) }
    var instAssignedCar by remember { mutableStateOf("Toyota Yaris (KR 12345)") }

    val instructorName = session.name
    val myCar = instructorCars[instructorName] ?: "Toyota Yaris (KR 12345)"
    val myRate = instructorRates[instructorName] ?: 60
    val myHours = instructorHours[instructorName] ?: 120
    val myEarnings = myHours * myRate

    // Pre-drive checklist state
    var chkLights by remember { mutableStateOf(true) }
    var chkMirrors by remember { mutableStateOf(true) }
    var chkFluids by remember { mutableStateOf(true) }
    var chkBrakes by remember { mutableStateOf(true) }

    // Student note evaluation state
    var evalStudentName by remember { mutableStateOf("Piotr Kowalski") }
    var evalManeuver by remember { mutableStateOf("Parkowanie Równoległe Tyłem") }
    var evalScore by remember { mutableStateOf("5 / 5 ⭐") }
    var evalComment by remember { mutableStateOf("Doskonałe wyczucie gabarytów pojazdu, płynne sprzęgło.") }

    // Filter events for today & general lessons for this instructor
    val instructorEvents = calendarEvents.filter {
        it.instructorName.equals(instructorName, ignoreCase = true)
    }

    val context = LocalContext.current
    val sdfDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HIGH-END TOP BAR WITH NEON 'L' LOGO HEADER BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NeonLHeaderCircle(size = 52.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Panel Instruktora",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981)
                            ) {
                                Text(
                                    text = "NA SŁUŻBIE 🟢",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showInstructorSettingsDialog = true },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .testTag("instructor_settings_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                            .testTag("instructor_logout_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Wyloguj", tint = Color(0xFFEF4444))
                    }
                }
            }

            // CLOUD STATUS & CAR SUMMARY BANNER
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth().testTag("instructor_cloud_status_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon3DBadge(
                            icon = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            size = 36.dp,
                            iconSize = 20.dp,
                            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Auto: $myCar",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Chmura OSK: Połączono • Synchronizacja bezpłatna dla Instruktora ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "ONLINE ⚡",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // 5 REORGANIZED WORKSPACE TABS FOR INSTRUCTOR
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFF38BDF8),
                edgePadding = 0.dp,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Jazdy & Plan 🚘", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null) },
                    modifier = Modifier.testTag("tab_instructor_drives")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Moi Kursanci 🎓", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.School, contentDescription = null) },
                    modifier = Modifier.testTag("tab_instructor_students")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Pojazd & Paliwo ⛽", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.LocalGasStation, contentDescription = null) },
                    modifier = Modifier.testTag("tab_instructor_vehicle")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Wykłady 📚", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = null) },
                    modifier = Modifier.testTag("tab_instructor_lectures")
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Zarobki 📊", fontWeight = FontWeight.Bold) },
                    icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = null) },
                    modifier = Modifier.testTag("tab_instructor_earnings")
                )
            }

            // TAB 0: TODAY'S SCHEDULE & ACTIVE DRIVE WORKSPACE
            if (selectedTab == 0) {
                // ACTIVE DRIVE SESSION HERO CARD
                val currentDrive = instructorEvents.firstOrNull { it.status == "PLANNED" } ?: instructorEvents.firstOrNull()
                if (currentDrive != null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2563EB)),
                        modifier = Modifier.fillMaxWidth().testTag("active_drive_hero_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon3DBadge(
                                        icon = Icons.Default.DirectionsCar,
                                        contentDescription = "Jazda",
                                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "AKTYWNA JAZDA W TOKU",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                            color = Color(0xFF60A5FA)
                                        )
                                        Text(
                                            text = currentDrive.studentName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF10B981)
                                ) {
                                    Text(
                                        text = "W TRACIE 🚗",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Godziny:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text("${currentDrive.startTime} - ${currentDrive.endTime}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                                Column {
                                    Text("Kategoria:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text("Kat. B (Manual)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                                Column {
                                    Text("Miejsce Podstawienia:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    Text("Plac Manewrowy", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // QUICK ACTION BUTTONS FOR ACTIVE DRIVE
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Dzwonienie do kursanta ${currentDrive.studentName}... [+48 500 123 456]", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zadzwoń", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Otwieranie nawigacji GPS do punktu podstawienia auta...", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GPS Trasa", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Jazda oznaczona jako WYKONANA! Dodano +2h do karty kursanta.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zakończ ✓", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // PRE-DRIVE 30-SECOND CHECKLIST CARD
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().testTag("pre_drive_checklist_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon3DBadge(
                                icon = Icons.Default.FactCheck,
                                contentDescription = null,
                                size = 38.dp,
                                iconSize = 20.dp,
                                gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Szybka Kontrola Bezpieczeństwa Pojazdu (30s) 📋",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Obowiązkowy przegląd stanu technicznego auta przed wyruszeniem",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            FilterChip(
                                selected = chkLights,
                                onClick = { chkLights = !chkLights },
                                label = { Text("Światła & Kierunki") },
                                leadingIcon = { if (chkLights) Icon(Icons.Default.Check, contentDescription = null) }
                            )
                            FilterChip(
                                selected = chkMirrors,
                                onClick = { chkMirrors = !chkMirrors },
                                label = { Text("Lusterka & Fotele") },
                                leadingIcon = { if (chkMirrors) Icon(Icons.Default.Check, contentDescription = null) }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            FilterChip(
                                selected = chkFluids,
                                onClick = { chkFluids = !chkFluids },
                                label = { Text("Płyn do spryskiwaczy") },
                                leadingIcon = { if (chkFluids) Icon(Icons.Default.Check, contentDescription = null) }
                            )
                            FilterChip(
                                selected = chkBrakes,
                                onClick = { chkBrakes = !chkBrakes },
                                label = { Text("Hamulce & Opony") },
                                leadingIcon = { if (chkBrakes) Icon(Icons.Default.Check, contentDescription = null) }
                            )
                        }
                    }
                }

                // OWNER SCHEDULE PROPOSALS & APPROVALS
                val pendingOwnerProposals = calendarEvents.filter {
                    it.instructorName.equals(instructorName, ignoreCase = true) &&
                    it.status == "PENDING_INSTRUCTOR_APPROVAL"
                }

                if (pendingOwnerProposals.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon3DBadge(
                                    icon = Icons.Default.Event,
                                    contentDescription = null,
                                    gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Grafik od Właściciela do Akceptacji 📅",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            pendingOwnerProposals.forEach { event ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(event.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("${sdfDate.format(Date(event.date))} • ${event.startTime} - ${event.endTime}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFBFDBFE))
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.instructorApproveEvent(event.id)
                                            Toast.makeText(context, "Zaakceptowano jazdę w grafiku!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Akceptuj ✓", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: MY STUDENTS & PROGRESS TRACKER
            if (selectedTab == 1) {
                // ROSTER TITLE
                Text(
                    text = "Licznik Postępów i Karty Kursantów 🎓",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                // SAMPLE STUDENT CARDS WITH PROGRESS METERS
                val sampleStudents = listOf(
                    Triple("Piotr Kowalski", "PKK-2026/90214", 22 to 30),
                    Triple("Anna Nowak", "PKK-2026/88312", 14 to 30),
                    Triple("Michał Wiśniewski", "PKK-2026/77102", 28 to 30),
                    Triple("Katarzyna Zielińska", "PKK-2026/65410", 6 to 30)
                )

                sampleStudents.forEach { (name, pkk, hours) ->
                    val (done, total) = hours
                    val progressRatio = done.toFloat() / total.toFloat()

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth().testTag("student_progress_card_$pkk")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon3DBadge(
                                        icon = Icons.Default.Person,
                                        contentDescription = null,
                                        size = 40.dp,
                                        iconSize = 22.dp,
                                        gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(name, fontWeight = FontWeight.ExtraBold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                        Text(pkk, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (done >= 28) Color(0xFFD1FAE5) else Color(0xFFDBEAFE)
                                ) {
                                    Text(
                                        text = if (done >= 28) "GOTOWY NA EGZAMIN 🏆" else "W TRAKCIE JAZD",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (done >= 28) Color(0xFF065F46) else Color(0xFF1E40AF)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wyjechane godziny:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                Text("$done z $total godz. (${(progressRatio * 100).toInt()}%)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progressRatio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (progressRatio >= 0.9f) Color(0xFF10B981) else Color(0xFF3B82F6),
                                trackColor = Color(0xFF334155)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Ocena manewrów dla $name zapisana w arkuszu!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Oceń Manewry", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Zaplanowano nową jazdę z $name!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Text("Zaplanuj Jazdę", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // QUICK EVALUATION NOTE FORM
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon3DBadge(
                                icon = Icons.Default.EditNote,
                                contentDescription = null,
                                gradientColors = listOf(Color(0xFF059669), Color(0xFF047857))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Szybka Notatka / Ocena Manewru 📝", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = evalStudentName,
                            onValueChange = { evalStudentName = it },
                            label = { Text("Wybrany Kursant") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = evalManeuver,
                            onValueChange = { evalManeuver = it },
                            label = { Text("Manewr / Temat Lekcji") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = evalComment,
                            onValueChange = { evalComment = it },
                            label = { Text("Komentarz Instruktora") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Zapisano ocenę dla $evalStudentName!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("Zapisz w Karcie Kursanta ✓")
                        }
                    }
                }
            }

            // TAB 2: VEHICLE, FUEL & FAULTS WORKSPACE
            if (selectedTab == 2) {
                // VEHICLE STATUS CARD
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon3DBadge(
                                    icon = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Pojazd Szkoleniowy: $myCar", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Badanie techniczne ważne do: 15.11.2026", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                }
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
                                Text("SPRAWNY ✓", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF065F46))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Poziom Paliwa w Zbiorniku: 85%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { 0.85f },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF334155)
                        )
                    }
                }

                // REFUEL LOGGER FORM
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon3DBadge(
                                icon = Icons.Default.LocalGasStation,
                                contentDescription = null,
                                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Dziennik Tankowania Paliwa ⛽", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fuelLitersStr,
                                onValueChange = { fuelLitersStr = it },
                                label = { Text("Litry (L)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = fuelCostStr,
                                onValueChange = { fuelCostStr = it },
                                label = { Text("Koszt (PLN)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fuelKmStr,
                                onValueChange = { fuelKmStr = it },
                                label = { Text("Licznik KM") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = fuelStationStr,
                                onValueChange = { fuelStationStr = it },
                                label = { Text("Stacja (Orlen/BP)") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                viewModel.addFuelRefuel(
                                    plateNumber = myCar,
                                    instructorName = instructorName,
                                    kmMileage = fuelKmStr.toIntOrNull() ?: 42550,
                                    liters = fuelLitersStr.toDoubleOrNull() ?: 38.5,
                                    totalCostPln = fuelCostStr.toDoubleOrNull() ?: 249.9,
                                    fuelType = fuelTypeStr,
                                    stationName = fuelStationStr,
                                    receiptNumber = fuelReceiptStr
                                )
                                Toast.makeText(context, "Zapisano tankowanie w rozliczeniu floty OSK! ✓", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Zapisz Tankowanie ✓")
                        }
                    }
                }

                // FAULT REPORT FORM
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon3DBadge(
                                icon = Icons.Default.Warning,
                                contentDescription = null,
                                gradientColors = listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Zgłoś Usterkę Pojazdu do Mechanika ⚠️", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = faultDesc,
                            onValueChange = { faultDesc = it },
                            label = { Text("Opis usterki (np. pisk w hamulcach, żarówka H7)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (faultDesc.isNotBlank()) {
                                    viewModel.reportVehicleFault(
                                        plateNumber = myCar,
                                        instructorName = instructorName,
                                        description = faultDesc,
                                        priority = faultPriority
                                    )
                                    faultDesc = ""
                                    Toast.makeText(context, "Usterka została przekazana do właściciela i warsztatu!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Wyślij Zgłoszenie Usterki")
                        }
                    }
                }
            }

            // TAB 3: LECTURES & GROUP CLASSES WORKSPACE
            if (selectedTab == 3) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon3DBadge(
                                    icon = Icons.Default.Groups,
                                    contentDescription = null,
                                    gradientColors = listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Grupowe Wykłady Teoretyczne 📚", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { showAddGroupScheduleDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Nowy Kurs")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (groupSchedules.isEmpty()) {
                            Text("Brak aktywnych harmonogramów kursów grupowych.", color = Color(0xFF94A3B8))
                        } else {
                            groupSchedules.forEach { schedule ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(schedule.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("Dni: ${schedule.datesDescription}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                        Text("Sala: ${schedule.location}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 4: EARNINGS & SETTLEMENTS WORKSPACE
            if (selectedTab == 4) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon3DBadge(
                                    icon = Icons.Default.Payments,
                                    contentDescription = null,
                                    gradientColors = listOf(Color(0xFF10B981), Color(0xFF047857))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Rozliczenie Miesięczne Instruktora", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Stawka podstawowa: $myRate PLN / godz.", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                }
                            }

                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
                                Text("WYPŁACONO ✓", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF065F46))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Przepracowane godz.:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text("$myHours h", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                            Column {
                                Text("Razem Zarobek:", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                Text(if (instHideRatesMode) "••• PLN" else "$myEarnings PLN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black), color = Color(0xFF10B981))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Pobieranie zestawienia godzin i podsumowania płatności PDF...", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pobierz Zestawienie Godzin (PDF)")
                        }
                    }
                }
            }

            // EMERGENCY SOS BUTTON
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Szybkie Wezwanie SOS / Kolizja 🚨", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFCA5A5))
                        Text("Natychmiastowe powiadomienie biura OSK i lawety", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFCA5A5).copy(alpha = 0.8f))
                    }

                    Button(
                        onClick = {
                            viewModel.addReminder(
                                text = "🚨 SOS! Instruktor $instructorName zgłosił awarię/kolizję dla auta $myCar!",
                                priority = "HIGH",
                                senderName = instructorName,
                                senderRole = "INSTRUKTOR",
                                recipientName = "Właściciel",
                                recipientRole = "OWNER"
                            )
                            Toast.makeText(context, "ALARM SOS WYSŁANY DO BIURA OSK!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ALARM SOS 🚨", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // BRANDED FOOTER WITH COMPANY NAME AND INSPIRING QUOTE
            FooterCompanyQuote()
        }
    }

    // ADD GROUP SCHEDULE DIALOG
    if (showAddGroupScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupScheduleDialog = false },
            title = { Text("Propozycja Harmonogramu Kursu Grupowego") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newGroupTitle,
                        onValueChange = { newGroupTitle = it },
                        label = { Text("Tytuł kursu / wykładów") },
                        placeholder = { Text("np. Wykłady Teoretyczne Kat. B - Grupa Popołudniowa") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newGroupCategory,
                        onValueChange = { newGroupCategory = it },
                        label = { Text("Kategoria (A, B, C, D)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newGroupDatesDesc,
                        onValueChange = { newGroupDatesDesc = it },
                        label = { Text("Rozpisanie dni i godzin wykładów") },
                        placeholder = { Text("np. Środy i Piątki w godz. 16:00 - 19:00") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newGroupLocation,
                        onValueChange = { newGroupLocation = it },
                        label = { Text("Lokalizacja / Sala") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupTitle.isNotBlank()) {
                            val startMs = System.currentTimeMillis() + 86400000L * 3
                            val endMs = startMs + 86400000L * 20
                            viewModel.createGroupSchedule(
                                title = newGroupTitle,
                                category = newGroupCategory,
                                startDate = startMs,
                                endDate = endMs,
                                location = newGroupLocation,
                                instructorName = instructorName,
                                datesDescription = newGroupDatesDesc,
                                createdByRole = "INSTRUKTOR"
                            )
                            showAddGroupScheduleDialog = false
                            newGroupTitle = ""
                            Toast.makeText(context, "Wysłano propozycję harmonogramu do akceptacji właściciela!", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = newGroupTitle.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Wyślij do Właściciela")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupScheduleDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // INSTRUCTOR SETTINGS DIALOG
    if (showInstructorSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructorSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ustawienia Instruktora ⚙️", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Grafik & Dostępność Pracownicza 🕒", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = instWorkHoursStart,
                            onValueChange = { instWorkHoursStart = it },
                            label = { Text("Początek pracy") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = instWorkHoursEnd,
                            onValueChange = { instWorkHoursEnd = it },
                            label = { Text("Koniec pracy") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatyczna akceptacja rezerwacji", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Zatwierdzaj rezerwacje kursantów w wolnych okienkach bez pytania", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = instAutoConfirmBookings,
                            onCheckedChange = { instAutoConfirmBookings = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dostępność w Weekendy (Sob/Niedz)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Pozwól na zapisy na jazdy weekendowe", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = instWeekendWorkEnabled,
                            onCheckedChange = { instWeekendWorkEnabled = it }
                        )
                    }

                    HorizontalDivider()

                    Text("Flota & Pojazd Szkoleniowy 🚗", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = instAssignedCar,
                        onValueChange = { instAssignedCar = it },
                        label = { Text("Przypisane Auto Szkoleniowe") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    HorizontalDivider()

                    Text("Alerty & Prywatność 🛡️", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Alert SMS o nowych rezerwacjach", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Powiadamiaj SMS o zapisie lub rezygnacji kursanta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = instSmsNewBookingAlert,
                            onCheckedChange = { instSmsNewBookingAlert = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tryb Dyskretny (Ukryj Wynagrodzenie)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Ukrywa stawki PLN/h i bilans finansowy na ekranie, gdy kursant patrzy w telefon", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = instHideRatesMode,
                            onCheckedChange = { instHideRatesMode = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Ustawienia instruktora zostały zapisane! ✓", Toast.LENGTH_SHORT).show()
                        showInstructorSettingsDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz Ustawienia ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructorSettingsDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}
