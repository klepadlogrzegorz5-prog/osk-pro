package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class ActiveSession(
    @PrimaryKey val id: Int = 1,
    val role: String, // "OWNER", "INSTRUKTOR", "KURSANT"
    val name: String,
    val codeUsed: String? = null
)
