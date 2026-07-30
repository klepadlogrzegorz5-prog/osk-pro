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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.db.ReservationEntity
import com.example.ui.components.EmptyState
import com.example.ui.viewmodel.UserRole

@Composable
fun ReservationsScreen(
    currentRole: UserRole,
    userName: String,
    reservations: List<ReservationEntity>,
    onOpenReservationModal: () -> Unit,
    onUpdateStatus: (id: Long, newStatus: String) -> Unit,
    onProposeAlternatives: (id: Long, alternatives: String, note: String) -> Unit = { _, _, _ -> },
    onAcceptAlternative: (id: Long, chosenDate: String, chosenTimeSlot: String) -> Unit = { _, _, _ -> },
    onDeleteReservation: (id: Long) -> Unit,
    onScheduleExam: (studentName: String, pkkNumber: String) -> Unit = { _, _ -> }
) {
    var selectedFilter by remember { mutableStateOf("WSZYSTKIE") }
    var targetReservationForAlternatives by remember { mutableStateOf<ReservationEntity?>(null) }

    // 1. GDPR & Tenant Isolation Filter: Students see ONLY their own reservations.
    // Managers and instructors see ALL reservations for their school.
    val roleFilteredReservations = remember(reservations, currentRole, userName) {
        if (currentRole == UserRole.STUDENT) {
            reservations.filter { it.fullName.trim().equals(userName.trim(), ignoreCase = true) }
        } else {
            reservations
        }
    }

    val finalFilteredList = when (selectedFilter) {
        "OCZEKUJĄCE" -> roleFilteredReservations.filter { it.status == "Oczekująca" }
        "ALTERNATYWY" -> roleFilteredReservations.filter { it.status == "Zaproponowano alternatywę" }
        "ZATWIERDZONE" -> roleFilteredReservations.filter { it.status == "Zatwierdzona" }
        "ODRZUCONE" -> roleFilteredReservations.filter { it.status == "Odrzucona" }
        else -> roleFilteredReservations
    }

    Scaffold(
        containerColor = Color(0xFF0B0F19),
        floatingActionButton = {
            if (currentRole == UserRole.STUDENT) {
                FloatingActionButton(
                    onClick = onOpenReservationModal,
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_reservation_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Zarezerwuj termin")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentRole == UserRole.STUDENT) "Twoje Zgłoszenia Rezerwacji" else "Rezerwacje & Zgłoszenia",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = if (currentRole == UserRole.STUDENT) {
                            "Statusy Twoich zapytań o jazdę: ${roleFilteredReservations.size}"
                        } else {
                            "Wszystkie zgłoszenia w systemie: ${roleFilteredReservations.size}"
                        },
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                if (currentRole == UserRole.STUDENT) {
                    Button(
                        onClick = onOpenReservationModal,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nowe zapytanie", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = selectedFilter == "WSZYSTKIE",
                    onClick = { selectedFilter = "WSZYSTKIE" },
                    label = { Text("Wszystkie (${roleFilteredReservations.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF131B2E),
                        labelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "WSZYSTKIE",
                        borderColor = Color.White.copy(alpha = 0.1f),
                        selectedBorderColor = Color(0xFF38BDF8)
                    )
                )
                FilterChip(
                    selected = selectedFilter == "OCZEKUJĄCE",
                    onClick = { selectedFilter = "OCZEKUJĄCE" },
                    label = { Text("Oczekujące (${roleFilteredReservations.count { it.status == "Oczekująca" }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF131B2E),
                        labelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "OCZEKUJĄCE",
                        borderColor = Color.White.copy(alpha = 0.1f),
                        selectedBorderColor = Color(0xFFF59E0B)
                    )
                )
                FilterChip(
                    selected = selectedFilter == "ALTERNATYWY",
                    onClick = { selectedFilter = "ALTERNATYWY" },
                    label = { Text("Propozycje (${roleFilteredReservations.count { it.status == "Zaproponowano alternatywę" }})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1E293B),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF131B2E),
                        labelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == "ALTERNATYWY",
                        borderColor = Color.White.copy(alpha = 0.1f),
                        selectedBorderColor = Color(0xFFA855F7)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (finalFilteredList.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.EventAvailable,
                    title = if (currentRole == UserRole.STUDENT) "Brak Twoich rezerwacji" else "Brak zgłoszeń rezerwacyjnych",
                    description = if (currentRole == UserRole.STUDENT) {
                        "Nie wysłałeś jeszcze żadnych zapytań o wolne terminy. Kliknij przycisk poniżej, aby wybrać dni i godziny."
                    } else {
                        "W bazie danych nie ma aktualnie zgłoszeń odpowiadających wybranym filtrom."
                    },
                    actionButtonText = if (currentRole == UserRole.STUDENT) "Zarezerwuj Termin Teraz" else "Wróć do wszystkich",
                    onActionClick = {
                        if (currentRole == UserRole.STUDENT) {
                            onOpenReservationModal()
                        } else {
                            selectedFilter = "WSZYSTKIE"
                        }
                    },
                    testTagPrefix = "reservations"
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(finalFilteredList, key = { it.id }) { item ->
                        ReservationCard(
                            reservation = item,
                            currentRole = currentRole,
                            onAccept = { onUpdateStatus(item.id, "Zatwierdzona") },
                            onReject = { onUpdateStatus(item.id, "Odrzucona") },
                            onOpenProposeDialog = { targetReservationForAlternatives = item },
                            onAcceptAlternative = { date, time -> onAcceptAlternative(item.id, date, time) },
                            onDelete = { onDeleteReservation(item.id) },
                            onScheduleExam = { onScheduleExam(item.fullName, item.pkkNumber ?: "") }
                        )
                    }
                }
            }
        }

        // Propose Alternatives Dialog for Instructor/Manager
        if (targetReservationForAlternatives != null) {
            ProposeAlternativesModalDialog(
                reservation = targetReservationForAlternatives!!,
                onDismiss = { targetReservationForAlternatives = null },
                onSend = { alts, note ->
                    onProposeAlternatives(targetReservationForAlternatives!!.id, alts, note)
                    targetReservationForAlternatives = null
                }
            )
        }
    }
}

@Composable
private fun ReservationCard(
    reservation: ReservationEntity,
    currentRole: UserRole,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onOpenProposeDialog: () -> Unit,
    onAcceptAlternative: (date: String, time: String) -> Unit,
    onDelete: () -> Unit,
    onScheduleExam: () -> Unit
) {
    val statusColor = when (reservation.status) {
        "Zatwierdzona" -> Color(0xFF10B981)
        "Odrzucona" -> Color(0xFFEF4444)
        "Zaproponowano alternatywę" -> Color(0xFFA855F7)
        else -> Color(0xFFF59E0B)
    }

    Surface(
        color = Color(0xFF131B2E),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, statusColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Info & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.3f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = reservation.fullName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        // If current user is not a Student, show GDPR confidential identifiers, otherwise hide to make interface cleaner
                        if (currentRole != UserRole.STUDENT) {
                            Text(
                                text = "PESEL: ${reservation.pesel} • Ur.: ${reservation.dateOfBirth}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        } else {
                            Text(
                                text = "Twoje zgłoszenie rezerwacyjne",
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = when(reservation.status) {
                            "Oczekująca" -> "OCZEKUJĄCA"
                            "Zatwierdzona" -> "ZATWIERDZONA"
                            "Odrzucona" -> "ODRZUCONA"
                            "Zaproponowano alternatywę" -> "ALTERNATYWA"
                            else -> reservation.status.uppercase()
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contact Info (only show for Instructor/Manager)
            if (currentRole != UserRole.STUDENT) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reservation.phone, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reservation.email, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }

            // Requested Dates/Hours Box (Soft Pale Red Theme to match "blada czerwień")
            Surface(
                color = Color(0xFFEF4444).copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wybrane Terminy (Zapytanie)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF87171),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = reservation.selectedDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Godziny: ${reservation.selectedTimeSlots}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Student Custom Info Message if Status is Pending
            if (currentRole == UserRole.STUDENT && reservation.status == "Oczekująca") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⌛ Twoje zapytanie czeka na weryfikację przez instruktora. Otrzymasz powiadomienie, gdy status się zmieni.",
                    fontSize = 11.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Alternatives Proposed Banner & Option Selector
            if (!reservation.alternativeSlots.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0xFFA855F7).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.SwapCalls, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROPONOWANE TERMINY ZASTĘPCZE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA855F7),
                                letterSpacing = 0.5.sp
                            )
                        }

                        if (!reservation.instructorNote.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uwaga od instruktora: \"${reservation.instructorNote}\"",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Render options
                        val alternativesList = reservation.alternativeSlots.split(";").map { it.trim() }.filter { it.isNotBlank() }
                        alternativesList.forEach { altStr ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        if (reservation.status == "Zaproponowano alternatywę") {
                                            val parts = altStr.split(" godz. ")
                                            val d = parts.getOrNull(0) ?: reservation.selectedDate
                                            val t = parts.getOrNull(1) ?: "10:00"
                                            onAcceptAlternative(d, t)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = altStr, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    
                                    if (reservation.status == "Zaproponowano alternatywę") {
                                        Surface(
                                            color = Color(0xFF10B981),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Wybierz ten termin",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action row: ONLY visible for Manager & Instructor
            if (currentRole != UserRole.STUDENT) {
                if (reservation.status == "Oczekująca" || reservation.status == "Zaproponowano alternatywę") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAccept,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("accept_reservation_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Zatwierdź", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onReject,
                                border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("reject_reservation_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Odrzuć", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onOpenProposeDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("propose_alternative_btn")
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Zaproponuj Terminy Zastępcze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // If logged in as Student, let them delete/cancel their own request if it hasn't been approved yet
                if (reservation.status == "Oczekująca" || reservation.status == "Zaproponowano alternatywę") {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Anuluj to zapytanie", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (currentRole != UserRole.STUDENT) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onScheduleExam,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ustal Egzamin Państwowy", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProposeAlternativesModalDialog(
    reservation: ReservationEntity,
    onDismiss: () -> Unit,
    onSend: (alternativesFormatted: String, note: String) -> Unit
) {
    var alt1 by remember { mutableStateOf("08-08-2026 godz. 10:00") }
    var alt2 by remember { mutableStateOf("08-08-2026 godz. 14:00") }
    var alt3 by remember { mutableStateOf("09-08-2026 godz. 12:00") }
    var instructorNote by remember { mutableStateOf("Wnioskowane godziny są już zajęte. Proponuję wybrane terminy zastępcze.") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFFA855F7)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PROPONUJ ALTERNATYWY", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Dla kursanta: ${reservation.fullName}", fontSize = 11.sp, color = Color(0xFF38BDF8))
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

                Spacer(modifier = Modifier.height(16.dp))

                Text("Wpisz do 3 propozycji alternatywnych terminów:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = alt1,
                    onValueChange = { alt1 = it },
                    label = { Text("Propozycja 1", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Today, null, tint = Color(0xFFA855F7)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alt_input_1")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = alt2,
                    onValueChange = { alt2 = it },
                    label = { Text("Propozycja 2 (Opcjonalnie)", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Today, null, tint = Color(0xFFA855F7)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alt_input_2")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = alt3,
                    onValueChange = { alt3 = it },
                    label = { Text("Propozycja 3 (Opcjonalnie)", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Today, null, tint = Color(0xFFA855F7)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFA855F7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alt_input_3")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = instructorNote,
                    onValueChange = { instructorNote = it },
                    label = { Text("Wiadomość / Komentarz dla Kursanta", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Comment, null, tint = Color(0xFF38BDF8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = Color(0xFF1E293B),
                        unfocusedContainerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val alts = listOf(alt1, alt2, alt3).filter { it.isNotBlank() }.joinToString("; ")
                        onSend(alts, instructorNote)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_alternatives_btn")
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wyślij Propozycje do Kursanta", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
