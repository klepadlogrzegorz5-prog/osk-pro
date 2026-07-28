package com.example.data.database

import kotlinx.coroutines.flow.Flow

class OskRepository(private val oskDao: OskDao) {
    val oskProfile: Flow<OskProfile?> = oskDao.getProfileFlow()
    val allAccessCodes: Flow<List<AccessCode>> = oskDao.getAllAccessCodesFlow()
    val activeSession: Flow<ActiveSession?> = oskDao.getActiveSessionFlow()

    suspend fun getProfileDirect(): OskProfile? = oskDao.getProfileDirect()

    suspend fun insertProfile(profile: OskProfile) = oskDao.insertProfile(profile)

    suspend fun getAccessCode(code: String): AccessCode? = oskDao.getAccessCode(code)

    suspend fun insertAccessCode(accessCode: AccessCode) = oskDao.insertAccessCode(accessCode)

    suspend fun deleteAccessCodeById(id: Int) = oskDao.deleteAccessCodeById(id)

    suspend fun getActiveSessionDirect(): ActiveSession? = oskDao.getActiveSessionDirect()

    suspend fun saveActiveSession(session: ActiveSession) = oskDao.saveActiveSession(session)

    suspend fun clearActiveSession() = oskDao.clearActiveSession()
}
