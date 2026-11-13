package com.example.tugasakhirpam.model

enum class RegistrationStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
}

data class Registration(
    val id: Long,
    val eventId: Long,
    val userId: String,
    val fullName: String,
    val nim: String,
    val email: String,
    val phone: String,
    val status: RegistrationStatus = RegistrationStatus.CONFIRMED,
    val createdAt: String,
    val event: Event? = null,
) {
    val isActive: Boolean get() = status != RegistrationStatus.CANCELLED
}

data class RegistrationInput(
    val eventId: Long,
    val fullName: String,
    val nim: String,
    val email: String,
    val phone: String,
)

data class RegistrationUpdate(
    val fullName: String,
    val email: String,
    val phone: String,
)
