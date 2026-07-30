package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ExamReservationEntity
import com.example.ui.components.EmptyState
import com.example.ui.components.ScheduleExamModal
import com.example.ui.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamReservationsScreen(
    currentRole: UserRole,
    examReservations: List<ExamReservationEntity>,
    onScheduleExam: (
        studentName: String,
        date: String,
        time: String,
        examType: String,
        pkkNumber: String,
        pkkStatus: String,
        hasPhoto: Boolean,
        isConfirmed: Boolean
    ) -> Unit,
    onUpdatePkk: (id: Long, pkkNumber: String, isConfirmed: Boolean, pkkStatus: String) -> Unit,
    onDeleteExam: (id: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("WSZYSTKIE") } // "WSZYSTKIE", "ZWOLNIONE", "ZABLOKOWANE"
    var showScheduleDialog by remember { mutableStateOf(false) }

    // Filter and search
    val filteredExams = remember(examReservations, searchQuery, selectedFilter) {
        examReservations.filter { exam ->
            val matchesSearch = exam.studentName.contains(searchQuery, ignoreCase = true) ||
                    exam.pkkNumber.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "ZWOLNIONE" -> exam.pkkStatus == "Zwolniony"
                "ZABLOKOWANE" -> exam.pkkStatus == "Zablokowany"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0F19),
        floatingActionButton = {
            if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
                FloatingActionButton(
                    onClick = { showScheduleDialog = true },
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("schedule_exam_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Ustal nowy egzamin")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Egzaminy, Rezerwacje & PKK",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Lista zaplanowanych egzaminów oraz statusy profili PKK",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )
                }
                
                Button(
                    onClick = { showScheduleDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ustal Egzamin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj po nazwisku lub numerze PKK...", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827),
                    focusedBorderColor = Color(0xFFEF4444),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("exam_search_field")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtering Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple("WSZYSTKIE", "Wszystkie (${examReservations.size})", Color(0xFF38BDF8)),
                    Triple("ZWOLNIONE", "Zwolnione (${examReservations.count { it.pkkStatus == "Zwolniony" }})", Color(0xFF10B981)),
                    Triple("ZABLOKOWANE", "Zablokowane (${examReservations.count { it.pkkStatus == "Zablokowany" }})", Color(0xFFEF4444))
                ).forEach { (filter, label, color) ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E293B),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF131B2E),
                            labelColor = Color.White.copy(alpha = 0.6f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.1f),
                            selectedBorderColor = color
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content List
            if (filteredExams.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "Brak terminów egzaminów",
                    description = "Nie znaleziono żadnych kursantów o zaplanowanych egzaminach pasujących do wybranego filtru.",
                    actionButtonText = "Dodaj Nowy Egzamin",
                    onActionClick = { showScheduleDialog = true },
                    testTagPrefix = "exams_list"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredExams, key = { it.id }) { exam ->
                        ExamReservationCard(
                            exam = exam,
                            currentRole = currentRole,
                            onUpdatePkk = { num, conf, status -> onUpdatePkk(exam.id, num, conf, status) },
                            onDelete = { onDeleteExam(exam.id) }
                        )
                    }
                }
            }
        }

        // Add exam dialog
        if (showScheduleDialog) {
            ScheduleExamModal(
                onDismiss = { showScheduleDialog = false },
                onConfirm = { name, date, time, type, pkk, status, hasPh, isConf ->
                    onScheduleExam(name, date, time, type, pkk, status, hasPh, isConf)
                    showScheduleDialog = false
                }
            )
        }
    }
}

@Composable
fun ExamReservationCard(
    exam: ExamReservationEntity,
    currentRole: UserRole,
    onUpdatePkk: (pkkNumber: String, isConfirmed: Boolean, pkkStatus: String) -> Unit,
    onDelete: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var editingPkk by remember { mutableStateOf(false) }
    var inputPkkNumber by remember { mutableStateOf(exam.pkkNumber) }
    
    // Dynamic border animation based on status
    val cardColor = if (exam.pkkStatus == "Zwolniony") Color(0xFF10B981) else Color(0xFFEF4444)
    val cardBg = Color(0xFF111827)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, cardColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Student Name, Exam type tag, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exam.studentName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (exam.examType == "Praktyczny") Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF38BDF8).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (exam.examType == "Praktyczny") Color(0xFFEF4444) else Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = exam.examType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (exam.examType == "Praktyczny") Color(0xFFFCA5A5) else Color(0xFF93C5FD),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (exam.hasPhoto) {
                            Surface(
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Photo, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "FOTO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF34D399)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("${exam.studentName}\nPKK: ${exam.pkkNumber}\nTermin: ${exam.examDate} ${exam.examTime}"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopiuj dane", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                    }

                    if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń termin", tint = Color(0xFFEF4444).copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            // Row 2: Date & Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                Text(
                    text = "Data egzaminu:",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "${exam.examDate} o godz. ${exam.examTime}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Row 3: PKK Input and status bubble
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profil PKK:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    // Glow status bubble
                    Surface(
                        color = if (exam.pkkStatus == "Zwolniony") Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (exam.pkkStatus == "Zwolniony") Color(0xFF10B981) else Color(0xFFEF4444)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable {
                                if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
                                    val newStatus = if (exam.pkkStatus == "Zwolniony") "Zablokowany" else "Zwolniony"
                                    onUpdatePkk(exam.pkkNumber, newStatus == "Zwolniony", newStatus)
                                }
                            }
                            .testTag("pkk_status_toggle_${exam.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (exam.pkkStatus == "Zwolniony") Color(0xFF10B981) else Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exam.pkkStatus.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (exam.pkkStatus == "Zwolniony") Color(0xFF34D399) else Color(0xFFFCA5A5)
                            )
                        }
                    }
                }

                // PKK number field or click-to-edit
                if (editingPkk) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = inputPkkNumber,
                            onValueChange = { inputPkkNumber = it },
                            placeholder = { Text("Wpisz numer PKK", fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedContainerColor = Color(0xFF1F2937),
                                unfocusedContainerColor = Color(0xFF1F2937)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pkk_edit_input_${exam.id}")
                        )

                        IconButton(
                            onClick = {
                                onUpdatePkk(inputPkkNumber, exam.pkkStatus == "Zwolniony", exam.pkkStatus)
                                editingPkk = false
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF10B981), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Zapisz", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        
                        IconButton(
                            onClick = {
                                editingPkk = false
                                inputPkkNumber = exam.pkkNumber
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF374151), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Anuluj", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (exam.pkkNumber.isEmpty()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚠️ Brak numeru PKK kandydata!",
                                    color = Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (exam.hasPhoto) {
                                    Text(
                                        text = "Zdjęcie jest dostępne. Kliknij edycję i przepisz numer!",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = exam.pkkNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.clickable {
                                    clipboardManager.setText(AnnotatedString(exam.pkkNumber))
                                }
                            )
                        }

                        if (currentRole == UserRole.MANAGER || currentRole == UserRole.INSTRUCTOR) {
                            TextButton(
                                onClick = { editingPkk = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (exam.pkkNumber.isEmpty()) "Wpisz" else "Edytuj", fontSize = 11.sp, color = Color(0xFF38BDF8))
                            }
                        }
                    }
                }
            }

            // Instructor warning and button triggers
            if (exam.isWishesSent) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                    Text(
                        text = "Wysłano miłe słowa powodzenia od instruktora 🤞",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
