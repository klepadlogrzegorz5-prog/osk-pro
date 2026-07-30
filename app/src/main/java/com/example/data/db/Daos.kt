package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY id DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Long)
}

@Dao
interface UserKeyDao {
    @Query("SELECT * FROM user_keys ORDER BY createdAt DESC")
    fun getAllKeys(): Flow<List<UserKeyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: UserKeyEntity)

    @Query("DELETE FROM user_keys WHERE id = :id")
    suspend fun deleteKeyById(id: Long)

    @Query("UPDATE user_keys SET isBlocked = :isBlocked WHERE id = :id")
    suspend fun updateBlockStatus(id: Long, isBlocked: Boolean)

    @Query("UPDATE user_keys SET isRemovedFromChat = :isRemoved WHERE id = :id")
    suspend fun updateRemoveStatus(id: Long, isRemoved: Boolean)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY date ASC, time ASC")
    fun getAllLessons(): Flow<List<LessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET status = :newStatus WHERE id = :id")
    suspend fun updateLessonStatus(id: Long, newStatus: String)

    @Query("DELETE FROM lessons WHERE id = :id")
    suspend fun deleteLessonById(id: Long)
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finances ORDER BY id DESC")
    fun getAllFinances(): Flow<List<FinanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(finance: FinanceEntity)

    @Query("DELETE FROM finances WHERE id = :id")
    suspend fun deleteFinanceById(id: Long)
}

@Dao
interface SickLeaveDao {
    @Query("SELECT * FROM sick_leaves ORDER BY id DESC")
    fun getAllSickLeaves(): Flow<List<SickLeaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSickLeave(sickLeave: SickLeaveEntity)

    @Query("UPDATE sick_leaves SET status = :newStatus WHERE id = :id")
    suspend fun updateSickLeaveStatus(id: Long, newStatus: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY createdAt DESC")
    fun getAllReservations(): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("UPDATE reservations SET status = :newStatus WHERE id = :id")
    suspend fun updateReservationStatus(id: Long, newStatus: String)

    @Query("UPDATE reservations SET status = :newStatus, alternativeSlots = :alternatives, instructorNote = :note WHERE id = :id")
    suspend fun proposeAlternatives(id: Long, newStatus: String = "Zaproponowano alternatywę", alternatives: String, note: String)

    @Query("UPDATE reservations SET status = :newStatus, selectedDate = :selectedDate, selectedTimeSlots = :selectedTimeSlots WHERE id = :id")
    suspend fun acceptAlternative(id: Long, newStatus: String = "Zatwierdzona", selectedDate: String, selectedTimeSlots: String)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteReservationById(id: Long)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM student_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM student_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}

@Dao
interface VehicleReportDao {
    @Query("SELECT * FROM vehicle_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<VehicleReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: VehicleReportEntity)

    @Query("DELETE FROM vehicle_reports WHERE id = :id")
    suspend fun deleteReportById(id: Long)
}

@Dao
interface ExamResultDao {
    @Query("SELECT * FROM exam_results ORDER BY examDate DESC")
    fun getAllExamResults(): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)

    @Query("DELETE FROM exam_results WHERE id = :id")
    suspend fun deleteExamResultById(id: Long)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotificationById(id: Long)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}

@Dao
interface ExamReservationDao {
    @Query("SELECT * FROM exam_reservations ORDER BY examDate ASC, examTime ASC")
    fun getAllExamReservations(): Flow<List<ExamReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamReservation(examReservation: ExamReservationEntity)

    @Query("UPDATE exam_reservations SET pkkNumber = :pkkNumber, isConfirmed = :isConfirmed, pkkStatus = :pkkStatus WHERE id = :id")
    suspend fun updatePkkInfo(id: Long, pkkNumber: String, isConfirmed: Boolean, pkkStatus: String)

    @Query("UPDATE exam_reservations SET isWishesSent = 1 WHERE id = :id")
    suspend fun markWishesSent(id: Long)

    @Query("DELETE FROM exam_reservations WHERE id = :id")
    suspend fun deleteExamReservationById(id: Long)

    @Query("DELETE FROM exam_reservations")
    suspend fun deleteAllExamReservations()
}


@Dao
interface CategoryPriceDao {
    @Query("SELECT * FROM category_prices ORDER BY category ASC")
    fun getAllCategoryPrices(): Flow<List<CategoryPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryPrice(price: CategoryPriceEntity)

    @Query("DELETE FROM category_prices WHERE category = :category")
    suspend fun deleteCategoryPrice(category: String)
}

@Dao
interface FixedCostConfigDao {
    @Query("SELECT * FROM fixed_costs ORDER BY id DESC")
    fun getAllFixedCosts(): Flow<List<FixedCostConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedCost(cost: FixedCostConfigEntity)

    @Query("DELETE FROM fixed_costs WHERE id = :id")
    suspend fun deleteFixedCostById(id: Long)
}

@Dao
interface InstructorRateDao {
    @Query("SELECT * FROM instructor_rates ORDER BY instructorName ASC")
    fun getAllInstructorRates(): Flow<List<InstructorRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstructorRate(rate: InstructorRateEntity)

    @Query("DELETE FROM instructor_rates WHERE instructorName = :name")
    suspend fun deleteInstructorRate(name: String)
}

@Dao
interface InstructorDayOffDao {
    @Query("SELECT * FROM instructor_days_off ORDER BY date ASC")
    fun getAllDaysOff(): Flow<List<InstructorDayOffEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayOff(dayOff: InstructorDayOffEntity)

    @Query("DELETE FROM instructor_days_off WHERE id = :id")
    suspend fun deleteDayOffById(id: Long)
}


