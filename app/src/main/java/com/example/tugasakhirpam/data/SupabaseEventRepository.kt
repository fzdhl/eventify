package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.EventInput
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseEventRepository(
    private val client: SupabaseClient,
) : EventRepository {

    private fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: error("Sesi berakhir. Silakan login kembali.")

    override suspend fun getEvents(): List<Event> {
        val userId = requireUserId()

        val events = client.from("events")
            .select()
            .decodeList<EventDto>()

        val categories = client.from("categories")
            .select()
            .decodeList<EventCategoryDto>()
            .associateBy { it.id }

        val profiles = client.from("profiles")
            .select()
            .decodeList<ProfileDto>()
            .associateBy { it.id }

        val registrations = client.from("registrations")
            .select()
            .decodeList<EventRegistrationDto>()

        val reviews = client.from("reviews")
            .select()
            .decodeList<EventReviewDto>()

        val favorites = client.from("favorites")
            .select()
            .decodeList<EventFavoriteDto>()
            .filter { it.userId == userId }
            .map { it.eventId }
            .toSet()

        val registrationCounts = registrations
            .filter { it.status.uppercase() != "CANCELLED" }
            .groupingBy { it.eventId }
            .eachCount()

        val reviewStats = reviews.groupBy { it.eventId }

        return events.map { dto ->
            val eventReviews = reviewStats[dto.id].orEmpty()

            dto.toEvent(
                categoryName = categories[dto.categoryId]?.name.orEmpty(),
                organizer = profiles[dto.createdBy]?.fullName.orEmpty(),
                registeredCount = registrationCounts[dto.id] ?: 0,
                isFavorite = dto.id in favorites,
                ratingAverage = eventReviews
                    .map { it.rating }
                    .average()
                    .takeIf { !it.isNaN() } ?: 0.0,
                reviewCount = eventReviews.size,
            )
        }.sortedBy { it.eventDate }
    }

    override suspend fun getEvent(id: Long): Event =
        getEvents().firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Event tidak ditemukan.")

    override suspend fun getMyEvents(): List<Event> {
        val userId = requireUserId()
        return getEvents()
            .filter { it.createdBy == userId }
            .sortedByDescending { it.eventDate }
    }

    override suspend fun getFavoriteEvents(): List<Event> {
        requireUserId()
        return getEvents().filter { it.isFavorite }
    }

    override suspend fun getParticipants(eventId: Long): List<Registration> {
        val userId = requireUserId()
        val event = getEvent(eventId)

        if (event.createdBy != userId) {
            error("Kamu tidak berhak melihat daftar peserta event ini.")
        }

        return client.from("registrations")
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<EventRegistrationDto>()
            .sortedBy { it.id }
            .map { it.toRegistration() }
    }

    override suspend fun createEvent(input: EventInput): Long {
        requireUserId()
        validateInput(input)

        return client.from("events")
            .insert(
                EventInsertDto(
                    title = input.title.trim(),
                    categoryId = input.categoryId,
                    eventDate = input.eventDate,
                    startTime = input.startTime,
                    endTime = input.endTime,
                    location = input.location.trim(),
                    quota = input.quota,
                    description = input.description.trim(),
                    imageUrl = input.imageUrl,
                )
            ) {
                select()
            }
            .decodeSingle<EventIdDto>()
            .id
    }

    override suspend fun updateEvent(id: Long, input: EventInput) {
        val userId = requireUserId()
        val current = getEvent(id)

        if (current.createdBy != userId) {
            error("Kamu tidak berhak mengubah event ini.")
        }

        validateInput(input)

        val activeCount = getActiveRegistrationCount(id)
        if (input.quota < activeCount) {
            error("Kuota tidak boleh kurang dari $activeCount peserta terdaftar.")
        }

        val updated = client.from("events")
            .update({
                set("title", input.title.trim())
                set("category_id", input.categoryId)
                set("event_date", input.eventDate)
                set("start_time", input.startTime)
                set("end_time", input.endTime)
                set("location", input.location.trim())
                set("quota", input.quota)
                set("description", input.description.trim())
                set("image_url", input.imageUrl)
            }) {
                filter {
                    eq("id", id)
                    eq("created_by", userId)
                }
                select()
            }
            .decodeSingleOrNull<EventIdDto>()

        if (updated == null) {
            throw NoSuchElementException("Event tidak ditemukan atau tidak dapat diubah.")
        }
    }

    override suspend fun deleteEvent(id: Long) {
        val userId = requireUserId()
        val event = getEvent(id)

        if (event.createdBy != userId) {
            error("Kamu tidak berhak menghapus event ini.")
        }

        val deleted = client.from("events")
            .delete {
                filter {
                    eq("id", id)
                    eq("created_by", userId)
                }
                select()
            }
            .decodeList<EventIdDto>()

        if (deleted.isEmpty()) {
            throw NoSuchElementException("Event tidak ditemukan atau tidak dapat dihapus.")
        }
    }

    override suspend fun toggleFavorite(id: Long): Boolean {
        val userId = requireUserId()
        getEvent(id)

        val existing = client.from("favorites")
            .select {
                filter {
                    eq("event_id", id)
                    eq("user_id", userId)
                }
            }
            .decodeList<EventFavoriteDto>()
            .firstOrNull()

        if (existing != null) {
            client.from("favorites").delete {
                filter {
                    eq("event_id", id)
                    eq("user_id", userId)
                }
            }
            return false
        }

        client.from("favorites").insert(
            EventFavoriteInsertDto(eventId = id)
        )

        return true
    }

    private suspend fun getActiveRegistrationCount(eventId: Long): Int {
        return client.from("registrations")
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<EventRegistrationDto>()
            .count { it.status.uppercase() != "CANCELLED" }
    }

    private fun validateInput(input: EventInput) {
        require(input.title.isNotBlank()) { "Judul event wajib diisi." }
        require(input.location.isNotBlank()) { "Lokasi event wajib diisi." }
        require(input.quota > 0) { "Kuota harus lebih dari nol." }
        require(input.categoryId > 0) { "Kategori event tidak valid." }
        require(input.startTime < input.endTime) {
            "Waktu selesai harus setelah waktu mulai."
        }
    }
}

// ============================================================
// DTO: Events
// ============================================================

@Serializable
private data class EventDto(
    val id: Long,
    val title: String,
    @SerialName("category_id") val categoryId: Long,
    @SerialName("created_by") val createdBy: String,
    @SerialName("event_date") val eventDate: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val location: String,
    val quota: Int,
    val description: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false,
)

@Serializable
private data class EventInsertDto(
    val title: String,
    @SerialName("category_id") val categoryId: Long,
    @SerialName("event_date") val eventDate: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("end_time") val endTime: String,
    val location: String,
    val quota: Int,
    val description: String,
    @SerialName("image_url") val imageUrl: String?,
)

@Serializable
private data class EventIdDto(val id: Long)

@Serializable
private data class EventCategoryDto(
    val id: Long,
    val name: String,
)

@Serializable
private data class EventRegistrationDto(
    val id: Long,
    @SerialName("event_id") val eventId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val nim: String,
    val email: String,
    val phone: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
) {
    fun toRegistration() = Registration(
        id = id,
        eventId = eventId,
        userId = userId,
        fullName = fullName,
        nim = nim,
        email = email,
        phone = phone,
        status = runCatching {
            RegistrationStatus.valueOf(status.uppercase())
        }.getOrDefault(RegistrationStatus.PENDING),
        createdAt = createdAt,
    )
}

@Serializable
private data class EventReviewDto(
    @SerialName("event_id") val eventId: Long,
    val rating: Int,
)

@Serializable
private data class EventFavoriteDto(
    @SerialName("event_id") val eventId: Long,
    @SerialName("user_id") val userId: String,
)

@Serializable
private data class EventFavoriteInsertDto(
    @SerialName("event_id") val eventId: Long,
)

private fun EventDto.toEvent(
    categoryName: String,
    organizer: String,
    registeredCount: Int,
    isFavorite: Boolean,
    ratingAverage: Double,
    reviewCount: Int,
) = Event(
    id = id,
    title = title,
    categoryId = categoryId,
    categoryName = categoryName,
    organizer = organizer,
    createdBy = createdBy,
    eventDate = eventDate,
    startTime = startTime,
    endTime = endTime,
    location = location,
    quota = quota,
    registeredCount = registeredCount,
    description = description,
    imageUrl = imageUrl,
    isFeatured = isFeatured,
    isFavorite = isFavorite,
    ratingAverage = ratingAverage,
    reviewCount = reviewCount,
)
