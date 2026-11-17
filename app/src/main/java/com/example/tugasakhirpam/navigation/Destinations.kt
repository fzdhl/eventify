package com.example.tugasakhirpam.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
object Categories

@Serializable
object Favorites

@Serializable
object Registrations

@Serializable
object MyEvents

// Profil (dibuka dari avatar di Home)
@Serializable
object Profile

@Serializable
object EditProfile

// Graf terpisah untuk user yang belum login (lihat AuthNavHost)
@Serializable
object Login

@Serializable
object Register

@Serializable
data class CategoryDetail(val categoryId: Long)

@Serializable
data class CategoryForm(val categoryId: Long = NO_ID)

@Serializable
data class EventList(val categoryId: Long = NO_ID)

@Serializable
data class EventDetail(val eventId: Long)

@Serializable
data class EventForm(val eventId: Long = NO_ID)

@Serializable
data class RegistrationDetail(val registrationId: Long)

@Serializable
data class RegistrationForm(
    val eventId: Long = NO_ID,
    val registrationId: Long = NO_ID,
)

@Serializable
data class Reviews(val eventId: Long)

@Serializable
data class ReviewForm(
    val eventId: Long,
    val reviewId: Long = NO_ID,
)

const val NO_ID = -1L

const val RESULT_MESSAGE_KEY = "result_message"
