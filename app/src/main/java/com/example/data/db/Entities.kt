package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val registrationPlate: String,
    val fuelLevelPercent: Int,
    val techStatus: String, // "W pełni sprawny", "Wymaga serwisu", "W naprawie"
    val nextInspectionDate: String,
    val insuranceExpiryDate: String = "2027-02-18",
    val assignedInstructor: String = "Brak przypisania"
)

@Entity(tableName = "vehicle_reports")
data class VehicleReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val vehiclePlate: String,
    val startOdometer: Double,
    val refuelOdometer: Double? = null,
    val fuelQuantity: Double? = null,
    val pricePerLiter: Double? = null,
    val extraExpenseName: String? = null,
    val extraExpenseCost: Double? = null,
    val endOdometer: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_keys")
data class UserKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val role: String, // "Zarządca", "Instruktor", "Kursant"
    val assignedName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false,
    val isRemovedFromChat: Boolean = false
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val instructorName: String,
    val vehiclePlate: String,
    val date: String,
    val time: String,
    val durationHours: Int,
    val status: String, // "Zaplanowana", "Zrealizowana", "Anulowana"
    val notes: String = ""
)

@Entity(tableName = "finances")
data class FinanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountPln: Double,
    val type: String, // "INCOME", "EXPENSE"
    val category: String, // "Czesne kursanta", "Paliwo", "Serwis", "Wynagrodzenie"
    val date: String
)

@Entity(tableName = "sick_leaves")
data class SickLeaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val instructorName: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "Zgłoszone" // "Zgłoszone", "Zatwierdzone", "Zastępstwo przydzielone"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderRole: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val channelId: String = "general"
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val dateOfBirth: String,
    val pesel: String,
    val phone: String,
    val email: String,
    val selectedDate: String,
    val selectedTimeSlots: String,
    val status: String = "Oczekująca", // "Oczekująca", "Zatwierdzona", "Odrzucona", "Zaproponowano alternatywę"
    val alternativeSlots: String? = null,
    val instructorNote: String? = null,
    val category: String = "Kat. B",
    val pkkNumber: String? = null,
    val documentPhotoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val category: String,
    val pkkNumber: String?,
    val photoPath: String?,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val score: Int,
    val maxPoints: Int,
    val isPassed: Boolean,
    val examDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val senderRole: String, // "Zarządca", "Instruktor", "System"
    val targetGroup: String, // "KURSANT" (Students), "INSTRUKTOR" (Instructors), "ALL" (Everyone)
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isWishReminder: Boolean = false, // True if this notification is a "Życz powodzenia" action button notification
    val relatedStudentName: String? = null, // Student to send wishes to
    val relatedExamType: String? = null // Type of exam (Theory/Practical)
)

@Entity(tableName = "exam_reservations")
data class ExamReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String,
    val examDate: String, // e.g., "2026-07-30"
    val examTime: String, // e.g., "16:30"
    val examType: String, // "Teoretyczny" lub "Praktyczny"
    val pkkNumber: String = "", // Driver's PKK number
    val pkkStatus: String = "Zablokowany", // "Zwolniony" (Released/Free) lub "Zablokowany" (Blocked)
    val hasPhoto: Boolean = false, // Does student have a photo in documents
    val isConfirmed: Boolean = false, // True if PKK is wolne/zwolnione and all OK (Green), False otherwise (Red)
    val isWishesSent: Boolean = false, // Has the instructor already sent good luck wishes
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "category_prices")
data class CategoryPriceEntity(
    @PrimaryKey val category: String, // "Kat. A", "Kat. B", etc.
    val priceCoursePln: Double = 0.0,
    val priceHourPln: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(tableName = "fixed_costs")
data class FixedCostConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amountPln: Double
)

@Entity(tableName = "instructor_rates")
data class InstructorRateEntity(
    @PrimaryKey val instructorName: String,
    val hourlyRatePln: Double
)

@Entity(tableName = "instructor_days_off")
data class InstructorDayOffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val instructorName: String,
    val date: String // format "YYYY-MM-DD" lub "yyyy-MM-dd"
)


