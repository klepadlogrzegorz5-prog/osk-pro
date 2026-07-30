package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.LessonEntity
import com.example.data.db.ReservationEntity
import com.example.ui.components.EmptyState
import com.example.ui.viewmodel.OskViewModel
import com.example.ui.viewmodel.UserRole
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * CONTROL FLAG FOR SIMULATION CONTROLS
 * Set to [true] to show the simulation/test panel.
 * Set to [false] or delete this block of code completely to remove the simulation from the final app.
 */
private const val SHOW_TEST_SIMULATION_PANEL = true

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: OskViewModel,
    currentRole: UserRole,
    userName: String,
    lessons: List<LessonEntity>,
    onBookLessonClick: () -> Unit,
    onUpdateLessonStatus: (Long, String) -> Unit,
    onDeleteLesson: (Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedFilter by remember { mutableStateOf("WSZYSTKIE") }
    var selectedDateFilter by remember { mutableStateOf<String?>(null) } // "yyyy-MM-dd"
    var isSimPanelExpanded by remember { mutableStateOf(true) }

    // 1. GDPR Role-based filtration (Privacy & Tenant isolation)
    val roleFilteredLessons = remember(lessons, currentRole, userName) {
        when (currentRole) {
            UserRole.STUDENT -> {
                lessons.filter { it.studentName.trim().equals(userName.trim(), ignoreCase = true) }
            }
            UserRole.INSTRUCTOR -> {
                lessons.filter { it.instructorName.trim().equals(userName.trim(), ignoreCase = true) }
            }
            UserRole.MANAGER -> {
                lessons // Manager is administrator - can see all schedules
            }
        }
    }

    // 2. Date-based filtration
    val dateFilteredLessons = remember(roleFilteredLessons, selectedDateFilter) {
        if (selectedDateFilter == null) {
            roleFilteredLessons
        } else {
            roleFilteredLessons.filter { it.date == selectedDateFilter }
        }
    }

    // 3. Status-based filtration
    val finalFilteredList = remember(dateFilteredLessons, selectedFilter) {
        when (selectedFilter) {
            "ZAPLANOWANE" -> dateFilteredLessons.filter { it.status == "Zaplanowana" }
            "ZREALIZOWANE" -> dateFilteredLessons.filter { it.status == "Zrealizowana" }
            "ANULOWANE" -> dateFilteredLessons.filter { it.status == "Anulowana" }
            else -> dateFilteredLessons
        }
    }

    // Generate Calendar Days (3 days ago, today, next 10 days)
    val calendarDays = remember {
        val list = mutableListOf<Pair<String, Triple<String, String, String>>>() // dbDate to (dayName, dayNum, monthNum)
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -3) // Start 3 days ago to show historic lessons
        val sdfDbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDayName = SimpleDateFormat("EEE", Locale("pl"))
        val sdfDayNum = SimpleDateFormat("dd", Locale("pl"))
        val sdfMonthNum = SimpleDateFormat("MM", Locale("pl"))

        for (i in 0 until 14) {
            val dbDate = sdfDbFormat.format(cal.time)
            val dayName = sdfDayName.format(cal.time).replace(".", "").uppercase()
            val dayNum = sdfDayNum.format(cal.time)
            val monthNum = sdfMonthNum.format(cal.time)
            list.add(dbDate to Triple(dayName, dayNum, monthNum))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Calculate real schedule statistics (Wyjeżdżone hours, zaplanowane hours, total lessons)
    val completedHours = remember(roleFilteredLessons) {
        roleFilteredLessons.filter { it.status == "Zrealizowana" }.sumOf { it.durationHours }
    }
    val plannedHours = remember(roleFilteredLessons) {
        roleFilteredLessons.filter { it.status == "Zaplanowana" }.sumOf { it.durationHours }
    }

    Scaffold(
        containerColor = Color(0xFF0B0F19),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onBookLessonClick,
                containerColor = Color(0xFFEF4444),
                contentColor = Color.White,
                modifier = Modifier.testTag("book_lesson_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Zapisz Nową Lekcję")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spacer to separate from top bar
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Title & Info Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Grafik Jazd Szkoleniowych",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentRole == UserRole.MANAGER) "Twoja aktywna rola: ${currentRole.label} (${userName})" else "Witaj, ${userName}!",
                            fontSize = 12.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onBookLessonClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nowa lekcja", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // -------------------------------------------------------------
            // SIMULATION PANEL (TOGGLEABLE/REMOVABLE TESTING WORKSPACE)
            // -------------------------------------------------------------
            if (SHOW_TEST_SIMULATION_PANEL) {
                item {
                    SimulationPanelWidget(
                        viewModel = viewModel,
                        currentRole = currentRole,
                        userName = userName,
                        isExpanded = isSimPanelExpanded,
                        onToggleExpand = { isSimPanelExpanded = !isSimPanelExpanded }
                    )
                }
            }

            // Interactive Statistics Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatBox(
                        title = "Wyjeżdżone",
                        value = "$completedHours godz",
                        icon = Icons.Default.CheckCircle,
                        tintColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Zaplanowane",
                        value = "$plannedHours godz",
                        icon = Icons.Default.Schedule,
                        tintColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Wszystkie lekcje",
                        value = "${roleFilteredLessons.size}",
                        icon = Icons.Default.CalendarToday,
                        tintColor = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Calendar Strip Header & Filters
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131B2E), RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Interaktywny Kalendarz",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        if (selectedDateFilter != null) {
                            TextButton(
                                onClick = { selectedDateFilter = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pokaż wszystkie dni", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Wybierz dzień by filtrować",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Scrollable Calendar Strip
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(calendarDays) { index, item ->
                            val dbDate = item.first
                            val (dayName, dayNum, monthNum) = item.second
                            val isSelected = selectedDateFilter == dbDate
                            val dayLessonCount = roleFilteredLessons.count { it.date == dbDate }

                            CalendarDayCard(
                                dayName = dayName,
                                dayNum = dayNum,
                                monthNum = monthNum,
                                isSelected = isSelected,
                                lessonCount = dayLessonCount,
                                onClick = {
                                    selectedDateFilter = if (isSelected) null else dbDate
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips (Zaplanowane, Zrealizowane, Wszystkie)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = selectedFilter == "WSZYSTKIE",
                        onClick = { selectedFilter = "WSZYSTKIE" },
                        label = { Text("Wszystkie (${dateFilteredLessons.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131B2E),
                            labelColor = Color.White.copy(alpha = 0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == "WSZYSTKIE",
                            borderColor = Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = Color(0xFF38BDF8)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ZAPLANOWANE",
                        onClick = { selectedFilter = "ZAPLANOWANE" },
                        label = { Text("Zaplanowane (${dateFilteredLessons.count { it.status == "Zaplanowana" }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0369A1).copy(alpha = 0.3f),
                            selectedLabelColor = Color(0xFF38BDF8),
                            containerColor = Color(0xFF131B2E),
                            labelColor = Color.White.copy(alpha = 0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == "ZAPLANOWANE",
                            borderColor = Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = Color(0xFF0284C7)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "ZREALIZOWANE",
                        onClick = { selectedFilter = "ZREALIZOWANE" },
                        label = { Text("Zrealizowane (${dateFilteredLessons.count { it.status == "Zrealizowana" }})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF064E3B).copy(alpha = 0.3f),
                            selectedLabelColor = Color(0xFF34D399),
                            containerColor = Color(0xFF131B2E),
                            labelColor = Color.White.copy(alpha = 0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == "ZREALIZOWANE",
                            borderColor = Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = Color(0xFF10B981)
                        )
                    )
                }
            }

            // List of Lessons
            if (finalFilteredList.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.CalendarMonth,
                        title = "Brak pasujących jazd",
                        description = if (selectedDateFilter != null) {
                            "Nie znaleziono żadnych jazd szkoleniowych zaplanowanych na dzień $selectedDateFilter."
                        } else {
                            "W wybranej zakładce nie ma żadnych lekcji w Twoim grafiku."
                        },
                        actionButtonText = "Zapisz nową lekcję",
                        onActionClick = onBookLessonClick,
                        testTagPrefix = "schedule"
                    )
                }
            } else {
                items(finalFilteredList, key = { it.id }) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        currentRole = currentRole,
                        onStatusChange = { newStatus -> onUpdateLessonStatus(lesson.id, newStatus) },
                        onDelete = { onDeleteLesson(lesson.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom space for FAB
        }
    }
}

@Composable
private fun CalendarDayCard(
    dayName: String,
    dayNum: String,
    monthNum: String,
    isSelected: Boolean,
    lessonCount: Int,
    onClick: () -> Unit
) {
    val activeBorderColor = Color(0xFF38BDF8)
    val inactiveBorderColor = Color.White.copy(alpha = 0.08f)
    val cardBg = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A)

    Surface(
        color = cardBg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) activeBorderColor else inactiveBorderColor),
        modifier = Modifier
            .width(62.dp)
            .height(78.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Text(
                text = dayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dayNum,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = ".$monthNum",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.4f)
            )

            // Dynamic Lesson Indicators (Dots show how many drives are planned for this day)
            if (lessonCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(minOf(lessonCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(Color(0xFF10B981), CircleShape)
                        )
                    }
                    if (lessonCount > 3) {
                        Text("+", fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(tintColor.copy(alpha = 0.4f), CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonEntity,
    currentRole: UserRole,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (lesson.status) {
        "Zrealizowana" -> Color(0xFF10B981)
        "Anulowana" -> Color(0xFFEF4444)
        else -> Color(0xFF38BDF8)
    }

    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header info & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "${lesson.date} • godz. ${lesson.time}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Czas: ${lesson.durationHours}h • Auto: ${lesson.vehiclePlate}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = lesson.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // Student / Instructor details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎓 Kursant: ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        Text(text = lesson.studentName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚗 Instruktor: ", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        Text(text = lesson.instructorName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
                    }
                }

                // Delete option: only for Managers and Instructors
                if (currentRole != UserRole.STUDENT) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń lekcję", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                }
            }

            if (lesson.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 Notatka: ${lesson.notes}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Quick status modification: available to Manager & Instructors, or for Students if it's not canceled/done
            if (lesson.status != "Zrealizowana" && lesson.status != "Anulowana") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentRole != UserRole.STUDENT) {
                        TextButton(
                            onClick = { onStatusChange("Zrealizowana") },
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("✓ Wykonana", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }

                    TextButton(
                        onClick = { onStatusChange("Anulowana") },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("✕ Anuluj", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------
// SYSTEM MULTI-TENANT TEST SIMULATOR WIDGET
// ----------------------------------------------------------------------
@Composable
private fun SimulationPanelWidget(
    viewModel: OskViewModel,
    currentRole: UserRole,
    userName: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val reservations by viewModel.reservations.collectAsState()
    val lessons by viewModel.lessons.collectAsState()

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Troubleshoot,
                        contentDescription = null,
                        tint = Color(0xFFA855F7),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CENTRALNY PANEL SYMULACJI SYNC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFA855F7),
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Testuj synchronizację ról w czasie rzeczywistym",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Zwiń/Rozwiń",
                        tint = Color(0xFFA855F7)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. One-Click Role Switches
                    Text(
                        text = "1. PRZEŁĄCZ ROLĘ & UŻYTKOWNIKA (ISOLACJA DANYCH)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleSwitchBtn(
                            label = "Zarządca",
                            emoji = "🏢",
                            isSelected = currentRole == UserRole.MANAGER,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setRoleAndUser(UserRole.MANAGER, "Jan Kowalski (Zarządca)") }
                        )
                        RoleSwitchBtn(
                            label = "Instruktor",
                            emoji = "🚗",
                            isSelected = currentRole == UserRole.INSTRUCTOR,
                            modifier = Modifier.weight(1.1f),
                            onClick = { viewModel.setRoleAndUser(UserRole.INSTRUCTOR, "Piotr Nowak") }
                        )
                        RoleSwitchBtn(
                            label = "Kursant Anna",
                            emoji = "🎓",
                            isSelected = currentRole == UserRole.STUDENT && userName == "Anna Wiśniewska",
                            modifier = Modifier.weight(1.2f),
                            onClick = { viewModel.setRoleAndUser(UserRole.STUDENT, "Anna Wiśniewska") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Load Simulation dataset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. BAZA DANYCH SYMULATORA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )

                        Button(
                            onClick = { viewModel.generateSimulationData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Wgraj dane demo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Automated scenario walkthrough
                    Text(
                        text = "3. INTERAKTYWNY SCENARIUSZ PRZEPŁYWU (SYNC FLOW)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Scenario Steps:
                    // Step 1: Student creates reservation
                    val pendingReservation = reservations.firstOrNull { it.status == "Oczekująca" && it.fullName == "Anna Wiśniewska" }
                    val upcomingLesson = lessons.firstOrNull { it.studentName == "Anna Wiśniewska" && it.status == "Zaplanowana" }

                    ScenarioStepCard(
                        stepNum = "1",
                        title = "Kursant wysyła rezerwację",
                        desc = "Anna Wiśniewska rezerwuje termin w kalendarzu.",
                        statusText = if (pendingReservation != null) "Aktywna rezerwacja ID: ${pendingReservation.id}" else "Brak rezerwacji",
                        statusColor = if (pendingReservation != null) Color(0xFFEAB308) else Color.White.copy(alpha = 0.4f),
                        buttonText = "Rezerwuj jako Anna",
                        onAction = {
                            viewModel.submitReservation(
                                fullName = "Anna Wiśniewska",
                                dateOfBirth = "15.05.2005",
                                pesel = "05251508214",
                                phone = "+48 501 202 303",
                                email = "anna.wisniewska@example.com",
                                selectedDate = "2026-07-30",
                                selectedTimeSlots = "12:00 - 14:00"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Step 2: Manager approves
                    ScenarioStepCard(
                        stepNum = "2",
                        title = "Zarządca zatwierdza rezerwację",
                        desc = "Automatycznie tworzy lekcję w kalendarzu instruktora Piotra.",
                        statusText = if (upcomingLesson != null) "Lekcja utworzona pomyślnie!" else "Oczekiwanie na zatwierdzenie",
                        statusColor = if (upcomingLesson != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                        buttonText = "Zatwierdź jako Zarządca",
                        enabled = pendingReservation != null,
                        onAction = {
                            pendingReservation?.let {
                                viewModel.updateReservationStatus(it.id, "Zatwierdzona")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Step 3: Instructor completes
                    val completedLesson = lessons.firstOrNull { it.studentName == "Anna Wiśniewska" && it.status == "Zrealizowana" }
                    ScenarioStepCard(
                        stepNum = "3",
                        title = "Instruktor realizuje jazdę",
                        desc = "Instruktor Piotr Nowak oznacza jazdę jako zrealizowaną.",
                        statusText = if (completedLesson != null) "Jazda zaliczona! Sukces" else "W toku",
                        statusColor = if (completedLesson != null) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                        buttonText = "Wykonaj jako Piotr",
                        enabled = upcomingLesson != null,
                        onAction = {
                            upcomingLesson?.let {
                                viewModel.updateLessonStatus(it.id, "Zrealizowana")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 Jak usunąć symulator przed wdrożeniem? Po prostu ustaw flagę [SHOW_TEST_SIMULATION_PANEL = false] na górze pliku ScheduleScreen.kt.",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleSwitchBtn(
    label: String,
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF131B2E),
        border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF8B5CF6) else Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(36.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ScenarioStepCard(
    stepNum: String,
    title: String,
    desc: String,
    statusText: String,
    statusColor: Color,
    buttonText: String,
    enabled: Boolean = true,
    onAction: () -> Unit
) {
    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(stepNum, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(desc, fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                    Text("Stan: $statusText", fontSize = 9.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onAction,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF38BDF8),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Text(
                    buttonText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) Color(0xFF0F172A) else Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}
