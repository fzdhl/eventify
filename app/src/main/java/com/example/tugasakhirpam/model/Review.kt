package com.example.tugasakhirpam.model

data class Review(
    val id: Long,
    val eventId: Long,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val updatedAt: String? = null,
) {
    val isEdited: Boolean get() = updatedAt != null

    val initials: String
        get() = userName.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}

data class ReviewInput(
    val rating: Int,
    val comment: String,
)
