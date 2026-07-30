package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VehicleEntity::class,
        UserKeyEntity::class,
        LessonEntity::class,
        FinanceEntity::class,
        SickLeaveEntity::class,
        ChatMessageEntity::class,
        ReservationEntity::class,
        DocumentEntity::class,
        VehicleReportEntity::class,
        ExamResultEntity::class,
        NotificationEntity::class,
        ExamReservationEntity::class,
        CategoryPriceEntity::class,
        FixedCostConfigEntity::class,
        InstructorRateEntity::class,
        InstructorDayOffEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun userKeyDao(): UserKeyDao
    abstract fun lessonDao(): LessonDao
    abstract fun financeDao(): FinanceDao
    abstract fun sickLeaveDao(): SickLeaveDao
    abstract fun chatDao(): ChatDao
    abstract fun reservationDao(): ReservationDao
    abstract fun documentDao(): DocumentDao
    abstract fun vehicleReportDao(): VehicleReportDao
    abstract fun examResultDao(): ExamResultDao
    abstract fun notificationDao(): NotificationDao
    abstract fun examReservationDao(): ExamReservationDao
    abstract fun categoryPriceDao(): CategoryPriceDao
    abstract fun fixedCostConfigDao(): FixedCostConfigDao
    abstract fun instructorRateDao(): InstructorRateDao
    abstract fun instructorDayOffDao(): InstructorDayOffDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "osk_pro_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
