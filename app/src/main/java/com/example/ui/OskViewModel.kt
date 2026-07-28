package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Calendar

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val date: String,
    val category: String = "INFO" // "INFO", "ALERT", "SUCCESS"
)

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val senderName: String = "System",
    val senderRole: String = "SYSTEM", // "OWNER", "INSTRUKTOR", "SYSTEM"
    val recipientName: String = "Właściciel", // "Właściciel", "Wszyscy" or specific instructor name
    val recipientRole: String = "OWNER" // "OWNER", "INSTRUKTOR", "ALL"
)

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: Long, // midnight timestamp in ms
    val startTime: String, // "10:00"
    val endTime: String, // "12:00"
    val type: String, // "TRAINING" or "COMPANY"
    val category: String = "LESSON", // "LESSON", "EXAM", "LECTURE", "VACATION", "SICK_LEAVE", "OFFICE"
    val studentName: String = "",
    val instructorName: String = "",
    val status: String = "PLANNED", // "PLANNED", "STARTED", "COMPLETED", "CANCEL_REQUESTED", "CANCELLED", "RESCHEDULE_PENDING", "PROPOSED_BY_STAFF", "PENDING_STAFF_APPROVAL"
    val cancelReason: String = "",
    val rescheduleNewDate: Long? = null,
    val rescheduleNewStartTime: String? = null,
    val rescheduleNewEndTime: String? = null
)

data class CoursePrice(
    val id: String = UUID.randomUUID().toString(),
    val categoryCode: String, // "A", "B", "C", "D", "EXPRESS", "EXTRA_HOURS"
    val categoryName: String,
    val pricePln: Int,
    val theoryHours: Int = 30,
    val practiceHours: Int = 30,
    val description: String = ""
)

data class GroupCourseSchedule(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // "A", "B", "C", "D"
    val startDate: Long,
    val endDate: Long,
    val location: String = "Główna Sala Wykładowa OSK",
    val instructorName: String,
    val createdByRole: String, // "OWNER" or "INSTRUKTOR"
    val status: String = "WAITING_FOR_PAIR_APPROVAL", // "WAITING_FOR_INSTRUCTOR_APPROVAL", "WAITING_FOR_OWNER_APPROVAL", "APPROVED", "REJECTED"
    val datesDescription: String = "Co wtorek i czwartek 17:00-20:00",
    val maxStudents: Int = 20
)

data class CalendarConflict(
    val id: String = UUID.randomUUID().toString(),
    val event1: CalendarEvent,
    val event2: CalendarEvent,
    val conflictType: String, // "INSTRUCTOR_OVERLAP", "STUDENT_OVERLAP", "LOCATION_OVERLAP"
    val description: String
)

data class SuggestedSlot(
    val id: String = UUID.randomUUID().toString(),
    val dateMs: Long,
    val dateFormatted: String, // e.g. "28.07.2026 (Jutro)"
    val startTime: String,     // e.g. "10:00"
    val endTime: String,       // e.g. "12:00"
    val instructorName: String
)

data class AlternativeSlotProposal(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val conflictDescription: String,
    val suggestedSlots: List<SuggestedSlot>,
    val expirationHours: Int = 24, // 24 hours or 6 hours
    val createdAt: Long = System.currentTimeMillis(),
    var status: String = "PENDING", // "PENDING", "ACCEPTED", "EXPIRED", "REJECTED"
    var acceptedSlot: SuggestedSlot? = null
)

data class StudentRegistration(
    val id: String = UUID.randomUUID().toString(),
    val accessCode: String,
    val name: String,
    val phone: String,
    val email: String,
    val category: String, // "A", "B", "C", "D"
    val pkkNumber: String = "", // Profil Kierowcy / Numer PKK
    val registrationDate: Long = System.currentTimeMillis(),
    val isApproved: Boolean = true,
    val selectedEventId: String? = null,
    val customDate: Long? = null,
    val customStartTime: String? = null,
    val customEndTime: String? = null,
    val timeframeStart: Long? = null,
    val timeframeEnd: Long? = null,
    val timeframeNote: String = "",
    val preferredInstructor: String = ""
)

data class StudentPayment(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val title: String, // e.g. "I Rata za Kurs Kat. B", "II Rata", "Opłata za jazdy dodatkowe"
    val amountPln: Int,
    val dueDate: String,
    val paidDate: String? = null, // "25.07.2026" or null if unpaid
    val paymentMethod: String = "Przelew Bankowy / BLIK",
    val status: String = "OPŁACONE", // "OPŁACONE", "OCZEKUJE", "ZALEGŁE"
    val receiptNumber: String = "FV/${System.currentTimeMillis().toString().takeLast(6)}/OSK"
)

data class DrivingSheetEntry(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val instructorName: String,
    val dateFormatted: String,
    val hoursCount: Float = 2.0f,
    val topicsCovered: String, // e.g. "Plac manewrowy: Ruszanie na wzniesieniu, parkowanie prostopadłe"
    val instructorNotes: String = "Brak uwag. Dobra dynamika jazdy.",
    val studentSigned: Boolean = true,
    val instructorSigned: Boolean = true
)

data class SimulatedSmsPushNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientPhone: String,
    val recipientName: String,
    val messageText: String,
    val scheduledTime: String,
    val type: String = "SMS_24H", // "SMS_24H", "PUSH_NOTIFICATION"
    val isSent: Boolean = true
)

data class WordQuestion(
    val id: Int,
    val text: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val correctAnswer: String, // "A", "B", or "C"
    val points: Int, // 3, 2, or 1
    val category: String = "BEZPIECZEŃSTWO"
)

data class PracticalExamEvaluation(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val instructorName: String,
    val dateFormatted: String,
    val preparationOk: Boolean = true,
    val maneuversManifoldOk: Boolean = true, // Łuk
    val hillStartOk: Boolean = true, // Górka
    val parkingOk: Boolean = true, // Parkowanie
    val cityIntersectionsOk: Boolean = true, // Skrzyżowania
    val overallResult: String = "POZYTYWNY", // "POZYTYWNY", "NEGATYWNY"
    val remarks: String = "Kandydat wykazał się dobrą opanowaniem pojazdu i przestrzeganiem przepisów."
)

data class FuelRefuelEntry(
    val id: String = UUID.randomUUID().toString(),
    val plateNumber: String,
    val instructorName: String,
    val dateFormatted: String,
    val kmMileage: Int, // Odczyt licznika podczas tankowania
    val liters: Double, // Litry paliwa (np. 38.5 l)
    val totalCostPln: Double, // Kwota rachunku (np. 248.50 PLN)
    val pricePerLiter: Double = if (liters > 0) totalCostPln / liters else 0.0,
    val fuelType: String = "Pb95", // "Pb95", "ON", "LPG", "EV"
    val stationName: String = "Orlen / Shell",
    val receiptNumber: String = "F/2026/07/882"
)

data class OdometerLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val plateNumber: String,
    val instructorName: String,
    val dateFormatted: String,
    val startKmMileage: Int,
    val endKmMileage: Int,
    val totalDrivenKm: Int = endKmMileage - startKmMileage,
    val notes: String = "Jazdy z kursantem"
)

data class VehicleFleetItem(
    val id: String = UUID.randomUUID().toString(),
    val plateNumber: String,
    val model: String,
    val kmMileage: Int,
    val nextMotDate: String, // Przegląd techniczny
    val insuranceExpiryDate: String, // OC/AC
    val status: String = "SPRAWNY" // "SPRAWNY", "W NAPRAWIE", "WYMAGA PRZEGLĄDU"
)

data class VehicleFaultReport(
    val id: String = UUID.randomUUID().toString(),
    val plateNumber: String,
    val instructorName: String,
    val reportDate: String,
    val description: String,
    val priority: String = "ŚREDNI", // "NISKI", "ŚREDNI", "PILNY"
    val isResolved: Boolean = false
)

data class WordRouteHotspot(
    val id: Int,
    val title: String,
    val locationName: String,
    val description: String,
    val difficultyLevel: String = "TRUDNE", // "ŚREDNIE", "TRUDNE", "BARDZO TRUDNE"
    val drivingTip: String
)

data class LessonFeedback(
    val id: String = UUID.randomUUID().toString(),
    val studentName: String,
    val instructorName: String,
    val dateFormatted: String,
    val ratingStars: Int, // 1-5
    val commentText: String
)

data class StudentReferralInfo(
    val studentName: String,
    val myReferralCode: String,
    val friendsJoinedCount: Int = 0,
    val freeBonusHours: Int = 0
)

data class CloudSubscriptionInfo(
    val planName: String = "proOsk Enterprise Cloud Multi-Device",
    val priceMonthlyPln: Int = 149,
    val isOwnerPayingForEveryone: Boolean = true,
    val isSyncActive: Boolean = true,
    val connectedDevicesCount: Int = 12,
    val lastSyncTimestamp: String = "Przed chwilą (Real-time WebSocket)",
    val syncFrequency: String = "Natychmiastowa (Real-time)",
    val smsGateActive: Boolean = true,
    val smsSenderId: String = "proOsk",
    val backupActive: Boolean = true,
    val lastBackupDate: String = "Dzisiaj, 02:00",
    val cloudStorageUsedMb: Int = 412,
    val cloudStorageMaxGb: Int = 50,
    val billingRenewalDate: String = "27.08.2026",
    val paymentCardMasked: String = "•••• •••• •••• 8842 (Visa Corporate OSK)",
    val pairingCode: String = "OSK-CLOUD-9982"
)

sealed interface RegisterOskUiState {
    object Idle : RegisterOskUiState
    object Step1Completed : RegisterOskUiState
    data class Error(val message: String) : RegisterOskUiState
    object Success : RegisterOskUiState
}

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class OskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OskRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OskRepository(database.oskDao())
    }

    val oskProfile: StateFlow<OskProfile?> = repository.oskProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeSession: StateFlow<ActiveSession?> = repository.activeSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAccessCodes: StateFlow<List<AccessCode>> = repository.allAccessCodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cloudSubscription = MutableStateFlow(CloudSubscriptionInfo())
    val cloudSubscription: StateFlow<CloudSubscriptionInfo> = _cloudSubscription.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(
        listOf(
            Announcement(
                title = "Nowa Toyota Yaris we flocie!",
                content = "Od przyszłego poniedziałku wprowadzamy do szkolenia nową Toyotę Yaris z automatyczną skrzynią biegów. Idealna opcja dla kursantów zainteresowanych kategorią B-automat!",
                date = "Dzisiaj, 09:30",
                category = "SUCCESS"
            ),
            Announcement(
                title = "Przerwa konserwacyjna w WORD",
                content = "Wojewódzki Ośrodek Ruchu Drogowego informuje o przerwie technicznej w dniach 10-12 sierpnia. Egzaminy państwowe w tych dniach nie będą przeprowadzane.",
                date = "Wczoraj, 14:15",
                category = "ALERT"
            ),
            Announcement(
                title = "Darmowe warsztaty z psychologii jazdy",
                content = "W sobotę o godzinie 16:00 w sali wykładowej odbędą się darmowe, dodatkowe warsztaty z radzenia sobie ze stresem egzaminacyjnym. Zapraszamy wszystkich chętnych!",
                date = "25.07.2026",
                category = "INFO"
            )
        )
    )
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(
        listOf(
            Reminder(
                text = "Zlecenie przeglądu technicznego Toyoty Yaris (KRA 12345) - ważne do 05.08",
                priority = "HIGH",
                senderName = "Właściciel",
                senderRole = "OWNER",
                recipientName = "Właściciel",
                recipientRole = "OWNER"
            ),
            Reminder(
                text = "Pamiętajcie o sprawdzeniu poziomu oleju i płynów w autach przed rozpoczęciem jazd",
                priority = "HIGH",
                senderName = "Właściciel",
                senderRole = "OWNER",
                recipientName = "Wszyscy",
                recipientRole = "ALL"
            ),
            Reminder(
                text = "Toyota Yaris (KRA 12345) ma lekki problem ze wspomaganiem przy parkowaniu",
                priority = "MEDIUM",
                senderName = "Tomasz Nowak",
                senderRole = "INSTRUKTOR",
                recipientName = "Właściciel",
                recipientRole = "OWNER"
            ),
            Reminder(
                text = "Opłacenie składki OC dla samochodów szkoleniowych L2 i L3",
                priority = "MEDIUM",
                senderName = "Właściciel",
                senderRole = "OWNER",
                recipientName = "Właściciel",
                recipientRole = "OWNER"
            )
        )
    )
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    fun addAnnouncement(title: String, content: String, category: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        _announcements.value = listOf(
            Announcement(title = title, content = content, date = dateStr, category = category)
        ) + _announcements.value
    }

    fun removeAnnouncement(id: String) {
        _announcements.value = _announcements.value.filter { it.id != id }
    }

    fun addReminder(
        text: String,
        priority: String,
        senderName: String = "System",
        senderRole: String = "SYSTEM",
        recipientName: String = "Właściciel",
        recipientRole: String = "OWNER"
    ) {
        if (text.isBlank()) return
        _reminders.value = _reminders.value + Reminder(
            text = text,
            priority = priority,
            senderName = senderName,
            senderRole = senderRole,
            recipientName = recipientName,
            recipientRole = recipientRole
        )
    }

    fun toggleReminder(id: String) {
        _reminders.value = _reminders.value.map {
            if (it.id == id) it.copy(isDone = !it.isDone) else it
        }
    }

    fun removeReminder(id: String) {
        _reminders.value = _reminders.value.filter { it.id != id }
    }

    private fun getTimestampForDay(dayOffset: Int): Long {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val _calendarEvents = MutableStateFlow<List<CalendarEvent>>(
        listOf(
            CalendarEvent(
                id = "evt_1",
                title = "Jazda próbna kat. B: Jan Kowalski",
                date = getTimestampForDay(0),
                startTime = "09:00",
                endTime = "11:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = "Jan Kowalski",
                instructorName = "Tomasz Nowak",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_2",
                title = "Jazda kat. B: Joanna Szpak",
                date = getTimestampForDay(0),
                startTime = "14:30",
                endTime = "16:30",
                type = "TRAINING",
                category = "LESSON",
                studentName = "Joanna Szpak",
                instructorName = "Tomasz Nowak",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_3",
                title = "Egzamin Teoretyczny Wewnętrzny",
                date = getTimestampForDay(1),
                startTime = "11:00",
                endTime = "12:30",
                type = "TRAINING",
                category = "EXAM",
                studentName = "Jan Kowalski",
                instructorName = "Alicja Kowalska",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_4",
                title = "Wykład: Pierwsza Pomoc Przedmedyczna",
                date = getTimestampForDay(1),
                startTime = "17:00",
                endTime = "19:00",
                type = "TRAINING",
                category = "LECTURE",
                studentName = "Wszyscy",
                instructorName = "Robert Wiśniewski",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_5",
                title = "Urlop wypoczynkowy: Tomasz Nowak",
                date = getTimestampForDay(2),
                startTime = "08:00",
                endTime = "16:00",
                type = "COMPANY",
                category = "VACATION",
                instructorName = "Tomasz Nowak",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_6",
                title = "Zwolnienie lekarskie: Robert Wiśniewski",
                date = getTimestampForDay(3),
                startTime = "08:00",
                endTime = "16:00",
                type = "COMPANY",
                category = "SICK_LEAVE",
                instructorName = "Robert Wiśniewski",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "evt_7",
                title = "Przegląd techniczny aut L1, L2",
                date = getTimestampForDay(-1),
                startTime = "10:00",
                endTime = "12:00",
                type = "COMPANY",
                category = "OFFICE",
                instructorName = "Tomasz Nowak",
                status = "COMPLETED"
            ),
            CalendarEvent(
                id = "evt_8",
                title = "Jazda kat. B: Marcin Głowacki",
                date = getTimestampForDay(1),
                startTime = "12:00",
                endTime = "14:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = "Marcin Głowacki",
                instructorName = "Alicja Kowalska",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "free_evt_1",
                title = "Wolna jazda praktyczna kat. B",
                date = getTimestampForDay(2),
                startTime = "10:00",
                endTime = "12:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = "",
                instructorName = "Tomasz Nowak",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "free_evt_2",
                title = "Wolna jazda praktyczna kat. A",
                date = getTimestampForDay(3),
                startTime = "14:00",
                endTime = "16:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = "",
                instructorName = "Alicja Kowalska",
                status = "PLANNED"
            ),
            CalendarEvent(
                id = "free_evt_3",
                title = "Wolna jazda praktyczna kat. C",
                date = getTimestampForDay(4),
                startTime = "08:00",
                endTime = "10:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = "",
                instructorName = "Robert Wiśniewski",
                status = "PLANNED"
            )
        )
    )

    private val _studentRegistrations = MutableStateFlow<List<StudentRegistration>>(
        listOf(
            StudentRegistration(
                accessCode = "BYPASS",
                name = "Jan Kowalski (Właściciel)",
                phone = "+48 501 202 303",
                email = "jan.kowalski@gmail.com",
                category = "B",
                isApproved = true
            ),
            StudentRegistration(
                accessCode = "KURS-111111",
                name = "Jan Kowalski",
                phone = "+48 501 202 303",
                email = "jan.kowalski@gmail.com",
                category = "B",
                isApproved = true
            ),
            StudentRegistration(
                accessCode = "KURS-222222",
                name = "Joanna Szpak",
                phone = "+48 602 303 404",
                email = "joanna.szpak@gmail.com",
                category = "B",
                isApproved = true
            ),
            StudentRegistration(
                accessCode = "KURS-333333",
                name = "Marcin Głowacki",
                phone = "+48 703 404 505",
                email = "marcin.glowacki@gmail.com",
                category = "B",
                isApproved = true
            )
        )
    )
    val studentRegistrations: StateFlow<List<StudentRegistration>> = _studentRegistrations.asStateFlow()
    val calendarEvents: StateFlow<List<CalendarEvent>> = _calendarEvents.asStateFlow()

    fun addCalendarEvent(
        title: String,
        date: Long,
        startTime: String,
        endTime: String,
        type: String,
        category: String,
        studentName: String = "",
        instructorName: String = ""
    ) {
        if (title.isBlank()) return
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val normalizedDate = cal.timeInMillis

        val status = if (instructorName.isNotEmpty()) "PENDING_INSTRUCTOR_APPROVAL" else "PLANNED"

        _calendarEvents.value = _calendarEvents.value + CalendarEvent(
            title = title,
            date = normalizedDate,
            startTime = startTime,
            endTime = endTime,
            type = type,
            category = category,
            studentName = studentName,
            instructorName = instructorName,
            status = status
        )

        if (instructorName.isNotEmpty()) {
            addReminder(
                text = "Nowe wydarzenie w Twoim grafiku: '$title' ($startTime-$endTime). Czeka na Twoje zaakceptowanie.",
                priority = "MEDIUM",
                senderName = "Właściciel",
                senderRole = "OWNER",
                recipientName = instructorName,
                recipientRole = "INSTRUKTOR"
            )
        }
    }

    fun removeCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.filter { it.id != id }
    }

    private fun calculateEventHours(start: String, end: String): Int {
        return try {
            val startParts = start.split(":")
            val endParts = end.split(":")
            val startMin = startParts[0].toInt() * 60 + startParts[1].toInt()
            val endMin = endParts[0].toInt() * 60 + endParts[1].toInt()
            val diff = endMin - startMin
            val hrs = (diff + 30) / 60
            if (hrs < 1) 1 else hrs
        } catch (e: Exception) {
            2 // default to 2 hours
        }
    }

    fun startCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                event.copy(status = "STARTED")
            } else {
                event
            }
        }
    }

    fun completeCalendarEvent(id: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                val updated = event.copy(status = "COMPLETED")
                val hoursDiff = calculateEventHours(event.startTime, event.endTime)
                val instr = event.instructorName
                if (instr.isNotEmpty()) {
                    val currentHours = _instructorHours.value[instr] ?: 0
                    _instructorHours.value = _instructorHours.value + (instr to (currentHours + hoursDiff))
                }
                updated
            } else {
                event
            }
        }
    }

    fun cancelCalendarEvent(id: String, reason: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                event.copy(status = "CANCELLED", cancelReason = reason)
            } else {
                event
            }
        }
    }

    fun requestReschedule(id: String, newDate: Long, newStartTime: String, newEndTime: String) {
        var eventTitle = ""
        var instName = ""
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                eventTitle = event.title
                instName = event.instructorName
                event.copy(
                    status = "RESCHEDULE_PENDING",
                    rescheduleNewDate = newDate,
                    rescheduleNewStartTime = newStartTime,
                    rescheduleNewEndTime = newEndTime
                )
            } else {
                event
            }
        }

        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(newDate))
        addReminder(
            text = "Instruktor $instName prosi o zmianę terminu dla '$eventTitle' na dzień $dateStr o godz. $newStartTime-$newEndTime",
            priority = "MEDIUM",
            senderName = instName,
            senderRole = "INSTRUKTOR",
            recipientName = "Właściciel",
            recipientRole = "OWNER"
        )
    }

    fun approveReschedule(id: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id && event.rescheduleNewDate != null && event.rescheduleNewStartTime != null && event.rescheduleNewEndTime != null) {
                event.copy(
                    date = event.rescheduleNewDate,
                    startTime = event.rescheduleNewStartTime,
                    endTime = event.rescheduleNewEndTime,
                    status = "PLANNED",
                    rescheduleNewDate = null,
                    rescheduleNewStartTime = null,
                    rescheduleNewEndTime = null
                )
            } else {
                event
            }
        }
    }

    fun rejectReschedule(id: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                event.copy(
                    status = "PLANNED",
                    rescheduleNewDate = null,
                    rescheduleNewStartTime = null,
                    rescheduleNewEndTime = null
                )
            } else {
                event
            }
        }
    }

    fun editCalendarEventByOwner(
        id: String,
        title: String,
        date: Long,
        startTime: String,
        endTime: String,
        type: String,
        category: String,
        studentName: String,
        instructorName: String
    ) {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val normalizedDate = cal.timeInMillis

        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                event.copy(
                    title = title,
                    date = normalizedDate,
                    startTime = startTime,
                    endTime = endTime,
                    type = type,
                    category = category,
                    studentName = studentName,
                    instructorName = instructorName,
                    status = if (instructorName.isNotEmpty()) "PENDING_INSTRUCTOR_APPROVAL" else "PLANNED"
                )
            } else {
                event
            }
        }

        if (instructorName.isNotEmpty()) {
            addReminder(
                text = "Właściciel zaktualizował Twój grafik dla lekcji '$title' ($startTime-$endTime). Zaakceptuj zmiany.",
                priority = "MEDIUM",
                senderName = "Właściciel",
                senderRole = "OWNER",
                recipientName = instructorName,
                recipientRole = "INSTRUKTOR"
            )
        }
    }

    fun instructorApproveEvent(id: String) {
        var eventTitle = ""
        var instName = ""
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                eventTitle = event.title
                instName = event.instructorName
                event.copy(status = "PLANNED")
            } else {
                event
            }
        }

        addReminder(
            text = "Instruktor $instName zaakceptował Twój grafik dla lekcji: '$eventTitle'.",
            priority = "LOW",
            senderName = instName,
            senderRole = "INSTRUKTOR",
            recipientName = "Właściciel",
            recipientRole = "OWNER"
        )
    }

    fun instructorRequestVacation(instructorName: String, date: Long, startTime: String, endTime: String, reason: String) {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = date
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val normalizedDate = cal.timeInMillis

        _calendarEvents.value = _calendarEvents.value + CalendarEvent(
            title = "Prośba o wolne: $instructorName",
            date = normalizedDate,
            startTime = startTime,
            endTime = endTime,
            type = "COMPANY",
            category = "VACATION",
            instructorName = instructorName,
            status = "PENDING_OWNER_VACATION",
            cancelReason = reason
        )

        val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(normalizedDate))
        addReminder(
            text = "Instruktor $instructorName prosi o wolne w dniu $dateStr ($startTime-$endTime). Powód: $reason",
            priority = "HIGH",
            senderName = instructorName,
            senderRole = "INSTRUKTOR",
            recipientName = "Właściciel",
            recipientRole = "OWNER"
        )
    }

    fun approveVacation(id: String) {
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                event.copy(status = "PLANNED")
            } else {
                event
            }
        }
    }

    fun rejectVacation(id: String) {
        _calendarEvents.value = _calendarEvents.value.filter { it.id != id }
    }

    fun requestSwapWithOther(id: String) {
        var eventTitle = ""
        var instName = ""
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                eventTitle = event.title
                instName = event.instructorName
                event.copy(status = "PENDING_SWAP_REQUEST")
            } else {
                event
            }
        }

        addReminder(
            text = "Instruktor $instName szuka zastępstwa/zamiany dla zajęć '$eventTitle'.",
            priority = "MEDIUM",
            senderName = instName,
            senderRole = "INSTRUKTOR",
            recipientName = "Wszyscy",
            recipientRole = "ALL"
        )
    }

    fun acceptSwapRequest(id: String, acceptingInstructorName: String) {
        var eventTitle = ""
        var originalInstructor = ""
        _calendarEvents.value = _calendarEvents.value.map { event ->
            if (event.id == id) {
                eventTitle = event.title
                originalInstructor = event.instructorName
                event.copy(
                    instructorName = acceptingInstructorName,
                    status = "PLANNED"
                )
            } else {
                event
            }
        }

        addReminder(
            text = "Instruktor $acceptingInstructorName przejął zajęcia '$eventTitle' od instruktora $originalInstructor.",
            priority = "MEDIUM",
            senderName = "System",
            senderRole = "SYSTEM",
            recipientName = "Właściciel",
            recipientRole = "OWNER"
        )
        addReminder(
            text = "Instruktor $acceptingInstructorName zaakceptował Twoją prośbę o zamianę dla zajęć '$eventTitle'!",
            priority = "HIGH",
            senderName = "System",
            senderRole = "SYSTEM",
            recipientName = originalInstructor,
            recipientRole = "INSTRUKTOR"
        )
    }

    fun triggerDailyReminders1400() {
        val pendingInstr = _calendarEvents.value.filter { it.status == "PENDING_INSTRUCTOR_APPROVAL" }
        val pendingVacation = _calendarEvents.value.filter { it.status == "PENDING_OWNER_VACATION" }
        val pendingResched = _calendarEvents.value.filter { it.status == "RESCHEDULE_PENDING" }
        val pendingSwap = _calendarEvents.value.filter { it.status == "PENDING_SWAP_REQUEST" }

        if (pendingInstr.isNotEmpty()) {
            pendingInstr.groupBy { it.instructorName }.forEach { (instr, events) ->
                if (instr.isNotEmpty()) {
                    addReminder(
                        text = "[RAPORT 14:00] Masz ${events.size} oczekujących zmian w grafiku od właściciela do zaakceptowania!",
                        priority = "HIGH",
                        senderName = "System",
                        senderRole = "SYSTEM",
                        recipientName = instr,
                        recipientRole = "INSTRUKTOR"
                    )
                }
            }
        }

        if (pendingVacation.isNotEmpty() || pendingResched.isNotEmpty()) {
            addReminder(
                text = "[RAPORT 14:00] Właścicielu, masz oczekujące wnioski od instruktorów: ${pendingVacation.size} próśb o wolne, ${pendingResched.size} o zmianę terminu!",
                priority = "HIGH",
                senderName = "System",
                senderRole = "SYSTEM",
                recipientName = "Właściciel",
                recipientRole = "OWNER"
            )
        }

        if (pendingSwap.isNotEmpty()) {
            addReminder(
                text = "[RAPORT 14:00] Uwaga instruktorzy! Na tablicy znajduje się ${pendingSwap.size} otwartych próśb o zamianę godzin.",
                priority = "MEDIUM",
                senderName = "System",
                senderRole = "SYSTEM",
                recipientName = "Wszyscy",
                recipientRole = "ALL"
            )
        }
    }

    fun bypassLogin(role: String, name: String) {
        viewModelScope.launch {
            val existing = repository.getProfileDirect()
            if (existing == null && role == "OWNER") {
                val profile = OskProfile(
                    companyName = "Super OSK Auto-Start",
                    nip = "1234567890",
                    phoneNumber = "+48 500 600 700",
                    email = "kontakt@superosk.pl",
                    city = "Kraków",
                    street = "Floriańska 12",
                    ownerName = "Jan Kowalski (Właściciel)",
                    login = "admin",
                    passwordHash = "admin",
                    securityQuestion = "Kolor?",
                    securityAnswer = "niebieski"
                )
                repository.insertProfile(profile)
            }
            repository.saveActiveSession(
                ActiveSession(
                    role = role,
                    name = name,
                    codeUsed = "BYPASS"
                )
            )
            _loginState.value = LoginUiState.Success
        }
    }

    // Temp form state for registration Step 1
    var tempCompanyName = ""
    var tempNip = ""
    var tempPhoneNumber = ""
    var tempEmail = ""
    var tempCity = ""
    var tempStreet = ""
    var tempOwnerName = ""

    private val _registerState = MutableStateFlow<RegisterOskUiState>(RegisterOskUiState.Idle)
    val registerState: StateFlow<RegisterOskUiState> = _registerState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    // Recovery State
    private val _recoveryMessage = MutableStateFlow<String?>(null)
    val recoveryMessage: StateFlow<String?> = _recoveryMessage.asStateFlow()

    fun resetRegisterState() {
        _registerState.value = RegisterOskUiState.Idle
    }

    fun completeStep1(companyName: String, nip: String, phoneNumber: String, email: String, city: String, street: String, ownerName: String) {
        if (companyName.isBlank() || nip.isBlank() || phoneNumber.isBlank() || email.isBlank() || city.isBlank() || street.isBlank() || ownerName.isBlank()) {
            _registerState.value = RegisterOskUiState.Error("Wszystkie pola są wymagane!")
            return
        }
        // Basic NIP validation (10 digits is standard in Poland, we can check for digit count or just check length if we want, but keeping it friendly is best)
        val cleanNip = nip.replace("-", "").replace(" ", "")
        if (cleanNip.length != 10 || !cleanNip.all { it.isDigit() }) {
            _registerState.value = RegisterOskUiState.Error("Niepoprawny format NIP (wymagane 10 cyfr)!")
            return
        }
        // Basic email check
        if (!email.contains("@") || !email.contains(".")) {
            _registerState.value = RegisterOskUiState.Error("Niepoprawny format adresu e-mail!")
            return
        }

        tempCompanyName = companyName
        tempNip = cleanNip
        tempPhoneNumber = phoneNumber
        tempEmail = email
        tempCity = city
        tempStreet = street
        tempOwnerName = ownerName
        _registerState.value = RegisterOskUiState.Step1Completed
    }

    fun completeStep2(login: String, haslo: String, powtorzHaslo: String, pytanie: String, odpowiedz: String) {
        if (login.isBlank() || haslo.isBlank() || powtorzHaslo.isBlank() || odpowiedz.isBlank()) {
            _registerState.value = RegisterOskUiState.Error("Wszystkie pola są wymagane!")
            return
        }
        if (haslo != powtorzHaslo) {
            _registerState.value = RegisterOskUiState.Error("Hasła nie są identyczne!")
            return
        }

        viewModelScope.launch {
            try {
                val profile = OskProfile(
                    companyName = tempCompanyName,
                    nip = tempNip,
                    phoneNumber = tempPhoneNumber,
                    email = tempEmail,
                    city = tempCity,
                    street = tempStreet,
                    ownerName = tempOwnerName,
                    login = login,
                    passwordHash = haslo,
                    securityQuestion = pytanie,
                    securityAnswer = odpowiedz
                )
                repository.insertProfile(profile)
                // Auto login as owner
                repository.saveActiveSession(ActiveSession(role = "OWNER", name = tempOwnerName))
                _registerState.value = RegisterOskUiState.Success
            } catch (e: Exception) {
                _registerState.value = RegisterOskUiState.Error("Błąd zapisu: ${e.localizedMessage}")
            }
        }
    }

    fun loginOwner(login: String, haslo: String) {
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null) {
                _loginState.value = LoginUiState.Error("Szkoła nie jest jeszcze zarejestrowana!")
                return@launch
            }
            if (profile.login == login && profile.passwordHash == haslo) {
                repository.saveActiveSession(ActiveSession(role = "OWNER", name = profile.ownerName))
                _loginState.value = LoginUiState.Success
            } else {
                _loginState.value = LoginUiState.Error("Niepoprawny login lub hasło!")
            }
        }
    }

    fun recoverPassword(odpowiedz: String) {
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null) {
                _recoveryMessage.value = "Szkoła nie jest jeszcze zarejestrowana!"
                return@launch
            }
            if (profile.securityAnswer.equals(odpowiedz, ignoreCase = true)) {
                _recoveryMessage.value = "Twoje hasło to: ${profile.passwordHash}"
            } else {
                _recoveryMessage.value = "Niepoprawna odpowiedź na pytanie pomocnicze!"
            }
        }
    }

    fun clearRecoveryMessage() {
        _recoveryMessage.value = null
    }

    fun resetLoginState() {
        _loginState.value = LoginUiState.Idle
    }

    fun loginWithAccessCode(code: String) {
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            val accessCode = repository.getAccessCode(code.trim().uppercase())
            if (accessCode == null) {
                _loginState.value = LoginUiState.Error("Niepoprawny kod dostępu!")
                return@launch
            }
            // Successfully verified code, log in with correct role
            repository.saveActiveSession(
                ActiveSession(
                    role = accessCode.role,
                    name = accessCode.name,
                    codeUsed = accessCode.code
                )
            )
            _loginState.value = LoginUiState.Success
        }
    }

    fun generateAccessCode(name: String, role: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val prefix = if (role == "INSTRUKTOR") "INST" else "KURS"
            val randomNum = Random.nextInt(100000, 999999)
            val codeString = "$prefix-$randomNum"
            val code = AccessCode(
                code = codeString,
                role = role,
                name = name
            )
            repository.insertAccessCode(code)
        }
    }

    fun deleteAccessCode(id: Int) {
        viewModelScope.launch {
            repository.deleteAccessCodeById(id)
        }
    }

    // Map of Instructor Name -> Assigned Car (e.g. Tomasz Nowak -> "Toyota Yaris (KRA 12345)")
    private val _instructorCars = MutableStateFlow<Map<String, String>>(
        mapOf(
            "Tomasz Nowak" to "Toyota Yaris (KRA 12345)",
            "Robert Wiśniewski" to "Hyundai i20 (KRA 98765)",
            "Alicja Kowalska" to "Toyota Yaris (KRA 54321)"
        )
    )
    val instructorCars: StateFlow<Map<String, String>> = _instructorCars.asStateFlow()

    // Map of Instructor Name -> Hourly Rate in PLN (e.g. Tomasz Nowak -> 60)
    private val _instructorRates = MutableStateFlow<Map<String, Int>>(
        mapOf(
            "Tomasz Nowak" to 60,
            "Robert Wiśniewski" to 55,
            "Alicja Kowalska" to 65
        )
    )
    val instructorRates: StateFlow<Map<String, Int>> = _instructorRates.asStateFlow()

    // Map of Instructor Name -> Logged Hours (e.g. Tomasz Nowak -> 120)
    private val _instructorHours = MutableStateFlow<Map<String, Int>>(
        mapOf(
            "Tomasz Nowak" to 124,
            "Robert Wiśniewski" to 98,
            "Alicja Kowalska" to 142
        )
    )
    val instructorHours: StateFlow<Map<String, Int>> = _instructorHours.asStateFlow()

    // Map of Instructor Name -> Pass rate (e.g. Tomasz Nowak -> 85)
    private val _instructorPassRates = MutableStateFlow<Map<String, Int>>(
        mapOf(
            "Tomasz Nowak" to 82,
            "Robert Wiśniewski" to 75,
            "Alicja Kowalska" to 88
        )
    )
    val instructorPassRates: StateFlow<Map<String, Int>> = _instructorPassRates.asStateFlow()

    fun updateInstructorDetails(name: String, car: String, rate: Int, hours: Int, passRate: Int) {
        _instructorCars.value = _instructorCars.value + (name to car)
        _instructorRates.value = _instructorRates.value + (name to rate)
        _instructorHours.value = _instructorHours.value + (name to hours)
        _instructorPassRates.value = _instructorPassRates.value + (name to passRate)
    }

    // COURSE PRICES STATE FLOW
    private val _coursePrices = MutableStateFlow<List<CoursePrice>>(
        listOf(
            CoursePrice(
                categoryCode = "A",
                categoryName = "Kategoria A (Motocykle)",
                pricePln = 2800,
                theoryHours = 30,
                practiceHours = 20,
                description = "Szkolenie na motocykle Yamaha MT-07 / Suzuki SV650. Strój ochronny na placu w cenie."
            ),
            CoursePrice(
                categoryCode = "B",
                categoryName = "Kategoria B (Samochody Osobowe)",
                pricePln = 3400,
                theoryHours = 30,
                practiceHours = 30,
                description = "Szkolenie na samochodach Toyota Yaris / Hyundai i20. Pełny pakiet materiałów szkoleniowych."
            ),
            CoursePrice(
                categoryCode = "C",
                categoryName = "Kategoria C (Samochody Ciężarowe)",
                pricePln = 4200,
                theoryHours = 20,
                practiceHours = 30,
                description = "Szkolenie na pojazdach MAN TGL z automatyczną skrzynią biegów. Przygotowanie do B96 / C+E."
            ),
            CoursePrice(
                categoryCode = "D",
                categoryName = "Kategoria D (Autobusy)",
                pricePln = 5100,
                theoryHours = 20,
                practiceHours = 60,
                description = "Szkolenie kierowców zawodowych na autobusie miejskim Solaris Urbino."
            ),
            CoursePrice(
                categoryCode = "EXPRESS",
                categoryName = "Kurs Przyspieszony Kat. B (14 Dni)",
                pricePln = 4200,
                theoryHours = 30,
                practiceHours = 30,
                description = "Indywidualny tryb zajęć i jazdy codziennie. Wyznaczone priorytetowe terminy."
            ),
            CoursePrice(
                categoryCode = "EXTRA_HOURS",
                categoryName = "Dodatkowa Godzina Jazdy",
                pricePln = 120,
                theoryHours = 0,
                practiceHours = 1,
                description = "Godzina doszkalająca z wybranym instruktorem przed egzaminem państwowym WORD."
            )
        )
    )
    val coursePrices: StateFlow<List<CoursePrice>> = _coursePrices.asStateFlow()

    fun updateCoursePrice(id: String, newPrice: Int, newDesc: String) {
        _coursePrices.value = _coursePrices.value.map {
            if (it.id == id) it.copy(pricePln = newPrice, description = newDesc) else it
        }
    }

    fun addCoursePrice(code: String, name: String, price: Int, theoryH: Int, practiceH: Int, desc: String) {
        val newItem = CoursePrice(
            categoryCode = code,
            categoryName = name,
            pricePln = price,
            theoryHours = theoryH,
            practiceHours = practiceH,
            description = desc
        )
        _coursePrices.value = _coursePrices.value + newItem
    }

    // GROUP COURSE SCHEDULES STATE FLOW & DUAL-APPROVAL
    private val _groupSchedules = MutableStateFlow<List<GroupCourseSchedule>>(
        listOf(
            GroupCourseSchedule(
                title = "Jesienny Kurs Teoretyczny Kat. B",
                category = "B",
                startDate = getTimestampForDay(5),
                endDate = getTimestampForDay(25),
                location = "Główna Sala Wykładowa OSK (Sala 1)",
                instructorName = "Tomasz Nowak",
                createdByRole = "OWNER",
                status = "WAITING_FOR_INSTRUCTOR_APPROVAL",
                datesDescription = "Wtorki i Czwartki w godz. 17:00 - 20:00"
            )
        )
    )
    val groupSchedules: StateFlow<List<GroupCourseSchedule>> = _groupSchedules.asStateFlow()

    fun createGroupSchedule(
        title: String,
        category: String,
        startDate: Long,
        endDate: Long,
        location: String,
        instructorName: String,
        datesDescription: String,
        createdByRole: String
    ) {
        val reqStatus = if (createdByRole == "OWNER") "WAITING_FOR_INSTRUCTOR_APPROVAL" else "WAITING_FOR_OWNER_APPROVAL"
        val schedule = GroupCourseSchedule(
            title = title,
            category = category,
            startDate = startDate,
            endDate = endDate,
            location = location,
            instructorName = instructorName,
            createdByRole = createdByRole,
            status = reqStatus,
            datesDescription = datesDescription
        )
        _groupSchedules.value = _groupSchedules.value + schedule

        val recipient = if (createdByRole == "OWNER") instructorName else "Właściciel"
        val recipientRole = if (createdByRole == "OWNER") "INSTRUKTOR" else "OWNER"

        addReminder(
            text = "Wysłano nowy harmonogram kursu grupowego: $title ($datesDescription). Oczekuje na Twoją akceptację!",
            priority = "HIGH",
            senderName = if (createdByRole == "OWNER") "Właściciel" else instructorName,
            senderRole = createdByRole,
            recipientName = recipient,
            recipientRole = recipientRole
        )
    }

    fun approveGroupSchedule(id: String) {
        _groupSchedules.value = _groupSchedules.value.map {
            if (it.id == id) it.copy(status = "APPROVED") else it
        }
        val item = _groupSchedules.value.find { it.id == id }
        if (item != null) {
            // Convert to calendar lecture event
            val lectureEvent = CalendarEvent(
                title = "WYKŁAD GRUPOWY (${item.category}): ${item.title}",
                date = item.startDate,
                startTime = "17:00",
                endTime = "20:00",
                type = "COMPANY",
                category = "LECTURE",
                instructorName = item.instructorName,
                status = "PLANNED"
            )
            _calendarEvents.value = _calendarEvents.value + lectureEvent

            addReminder(
                text = "Zatwierdzono harmonogram kursu grupowego: ${item.title}! Wykłady zostały opublikowane w kalendarzu.",
                priority = "MEDIUM",
                senderName = "System",
                senderRole = "SYSTEM",
                recipientName = "Wszyscy",
                recipientRole = "ALL"
            )
        }
    }

    fun rejectGroupSchedule(id: String) {
        _groupSchedules.value = _groupSchedules.value.map {
            if (it.id == id) it.copy(status = "REJECTED") else it
        }
    }

    // CONFLICT ENGINE
    private fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].trim().toInt()
            val m = parts[1].trim().toInt()
            h * 60 + m
        } catch (e: Exception) {
            0
        }
    }

    private fun isTimeOverlapping(start1: String, end1: String, start2: String, end2: String): Boolean {
        val s1 = parseTimeToMinutes(start1)
        val e1 = parseTimeToMinutes(end1)
        val s2 = parseTimeToMinutes(start2)
        val e2 = parseTimeToMinutes(end2)
        return (s1 < e2 && s2 < e1)
    }

    val calendarConflicts: StateFlow<List<CalendarConflict>> = _calendarEvents.map { events ->
        val activeEvents = events.filter { it.status != "CANCELLED" }
        val conflicts = mutableListOf<CalendarConflict>()

        for (i in activeEvents.indices) {
            for (j in i + 1 until activeEvents.size) {
                val e1 = activeEvents[i]
                val e2 = activeEvents[j]

                if (e1.date == e2.date && isTimeOverlapping(e1.startTime, e1.endTime, e2.startTime, e2.endTime)) {
                    // Check Instructor Conflict
                    if (e1.instructorName.isNotBlank() && e1.instructorName.equals(e2.instructorName, ignoreCase = true)) {
                        conflicts.add(
                            CalendarConflict(
                                event1 = e1,
                                event2 = e2,
                                conflictType = "INSTRUCTOR_OVERLAP",
                                description = "Instruktor ${e1.instructorName} ma 2 nakładające się jazdy w godzinach ${e1.startTime}-${e1.endTime} oraz ${e2.startTime}-${e2.endTime}!"
                            )
                        )
                    }
                    // Check Student Conflict
                    else if (e1.studentName.isNotBlank() && e1.studentName.equals(e2.studentName, ignoreCase = true)) {
                        conflicts.add(
                            CalendarConflict(
                                event1 = e1,
                                event2 = e2,
                                conflictType = "STUDENT_OVERLAP",
                                description = "Kursant ${e1.studentName} posiada 2 nakładające się lekcje jednocześnie!"
                            )
                        )
                    }
                }
            }
        }
        conflicts
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun resolveConflict(conflictId: String, action: String) {
        val conflictList = calendarConflicts.value
        val conflict = conflictList.find { it.id == conflictId } ?: return

        if (action == "CANCEL_SECOND") {
            _calendarEvents.value = _calendarEvents.value.map { evt ->
                if (evt.id == conflict.event2.id) {
                    evt.copy(
                        status = "CANCELLED",
                        cancelReason = "Automatyczne wyeliminowanie konfliktu nakładających się terminów przez silnik OSK."
                    )
                } else evt
            }
            addReminder(
                text = "Silnik konfliktów wyeliminował nakładający się termin dla: ${conflict.event2.title}.",
                priority = "HIGH",
                recipientRole = "OWNER",
                recipientName = "Właściciel"
            )
        } else if (action == "AUTO_SHIFT") {
            val oldStart = parseTimeToMinutes(conflict.event2.startTime)
            val newStartMin = (oldStart + 120) % (24 * 60)
            val newEndMin = (newStartMin + 120) % (24 * 60)

            val newStartStr = String.format(Locale.getDefault(), "%02d:%02d", newStartMin / 60, newStartMin % 60)
            val newEndStr = String.format(Locale.getDefault(), "%02d:%02d", newEndMin / 60, newEndMin % 60)

            _calendarEvents.value = _calendarEvents.value.map { evt ->
                if (evt.id == conflict.event2.id) {
                    evt.copy(
                        startTime = newStartStr,
                        endTime = newEndStr
                    )
                } else evt
            }
            addReminder(
                text = "Przesunięto nakładający się termin ${conflict.event2.title} na $newStartStr-$newEndStr w celu usunięcia konfliktu.",
                priority = "MEDIUM",
                recipientRole = "OWNER",
                recipientName = "Właściciel"
            )
        }
    }

    // ALTERNATIVE SLOT PROPOSALS (3-DAY SCANNING + 24H/6H LIMIT)
    private val _alternativeProposals = MutableStateFlow<List<AlternativeSlotProposal>>(emptyList())
    val alternativeProposals: StateFlow<List<AlternativeSlotProposal>> = _alternativeProposals.asStateFlow()

    // 1. PAYMENTS & INSTALLMENTS MODULE
    private val _studentPayments = MutableStateFlow<List<StudentPayment>>(
        listOf(
            StudentPayment(studentName = "Jan Kowalski", title = "I Rata - Teoria i Plac (Kat. B)", amountPln = 1200, dueDate = "01.07.2026", paidDate = "02.07.2026", status = "OPŁACONE", paymentMethod = "Przelew Bankowy"),
            StudentPayment(studentName = "Jan Kowalski", title = "II Rata - Jazdy w Ruchu Miejskim", amountPln = 1000, dueDate = "01.08.2026", paidDate = null, status = "OCZEKUJE", paymentMethod = "Przelew Bankowy / BLIK"),
            StudentPayment(studentName = "Jan Kowalski", title = "III Rata - Egzamin Wewnętrzny", amountPln = 800, dueDate = "25.08.2026", paidDate = null, status = "OCZEKUJE", paymentMethod = "BLIK"),
            StudentPayment(studentName = "Anna Nowak", title = "I Rata - Kurs Kat. B", amountPln = 1500, dueDate = "15.06.2026", paidDate = "15.06.2026", status = "OPŁACONE", paymentMethod = "Karta w Biurze OSK"),
            StudentPayment(studentName = "Piotr Wiśniewski", title = "Jazdy Dodatkowe (2h)", amountPln = 200, dueDate = "20.07.2026", paidDate = "20.07.2026", status = "OPŁACONE", paymentMethod = "BLIK")
        )
    )
    val studentPayments: StateFlow<List<StudentPayment>> = _studentPayments.asStateFlow()

    fun addPayment(studentName: String, title: String, amountPln: Int, dueDate: String) {
        val newP = StudentPayment(
            studentName = studentName,
            title = title,
            amountPln = amountPln,
            dueDate = dueDate,
            status = "OCZEKUJE"
        )
        _studentPayments.value = _studentPayments.value + newP
    }

    fun markPaymentAsPaid(paymentId: String, method: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        _studentPayments.value = _studentPayments.value.map { p ->
            if (p.id == paymentId) {
                p.copy(status = "OPŁACONE", paidDate = todayStr, paymentMethod = method)
            } else p
        }
    }

    // 2. DIGITAL DRIVING SHEET (KARTA JAZD)
    private val _drivingSheets = MutableStateFlow<List<DrivingSheetEntry>>(
        listOf(
            DrivingSheetEntry(studentName = "Jan Kowalski", instructorName = "Tomasz Nowak", dateFormatted = "20.07.2026", hoursCount = 2.0f, topicsCovered = "Plac: przygotowanie do jazdy, pas ruchu, łuk, ruszanie na wzniesieniu", instructorNotes = "Wyśmienite opanowanie sprzęgła.", studentSigned = true, instructorSigned = true),
            DrivingSheetEntry(studentName = "Jan Kowalski", instructorName = "Tomasz Nowak", dateFormatted = "22.07.2026", hoursCount = 2.0f, topicsCovered = "Jazda w ruchu miejskim: rondo, skrzyżowania ze światłami", instructorNotes = "Zwracać uwagę na lusterko wsteczne.", studentSigned = true, instructorSigned = true),
            DrivingSheetEntry(studentName = "Jan Kowalski", instructorName = "Tomasz Nowak", dateFormatted = "25.07.2026", hoursCount = 2.0f, topicsCovered = "Parkowanie prostopadłe i równoległe w centrum", instructorNotes = "Parkowanie wykonane poprawnie za 1. razem.", studentSigned = false, instructorSigned = true)
        )
    )
    val drivingSheets: StateFlow<List<DrivingSheetEntry>> = _drivingSheets.asStateFlow()

    fun addDrivingSheetEntry(studentName: String, instructorName: String, dateFormatted: String, hours: Float, topics: String, notes: String) {
        val entry = DrivingSheetEntry(
            studentName = studentName,
            instructorName = instructorName,
            dateFormatted = dateFormatted,
            hoursCount = hours,
            topicsCovered = topics,
            instructorNotes = notes,
            studentSigned = false,
            instructorSigned = true
        )
        _drivingSheets.value = _drivingSheets.value + entry
    }

    fun studentSignDrivingSheet(entryId: String) {
        _drivingSheets.value = _drivingSheets.value.map { entry ->
            if (entry.id == entryId) entry.copy(studentSigned = true) else entry
        }
    }

    // 3. SMS & PUSH AUTOMATED REMINDERS SIMULATION
    private val _smsNotifications = MutableStateFlow<List<SimulatedSmsPushNotification>>(
        listOf(
            SimulatedSmsPushNotification(recipientPhone = "+48 601 234 567", recipientName = "Jan Kowalski", messageText = "Cześć Jan! Przypominamy o Twojej planowanej jeździe jutro o 10:00 z instruktorem Tomasz Nowak. Szerokiej drogi!", scheduledTime = "Dzisiaj, 10:00", type = "SMS_24H"),
            SimulatedSmsPushNotification(recipientPhone = "+48 502 987 654", recipientName = "Anna Nowak", messageText = "Przypomnienie OSK: Wykład z teorii Kat. B odbędzie się jutro o godz. 17:00 w Sali 1.", scheduledTime = "Wczoraj, 17:00", type = "PUSH_NOTIFICATION")
        )
    )
    val smsNotifications: StateFlow<List<SimulatedSmsPushNotification>> = _smsNotifications.asStateFlow()

    fun sendSimulatedSms(phone: String, name: String, text: String) {
        val notif = SimulatedSmsPushNotification(
            recipientPhone = phone,
            recipientName = name,
            messageText = text,
            scheduledTime = "Teraz",
            type = "SMS_24H"
        )
        _smsNotifications.value = listOf(notif) + _smsNotifications.value
    }

    // 4. WORD INTERNAL EXAM SIMULATOR
    val wordQuestions = listOf(
        WordQuestion(1, "Jaka jest maksymalna dopuszczalna prędkość samochodu osobowego w obszarze zabudowanym w Polsce?", "50 km/h", "60 km/h", "70 km/h", "A", 3, "PRĘDKOŚĆ"),
        WordQuestion(2, "Czy dopuszczalne jest wyprzedzanie pojazdu na przejściu dla pieszych bez sygnalizacji świetlnej?", "Tak, jeśli pieszych nie ma na przejściu", "Zabronione w każdym przypadku", "Tak, jeśli nie przekraczamy 30 km/h", "B", 3, "PIESI"),
        WordQuestion(3, "Co oznacza sygnał żółty ciągły nadawany przez sygnalizator świetlny?", "Zakaz wjazdu za sygnalizator, chyba że pojazd nie może zatrzymać się bez gwałtownego hamowania", "Można wjechać za sygnalizator bez ograniczeń", "Sygnał ostrzegawczy - pierwszeństwo mają piesi", "A", 3, "SYGNALIZACJA"),
        WordQuestion(4, "Jaki jest minimalny bieżnik opony w pojeździe osobowym dopuszczonym do ruchu?", "1.6 mm", "2.0 mm", "3.0 mm", "A", 2, "STAN TECHNICZNY"),
        WordQuestion(5, "Co należy zrobić w pierwszej kolejności po przybyciu na miejsce wypadku drogowego?", "Zabezpieczyć miejsce zdarzenia i wezwać służby ratunkowe (112)", "Natychmiast wyciągać poszkodowanych z pojazdu", "Zrobić zdjęcia do dokumentacji ubezpieczeniowej", "A", 3, "PIERWSZA POMOC")
    )

    private val _practicalEvaluations = MutableStateFlow<List<PracticalExamEvaluation>>(
        listOf(
            PracticalExamEvaluation(studentName = "Jan Kowalski", instructorName = "Tomasz Nowak", dateFormatted = "24.07.2026", preparationOk = true, maneuversManifoldOk = true, hillStartOk = true, parkingOk = true, cityIntersectionsOk = true, overallResult = "POZYTYWNY", remarks = "Egzamin wewnętrzny zaliczony wzorowo!")
        )
    )
    val practicalEvaluations: StateFlow<List<PracticalExamEvaluation>> = _practicalEvaluations.asStateFlow()

    fun addPracticalEvaluation(evaluation: PracticalExamEvaluation) {
        _practicalEvaluations.value = listOf(evaluation) + _practicalEvaluations.value
    }

    // 5. FLEET MANAGEMENT & VEHICLE FAULT REPORTS
    private val _fleetVehicles = MutableStateFlow<List<VehicleFleetItem>>(
        listOf(
            VehicleFleetItem(plateNumber = "KR 12345", model = "Hyundai i20 (2023) - Kat. B", kmMileage = 42500, nextMotDate = "15.09.2026", insuranceExpiryDate = "01.11.2026", status = "SPRAWNY"),
            VehicleFleetItem(plateNumber = "KR 67890", model = "Toyota Yaris (2022) - Kat. B", kmMileage = 68100, nextMotDate = "01.08.2026", insuranceExpiryDate = "20.08.2026", status = "WYMAGA PRZEGLĄDU"),
            VehicleFleetItem(plateNumber = "KR 11223", model = "Kia Rio (2024) - Kat. B", kmMileage = 18400, nextMotDate = "10.02.2027", insuranceExpiryDate = "15.03.2027", status = "SPRAWNY"),
            VehicleFleetItem(plateNumber = "KR 99887", model = "Yamaha MT-07 - Kat. A", kmMileage = 12300, nextMotDate = "05.10.2026", insuranceExpiryDate = "05.10.2026", status = "SPRAWNY")
        )
    )
    val fleetVehicles: StateFlow<List<VehicleFleetItem>> = _fleetVehicles.asStateFlow()

    private val _vehicleFaults = MutableStateFlow<List<VehicleFaultReport>>(
        listOf(
            VehicleFaultReport(plateNumber = "KR 67890", instructorName = "Tomasz Nowak", reportDate = "26.07.2026", description = "Słaby płyn w spryskiwaczach oraz cicha praca żarówki mijania lewa strona.", priority = "ŚREDNI", isResolved = false),
            VehicleFaultReport(plateNumber = "KR 12345", instructorName = "Marek Wiśniewski", reportDate = "20.07.2026", description = "Wymiana oleju silnikowego i filtrów.", priority = "NISKI", isResolved = true)
        )
    )
    val vehicleFaults: StateFlow<List<VehicleFaultReport>> = _vehicleFaults.asStateFlow()

    private val _fuelRefuels = MutableStateFlow<List<FuelRefuelEntry>>(
        listOf(
            FuelRefuelEntry(plateNumber = "KR 12345", instructorName = "Tomasz Nowak", dateFormatted = "25.07.2026", kmMileage = 42500, liters = 38.2, totalCostPln = 248.30, fuelType = "Pb95", stationName = "Orlen Balicka", receiptNumber = "FV/2026/07/112"),
            FuelRefuelEntry(plateNumber = "KR 12345", instructorName = "Tomasz Nowak", dateFormatted = "18.07.2026", kmMileage = 41920, liters = 37.5, totalCostPln = 243.75, fuelType = "Pb95", stationName = "Shell Armii Krajowej", receiptNumber = "FV/2026/07/089"),
            FuelRefuelEntry(plateNumber = "KR 67890", instructorName = "Marek Wiśniewski", dateFormatted = "24.07.2026", kmMileage = 68100, liters = 41.0, totalCostPln = 266.50, fuelType = "Pb95", stationName = "Orlen Jasnogórska", receiptNumber = "FV/2026/07/105"),
            FuelRefuelEntry(plateNumber = "KR 67890", instructorName = "Marek Wiśniewski", dateFormatted = "15.07.2026", kmMileage = 67450, liters = 42.5, totalCostPln = 276.25, fuelType = "Pb95", stationName = "BP Opolska", receiptNumber = "FV/2026/07/064"),
            FuelRefuelEntry(plateNumber = "KR 11223", instructorName = "Piotr Kowalczyk", dateFormatted = "22.07.2026", kmMileage = 18400, liters = 35.0, totalCostPln = 227.50, fuelType = "Pb95", stationName = "MOL Bora-Komorowskiego", receiptNumber = "FV/2026/07/098"),
            FuelRefuelEntry(plateNumber = "KR 99887", instructorName = "Anna Zielińska", dateFormatted = "20.07.2026", kmMileage = 12300, liters = 12.0, totalCostPln = 78.00, fuelType = "Pb95", stationName = "Orlen Balicka", receiptNumber = "FV/2026/07/091")
        )
    )
    val fuelRefuels: StateFlow<List<FuelRefuelEntry>> = _fuelRefuels.asStateFlow()

    private val _odometerLogs = MutableStateFlow<List<OdometerLogEntry>>(
        listOf(
            OdometerLogEntry(plateNumber = "KR 12345", instructorName = "Tomasz Nowak", dateFormatted = "26.07.2026", startKmMileage = 42420, endKmMileage = 42500, notes = "Jazdy z kursantem Jan Kowalski i Anna Nowak (8h)"),
            OdometerLogEntry(plateNumber = "KR 67890", instructorName = "Marek Wiśniewski", dateFormatted = "26.07.2026", startKmMileage = 68030, endKmMileage = 68100, notes = "Jazdy z kursantem Piotr Wiśniewski (7h)"),
            OdometerLogEntry(plateNumber = "KR 11223", instructorName = "Piotr Kowalczyk", dateFormatted = "25.07.2026", startKmMileage = 18320, endKmMileage = 18400, notes = "Plac manewrowy i ruch miejski (8h)")
        )
    )
    val odometerLogs: StateFlow<List<OdometerLogEntry>> = _odometerLogs.asStateFlow()

    fun addFuelRefuel(
        plateNumber: String,
        instructorName: String,
        kmMileage: Int,
        liters: Double,
        totalCostPln: Double,
        fuelType: String,
        stationName: String,
        receiptNumber: String
    ) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val entry = FuelRefuelEntry(
            plateNumber = plateNumber,
            instructorName = instructorName,
            dateFormatted = todayStr,
            kmMileage = kmMileage,
            liters = liters,
            totalCostPln = totalCostPln,
            fuelType = fuelType,
            stationName = stationName,
            receiptNumber = receiptNumber
        )
        _fuelRefuels.value = listOf(entry) + _fuelRefuels.value

        // Update mileage in vehicle fleet item if higher
        _fleetVehicles.value = _fleetVehicles.value.map { car ->
            if (car.plateNumber == plateNumber && kmMileage > car.kmMileage) {
                car.copy(kmMileage = kmMileage)
            } else car
        }

        // Auto-add expense to StudentPayments/OSK Finance accounting
        val paymentDesc = "Faktura Paliwo $receiptNumber ($plateNumber - $liters L)"
        addReminder(
            text = "⛽ Zaksięgowano tankowanie $plateNumber przez $instructorName: $liters L ($totalCostPln PLN). Faktura: $receiptNumber",
            priority = "MEDIUM",
            recipientRole = "OWNER",
            recipientName = "Właściciel"
        )
    }

    fun addOdometerLog(
        plateNumber: String,
        instructorName: String,
        startKm: Int,
        endKm: Int,
        notes: String
    ) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val entry = OdometerLogEntry(
            plateNumber = plateNumber,
            instructorName = instructorName,
            dateFormatted = todayStr,
            startKmMileage = startKm,
            endKmMileage = endKm,
            notes = notes
        )
        _odometerLogs.value = listOf(entry) + _odometerLogs.value

        // Update mileage in vehicle fleet item
        _fleetVehicles.value = _fleetVehicles.value.map { car ->
            if (car.plateNumber == plateNumber && endKm > car.kmMileage) {
                car.copy(kmMileage = endKm)
            } else car
        }
    }

    fun reportVehicleFault(plateNumber: String, instructorName: String, description: String, priority: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val report = VehicleFaultReport(
            plateNumber = plateNumber,
            instructorName = instructorName,
            reportDate = todayStr,
            description = description,
            priority = priority,
            isResolved = false
        )
        _vehicleFaults.value = listOf(report) + _vehicleFaults.value
        addReminder(
            text = "⚠️ Zgłoszono usterkę pojazdu $plateNumber ($priority): $description",
            priority = if (priority == "PILNY") "HIGH" else "MEDIUM",
            recipientRole = "OWNER",
            recipientName = "Właściciel"
        )
    }

    fun resolveVehicleFault(faultId: String) {
        _vehicleFaults.value = _vehicleFaults.value.map { f ->
            if (f.id == faultId) f.copy(isResolved = true) else f
        }
    }

    // 6. WORD ROUTE HOTSPOTS & EXAM TIPS
    val wordHotspots = listOf(
        WordRouteHotspot(1, "Rondo Ofiar Katynia (Kraków)", "Rondo Wielopasmowe", "Główne rondo dojazdowe do WORD. Uwaga na właściwy pas przed wjazdem i ustąpienie pierwszeństwa pojazdom z lewej.", "TRUDNE", "Wjeżdżając na rondo z lewego pasa, musisz zjechać z lewego lub środkowego. Pamiętaj o sygnalizowaniu zjazdu kierunkowskazem!"),
        WordRouteHotspot(2, "Skrzyżowanie Armii Krajowej / Piastowska", "Sygnalizacja Świetlna ze Strzałką Warunkową", "Częste miejsce oblewaniu egzaminu za niestatnowcze zatrzymanie się przed strzałką skrętu w prawo.", "BARDZO TRUDNE", "PAMIĘTAJ: Przed strzałką zieloną do skrętu w prawo MUSISZ zatrzymać się całkowicie (jak przed znakiem STOP)!"),
        WordRouteHotspot(3, "Strefa Zamieszkania przy ul. Balickiej", "Przejście dla Pieszych i Próg Zwalniający", "Ograniczenie prędkości do 20 km/h, piesi mają bezwzględne pierwszeństwo w całej strefie.", "ŚREDNIE", "W strefie zamieszkania parkowanie dopuszczalne jest wyłącznie w miejscach wyznaczonych!"),
        WordRouteHotspot(4, "Plac Manewrowy WORD - Ruszanie na Wzniesieniu", "Górka Egzaminacyjna", "Ruszanie z hamulca pomocniczego (ręcznego) bez stoczenia się pojazdu do tyłu o więcej niż 20 cm.", "ŚREDNIE", "Trzymaj gaz na około 1500-2000 obr/min, powoli puszczaj sprzęgło aż poczujesz lekki opór (tył auta nieco siądzie), po czym zwolnij hamulec ręczny.")
    )

    // 7. LESSON FEEDBACK / OCENA INSTRUKTORA
    private val _lessonFeedbacks = MutableStateFlow<List<LessonFeedback>>(
        listOf(
            LessonFeedback(studentName = "Jan Kowalski", instructorName = "Tomasz Nowak", dateFormatted = "22.07.2026", ratingStars = 5, commentText = "Super podejście, wszystko dokładnie i spokojnie wytłumaczone na rondzie!")
        )
    )
    val lessonFeedbacks: StateFlow<List<LessonFeedback>> = _lessonFeedbacks.asStateFlow()

    fun submitLessonFeedback(studentName: String, instructorName: String, ratingStars: Int, comment: String) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val todayStr = sdf.format(Date())
        val fb = LessonFeedback(
            studentName = studentName,
            instructorName = instructorName,
            dateFormatted = todayStr,
            ratingStars = ratingStars,
            commentText = comment
        )
        _lessonFeedbacks.value = listOf(fb) + _lessonFeedbacks.value
    }

    // 8. STUDENT REFERRAL PROGRAM (POLEĆ ZNAJOMEGO)
    private val _studentReferrals = MutableStateFlow<List<StudentReferralInfo>>(
        listOf(
            StudentReferralInfo(studentName = "Jan Kowalski", myReferralCode = "JAN2026", friendsJoinedCount = 2, freeBonusHours = 2)
        )
    )
    val studentReferrals: StateFlow<List<StudentReferralInfo>> = _studentReferrals.asStateFlow()

    fun generateAvailableSlotsNext3Days(preferredInstructor: String = ""): List<SuggestedSlot> {
        val result = mutableListOf<SuggestedSlot>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val instructors = if (preferredInstructor.isNotBlank()) {
            listOf(preferredInstructor)
        } else {
            listOf("Tomasz Nowak", "Marek Wiśniewski", "Anna Kowalska")
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dayNames = listOf("Jutro", "Pojutrze", "Za 3 dni")
        val defaultTimes = listOf("08:00" to "10:00", "10:00" to "12:00", "12:00" to "14:00", "14:00" to "16:00", "16:00" to "18:00")

        for (i in 1..3) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dayMs = cal.timeInMillis
            val dateStr = "${sdf.format(Date(dayMs))} (${dayNames[i-1]})"

            for (inst in instructors) {
                for (time in defaultTimes) {
                    val isOccupied = _calendarEvents.value.any { evt ->
                        evt.date == dayMs &&
                        evt.instructorName.equals(inst, ignoreCase = true) &&
                        evt.status != "CANCELLED" &&
                        isTimeOverlapping(evt.startTime, evt.endTime, time.first, time.second)
                    }
                    if (!isOccupied) {
                        result.add(
                            SuggestedSlot(
                                dateMs = dayMs,
                                dateFormatted = dateStr,
                                startTime = time.first,
                                endTime = time.second,
                                instructorName = inst
                            )
                        )
                    }
                }
            }
        }
        return result.take(6)
    }

    fun propose3DayAlternativesToStudent(
        studentName: String,
        conflictDesc: String,
        isTightSchedule: Boolean,
        preferredInstructor: String = ""
    ) {
        val freeSlots = generateAvailableSlotsNext3Days(preferredInstructor)
        val expirationHours = if (isTightSchedule) 6 else 24

        val proposal = AlternativeSlotProposal(
            studentName = studentName,
            conflictDescription = conflictDesc,
            suggestedSlots = freeSlots,
            expirationHours = expirationHours
        )

        _alternativeProposals.value = _alternativeProposals.value + proposal

        val limitMsg = if (isTightSchedule) "Maksymalnie 6 godzin (napięty grafik)" else "Maksymalnie 24 godziny"
        addReminder(
            text = "Wysłano propozycję alternatywnych terminów (na 3 dni do przodu) do kursanta $studentName. Czas na akceptację przez kursanta: $limitMsg.",
            priority = "HIGH",
            recipientRole = "ALL",
            recipientName = "System"
        )
    }

    fun acceptAlternativeProposal(proposalId: String, slot: SuggestedSlot) {
        val proposal = _alternativeProposals.value.find { it.id == proposalId } ?: return

        _alternativeProposals.value = _alternativeProposals.value.map {
            if (it.id == proposalId) {
                it.copy(status = "ACCEPTED", acceptedSlot = slot)
            } else it
        }

        val newEvent = CalendarEvent(
            title = "Jazda kat. B: ${proposal.studentName}",
            date = slot.dateMs,
            startTime = slot.startTime,
            endTime = slot.endTime,
            type = "TRAINING",
            category = "LESSON",
            studentName = proposal.studentName,
            instructorName = slot.instructorName,
            status = "PLANNED"
        )
        _calendarEvents.value = _calendarEvents.value + newEvent

        addReminder(
            text = "Kursant ${proposal.studentName} zaakceptował alternatywny termin jazdy (${slot.dateFormatted} o ${slot.startTime} z ${slot.instructorName})!",
            priority = "HIGH",
            recipientRole = "OWNER",
            recipientName = "Właściciel"
        )
    }

    fun registerStudentSelf(
        name: String,
        email: String,
        phone: String,
        category: String,
        pkkNumber: String = "",
        slotChoice: String, // "EXISTING", "CUSTOM", "TIMEFRAME"
        selectedEventId: String? = null,
        customDate: Long? = null,
        customStartTime: String? = null,
        customEndTime: String? = null,
        timeframeStart: Long? = null,
        timeframeEnd: Long? = null,
        timeframeNote: String = "",
        preferredInstructor: String = "",
        accessCode: String
    ) {
        val approved = slotChoice == "EXISTING"
        val registration = StudentRegistration(
            accessCode = accessCode,
            name = name,
            phone = phone,
            email = email,
            category = category,
            pkkNumber = pkkNumber,
            isApproved = approved,
            selectedEventId = selectedEventId,
            customDate = customDate,
            customStartTime = customStartTime,
            customEndTime = customEndTime,
            timeframeStart = timeframeStart,
            timeframeEnd = timeframeEnd,
            timeframeNote = timeframeNote,
            preferredInstructor = preferredInstructor
        )
        
        _studentRegistrations.value = _studentRegistrations.value + registration

        if (slotChoice == "EXISTING" && selectedEventId != null) {
            // Assign student to the chosen free event
            _calendarEvents.value = _calendarEvents.value.map { evt ->
                if (evt.id == selectedEventId) {
                    evt.copy(
                        studentName = name,
                        title = "Jazda kat. $category: $name",
                        status = "PLANNED",
                        instructorName = preferredInstructor.ifEmpty { evt.instructorName }
                    )
                } else evt
            }
            // Add reminder
            addReminder(
                text = "Nowy kursant $name (PKK: ${pkkNumber.ifEmpty { "brak" }}) zapisał się na wolny termin (${category})!",
                priority = "MEDIUM",
                recipientRole = "OWNER",
                recipientName = "Właściciel"
            )
        } else if (slotChoice == "CUSTOM" && customDate != null && customStartTime != null && customEndTime != null) {
            // Create a custom event pending staff approval
            val newEvent = CalendarEvent(
                title = "Oczekiwanie: Jazda kat. $category: $name",
                date = customDate,
                startTime = customStartTime,
                endTime = customEndTime,
                type = "TRAINING",
                category = "LESSON",
                studentName = name,
                instructorName = preferredInstructor,
                status = "PENDING_STAFF_APPROVAL"
            )
            _calendarEvents.value = _calendarEvents.value + newEvent
            
            // Add notification for staff
            addReminder(
                text = "Kursant $name (PKK: ${pkkNumber.ifEmpty { "brak" }}) prosi o zatwierdzenie niestandardowego terminu jazdy! (Czas na odpowiedź: 3 dni robocze)",
                priority = "MEDIUM",
                recipientRole = "OWNER",
                recipientName = "Właściciel"
            )
        } else if (slotChoice == "TIMEFRAME" && timeframeStart != null) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val rangeStr = "${sdf.format(Date(timeframeStart))} - ${if (timeframeEnd != null) sdf.format(Date(timeframeEnd)) else "bez końca"}"
            val newEvent = CalendarEvent(
                title = "Ramy czasowe: $name (Kat. $category)",
                date = timeframeStart,
                startTime = "10:00",
                endTime = "12:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = name,
                instructorName = preferredInstructor,
                status = "PENDING_STAFF_APPROVAL"
            )
            _calendarEvents.value = _calendarEvents.value + newEvent

            val destRole = if (preferredInstructor.isNotBlank()) "INSTRUKTOR" else "OWNER"
            val destName = if (preferredInstructor.isNotBlank()) preferredInstructor else "Właściciel"

            addReminder(
                text = "Wniosek o ramy czasowe dla kursanta $name ($rangeStr, Dedykowany instruktor: ${preferredInstructor.ifEmpty { "Dowolny" }}). Masz 3 dni robocze na akceptację lub propozycję poprawek!",
                priority = "HIGH",
                recipientRole = destRole,
                recipientName = destName
            )
        }
    }

    fun approveStudentRegistrationAndSlot(registrationId: String, eventId: String) {
        _studentRegistrations.value = _studentRegistrations.value.map { reg ->
            if (reg.id == registrationId) reg.copy(isApproved = true) else reg
        }
        _calendarEvents.value = _calendarEvents.value.map { evt ->
            if (evt.id == eventId) {
                val titleStr = evt.title
                val categoryExtracted = if (titleStr.contains("kat. ")) {
                    titleStr.substringAfter("kat. ").substringBefore(":")
                } else "B"
                evt.copy(
                    title = "Jazda kat. ${if (categoryExtracted.length <= 2) categoryExtracted else "B"}: ${evt.studentName}",
                    status = "PLANNED"
                )
            } else evt
        }
    }

    fun registerStudentManual(
        name: String,
        email: String,
        phone: String,
        category: String,
        instructorName: String
    ) {
        // Generate an access code first
        val randomNum = Random.nextInt(100000, 999999)
        val codeString = "KURS-$randomNum"
        
        viewModelScope.launch {
            val code = AccessCode(
                code = codeString,
                role = "KURSANT",
                name = name
            )
            repository.insertAccessCode(code)
        }

        // Add to registrations as pre-approved manual entry
        val registration = StudentRegistration(
            accessCode = codeString,
            name = name,
            phone = phone,
            email = email,
            category = category,
            preferredInstructor = instructorName,
            isApproved = true
        )
        _studentRegistrations.value = _studentRegistrations.value + registration

        // Pre-create a planned lesson with assigned instructor if selected
        if (instructorName.isNotEmpty()) {
            val newEvent = CalendarEvent(
                title = "Jazda kat. $category: $name",
                date = getTimestampForDay(2),
                startTime = "12:00",
                endTime = "14:00",
                type = "TRAINING",
                category = "LESSON",
                studentName = name,
                instructorName = instructorName,
                status = "PLANNED"
            )
            _calendarEvents.value = _calendarEvents.value + newEvent
        }
    }

    fun proposeNewTermForCancelledEvent(
        eventId: String,
        newDate: Long,
        newStartTime: String,
        newEndTime: String
    ) {
        _calendarEvents.value = _calendarEvents.value.map { evt ->
            if (evt.id == eventId) {
                evt.copy(
                    status = "PROPOSED_BY_STAFF",
                    rescheduleNewDate = newDate,
                    rescheduleNewStartTime = newStartTime,
                    rescheduleNewEndTime = newEndTime
                )
            } else evt
        }
        val affectedEvent = _calendarEvents.value.find { it.id == eventId }
        val student = affectedEvent?.studentName ?: "Kursant"
        
        // Add a reminder to the student
        addReminder(
            text = "Zaproponowano nowy termin dla odwołanej lekcji: ${affectedEvent?.title}. Zaakceptuj lub poprosić o inny.",
            priority = "MEDIUM",
            recipientRole = "KURSANT",
            recipientName = student
        )
    }

    fun studentAcceptProposedTerm(eventId: String) {
        _calendarEvents.value = _calendarEvents.value.map { evt ->
            if (evt.id == eventId && evt.rescheduleNewDate != null) {
                evt.copy(
                    date = evt.rescheduleNewDate,
                    startTime = evt.rescheduleNewStartTime ?: evt.startTime,
                    endTime = evt.rescheduleNewEndTime ?: evt.endTime,
                    status = "PLANNED",
                    rescheduleNewDate = null,
                    rescheduleNewStartTime = null,
                    rescheduleNewEndTime = null
                )
            } else evt
        }
    }

    fun studentRejectAndProposeAnotherTerm(
        eventId: String,
        otherDate: Long,
        otherStartTime: String,
        otherEndTime: String
    ) {
        _calendarEvents.value = _calendarEvents.value.map { evt ->
            if (evt.id == eventId) {
                evt.copy(
                    status = "RESCHEDULE_PENDING",
                    rescheduleNewDate = otherDate,
                    rescheduleNewStartTime = otherStartTime,
                    rescheduleNewEndTime = otherEndTime
                )
            } else evt
        }
        val affectedEvent = _calendarEvents.value.find { it.id == eventId }
        val student = affectedEvent?.studentName ?: "Kursant"
        val instructor = affectedEvent?.instructorName ?: "Tomasz Nowak"
        
        addReminder(
            text = "Kursant $student poprosił o inny termin lekcji: $otherStartTime-$otherEndTime.",
            priority = "MEDIUM",
            recipientRole = "INSTRUKTOR",
            recipientName = instructor
        )
        addReminder(
            text = "Kursant $student poprosił o inny termin lekcji: $otherStartTime-$otherEndTime.",
            priority = "MEDIUM",
            recipientRole = "OWNER",
            recipientName = "Właściciel"
        )
    }

    fun toggleOwnerCoverage(enabled: Boolean) {
        _cloudSubscription.value = _cloudSubscription.value.copy(
            isOwnerPayingForEveryone = enabled
        )
    }

    fun triggerForceCloudSync() {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeNow = sdf.format(Date())
        _cloudSubscription.value = _cloudSubscription.value.copy(
            lastSyncTimestamp = "Przed chwilą ($timeNow • Sync OK)",
            isSyncActive = true
        )
    }

    fun updateSmsSenderId(newSenderId: String) {
        if (newSenderId.isNotBlank()) {
            _cloudSubscription.value = _cloudSubscription.value.copy(
                smsSenderId = newSenderId.trim()
            )
        }
    }

    fun generateNewPairingCode() {
        val randomDigits = Random.nextInt(1000, 9999)
        _cloudSubscription.value = _cloudSubscription.value.copy(
            pairingCode = "OSK-CLOUD-$randomDigits"
        )
    }

    fun updateSyncFrequency(newFreq: String) {
        _cloudSubscription.value = _cloudSubscription.value.copy(
            syncFrequency = newFreq
        )
    }

    fun logout() {
        viewModelScope.launch {
            repository.clearActiveSession()
            _loginState.value = LoginUiState.Idle
            _registerState.value = RegisterOskUiState.Idle
        }
    }
}
