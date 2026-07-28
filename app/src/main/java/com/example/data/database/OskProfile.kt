package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "osk_profile")
data class OskProfile(
    @PrimaryKey val id: Int = 1,
    val companyName: String,
    val nip: String,
    val phoneNumber: String,
    val email: String,
    val city: String,
    val street: String,
    val ownerName: String,
    val login: String,
    val passwordHash: String,
    val securityQuestion: String,
    val securityAnswer: String
)
