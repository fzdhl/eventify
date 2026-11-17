package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Review
import com.example.tugasakhirpam.model.ReviewInput
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseReviewRepository(
    private val client: SupabaseClient,
) : ReviewRepository {

    private fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: error("Silakan login terlebih dahulu.")

    override suspend fun getReviews(eventId: Long): List<Review> {
        requireUserId()

        val reviews = client.from("reviews")
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<ReviewDto>()

        val profiles = client.from("profiles")
            .select()
            .decodeList<ReviewProfileDto>()
            .associateBy { it.id }

        return reviews
            .sortedByDescending { it.createdAt }
            .map { dto ->
                dto.toReview(
                    userName = profiles[dto.userId]?.fullName
                        ?.takeIf { it.isNotBlank() }
                        ?: "Pengguna Eventify"
                )
            }
    }

    override suspend fun getReview(id: Long): Review {
        requireUserId()

        val dto = client.from("reviews")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<ReviewDto>()
            ?: throw NoSuchElementException("Ulasan tidak ditemukan.")

        val profile = client.from("profiles")
            .select {
                filter {
                    eq("id", dto.userId)
                }
            }
            .decodeSingleOrNull<ReviewProfileDto>()

        return dto.toReview(
            userName = profile?.fullName
                ?.takeIf { it.isNotBlank() }
                ?: "Pengguna Eventify"
        )
    }

    override suspend fun createReview(
        eventId: Long,
        input: ReviewInput,
    ) {
        val userId = requireUserId()

        validateInput(input)

        val event = client.from("events")
            .select {
                filter {
                    eq("id", eventId)
                }
            }
            .decodeSingleOrNull<ReviewEventDto>()
            ?: throw NoSuchElementException("Event tidak ditemukan.")

        if (event.createdBy == userId) {
            error("Penyelenggara tidak bisa mengulas event sendiri.")
        }

        client.from("reviews").insert(
            ReviewInsertDto(
                eventId = eventId,
                rating = input.rating,
                comment = input.comment.trim(),
            )
        )
    }

    override suspend fun updateReview(
        id: Long,
        input: ReviewInput,
    ) {
        val userId = requireUserId()
        validateInput(input)

        val existing = client.from("reviews")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<ReviewDto>()
            ?: throw NoSuchElementException("Ulasan tidak ditemukan.")

        if (existing.userId != userId) {
            error("Kamu hanya bisa mengubah ulasanmu sendiri.")
        }

        client.from("reviews").update({
            set("rating", input.rating)
            set("comment", input.comment.trim())
        }) {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun deleteReview(id: Long) {
        val userId = requireUserId()

        val existing = client.from("reviews")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<ReviewDto>()
            ?: throw NoSuchElementException("Ulasan tidak ditemukan.")

        if (existing.userId != userId) {
            error("Kamu hanya bisa menghapus ulasanmu sendiri.")
        }

        client.from("reviews").delete {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    private fun validateInput(input: ReviewInput) {
        require(input.rating in 1..5) {
            "Rating harus 1 sampai 5 bintang."
        }

        require(input.comment.trim().length in 10..500) {
            "Ulasan harus terdiri dari 10 sampai 500 karakter."
        }
    }
}

// ============================================================
// DTO
// ============================================================

@Serializable
private data class ReviewDto(
    val id: Long,
    @SerialName("event_id") val eventId: Long,
    @SerialName("user_id") val userId: String,
    val rating: Int,
    val comment: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
private data class ReviewInsertDto(
    @SerialName("event_id") val eventId: Long,
    val rating: Int,
    val comment: String,
)

@Serializable
private data class ReviewProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
)

@Serializable
private data class ReviewEventDto(
    val id: Long,
    @SerialName("created_by") val createdBy: String,
)

private fun ReviewDto.toReview(
    userName: String,
) = Review(
    id = id,
    eventId = eventId,
    userId = userId,
    userName = userName,
    rating = rating,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
