package com.example.tugasakhirpam.model

import java.time.LocalDate

enum class EventCategory(val label: String) {
    SEMINAR("Seminar"),
    WORKSHOP("Workshop"),
    COMPETITION("Competition"),
    ORGANIZATION("Organization"),
}

data class Event(
    val id: Long,
    val title: String,
    val category: EventCategory,
    val organizer: String,
    val createdBy: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val quota: Int,
    val registeredCount: Int = 0,
    val description: String = "",
    val imageUrl: String? = null,
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val ratingAverage: Double = 0.0,
    val reviewCount: Int = 0,
) {
    val remainingQuota: Int get() = (quota - registeredCount).coerceAtLeast(0)
    val isFull: Boolean get() = remainingQuota == 0
    val isPast: Boolean
        get() = runCatching { LocalDate.parse(eventDate).isBefore(LocalDate.now()) }.getOrDefault(false)
}

data class EventInput(
    val title: String,
    val category: EventCategory,
    val description: String,
    val eventDate: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val quota: Int,
    val imageUrl: String?,
)
