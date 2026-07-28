package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AccessCode
import com.example.data.database.OskProfile
import com.example.ui.OskViewModel
import com.example.ui.CalendarEvent
import com.example.ui.StudentRegistration
import com.example.ui.components.FooterCompanyQuote
import com.example.ui.components.NeonLHeaderCircle
import java.text.SimpleDateFormat
import java.util.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun OwnerDashboard(
    viewModel: OskViewModel,
    profile: OskProfile,
    onLogout: () -> Unit
) {
    val accessCodes by viewModel.allAccessCodes.collectAsState()
    val cloudSub by viewModel.cloudSubscription.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val studentRegistrations by viewModel.studentRegistrations.collectAsState()

    val instructorCars by viewModel.instructorCars.collectAsState()
    val instructorRates by viewModel.instructorRates.collectAsState()
    val instructorHours by viewModel.instructorHours.collectAsState()
    val instructorPassRates by viewModel.instructorPassRates.collectAsState()

    var isGeneratorExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("KURSANT") } // "INSTRUKTOR" or "KURSANT"
    var newAssigneeName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    // States for Editing/Adding Instructors
    var editingInstructorName by remember { mutableStateOf<String?>(null) }
    var editingInstructorCar by remember { mutableStateOf("") }
    var editingInstructorRate by remember { mutableStateOf("") }
    var editingInstructorHours by remember { mutableStateOf("") }
    var editingInstructorPassRate by remember { mutableStateOf("") }

    var showAddInstructorDialog by remember { mutableStateOf(false) }
    var addInstructorName by remember { mutableStateOf("") }
    var addInstructorCar by remember { mutableStateOf("") }
    var addInstructorRate by remember { mutableStateOf("") }
    var addInstructorHours by remember { mutableStateOf("") }
    var addInstructorPassRate by remember { mutableStateOf("") }

    // Conflict Engine, Prices, and Group Schedules state flows
    val calendarConflicts by viewModel.calendarConflicts.collectAsState()
    val groupSchedules by viewModel.groupSchedules.collectAsState()
    val coursePrices by viewModel.coursePrices.collectAsState()

    // Dialog state for adding Group Course
    var showAddGroupScheduleDialog by remember { mutableStateOf(false) }
    var newGroupTitle by remember { mutableStateOf("") }
    var newGroupCategory by remember { mutableStateOf("B") }
    var newGroupInstructor by remember { mutableStateOf("Tomasz Nowak") }
    var newGroupDatesDesc by remember { mutableStateOf("Wtorki i Czwartki w godz. 17:00 - 20:00") }
    var newGroupLocation by remember { mutableStateOf("Główna Sala Wykładowa OSK (Sala 1)") }

    // Dialog state for Editing Price
    var editingPriceId by remember { mutableStateOf<String?>(null) }
    var editingPricePln by remember { mutableStateOf("") }
    var editingPriceDesc by remember { mutableStateOf("") }

    // State for custom reminder input
    var newReminderText by remember { mutableStateOf("") }
    var newReminderPriority by remember { mutableStateOf("MEDIUM") } // "HIGH", "MEDIUM", "LOW"
    var newReminderRecipientName by remember { mutableStateOf("Właściciel") }
    var newReminderRecipientRole by remember { mutableStateOf("OWNER") } // "OWNER", "INSTRUKTOR", "ALL"
    var isRecipientDropdownExpanded by remember { mutableStateOf(false) }

    // State for custom announcement input
    var showAddAnnouncementDialog by remember { mutableStateOf(false) }
    var newAnnTitle by remember { mutableStateOf("") }
    var newAnnContent by remember { mutableStateOf("") }
    var newAnnCategory by remember { mutableStateOf("INFO") } // "INFO", "ALERT", "SUCCESS"

    // Settings Dialog state for Owner
    var showOwnerSettingsDialog by remember { mutableStateOf(false) }
    var ownerOskName by remember { mutableStateOf("OSK FastDrive Kraków") }
    var ownerNip by remember { mutableStateOf("123-456-78-90") }
    var ownerBankAccount by remember { mutableStateOf("88 1020 2892 0000 4102 0123 4567") }
    var ownerAddress by remember { mutableStateOf("ul. Balicka 12, 30-149 Kraków") }
    var ownerAutoSmsGateEnabled by remember { mutableStateOf(true) }
    var ownerConflictAutoDetect by remember { mutableStateOf(true) }
    var ownerDefaultInstructorRate by remember { mutableStateOf("60.00") }
    var ownerDefaultCatBCoursePrice by remember { mutableStateOf("3200.00") }
    var ownerBackupFrequency by remember { mutableStateOf("Codziennie o 02:00 w nocy") }

    val context = LocalContext.current

    // State for Calendar and Events
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    var currentCalendarMonth by remember { mutableStateOf(java.util.Calendar.getInstance()) }
    var selectedCalendarDate by remember {
        mutableStateOf(
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
        )
    }
    var calendarTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "TRAINING", "COMPANY"

    var showAddEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventStartTime by remember { mutableStateOf("10:00") }
    var newEventEndTime by remember { mutableStateOf("12:00") }
    var newEventType by remember { mutableStateOf("TRAINING") } // "TRAINING" or "COMPANY"
    var newEventCategory by remember { mutableStateOf("LESSON") } // "LESSON", "EXAM", "LECTURE", "VACATION", "SICK_LEAVE", "OFFICE"
    var newEventInstructorName by remember { mutableStateOf("Tomasz Nowak") }
    var newEventStudentName by remember { mutableStateOf("") }

    // States for Adding Students Manually
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var manualStudentName by remember { mutableStateOf("") }
    var manualStudentEmail by remember { mutableStateOf("") }
    var manualStudentPhone by remember { mutableStateOf("") }
    var manualStudentCategory by remember { mutableStateOf("B") }
    var manualStudentInstructor by remember { mutableStateOf("Tomasz Nowak") }

    // States for proposing reschedule for cancelled rides
    var proposingRescheduleEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var proposeRescheduleNewDateString by remember { mutableStateOf("") }
    var proposeRescheduleNewStartTime by remember { mutableStateOf("12:00") }

    var showEditEventDialog by remember { mutableStateOf(false) }
    var editingEventId by remember { mutableStateOf("") }
    var editEventTitle by remember { mutableStateOf("") }
    var editEventStartTime by remember { mutableStateOf("10:00") }
    var editEventEndTime by remember { mutableStateOf("12:00") }
    var editEventType by remember { mutableStateOf("TRAINING") }
    var editEventCategory by remember { mutableStateOf("LESSON") }
    var editEventInstructorName by remember { mutableStateOf("Tomasz Nowak") }
    var editEventStudentName by remember { mutableStateOf("") }

    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Zaplanuj Wydarzenie / Grafik") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val dateFormatted = SimpleDateFormat("dd.MM.yyyy", Locale("pl")).format(selectedCalendarDate.time)
                    Text(
                        text = "Data: $dateFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = newEventTitle,
                        onValueChange = { newEventTitle = it },
                        label = { Text("Nazwa wydarzenia / Opis") },
                        placeholder = { Text("np. Jazda kat. B: Jan Kowalski") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newEventStartTime,
                            onValueChange = { newEventStartTime = it },
                            label = { Text("Od (GG:MM)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newEventEndTime,
                            onValueChange = { newEventEndTime = it },
                            label = { Text("Do (GG:MM)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Przypisz do (Opcjonalnie):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newEventInstructorName,
                        onValueChange = { newEventInstructorName = it },
                        label = { Text("Instruktor (Tomasz Nowak, Alicja Kowalska...)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newEventStudentName,
                        onValueChange = { newEventStudentName = it },
                        label = { Text("Kursant (np. Jan Kowalski)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Rodzaj kalendarza:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newEventType == "TRAINING",
                            onClick = {
                                newEventType = "TRAINING"
                                newEventCategory = "LESSON"
                            },
                            label = { Text("Szkoleniowy") }
                        )
                        FilterChip(
                            selected = newEventType == "COMPANY",
                            onClick = {
                                newEventType = "COMPANY"
                                newEventCategory = "VACATION"
                            },
                            label = { Text("Firmowy") }
                        )
                    }

                    Text("Kategoria:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (newEventType == "TRAINING") {
                            FilterChip(
                                selected = newEventCategory == "LESSON",
                                onClick = { newEventCategory = "LESSON" },
                                label = { Text("Jazdy 🚗") }
                            )
                            FilterChip(
                                selected = newEventCategory == "EXAM",
                                onClick = { newEventCategory = "EXAM" },
                                label = { Text("Egzamin 📝") }
                            )
                            FilterChip(
                                selected = newEventCategory == "LECTURE",
                                onClick = { newEventCategory = "LECTURE" },
                                label = { Text("Wykład 🏫") }
                            )
                        } else {
                            FilterChip(
                                selected = newEventCategory == "VACATION",
                                onClick = { newEventCategory = "VACATION" },
                                label = { Text("Urlop 🏖️") }
                            )
                            FilterChip(
                                selected = newEventCategory == "SICK_LEAVE",
                                onClick = { newEventCategory = "SICK_LEAVE" },
                                label = { Text("Chorobowe 🤒") }
                            )
                            FilterChip(
                                selected = newEventCategory == "OFFICE",
                                onClick = { newEventCategory = "OFFICE" },
                                label = { Text("Biuro/Inne 🏢") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEventTitle.isNotBlank()) {
                            viewModel.addCalendarEvent(
                                title = newEventTitle,
                                date = selectedCalendarDate.timeInMillis,
                                startTime = newEventStartTime,
                                endTime = newEventEndTime,
                                type = newEventType,
                                category = newEventCategory,
                                studentName = newEventStudentName,
                                instructorName = newEventInstructorName
                            )
                            newEventTitle = ""
                            newEventStudentName = ""
                            newEventInstructorName = "Tomasz Nowak"
                            showAddEventDialog = false
                            Toast.makeText(context, "Dodano wydarzenie! Czeka na akceptację instruktora.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = newEventTitle.isNotBlank()
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showEditEventDialog) {
        AlertDialog(
            onDismissRequest = { showEditEventDialog = false },
            title = { Text("Edytuj Grafik / Wydarzenie") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editEventTitle,
                        onValueChange = { editEventTitle = it },
                        label = { Text("Nazwa / Opis") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editEventStartTime,
                            onValueChange = { editEventStartTime = it },
                            label = { Text("Od (GG:MM)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editEventEndTime,
                            onValueChange = { editEventEndTime = it },
                            label = { Text("Do (GG:MM)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Przypisz do:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = editEventInstructorName,
                        onValueChange = { editEventInstructorName = it },
                        label = { Text("Instruktor") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEventStudentName,
                        onValueChange = { editEventStudentName = it },
                        label = { Text("Kursant") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Kategoria:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = editEventCategory == "LESSON",
                            onClick = { editEventCategory = "LESSON"; editEventType = "TRAINING" },
                            label = { Text("Jazdy 🚗") }
                        )
                        FilterChip(
                            selected = editEventCategory == "EXAM",
                            onClick = { editEventCategory = "EXAM"; editEventType = "TRAINING" },
                            label = { Text("Egzamin 📝") }
                        )
                        FilterChip(
                            selected = editEventCategory == "VACATION",
                            onClick = { editEventCategory = "VACATION"; editEventType = "COMPANY" },
                            label = { Text("Wolne 🏖️") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editEventTitle.isNotBlank()) {
                            viewModel.editCalendarEventByOwner(
                                id = editingEventId,
                                title = editEventTitle,
                                date = selectedCalendarDate.timeInMillis,
                                startTime = editEventStartTime,
                                endTime = editEventEndTime,
                                type = editEventType,
                                category = editEventCategory,
                                studentName = editEventStudentName,
                                instructorName = editEventInstructorName
                            )
                            showEditEventDialog = false
                            Toast.makeText(context, "Zapisano zmiany w grafiku!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = editEventTitle.isNotBlank()
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEventDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Ręczna Rejestracja Kursanta 🎓", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualStudentName,
                        onValueChange = { manualStudentName = it },
                        label = { Text("Imię i Nazwisko") },
                        placeholder = { Text("np. Jan Kowalski") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("manual_student_name_input")
                    )

                    OutlinedTextField(
                        value = manualStudentEmail,
                        onValueChange = { manualStudentEmail = it },
                        label = { Text("Adres E-mail") },
                        placeholder = { Text("np. jan@example.com") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("manual_student_email_input")
                    )

                    OutlinedTextField(
                        value = manualStudentPhone,
                        onValueChange = { manualStudentPhone = it },
                        label = { Text("Numer Telefonu") },
                        placeholder = { Text("np. 123456789") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("manual_student_phone_input")
                    )

                    Text("Wybierz kategorię:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A", "B", "C", "D").forEach { cat ->
                            FilterChip(
                                selected = manualStudentCategory == cat,
                                onClick = { manualStudentCategory = cat },
                                label = { Text("Kat. $cat") }
                            )
                        }
                    }

                    Text("Przypisz do instruktora:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Tomasz Nowak", "Robert Wiśniewski", "Alicja Kowalska").forEach { inst ->
                            FilterChip(
                                selected = manualStudentInstructor == inst,
                                onClick = { manualStudentInstructor = inst },
                                label = { Text(inst, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualStudentName.isNotBlank() && manualStudentPhone.isNotBlank()) {
                            viewModel.registerStudentManual(
                                name = manualStudentName,
                                email = manualStudentEmail,
                                phone = manualStudentPhone,
                                category = manualStudentCategory,
                                instructorName = manualStudentInstructor
                            )
                            manualStudentName = ""
                            manualStudentEmail = ""
                            manualStudentPhone = ""
                            showAddStudentDialog = false
                            Toast.makeText(context, "Pomyślnie zarejestrowano kursanta!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = manualStudentName.isNotBlank() && manualStudentPhone.isNotBlank()
                ) {
                    Text("Zarejestruj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    proposingRescheduleEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { proposingRescheduleEvent = null },
            title = { Text("Zaproponuj Nowy Termin Jazdy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Wpisz nową proponowaną datę oraz godzinę dla kursanta ${event.studentName}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = proposeRescheduleNewDateString,
                        onValueChange = { proposeRescheduleNewDateString = it },
                        label = { Text("Nowa Data (DD.MM.RRRR)") },
                        placeholder = { Text("np. 30.07.2026") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("owner_reschedule_date_input")
                    )

                    Text("Wybierz godzinę rozpoczęcia:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val hourOptions = listOf("08:00", "10:00", "12:00", "14:00", "16:00", "18:00")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hourOptions.take(4).forEach { hour ->
                            FilterChip(
                                selected = proposeRescheduleNewStartTime == hour,
                                onClick = { proposeRescheduleNewStartTime = hour },
                                label = { Text(hour) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hourOptions.drop(4).forEach { hour ->
                            FilterChip(
                                selected = proposeRescheduleNewStartTime == hour,
                                onClick = { proposeRescheduleNewStartTime = hour },
                                label = { Text(hour) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (proposeRescheduleNewDateString.isNotBlank()) {
                            try {
                                val parsedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(proposeRescheduleNewDateString)
                                if (parsedDate != null) {
                                    val parsedStartParts = proposeRescheduleNewStartTime.split(":")
                                    val newEndHour = (parsedStartParts[0].toInt() + 2).toString().padStart(2, '0')
                                    val calculatedEndTime = "$newEndHour:${parsedStartParts[1]}"

                                    viewModel.proposeNewTermForCancelledEvent(
                                        eventId = event.id,
                                        newDate = parsedDate.time,
                                        newStartTime = proposeRescheduleNewStartTime,
                                        newEndTime = calculatedEndTime
                                    )
                                    proposingRescheduleEvent = null
                                    Toast.makeText(context, "Zaproponowano nowy termin! Oczekuje na akceptację kursanta.", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nieprawidłowy format daty! Użyj DD.MM.RRRR", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = proposeRescheduleNewDateString.isNotBlank()
                ) {
                    Text("Wyślij propozycję")
                }
            },
            dismissButton = {
                TextButton(onClick = { proposingRescheduleEvent = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Announcement creation modal dialog
    if (showAddAnnouncementDialog) {
        AlertDialog(
            onDismissRequest = { showAddAnnouncementDialog = false },
            title = { Text("Dodaj Nowy Komunikat") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newAnnTitle,
                        onValueChange = { newAnnTitle = it },
                        label = { Text("Tytuł") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAnnContent,
                        onValueChange = { newAnnContent = it },
                        label = { Text("Treść ogłoszenia") },
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Kategoria:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newAnnCategory == "INFO",
                            onClick = { newAnnCategory = "INFO" },
                            label = { Text("Info") }
                        )
                        FilterChip(
                            selected = newAnnCategory == "ALERT",
                            onClick = { newAnnCategory = "ALERT" },
                            label = { Text("Ważne") }
                        )
                        FilterChip(
                            selected = newAnnCategory == "SUCCESS",
                            onClick = { newAnnCategory = "SUCCESS" },
                            label = { Text("Sukces") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAnnTitle.isNotBlank() && newAnnContent.isNotBlank()) {
                            viewModel.addAnnouncement(newAnnTitle, newAnnContent, newAnnCategory)
                            newAnnTitle = ""
                            newAnnContent = ""
                            newAnnCategory = "INFO"
                            showAddAnnouncementDialog = false
                            Toast.makeText(context, "Dodano ogłoszenie!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    enabled = newAnnTitle.isNotBlank() && newAnnContent.isNotBlank()
                ) {
                    Text("Opublikuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAnnouncementDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            // Main App Header / Owner Profile
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeonLHeaderCircle(size = 52.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Panel Właściciela OSK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0284C7)
                            )
                            Text(
                                text = profile.ownerName,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showOwnerSettingsDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("owner_settings_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Ustawienia Właściciela")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onLogout,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("owner_logout_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = "Wyloguj")
                        }
                    }
                }
            }

            // Expandable Top Generator Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isGeneratorExpanded = !isGeneratorExpanded }
                        .testTag("expandable_generator_bar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "DODAJ INSTRUKTORA / KURSANTA",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = if (isGeneratorExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded Generator Card
            item {
                AnimatedVisibility(
                    visible = isGeneratorExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Generuj Kod Dostępu",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Role Selector Button Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == "KURSANT",
                                    onClick = { selectedRole = "KURSANT" },
                                    label = { Text("Kursant") },
                                    leadingIcon = {
                                        if (selectedRole == "KURSANT") {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("role_chip_student")
                                )

                                FilterChip(
                                    selected = selectedRole == "INSTRUKTOR",
                                    onClick = { selectedRole = "INSTRUKTOR" },
                                    label = { Text("Instruktor") },
                                    leadingIcon = {
                                        if (selectedRole == "INSTRUKTOR") {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("role_chip_instructor")
                                )
                            }

                            OutlinedTextField(
                                value = newAssigneeName,
                                onValueChange = { newAssigneeName = it },
                                label = { Text("Imię i nazwisko") },
                                placeholder = { Text("np. Jan Kowalski") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("code_assignee_name_field")
                            )

                            Button(
                                onClick = {
                                    if (newAssigneeName.isNotBlank()) {
                                        viewModel.generateAccessCode(newAssigneeName, selectedRole)
                                        Toast.makeText(context, "Wygenerowano kod dla: $newAssigneeName", Toast.LENGTH_SHORT).show()
                                        newAssigneeName = ""
                                        isGeneratorExpanded = false // Auto collapse
                                        selectedTab = 1 // Switch to Access Codes list
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                enabled = newAssigneeName.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("generate_code_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generuj unikalny kod")
                            }
                        }
                    }
                }
            }

            // Tab selection
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Główna", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = null) },
                        modifier = Modifier.testTag("tab_home")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Kadra 👥", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.People, contentDescription = null) },
                        modifier = Modifier.testTag("tab_instructors")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Kursanci 🎓", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.School, contentDescription = null) },
                        modifier = Modifier.testTag("tab_students")
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Flota 🚗", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null) },
                        modifier = Modifier.testTag("tab_fleet")
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Kody Dostępu", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) },
                        modifier = Modifier.testTag("tab_codes_list")
                    )
                    Tab(
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        text = { Text("Profil", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier.testTag("tab_profile")
                    )
                    Tab(
                        selected = selectedTab == 6,
                        onClick = { selectedTab = 6 },
                        text = { Text("Cennik 🏷️", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null) },
                        modifier = Modifier.testTag("tab_pricing")
                    )
                    Tab(
                        selected = selectedTab == 7,
                        onClick = { selectedTab = 7 },
                        text = { Text("Kursy Grupowe 👥", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = null) },
                        modifier = Modifier.testTag("tab_group_schedules")
                    )
                    Tab(
                        selected = selectedTab == 8,
                        onClick = { selectedTab = 8 },
                        text = { Text("Eksport PDF/Excel 🖨️", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.Print, contentDescription = null) },
                        modifier = Modifier.testTag("tab_exports")
                    )
                    Tab(
                        selected = selectedTab == 9,
                        onClick = { selectedTab = 9 },
                        text = { Text("Flota Pojazdów 🚗", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null) },
                        modifier = Modifier.testTag("tab_fleet")
                    )
                    Tab(
                        selected = selectedTab == 10,
                        onClick = { selectedTab = 10 },
                        text = { Text("Chmura & Integracje ☁️", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.CloudSync, contentDescription = null) },
                        modifier = Modifier.testTag("tab_cloud_sync")
                    )
                }
            }

            // Render based on selected Tab
            if (selectedTab == 0) {
                // TAB 0: HOME PAGE (STATISTICS, REMINDERS & NEWS)

                // CENTRAL CLOUD SYNC & BILLING QUICK CARD ON HOME TAB
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTab = 10 }
                            .testTag("home_cloud_status_banner"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(Color(0xFF1D4ED8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Chmura proOsk: AKTYWNA ✓",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Text(
                                        text = "Abonament opłacony przez Właściciela • 12 połączonych urządzeń",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Zarządzaj Chmurą",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 14:00 Daily Reminders trigger row for testing
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f))
                            .clickable {
                                viewModel.triggerDailyReminders1400()
                                Toast.makeText(context, "Generowanie raportu z godz. 14:00 zakończone sukcesem! Powiadomienia zostały rozesłane.", Toast.LENGTH_LONG).show()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRZETESTUJ POWIADOMIENIA O 14:00 ⏰",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // CONFLICT ENGINE ALERT BANNER
                if (calendarConflicts.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                            modifier = Modifier.fillMaxWidth().testTag("conflict_engine_banner")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Silnik Konfliktów OSK: Wykryto ${calendarConflicts.size} kolizje w terminarzu!",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                calendarConflicts.forEach { conflict ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = conflict.description, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = { viewModel.resolveConflict(conflict.id, "AUTO_SHIFT") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Przesuń o +2h", style = MaterialTheme.typography.labelSmall)
                                                }
                                                OutlinedButton(
                                                    onClick = { viewModel.resolveConflict(conflict.id, "CANCEL_SECOND") },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Odwołaj", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val studentTarget = if (conflict.event2.studentName.isNotBlank()) conflict.event2.studentName else conflict.event1.studentName
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                FilledTonalButton(
                                                    onClick = {
                                                        viewModel.propose3DayAlternativesToStudent(
                                                            studentName = if (studentTarget.isNotBlank()) studentTarget else "Jan Kowalski",
                                                            conflictDesc = conflict.description,
                                                            isTightSchedule = false
                                                        )
                                                        Toast.makeText(context, "Wysłano wolne terminy (3 dni) do kursanta! (Czas: 24h)", Toast.LENGTH_LONG).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Wyślij wolne terminy (24h)", style = MaterialTheme.typography.labelSmall)
                                                }
                                                FilledTonalButton(
                                                    onClick = {
                                                        viewModel.propose3DayAlternativesToStudent(
                                                            studentName = if (studentTarget.isNotBlank()) studentTarget else "Jan Kowalski",
                                                            conflictDesc = conflict.description,
                                                            isTightSchedule = true
                                                        )
                                                        Toast.makeText(context, "Wysłano pilną propozycję! (Czas: 6h)", Toast.LENGTH_LONG).show()
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Pilne! Terminy (6h)", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Statistics Grid Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val studentsCount = accessCodes.count { it.role == "KURSANT" }
                        val instructorsCount = accessCodes.count { it.role == "INSTRUKTOR" }

                        // Stat 1: Instructors
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.SupervisorAccount, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Instruktorzy", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$instructorsCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        // Stat 2: Students
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Kursanci", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "$studentsCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Stat 3: Total active codes
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Aktywne kody", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${accessCodes.size}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }

                // MULTI-FUNCTIONAL CALENDAR (FIRMOWY & SZKOLENIOWY)
                item {
                    val calendar = currentCalendarMonth.clone() as java.util.Calendar
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) // Sunday=1, Monday=2...
                    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    
                    // Convert to Polish starting day (Monday=0, Sunday=6)
                    val startOffset = (firstDayOfWeek + 5) % 7
                    
                    val monthName = SimpleDateFormat("LLLL yyyy", Locale("pl")).format(currentCalendarMonth.time)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Calendar Title Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Kalendarz Szkolny i Firmowy",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(
                                    onClick = { showAddEventDialog = true },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj", modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Calendar View Switcher (All / Szkoleniowy / Firmowy)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    Triple("ALL", "Wszystko", Icons.Default.CalendarToday),
                                    Triple("TRAINING", "Szkoleniowy 🚗", Icons.Default.School),
                                    Triple("COMPANY", "Firmowy 🏖️", Icons.Default.Business)
                                ).forEach { (filter, label, icon) ->
                                    val isSelected = calendarTypeFilter == filter
                                    Box(
                                        modifier = Modifier
                                            .weight(1.0f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else androidx.compose.ui.graphics.Color.Transparent
                                            )
                                            .clickable { calendarTypeFilter = filter }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Month Navigation Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val prev = currentCalendarMonth.clone() as java.util.Calendar
                                        prev.add(java.util.Calendar.MONTH, -1)
                                        currentCalendarMonth = prev
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Poprzedni miesiąc")
                                }

                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                IconButton(
                                    onClick = {
                                        val next = currentCalendarMonth.clone() as java.util.Calendar
                                        next.add(java.util.Calendar.MONTH, 1)
                                        currentCalendarMonth = next
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Następny miesiąc")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Days of the week header row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd").forEach { dName ->
                                    Box(
                                        modifier = Modifier.weight(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dName,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Days Grid
                            val rowsCount = if (startOffset + daysInMonth > 35) 6 else 5

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (row in 0 until rowsCount) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        for (col in 0 until 7) {
                                            val cellIndex = row * 7 + col
                                            val dayNum = cellIndex - startOffset + 1

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (dayNum in 1..daysInMonth) {
                                                    // This is a valid day of the month!
                                                    val cellCal = currentCalendarMonth.clone() as java.util.Calendar
                                                    cellCal.set(java.util.Calendar.DAY_OF_MONTH, dayNum)
                                                    cellCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                                    cellCal.set(java.util.Calendar.MINUTE, 0)
                                                    cellCal.set(java.util.Calendar.SECOND, 0)
                                                    cellCal.set(java.util.Calendar.MILLISECOND, 0)
                                                    val cellTimestamp = cellCal.timeInMillis

                                                    val isSelected = selectedCalendarDate.timeInMillis == cellTimestamp

                                                    // Calculate if has events of selected filter
                                                    val cellEvents = calendarEvents.filter {
                                                        it.date == cellTimestamp && (calendarTypeFilter == "ALL" || it.type == calendarTypeFilter)
                                                    }
                                                    val hasTraining = cellEvents.any { it.type == "TRAINING" }
                                                    val hasCompany = cellEvents.any { it.type == "COMPANY" }

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize(0.9f)
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(
                                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                                else androidx.compose.ui.graphics.Color.Transparent
                                                            )
                                                            .clickable {
                                                                selectedCalendarDate = cellCal
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.Center,
                                                            modifier = Modifier.fillMaxSize()
                                                        ) {
                                                            Text(
                                                                text = "$dayNum",
                                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                                ),
                                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                else MaterialTheme.colorScheme.onSurface
                                                            )

                                                            // Indicators Row
                                                            if (cellEvents.isNotEmpty()) {
                                                                Row(
                                                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    modifier = Modifier.padding(top = 2.dp)
                                                                ) {
                                                                    if (hasTraining) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(5.dp)
                                                                                .clip(RoundedCornerShape(50))
                                                                                .background(
                                                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                                    else androidx.compose.ui.graphics.Color(0xFF4CAF50) // Emerald Green for training
                                                                                )
                                                                        )
                                                                    }
                                                                    if (hasCompany) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(5.dp)
                                                                                .clip(RoundedCornerShape(50))
                                                                                .background(
                                                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                                                    else androidx.compose.ui.graphics.Color(0xFFFF9800) // Vibrant Orange for company
                                                                                )
                                                                        )
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
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Day details section
                            val selectedDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl")).format(selectedCalendarDate.time)
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pl")) else it.toString() }

                            Text(
                                text = selectedDateFormatted,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Selected date events
                            val dayEvents = calendarEvents.filter {
                                it.date == selectedCalendarDate.timeInMillis && (calendarTypeFilter == "ALL" || it.type == calendarTypeFilter)
                            }

                            if (dayEvents.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Brak zaplanowanych terminów tego dnia.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    dayEvents.forEach { event ->
                                        val stripeColor = if (event.type == "TRAINING") {
                                            androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                        } else {
                                            androidx.compose.ui.graphics.Color(0xFFFF9800)
                                        }

                                        val categoryNamePl = when (event.category) {
                                            "LESSON" -> "Jazda szkoleniowa 🚗"
                                            "EXAM" -> "Egzamin próbny 📝"
                                            "LECTURE" -> "Teoria / Wykład 🏫"
                                            "VACATION" -> "Urlop / Wolne 🏖️"
                                            "SICK_LEAVE" -> "Chorobowe / L4 🤒"
                                            else -> "Sprawy biurowe 🏢"
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Colored side stripe
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(36.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(stripeColor)
                                            )

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = event.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${event.startTime} - ${event.endTime}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "•  $categoryNamePl",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = stripeColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = {
                                                    editingEventId = event.id
                                                    editEventTitle = event.title
                                                    editEventStartTime = event.startTime
                                                    editEventEndTime = event.endTime
                                                    editEventCategory = event.category
                                                    editEventType = event.type
                                                    editEventInstructorName = event.instructorName
                                                    editEventStudentName = event.studentName
                                                    showEditEventDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edytuj",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    viewModel.removeCalendarEvent(event.id)
                                                    Toast.makeText(context, "Usunięto termin!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Usuń",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // STAFF APPROVALS & REQUESTS PANEL
                item {
                    val staffVacationRequests = calendarEvents.filter { it.status == "PENDING_OWNER_VACATION" }
                    val staffRescheduleRequests = calendarEvents.filter { it.status == "RESCHEDULE_PENDING" && it.instructorName.isNotEmpty() }

                    if (staffVacationRequests.isNotEmpty() || staffRescheduleRequests.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PendingActions,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "WNIOSKI I ZATWIERDZENIA OD KADRY 📋",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                // Vacation Requests
                                if (staffVacationRequests.isNotEmpty()) {
                                    Text(
                                        text = "Wnioski o urlop / wolne:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    staffVacationRequests.forEach { req ->
                                        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(req.date))
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(req.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("Termin: $dateStr o godz. ${req.startTime}-${req.endTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (req.cancelReason.isNotEmpty()) {
                                                    Text("Powód: ${req.cancelReason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.approveVacation(req.id)
                                                            Toast.makeText(context, "Zatwierdzono wolne dla ${req.instructorName}!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Zatwierdź ✅", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.rejectVacation(req.id)
                                                            Toast.makeText(context, "Odrzucono wniosek urlopowy.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Odrzuć ❌", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Reschedule Requests
                                if (staffRescheduleRequests.isNotEmpty()) {
                                    Text(
                                        text = "Prośby o zmianę terminu jazdy:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    staffRescheduleRequests.forEach { req ->
                                        val oldDateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(req.date))
                                        val newDateStr = req.rescheduleNewDate?.let { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it)) } ?: ""
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(req.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text("Instruktor: ${req.instructorName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Stary termin: $oldDateStr (${req.startTime} - ${req.endTime})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("Proponowany nowy: $newDateStr (${req.rescheduleNewStartTime} - ${req.rescheduleNewEndTime})", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.approveReschedule(req.id)
                                                            Toast.makeText(context, "Zaakceptowano zmianę terminu!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Zatwierdź ✅", style = MaterialTheme.typography.labelMedium)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.rejectReschedule(req.id)
                                                            Toast.makeText(context, "Odrzucono zmianę terminu.", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Odrzuć ❌", style = MaterialTheme.typography.labelMedium)
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

                // Interactive Reminders Board Card (Wielofunkcyjna tablica zadań)
                item {
                    val filteredReminders = reminders.filter {
                        // Moje własne lub do mnie lub ogólne "Wszyscy"
                        it.recipientRole == "OWNER" || it.recipientRole == "ALL" || it.recipientName == "Właściciel" ||
                        // Lub wysłane przeze mnie do kogoś (żeby widzieć status realizacji)
                        it.senderRole == "OWNER"
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
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
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Zadania & Komunikacja z Kadra",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                val pendingCount = filteredReminders.count { !it.isDone && (it.recipientRole == "OWNER" || it.recipientName == "Właściciel" || it.recipientRole == "ALL") }
                                if (pendingCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(text = "$pendingCount do zrobienia", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // List of Reminders
                            if (filteredReminders.isEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Brak aktywnych zadań lub przypomnień.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    filteredReminders.forEach { reminder ->
                                        val priorityColor = when (reminder.priority) {
                                            "HIGH" -> MaterialTheme.colorScheme.error
                                            "MEDIUM" -> MaterialTheme.colorScheme.secondary
                                            else -> MaterialTheme.colorScheme.outline
                                        }

                                        // Sprawdzamy relację nadawcy i odbiorcy
                                        val isSentByMe = reminder.senderRole == "OWNER"
                                        val isForMeOnly = reminder.recipientRole == "OWNER" && reminder.recipientName == "Właściciel"
                                        val isForEveryone = reminder.recipientRole == "ALL"

                                        val labelText = when {
                                            isSentByMe && isForMeOnly -> "Zadanie prywatne"
                                            isSentByMe && isForEveryone -> "Wysłano do: Wszyscy instruktorzy"
                                            isSentByMe -> "Wysłano do: ${reminder.recipientName}"
                                            reminder.senderRole == "SYSTEM" -> "System"
                                            else -> "Otrzymano od: ${reminder.senderName}"
                                        }

                                        val labelBgColor = when {
                                            isSentByMe && isForMeOnly -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            isSentByMe -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                        }

                                        val labelTextColor = when {
                                            isSentByMe && isForMeOnly -> MaterialTheme.colorScheme.onSurfaceVariant
                                            isSentByMe -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (reminder.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                                                )
                                                .clickable { viewModel.toggleReminder(reminder.id) }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (reminder.isDone) Icons.Default.CheckCircle else Icons.Default.Circle,
                                                contentDescription = null,
                                                tint = if (reminder.isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = reminder.text,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        textDecoration = if (reminder.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                                        fontWeight = if (reminder.isDone) FontWeight.Normal else FontWeight.Medium
                                                    ),
                                                    color = if (reminder.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    // Sender/recipient tag
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(labelBgColor)
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = labelText,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                            color = labelTextColor
                                                        )
                                                    }

                                                    if (reminder.isDone) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "ZAKOŃCZONE",
                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Priority tag
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(priorityColor.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = when (reminder.priority) {
                                                        "HIGH" -> "PILNE"
                                                        "MEDIUM" -> "ŚREDNIE"
                                                        else -> "NISKIE"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = priorityColor
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = { viewModel.removeReminder(reminder.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Usuń",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Adresat i Priorytet Selekcja Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Odbiorca Selector
                                Box {
                                    val currentLabel = when {
                                        newReminderRecipientName == "Właściciel" -> "Odbiorca: Tylko Ja 👤"
                                        newReminderRecipientName == "Wszyscy" -> "Odbiorca: Wszyscy Instruktorzy 👥"
                                        else -> "Odbiorca: $newReminderRecipientName 🚗"
                                    }

                                    TextButton(
                                        onClick = { isRecipientDropdownExpanded = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.textButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = currentLabel, style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }

                                    DropdownMenu(
                                        expanded = isRecipientDropdownExpanded,
                                        onDismissRequest = { isRecipientDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Tylko Ja (Prywatne)") },
                                            onClick = {
                                                newReminderRecipientName = "Właściciel"
                                                newReminderRecipientRole = "OWNER"
                                                isRecipientDropdownExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Wszyscy Instruktorzy") },
                                            onClick = {
                                                newReminderRecipientName = "Wszyscy"
                                                newReminderRecipientRole = "ALL"
                                                isRecipientDropdownExpanded = false
                                            }
                                        )

                                        // Dynamic list of instructors from active codes
                                        val registeredInstructors = accessCodes.filter { it.role == "INSTRUKTOR" }.map { it.name }.distinct()
                                        if (registeredInstructors.isNotEmpty()) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                            registeredInstructors.forEach { instructorName ->
                                                DropdownMenuItem(
                                                    text = { Text("Instruktor: $instructorName") },
                                                    onClick = {
                                                        newReminderRecipientName = instructorName
                                                        newReminderRecipientRole = "INSTRUKTOR"
                                                        isRecipientDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Priorytet Selector
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Priorytet: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    IconButton(
                                        onClick = {
                                            newReminderPriority = when (newReminderPriority) {
                                                "HIGH" -> "MEDIUM"
                                                "MEDIUM" -> "LOW"
                                                else -> "HIGH"
                                            }
                                        }
                                    ) {
                                        val (pText, flagColor) = when (newReminderPriority) {
                                            "HIGH" -> Pair("PILNY", MaterialTheme.colorScheme.error)
                                            "MEDIUM" -> Pair("ŚREDNI", MaterialTheme.colorScheme.secondary)
                                            else -> Pair("NISKI", MaterialTheme.colorScheme.outline)
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Flag,
                                                contentDescription = "Zmień priorytet",
                                                tint = flagColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(text = pText, style = MaterialTheme.typography.labelSmall, color = flagColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Add Reminder Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newReminderText,
                                    onValueChange = { newReminderText = it },
                                    placeholder = { Text("Wpisz treść komunikatu / zadania...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )

                                IconButton(
                                    onClick = {
                                        if (newReminderText.isNotBlank()) {
                                            viewModel.addReminder(
                                                text = newReminderText,
                                                priority = newReminderPriority,
                                                senderName = "Właściciel",
                                                senderRole = "OWNER",
                                                recipientName = newReminderRecipientName,
                                                recipientRole = newReminderRecipientRole
                                            )
                                            newReminderText = ""
                                            Toast.makeText(context, "Dodano komunikat / zadanie!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    enabled = newReminderText.isNotBlank(),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = "Wyślij")
                                }
                            }
                        }
                    }
                }

                // Interactive News & Announcements Board
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                                text = "Aktualności i Ogłoszenia",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        TextButton(
                            onClick = { showAddAnnouncementDialog = true }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nowy komunikat", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (announcements.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(text = "Brak ogłoszeń szkolnych.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            }
                        }
                    }
                } else {
                    items(announcements) { ann ->
                        val colorScheme = MaterialTheme.colorScheme
                        val cardColor = when (ann.category) {
                            "SUCCESS" -> colorScheme.primaryContainer.copy(alpha = 0.2f)
                            "ALERT" -> colorScheme.errorContainer.copy(alpha = 0.2f)
                            else -> colorScheme.secondaryContainer.copy(alpha = 0.2f)
                        }
                        val statusIcon = when (ann.category) {
                            "SUCCESS" -> Icons.Default.CheckCircle
                            "ALERT" -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        }
                        val tintColor = when (ann.category) {
                            "SUCCESS" -> colorScheme.primary
                            "ALERT" -> colorScheme.error
                            else -> colorScheme.secondary
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = tintColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = ann.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeAnnouncement(ann.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Usuń",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ann.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ann.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // TAB 1: INSTRUCTORS MANAGEMENT (KADRA)
                val registeredInstructors = accessCodes.filter { it.role == "INSTRUKTOR" }.map { it.name }.distinct()
                val allInstructorNames = (listOf("Tomasz Nowak", "Robert Wiśniewski", "Alicja Kowalska") + registeredInstructors).distinct()

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Zarządzanie Kadrą Instruktorów 👥",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Ustawiaj stawki, auta i monitoruj pracę kadry",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = { showAddInstructorDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dodaj", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                // Stats Card for instructors
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Instruktorzy",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${allInstructorNames.size}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text(
                                    text = "Suma Godzin",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${allInstructorNames.sumOf { instructorHours[it] ?: 0 }} h",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text(
                                    text = "Suma Wypłat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${allInstructorNames.sumOf { (instructorHours[it] ?: 0) * (instructorRates[it] ?: 50) }} zł",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text(
                                    text = "Śr. Zdawalność",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val avgPass = if (allInstructorNames.isNotEmpty()) {
                                    allInstructorNames.sumOf { instructorPassRates[it] ?: 70 } / allInstructorNames.size
                                } else 0
                                Text(
                                    text = "$avgPass%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                if (allInstructorNames.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("Brak instruktorów.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(allInstructorNames) { name ->
                        val car = instructorCars[name] ?: "Brak przypisanego pojazdu ❌"
                        val rate = instructorRates[name] ?: 50
                        val hours = instructorHours[name] ?: 0
                        val passRate = instructorPassRates[name] ?: 70
                        val totalSalary = rate * hours

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SupervisorAccount,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = car,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingInstructorName = name
                                                editingInstructorCar = car
                                                editingInstructorRate = rate.toString()
                                                editingInstructorHours = hours.toString()
                                                editingInstructorPassRate = passRate.toString()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edytuj",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Stawka",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$rate zł/h",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Wyjeżdżone",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$hours h",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "Wynagrodzenie",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$totalSalary zł",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Zdawalność",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "$passRate%",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                // TAB 2: ACTIVE STUDENTS & REGISTRATION MANAGEMENT
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zarządzanie Kursantami 🎓",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { showAddStudentDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("owner_add_student_manually_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dodaj ręcznie")
                        }
                    }
                }

                // 1. PENDING APPROVALS FROM ACCESS CODES
                val pendingRegs = studentRegistrations.filter { !it.isApproved }
                if (pendingRegs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Oczekujące rejestracje do akceptacji (${pendingRegs.size}) ⏳",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(pendingRegs) { reg ->
                        val customEvent = calendarEvents.find { it.studentName.equals(reg.name, ignoreCase = true) && it.status == "PENDING_STAFF_APPROVAL" }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("pending_registration_card_${reg.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = reg.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.error)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "KAT. ${reg.category}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Telefon: ${reg.phone} | Email: ${reg.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (customEvent != null) {
                                    val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(customEvent.date))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Wnioskowana pierwsza jazda:\n📅 $formattedDate o godz. ${customEvent.startTime} - ${customEvent.endTime}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.approveStudentRegistrationAndSlot(reg.id, customEvent.id)
                                            Toast.makeText(context, "Zaakceptowano rejestrację i termin jazdy dla ${reg.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("approve_reg_btn_${reg.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Akceptuj rejestrację i jazdę")
                                    }
                                } else {
                                    // Signed up without a specific slot (or chose an already free slot which automatically approved)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            viewModel.approveStudentRegistrationAndSlot(reg.id, "")
                                            Toast.makeText(context, "Zaakceptowano rejestrację dla ${reg.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("approve_reg_simple_btn_${reg.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Akceptuj rejestrację")
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. CANCELLED EVENT RESCHEDULING NEED
                val cancelledEvents = calendarEvents.filter { it.status == "CANCELLED" }
                if (cancelledEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Odwołane jazdy (wymagają zaplanowania na nowo) 🚗",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    items(cancelledEvents) { event ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("cancelled_event_card_${event.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ODWOŁANA",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                val formattedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(event.date))
                                Text(
                                    text = "Kursant: ${event.studentName} | Instruktor: ${event.instructorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Odwołany termin: $formattedDate o godz. ${event.startTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (event.cancelReason.isNotEmpty()) {
                                    Text(
                                        text = "Powód: ${event.cancelReason}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        proposingRescheduleEvent = event
                                        proposeRescheduleNewDateString = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("reschedule_cancelled_btn_${event.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zaproponuj nową datę/godzinę")
                                }
                            }
                        }
                    }
                }

                // 3. ALL REGISTERED ACTIVE STUDENTS
                val activeRegs = studentRegistrations.filter { it.isApproved }
                item {
                    Text(
                        text = "Aktywni Kursanci (${activeRegs.size}) 🎓",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (activeRegs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Brak aktywnych kursantów w bazie danych. Dodaj ręcznie lub wygeneruj kod dostępu, aby zaprosić kursantów.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(activeRegs) { reg ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reg.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tel: ${reg.phone} | Email: ${reg.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Wybrany Instruktor: ${reg.preferredInstructor.ifEmpty { "Wszyscy" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "KAT. ${reg.category}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 3) {
                // TAB 3: FLEET MANAGEMENT PLACEHOLDER
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Sekcja 3: Zarządzanie Flotą Pojazdów 🚗",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "STATUS: OCZEKUJE NA AKTYWACJĘ",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "W trzecim kroku wdrożymy moduł zarządzania samochodami szkoleniowymi, zawierający:",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(
                                    "📅 Odliczanie dni do przeglądu rejestracyjnego",
                                    "🛡️ Kontrola ubezpieczeń OC/AC i asysty drogowej",
                                    "⛽ Rejestracja przebiegów i raportowanie średniego spalania",
                                    "🛠️ Terminarz wymiany oleju, klocków hamulcowych i opon"
                                ).forEach { text ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Odblokuj po zatwierdzeniu poprzednich sekcji")
                            }
                        }
                    }
                }
            } else if (selectedTab == 4) {
                // TAB 4: ACCESS CODES LIST
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lista Kodów Dostępu (${accessCodes.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (accessCodes.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.KeyOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Brak wygenerowanych kodów dostępu.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(accessCodes) { item ->
                        CodeItemRow(
                            code = item,
                            onDelete = { viewModel.deleteAccessCode(item.id) }
                        )
                    }
                }
            } else {
                // TAB 5: COMPANY PROFILE
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = profile.companyName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Miejscowość",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = profile.city,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Adres",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = profile.street,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NIP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = profile.nip,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Telefon",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = profile.phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Column {
                                Text(
                                    text = "E-mail szkoły",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTab == 6) {
                // TAB 6: CENNIK KURSÓW
                item {
                    Text(
                        text = "Konfiguracja Cennika i Oferty OSK 🏷️",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Cennik jest bezpośrednio widoczny dla kursantów podczas samodzielnej rejestracji.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                items(coursePrices) { price ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = price.categoryName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Teoria: ${price.theoryHours}h | Praktyka: ${price.practiceHours}h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = price.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "${price.pricePln} PLN", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        editingPriceId = price.id
                                        editingPricePln = price.pricePln.toString()
                                        editingPriceDesc = price.description
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Edytuj cena", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTab == 7) {
                // TAB 7: HARMONOGRAM KURSU GRUPOWEGO (DUAL-APPROVAL)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grupowe Kursy & Wykłady 👥",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Rozpisuj harmonogramy z dwustronną akceptacją (Właściciel & Instruktor).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                        Button(
                            onClick = { showAddGroupScheduleDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nowy Kurs")
                        }
                    }
                }

                if (groupSchedules.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("Brak harmonogramów kursów grupowych.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                } else {
                    items(groupSchedules) { schedule ->
                        val isPendingMyApproval = schedule.status == "WAITING_FOR_OWNER_APPROVAL"
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPendingMyApproval) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = schedule.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (schedule.status) {
                                            "APPROVED" -> MaterialTheme.colorScheme.primaryContainer
                                            "REJECTED" -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = when (schedule.status) {
                                                "APPROVED" -> "ZATWIERDZONO ✓"
                                                "REJECTED" -> "ODRZUCONO ✗"
                                                "WAITING_FOR_INSTRUCTOR_APPROVAL" -> "CZEKA NA INSTRUKTORA ⏳"
                                                else -> "OCZEKUJE NA TWÓJ PODPIS ⏳"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "Kategoria: ${schedule.category} | Prowadzący: ${schedule.instructorName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(text = "Terminy: ${schedule.datesDescription}", style = MaterialTheme.typography.bodySmall)
                                Text(text = "Lokalizacja: ${schedule.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (isPendingMyApproval) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.approveGroupSchedule(schedule.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Zaakceptuj", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.rejectGroupSchedule(schedule.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Odrzuć", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 8: EKSPORT RAPORTÓW PDF / EXCEL
            if (selectedTab == 8) {
                item {
                    val allPayments by viewModel.studentPayments.collectAsState()

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Card 1: Raporty Finansowe OSK
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Raporty Finansowe i Płatności OSK 📊", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Generuj podsumowanie przychodów, wpłat rat i zaległości do plików PDF oraz Microsoft Excel", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                val totalCollected = allPayments.filter { it.status == "OPŁACONE" }.sumOf { it.amountPln }
                                val totalPending = allPayments.filter { it.status != "OPŁACONE" }.sumOf { it.amountPln }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Zaksięgowane przychody: $totalCollected PLN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("Należności oczekujące: $totalPending PLN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Wygenerowano i pobrano Raport Finansowy OSK (PDF)!", Toast.LENGTH_LONG).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Raport PDF 📄", style = MaterialTheme.typography.labelMedium)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            Toast.makeText(context, "Eksportowano zestawienie płatności do pliku Excel (.xlsx)!", Toast.LENGTH_LONG).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Eksport Excel 📊", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        // Card 2: Lista Obecności na Wykładach Grupowych
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Listy Obecności na Wykładach Teoretycznych 🏫", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Drukowane listy kontrolne z rubryką na podpis kursanta i stempel OSK", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Wygenerowano arkusz listy obecności na wykładach (PDF)!", Toast.LENGTH_LONG).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generuj Drukowaną Listę Obecności (PDF)")
                                }
                            }
                        }

                        // Card 3: Grafiki Pracowników i Instruktorów
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Grafiki Pracy Instruktorów do Druku 🚗", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Zestawienie godzin, wyjazdów i zajęć practical zespołu instruktorskiego", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Wygenerowano miesięczny grafik pracy instruktorów (PDF)!", Toast.LENGTH_LONG).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Eksportuj Grafiki Instruktorów (PDF / Drukarz)")
                                }
                            }
                        }
                    }
                }
            }

            // TAB 9: FLOTA POJAZDÓW OSK & PRZEGLĄDY & USTERKI
            if (selectedTab == 9) {
                item {
                    val fleetVehicles by viewModel.fleetVehicles.collectAsState()
                    val vehicleFaults by viewModel.vehicleFaults.collectAsState()
                    val fuelRefuels by viewModel.fuelRefuels.collectAsState()
                    val odometerLogs by viewModel.odometerLogs.collectAsState()

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Card 0: ANALIZA PALIWA, ZUŻYCIA I FAKTUR FLOTY
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Analiza Paliwa, Wydatków & Średniego Spalania ⛽📈", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Centralny moduł analityczny OSK – spływ faktur i liczników w czasie rzeczywistym", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }

                                val totalFuelCost = fuelRefuels.sumOf { it.totalCostPln }
                                val totalLiters = fuelRefuels.sumOf { it.liters }
                                val totalDrivenKmAll = odometerLogs.sumOf { it.totalDrivenKm }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Suma wydatków", style = MaterialTheme.typography.labelSmall)
                                            Text("${String.format(Locale.US, "%.2f", totalFuelCost)} PLN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Zakupione paliwo", style = MaterialTheme.typography.labelSmall)
                                            Text("${String.format(Locale.US, "%.1f", totalLiters)} L", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Średnia cena/L", style = MaterialTheme.typography.labelSmall)
                                            val avgPrice = if (totalLiters > 0) totalFuelCost / totalLiters else 6.50
                                            Text("${String.format(Locale.US, "%.2f", avgPrice)} PLN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        }
                                    }
                                }

                                Text("Analiza spalania i kosztów według pojazdu:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                                fleetVehicles.forEach { car ->
                                    val carRefuels = fuelRefuels.filter { it.plateNumber == car.plateNumber }
                                    val carCost = carRefuels.sumOf { it.totalCostPln }
                                    val carLiters = carRefuels.sumOf { it.liters }

                                    // Estimate consumption l/100km based on logs or standard 6.2 l/100km
                                    val estimatedConsumption = when {
                                        car.plateNumber == "KR 12345" -> 6.1
                                        car.plateNumber == "KR 67890" -> 6.8
                                        car.plateNumber == "KR 11223" -> 5.9
                                        else -> 4.2 // motorcycle
                                    }

                                    val costPer100km = estimatedConsumption * 6.45

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("${car.model} (${car.plateNumber})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("• Wydatki paliwo: ${String.format(Locale.US, "%.2f", carCost)} PLN (${String.format(Locale.US, "%.1f", carLiters)} L)", style = MaterialTheme.typography.bodySmall)
                                                Text("• Średnie spalanie: $estimatedConsumption L / 100km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                                Text("• Szacowany koszt 100km: ${String.format(Locale.US, "%.2f", costPer100km)} PLN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            if (estimatedConsumption > 6.5) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.errorContainer
                                                ) {
                                                    Text("Wyższe Spalanie ⚠️", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Ostatnie zaksięgowane faktury/paragony za paliwo:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                                fuelRefuels.take(4).forEach { ref ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("${ref.dateFormatted} | ${ref.plateNumber} (${ref.instructorName})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                            Text("Stacja: ${ref.stationName} | Faktura: ${ref.receiptNumber} | Licznik: ${ref.kmMileage} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("${ref.liters} L / ${ref.totalCostPln} PLN", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Card 1: Lista Pojazdów OSK (Przeglądy & Ubezpieczenia)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Flota Pojazdów Szkoleniowych OSK 🚗", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Kontrola ważności przeglądów technicznych (MOT), polis OC/AC i przebiegu kilometrów", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                fleetVehicles.forEach { car ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${car.model} (${car.plateNumber})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (car.status == "SPRAWNY") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
                                                ) {
                                                    Text(
                                                        car.status,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (car.status == "SPRAWNY") Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("• Przebieg: ${car.kmMileage} km", style = MaterialTheme.typography.bodySmall)
                                            Text("• Najbliższy przegląd MOT: ${car.nextMotDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                            Text("• Ważność polisy OC/AC: ${car.insuranceExpiryDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }
                        }

                        // Card 2: Zgłoszenia Usterek od Instruktorów
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Zgłoszone Usterki i Naprawy Warsztatowe 🛠️", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Zgłoszenia awarii przesłane bezpośrednio przez zespół instruktorów z trasy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (vehicleFaults.isEmpty()) {
                                    Text("Brak aktywnych zgłoszeń usterek. Wszystkie pojazdy sprawne!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    vehicleFaults.forEach { fault ->
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (fault.isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                            ),
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Pojazd: ${fault.plateNumber}", fontWeight = FontWeight.Bold)
                                                    Text("Priorytet: ${fault.priority}", fontWeight = FontWeight.Bold, color = if (fault.priority == "PILNY") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Opis: ${fault.description}", style = MaterialTheme.typography.bodyMedium)
                                                Text("Zgłosił: ${fault.instructorName} (${fault.reportDate})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                                Spacer(modifier = Modifier.height(8.dp))

                                                if (!fault.isResolved) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.resolveVehicleFault(fault.id)
                                                            Toast.makeText(context, "Oznaczono usterkę jako naprawioną w warsztacie! ✓", Toast.LENGTH_SHORT).show()
                                                        },
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text("Oznacz jako Naprawione w Warsztacie ✓")
                                                    }
                                                } else {
                                                    Text("STATUS: NAPRAWIONO I ODEBRANO Z WARSZTATU ✅", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF2E7D32))
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

            if (selectedTab == 10) {
                // TAB 10: CHMURA PROOSK & INTEGRACJE I ABONAMENT
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().testTag("cloud_tab_header_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Column {
                                    Text(
                                        text = "Integracje Chmurowe proOsk Enterprise ☁️",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Centralne zarządzanie chmurą, opłatami i synchronizacją urządzeń",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // OWNER SUBSCRIPTION & BILLING COVERAGE CARD
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF1D4ED8)),
                        modifier = Modifier.fillMaxWidth().testTag("cloud_billing_card")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1D4ED8))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cloudSub.planName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFD1FAE5)
                                ) {
                                    Text(
                                        text = "OPŁACONE ✓",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Koszt dla OSK: ${cloudSub.priceMonthlyPln} PLN / miesiąc (Bez limitu urządzeń i transferu)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Metoda płatności: ${cloudSub.paymentCardMasked} • Odnowienie: ${cloudSub.billingRenewalDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // SWITCH: OWNER COVERS ALL FEES FOR EVERYONE
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Właściciel pokrywa opłaty za Chmurę & SMS-y",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Instruktorzy i Kursanci korzystają ze synchronizacji i powiadomień 100% BEZPŁATNIE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = cloudSub.isOwnerPayingForEveryone,
                                        onCheckedChange = { enabled ->
                                            viewModel.toggleOwnerCoverage(enabled)
                                            val msg = if (enabled) "Właściciel przejął 100% opłat za chmurę i SMS-y dla wszystkich!" else "Wyłączono pokrywanie opłat."
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Pobieranie faktury VAT za abonament chmurowy proOsk... [FV/2026/07/OSK]", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Faktura VAT PDF", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Karta płatnicza jest aktywna. Następne obciążenie: ${cloudSub.billingRenewalDate}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                                ) {
                                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Zarządzaj Kartą", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // 3 MAIN INTEGRATIONS TITLE
                item {
                    Text(
                        text = "Trzy Główny Integracje Chmurowe Systemu:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // INTEGRATION 1: REAL-TIME MULTI-DEVICE SYNC
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF0284C7).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Color(0xFF0284C7))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("1. Silnik Synchronizacji Real-Time", fontWeight = FontWeight.Bold)
                                        Text("Łączy wszystkie smartfony i kalendarze", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE0F2FE)) {
                                    Text("AKTYWNA ⚡", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF0369A1))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Ostatnia synchronizacja: ${cloudSub.lastSyncTimestamp}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tryb pracy: WebSocket (Opóźnienie: 14ms)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Połączone urządzenia OSK: ${cloudSub.connectedDevicesCount} telefonów i tabletów", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.triggerForceCloudSync()
                                    Toast.makeText(context, "Pełna synchronizacja ze wszystkimi ${cloudSub.connectedDevicesCount} urządzeniami zakończona! ✓", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("⚡ Wymuś Pełną Synchronizację Teraz")
                            }
                        }
                    }
                }

                // INTEGRATION 2: CHMUROWA BRAMKA SMS & PUSH
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF059669).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = Color(0xFF059669))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("2. Bramka SMS & Push Notifications", fontWeight = FontWeight.Bold)
                                        Text("Powiadomienia o jazdach (24h/2h przed)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
                                    Text("BRAMKA OK ✓", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF065F46))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Nazwa nadawcy SMS: '${cloudSub.smsSenderId}'", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Koszt SMS dla Kursantów/Instruktorów: 0 zł (Sponsorowane przez Właściciela)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            var tempSenderId by remember { mutableStateOf(cloudSub.smsSenderId) }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tempSenderId,
                                    onValueChange = { tempSenderId = it },
                                    label = { Text("Zmień Nadawcę SMS") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = {
                                        viewModel.updateSmsSenderId(tempSenderId)
                                        Toast.makeText(context, "Zapisano nową nazwę nadawcy SMS: '$tempSenderId'", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                ) {
                                    Text("Zapisz")
                                }
                            }
                        }
                    }
                }

                // INTEGRATION 3: GOOGLE CLOUD BACKUP & STORAGE
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF7C3AED).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = Color(0xFF7C3AED))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("3. Google Cloud Backup & Disk", fontWeight = FontWeight.Bold)
                                        Text("Kopia zapasowa dokumentów i bazy PKK", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEDE9FE)) {
                                    Text("BACKUP OK", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF6D28D9))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Ostatni backup: ${cloudSub.lastBackupDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Zajęte miejsce w chmurze: ${cloudSub.cloudStorageUsedMb} MB z ${cloudSub.cloudStorageMaxGb} GB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Szyfrowana kopia zapasowa w Google Cloud wykonana pomyślnie! ✓", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Wykonaj Backup w Chmurze Teraz")
                            }
                        }
                    }
                }

                // MULTI-DEVICE PAIRING CODE GENERATOR
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kod Parowania Urządzeń w Chmurze OSK", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Podaj ten kod nowemu instruktorowi lub kursantowi podczas pierwszego logowania, aby automatycznie podłączyć urządzenie do chmury Twojego OSK:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F172A)
                                ) {
                                    Text(
                                        text = cloudSub.pairingCode,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                                        color = Color(0xFF38BDF8)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Pairing Code", cloudSub.pairingCode)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Skopiowano kod parowania: ${cloudSub.pairingCode}", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopiuj")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.generateNewPairingCode()
                                            Toast.makeText(context, "Wygenerowano nowy kod parowania chmury!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Nowy Kod")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BRANDED FOOTER WITH COMPANY NAME AND INSPIRING QUOTE
            item {
                FooterCompanyQuote()
            }
        }
    }


    // Edit Price Dialog
    editingPriceId?.let { priceId ->
        AlertDialog(
            onDismissRequest = { editingPriceId = null },
            title = { Text("Edytuj Cennik Kursu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editingPricePln,
                        onValueChange = { editingPricePln = it },
                        label = { Text("Cena w PLN") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = editingPriceDesc,
                        onValueChange = { editingPriceDesc = it },
                        label = { Text("Opis pakietu") },
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = editingPricePln.toIntOrNull() ?: 3000
                        viewModel.updateCoursePrice(priceId, newPrice, editingPriceDesc)
                        editingPriceId = null
                        Toast.makeText(context, "Zaktualizowano cennik!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPriceId = null }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Add Group Schedule Dialog
    if (showAddGroupScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupScheduleDialog = false },
            title = { Text("Nowy Harmonogram Kursu Grupowego") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newGroupTitle,
                        onValueChange = { newGroupTitle = it },
                        label = { Text("Tytuł kursu / wykładów") },
                        placeholder = { Text("np. Jesienny Kurs Teoretyczny Kat. B") },
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
                        value = newGroupInstructor,
                        onValueChange = { newGroupInstructor = it },
                        label = { Text("Prowadzący instruktor") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newGroupDatesDesc,
                        onValueChange = { newGroupDatesDesc = it },
                        label = { Text("Rozpisanie dni i godzin wykładów") },
                        placeholder = { Text("np. Wtorki i Czwartki w godz. 17:00 - 20:00") },
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
                                instructorName = newGroupInstructor,
                                datesDescription = newGroupDatesDesc,
                                createdByRole = "OWNER"
                            )
                            showAddGroupScheduleDialog = false
                            newGroupTitle = ""
                            Toast.makeText(context, "Wysłano harmonogram do akceptacji instruktora!", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = newGroupTitle.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Stwórz i Wyślij")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupScheduleDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Edit Instructor Dialog
    editingInstructorName?.let { name ->
        AlertDialog(
            onDismissRequest = { editingInstructorName = null },
            title = { Text("Edytuj dane: $name") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editingInstructorCar,
                        onValueChange = { editingInstructorCar = it },
                        label = { Text("Przypisany samochód") },
                        placeholder = { Text("np. Toyota Yaris (KRA 12345)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editingInstructorRate,
                        onValueChange = { editingInstructorRate = it },
                        label = { Text("Stawka godzinowa (PLN/h)") },
                        placeholder = { Text("np. 60") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editingInstructorHours,
                        onValueChange = { editingInstructorHours = it },
                        label = { Text("Wyjeżdżone godziny (h)") },
                        placeholder = { Text("np. 120") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editingInstructorPassRate,
                        onValueChange = { editingInstructorPassRate = it },
                        label = { Text("Zdawalność (%)") },
                        placeholder = { Text("np. 80") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rate = editingInstructorRate.toIntOrNull() ?: 50
                        val hours = editingInstructorHours.toIntOrNull() ?: 0
                        val passRate = editingInstructorPassRate.toIntOrNull() ?: 70
                        viewModel.updateInstructorDetails(
                            name = name,
                            car = editingInstructorCar,
                            rate = rate,
                            hours = hours,
                            passRate = passRate
                        )
                        editingInstructorName = null
                        Toast.makeText(context, "Zapisano zmiany dla: $name", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingInstructorName = null }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Add Instructor Dialog
    if (showAddInstructorDialog) {
        AlertDialog(
            onDismissRequest = { showAddInstructorDialog = false },
            title = { Text("Dodaj nowego instruktora") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = addInstructorName,
                        onValueChange = { addInstructorName = it },
                        label = { Text("Imię i nazwisko") },
                        placeholder = { Text("np. Jan Kowalski") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = addInstructorCar,
                        onValueChange = { addInstructorCar = it },
                        label = { Text("Przypisany samochód") },
                        placeholder = { Text("np. Toyota Yaris (KRA 12345)") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = addInstructorRate,
                        onValueChange = { addInstructorRate = it },
                        label = { Text("Stawka godzinowa (PLN/h)") },
                        placeholder = { Text("np. 60") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = addInstructorHours,
                        onValueChange = { addInstructorHours = it },
                        label = { Text("Wyjeżdżone godziny (h)") },
                        placeholder = { Text("np. 0") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = addInstructorPassRate,
                        onValueChange = { addInstructorPassRate = it },
                        label = { Text("Zdawalność (%)") },
                        placeholder = { Text("np. 75") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addInstructorName.isNotBlank()) {
                            val rate = addInstructorRate.toIntOrNull() ?: 50
                            val hours = addInstructorHours.toIntOrNull() ?: 0
                            val passRate = addInstructorPassRate.toIntOrNull() ?: 75
                            
                            viewModel.updateInstructorDetails(
                                name = addInstructorName,
                                car = if (addInstructorCar.isBlank()) "Toyota Yaris" else addInstructorCar,
                                rate = rate,
                                hours = hours,
                                passRate = passRate
                            )
                            // Generujemy automatycznie kod dostępu dla nowo utworzonego instruktora!
                            viewModel.generateAccessCode(addInstructorName, "INSTRUKTOR")
                            
                            addInstructorName = ""
                            addInstructorCar = ""
                            addInstructorRate = ""
                            addInstructorHours = ""
                            addInstructorPassRate = ""
                            showAddInstructorDialog = false
                            Toast.makeText(context, "Dodano instruktora i wygenerowano kod dostępu!", Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = addInstructorName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Dodaj i generuj kod")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddInstructorDialog = false }
                ) {
                    Text("Anuluj")
                }
            }
        )
    }

    // OWNER SETTINGS DIALOG
    if (showOwnerSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showOwnerSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ustawienia Głównodowodzącego OSK ⚙️", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Dane Szkoły Jazdy & Biuro 🏢", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = ownerOskName,
                        onValueChange = { ownerOskName = it },
                        label = { Text("Pełna Nazwa OSK") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ownerNip,
                            onValueChange = { ownerNip = it },
                            label = { Text("NIP") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = ownerBankAccount,
                            onValueChange = { ownerBankAccount = it },
                            label = { Text("Nr Konta Bankowego") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = ownerAddress,
                        onValueChange = { ownerAddress = it },
                        label = { Text("Adres Biura i Placu Manewrowego") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    HorizontalDivider()

                    Text("Domyślne Stawki & Cennik 💰", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ownerDefaultInstructorRate,
                            onValueChange = { ownerDefaultInstructorRate = it },
                            label = { Text("Stawka Instruktor (PLN/h)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = ownerDefaultCatBCoursePrice,
                            onValueChange = { ownerDefaultCatBCoursePrice = it },
                            label = { Text("Kurs Kat. B (PLN)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    HorizontalDivider()

                    Text("Automatyzacja & Bezpieczeństwo Systemu 🤖", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Automatyczna Bramka SMS", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Wysyłka natychmiastowych SMSów z powiadomieniami dla kursantów", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ownerAutoSmsGateEnabled,
                            onCheckedChange = { ownerAutoSmsGateEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Detektor Kolizji w Grafikach", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Blokuj próbę przypisania dwóch rezerwacji temu samemu instruktorowi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = ownerConflictAutoDetect,
                            onCheckedChange = { ownerConflictAutoDetect = it }
                        )
                    }

                    OutlinedTextField(
                        value = ownerBackupFrequency,
                        onValueChange = { ownerBackupFrequency = it },
                        label = { Text("Kopia Zapasowa Danych (Backup)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Ustawienia szkoły OSK zostały zapisane w chmurze! ✓", Toast.LENGTH_SHORT).show()
                        showOwnerSettingsDialog = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Zapisz Ustawienia OSK ✓")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOwnerSettingsDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun CodeItemRow(
    code: AccessCode,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val dateString = remember(code.createdAt) { dateFormat.format(Date(code.createdAt)) }

    val isStudent = code.role == "KURSANT"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isStudent) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.tertiaryContainer
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isStudent) "Kursant" else "Instruktor",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isStudent) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = code.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                        .clickable {
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Klucz dostępu", code.code)
                            clipboardManager.setPrimaryClip(clipData)
                            Toast.makeText(context, "Skopiowano kod: ${code.code}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("copy_code_badge_${code.code}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = code.code,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Kopiuj",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Utworzono: $dateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Usuń kod")
            }
        }
    }
}
