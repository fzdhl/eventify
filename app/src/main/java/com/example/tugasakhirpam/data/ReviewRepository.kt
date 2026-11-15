package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.Review
import com.example.tugasakhirpam.model.ReviewInput
import java.time.OffsetDateTime

interface ReviewRepository {
    suspend fun getReviews(eventId: Long): List<Review>
    suspend fun getReview(id: Long): Review
    suspend fun createReview(eventId: Long, input: ReviewInput)
    suspend fun updateReview(id: Long, input: ReviewInput)
    suspend fun deleteReview(id: Long)
}

class FakeReviewRepository : ReviewRepository {

    private val db = FakeDataSource
    private val user get() = db.currentUser

    override suspend fun getReviews(eventId: Long): List<Review> = db.transaction {
        db.reviews.filter { it.eventId == eventId }.sortedByDescending { it.createdAt }
    }

    override suspend fun getReview(id: Long): Review = db.transaction {
        db.reviews.firstOrNull { it.id == id } ?: throw NoSuchElementException("Ulasan tidak ditemukan.")
    }

    override suspend fun createReview(eventId: Long, input: ReviewInput) {
        db.transaction {
            val event = db.findEvent(eventId)
            val registration = db.registrations.firstOrNull { it.eventId == eventId && it.userId == user.id }
            when {
                !event.isPast -> error("Ulasan bisa ditulis setelah event selesai.")
                event.createdBy == user.id -> error("Penyelenggara tidak bisa mengulas event sendiri.")
                registration?.status != RegistrationStatus.CONFIRMED ->
                    error("Hanya peserta yang hadir (Confirmed) yang bisa memberi ulasan.")
                db.reviews.any { it.eventId == eventId && it.userId == user.id } ->
                    error("Kamu sudah mengulas event ini. Silakan edit ulasanmu.")
                input.rating !in 1..5 -> error("Rating harus 1 sampai 5 bintang.")
            }
            db.reviews += Review(
                id = db.nextReviewId++,
                eventId = eventId,
                userId = user.id,
                userName = user.name,
                rating = input.rating,
                comment = input.comment,
                createdAt = OffsetDateTime.now().toString(),
            )
        }
    }

    override suspend fun updateReview(id: Long, input: ReviewInput) {
        db.transaction {
            val index = indexOf(id)
            val current = db.reviews[index]
            if (current.userId != user.id) error("Kamu hanya bisa mengubah ulasanmu sendiri.")
            if (input.rating !in 1..5) error("Rating harus 1 sampai 5 bintang.")
            db.reviews[index] = current.copy(
                rating = input.rating,
                comment = input.comment,
                updatedAt = OffsetDateTime.now().toString(),
            )
        }
    }

    override suspend fun deleteReview(id: Long) {
        db.transaction {
            val current = db.reviews[indexOf(id)]
            if (current.userId != user.id) error("Kamu hanya bisa menghapus ulasanmu sendiri.")
            db.reviews.removeAll { it.id == id }
        }
    }

    private fun indexOf(id: Long): Int {
        val index = db.reviews.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Ulasan tidak ditemukan.")
        return index
    }
}
