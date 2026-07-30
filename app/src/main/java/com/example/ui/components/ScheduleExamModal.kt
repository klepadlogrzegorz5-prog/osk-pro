package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleExamModal(
    prefilledStudentName: String = "",
    prefilledPkkNumber: String = "",
    hasPhotoInDocs: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (
        studentName: String,
        date: String,
        time: String,
        examType: String,
        pkkNumber: String,
        pkkStatus: String,
        hasPhoto: Boolean,
        isConfirmed: Boolean
    ) -> Unit
) {
    var studentName by remember { mutableStateOf(prefilledStudentName) }
    var examDate by remember { mutableStateOf("") }
    var examTime by remember { mutableStateOf("") }
    var examType by remember { mutableStateOf("Praktyczny") } // "Teoretyczny" lub "Praktyczny"
    var pkkNumber by remember { mutableStateOf(prefilledPkkNumber) }
    var pkkStatus by remember { mutableStateOf(if (prefilledPkkNumber.isNotEmpty()) "Zwolniony" else "Zablokowany") } // "Zwolniony", "Zablokowany"
    var hasPhoto by remember { mutableStateOf(hasPhotoInDocs) }

    // Validation
    val isFormValid = studentName.isNotBlank() && examDate.isNotBlank() && examTime.isNotBlank()

    // Autocomplete date/time helper for ease of testing
    LaunchedEffect(Unit) {
        if (examDate.isEmpty()) {
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 2) // set exam 2 days in future
            examDate = sdfDate.format(calendar.time)
            examTime = "11:30"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("schedule_exam_dialog_surface"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF111827),
            border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "USTAL TERMIN EGZAMINU",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Wprowadź termin i PKK profil kandydata",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Student Name
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { studentName = it },
                    label = { Text("Imię i nazwisko kursanta", color = Color.White.copy(alpha = 0.6f)) },
                    readOnly = prefilledStudentName.isNotEmpty(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1F2937),
                        unfocusedContainerColor = Color(0xFF1F2937)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_exam_student_name")
                )

                // Exam Type Choice (Theory or Practical)
                Column {
                    Text(
                        text = "Rodzaj egzaminu:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Teoretyczny", "Praktyczny").forEach { type ->
                            val isSelected = examType == type
                            Surface(
                                onClick = { examType = type },
                                color = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF1F2937),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFEF4444) else Color.White.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = type,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Date and Time (Side-by-side)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = examDate,
                        onValueChange = { examDate = it },
                        label = { Text("Data (RRRR-MM-DD)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF1F2937),
                            unfocusedContainerColor = Color(0xFF1F2937)
                        ),
                        modifier = Modifier.weight(1.2f).testTag("input_exam_date")
                    )

                    OutlinedTextField(
                        value = examTime,
                        onValueChange = { examTime = it },
                        label = { Text("Godzina (GG:MM)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color(0xFF1F2937),
                            unfocusedContainerColor = Color(0xFF1F2937)
                        ),
                        modifier = Modifier.weight(0.8f).testTag("input_exam_time")
                    )
                }

                // PKK number
                OutlinedTextField(
                    value = pkkNumber,
                    onValueChange = { pkkNumber = it },
                    label = { Text("Numer PKK (kierowcy)", color = Color.White.copy(alpha = 0.6f)) },
                    placeholder = { Text("np. 12345678901234567890", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1F2937),
                        unfocusedContainerColor = Color(0xFF1F2937)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_exam_pkk_number")
                )

                // PKK Status Choices (Zwolniony vs Zablokowany)
                Column {
                    Text(
                        text = "Status profilu PKK:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("Zwolniony", "Zablokowany").forEach { status ->
                            val isSelected = pkkStatus == status
                            val borderCol = if (isSelected) {
                                if (status == "Zwolniony") Color(0xFF10B981) else Color(0xFFEF4444)
                            } else {
                                Color.White.copy(alpha = 0.15f)
                            }
                            val bgCol = if (isSelected) {
                                if (status == "Zwolniony") Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                            } else {
                                Color(0xFF1F2937)
                            }

                            Surface(
                                onClick = { pkkStatus = status },
                                color = bgCol,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderCol),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("pkk_status_choice_$status")
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (status == "Zwolniony") Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (status == "Zwolniony") Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = status,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Checkbox: photo uploaded helper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = hasPhoto,
                        onCheckedChange = { hasPhoto = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFEF4444),
                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        text = "Czy w dokumentach znajduje się zdjęcie kandydata?",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj", color = Color.White.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            if (isFormValid) {
                                // "Zwolniony" status represents isConfirmed = true
                                val isConfirmed = pkkStatus == "Zwolniony"
                                onConfirm(studentName, examDate, examTime, examType, pkkNumber, pkkStatus, hasPhoto, isConfirmed)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isFormValid,
                        modifier = Modifier.weight(1.5f).testTag("btn_confirm_schedule_exam")
                    ) {
                        Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ustal Egzamin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
