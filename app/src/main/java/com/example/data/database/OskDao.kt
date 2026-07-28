package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OskDao {
    @Query("SELECT * FROM osk_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<OskProfile?>

    @Query("SELECT * FROM osk_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): OskProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: OskProfile)

    @Query("SELECT * FROM access_codes ORDER BY createdAt DESC")
    fun getAllAccessCodesFlow(): Flow<List<AccessCode>>

    @Query("SELECT * FROM access_codes WHERE code = :code LIMIT 1")
    suspend fun getAccessCode(code: String): AccessCode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessCode(accessCode: AccessCode)

    @Query("DELETE FROM access_codes WHERE id = :id")
    suspend fun deleteAccessCodeById(id: Int)

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<ActiveSession?>

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSessionDirect(): ActiveSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveSession(session: ActiveSession)

    @Query("DELETE FROM active_session WHERE id = 1")
    suspend fun clearActiveSession()
}
