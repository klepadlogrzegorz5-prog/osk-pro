package com.example.data.repository

import com.example.data.db.*
import kotlinx.coroutines.flow.Flow

class OskRepository(private val database: AppDatabase) {

    // Vehicles
    val vehicles: Flow<List<VehicleEntity>> = database.vehicleDao().getAllVehicles()
    suspend fun insertVehicle(vehicle: VehicleEntity) = database.vehicleDao().insertVehicle(vehicle)
    suspend fun deleteVehicle(id: Long) = database.vehicleDao().deleteVehicleById(id)

    // User Access Keys
    val userKeys: Flow<List<UserKeyEntity>> = database.userKeyDao().getAllKeys()
    suspend fun insertUserKey(key: UserKeyEntity) = database.userKeyDao().insertKey(key)
    suspend fun deleteUserKey(id: Long) = database.userKeyDao().deleteKeyById(id)
    suspend fun updateUserBlockStatus(id: Long, isBlocked: Boolean) = database.userKeyDao().updateBlockStatus(id, isBlocked)
    suspend fun updateUserRemoveStatus(id: Long, isRemoved: Boolean) = database.userKeyDao().updateRemoveStatus(id, isRemoved)

    // Lessons / Schedule
    val lessons: Flow<List<LessonEntity>> = database.lessonDao().getAllLessons()
    suspend fun insertLesson(lesson: LessonEntity) = database.lessonDao().insertLesson(lesson)
    suspend fun updateLessonStatus(id: Long, status: String) = database.lessonDao().updateLessonStatus(id, status)
    suspend fun deleteLesson(id: Long) = database.lessonDao().deleteLessonById(id)

    // Finances
    val finances: Flow<List<FinanceEntity>> = database.financeDao().getAllFinances()
    suspend fun insertFinance(finance: FinanceEntity) = database.financeDao().insertFinance(finance)
    suspend fun deleteFinance(id: Long) = database.financeDao().deleteFinanceById(id)

    // Sick Leaves (L4)
    val sickLeaves: Flow<List<SickLeaveEntity>> = database.sickLeaveDao().getAllSickLeaves()
    suspend fun insertSickLeave(sickLeave: SickLeaveEntity) = database.sickLeaveDao().insertSickLeave(sickLeave)
    suspend fun updateSickLeaveStatus(id: Long, status: String) = database.sickLeaveDao().updateSickLeaveStatus(id, status)

    // Chat
    val chatMessages: Flow<List<ChatMessageEntity>> = database.chatDao().getAllMessages()
    suspend fun sendMessage(message: ChatMessageEntity) = database.chatDao().insertMessage(message)

    // Reservations
    val reservations: Flow<List<ReservationEntity>> = database.reservationDao().getAllReservations()
    suspend fun insertReservation(reservation: ReservationEntity) = database.reservationDao().insertReservation(reservation)
    suspend fun updateReservationStatus(id: Long, status: String) = database.reservationDao().updateReservationStatus(id, status)
    suspend fun proposeAlternatives(id: Long, alternatives: String, note: String) = database.reservationDao().proposeAlternatives(id, alternatives = alternatives, note = note)
    suspend fun acceptAlternative(id: Long, selectedDate: String, selectedTimeSlots: String) = database.reservationDao().acceptAlternative(id, selectedDate = selectedDate, selectedTimeSlots = selectedTimeSlots)
    suspend fun deleteReservation(id: Long) = database.reservationDao().deleteReservationById(id)

    // Documents
    val studentDocuments: Flow<List<DocumentEntity>> = database.documentDao().getAllDocuments()
    suspend fun insertDocument(document: DocumentEntity) = database.documentDao().insertDocument(document)
    suspend fun deleteDocument(id: Long) = database.documentDao().deleteDocumentById(id)

    // Vehicle Reports
    val vehicleReports: Flow<List<VehicleReportEntity>> = database.vehicleReportDao().getAllReports()
    suspend fun insertVehicleReport(report: VehicleReportEntity) = database.vehicleReportDao().insertReport(report)
    suspend fun deleteVehicleReport(id: Long) = database.vehicleReportDao().deleteReportById(id)

    // Exam Results
    val examResults: Flow<List<ExamResultEntity>> = database.examResultDao().getAllExamResults()
    suspend fun insertExamResult(result: ExamResultEntity) = database.examResultDao().insertExamResult(result)
    suspend fun deleteExamResult(id: Long) = database.examResultDao().deleteExamResultById(id)

    // Notifications
    val notifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()
    suspend fun insertNotification(notification: NotificationEntity) = database.notificationDao().insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = database.notificationDao().markAsRead(id)
    suspend fun markAllNotificationsAsRead() = database.notificationDao().markAllAsRead()
    suspend fun deleteNotification(id: Long) = database.notificationDao().deleteNotificationById(id)
    suspend fun deleteAllNotifications() = database.notificationDao().deleteAllNotifications()

    // Exam Reservations
    val examReservations: Flow<List<ExamReservationEntity>> = database.examReservationDao().getAllExamReservations()
    suspend fun insertExamReservation(reservation: ExamReservationEntity) = database.examReservationDao().insertExamReservation(reservation)
    suspend fun updateExamReservationPkk(id: Long, pkkNumber: String, isConfirmed: Boolean, pkkStatus: String) = 
        database.examReservationDao().updatePkkInfo(id, pkkNumber, isConfirmed, pkkStatus)
    suspend fun markExamReservationWishesSent(id: Long) = database.examReservationDao().markWishesSent(id)
    suspend fun deleteExamReservation(id: Long) = database.examReservationDao().deleteExamReservationById(id)
    suspend fun deleteAllExamReservations() = database.examReservationDao().deleteAllExamReservations()

    // Category prices
    val categoryPrices: kotlinx.coroutines.flow.Flow<List<CategoryPriceEntity>> = database.categoryPriceDao().getAllCategoryPrices()
    suspend fun insertCategoryPrice(price: CategoryPriceEntity) = database.categoryPriceDao().insertCategoryPrice(price)
    suspend fun deleteCategoryPrice(category: String) = database.categoryPriceDao().deleteCategoryPrice(category)

    // Fixed costs
    val fixedCosts: Flow<List<FixedCostConfigEntity>> = database.fixedCostConfigDao().getAllFixedCosts()
    suspend fun insertFixedCost(cost: FixedCostConfigEntity) = database.fixedCostConfigDao().insertFixedCost(cost)
    suspend fun deleteFixedCost(id: Long) = database.fixedCostConfigDao().deleteFixedCostById(id)

    // Instructor rates
    val instructorRates: Flow<List<InstructorRateEntity>> = database.instructorRateDao().getAllInstructorRates()
    suspend fun insertInstructorRate(rate: InstructorRateEntity) = database.instructorRateDao().insertInstructorRate(rate)
    suspend fun deleteInstructorRate(name: String) = database.instructorRateDao().deleteInstructorRate(name)

    // Days off
    val instructorDaysOff: Flow<List<InstructorDayOffEntity>> = database.instructorDayOffDao().getAllDaysOff()
    suspend fun insertDayOff(dayOff: InstructorDayOffEntity) = database.instructorDayOffDao().insertDayOff(dayOff)
    suspend fun deleteDayOff(id: Long) = database.instructorDayOffDao().deleteDayOffById(id)
    suspend fun clearAllData() { database.clearAllTables() }
}
