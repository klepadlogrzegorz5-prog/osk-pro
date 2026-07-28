package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "access_codes")
data class AccessCode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,
    val role: String, // "INSTRUKTOR" or "KURSANT"
    val name: String,
    val isUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
