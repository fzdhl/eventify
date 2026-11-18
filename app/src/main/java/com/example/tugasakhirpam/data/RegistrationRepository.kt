package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationInput
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.RegistrationUpdate
import java.time.OffsetDateTime

interface RegistrationRepository {
    suspend fun getMyRegistrations(): List<Registration>
    suspend fun getRegistration(id: Long): Registration
    suspend fun findMyRegistration(eventId: Long): Registration?
    suspend fun createRegistration(input: RegistrationInput)
    suspend fun updateRegistration(id: Long, input: RegistrationUpdate)
    suspend fun updateStatus(id: Long, status: RegistrationStatus)
    suspend fun deleteRegistration(id: Long)
}

class FakeRegistrationRepository : RegistrationRepository {

    private val db = FakeDataSource
    private val userId get() = db.currentUser.id

    override suspend fun getMyRegistrations(): List<Registration> = db.transaction {
        db.registrations.filter { it.userId == userId }
            .sortedByDescending { it.id }
            .map(db::withEvent)
    }

    override suspend fun getRegistration(id: Long): Registration = db.transaction {
        db.withEvent(findById(id))
    }

    override suspend fun findMyRegistration(eventId: Long): Registration? = db.transaction {
        db.registrations
            .filter { it.eventId == eventId && it.userId == userId }
            .maxByOrNull { if (it.isActive) 1 else 0 }
            ?.let(db::withEvent)
    }

    override suspend fun createRegistration(input: RegistrationInput) {
        db.transaction {
            val event = db.findEvent(input.eventId)
            when {
                event.createdBy == userId -> error("Kamu tidak bisa mendaftar ke event milikmu sendiri.")
                event.isPast -> error("Pendaftaran ditutup karena event sudah lewat.")
                db.registrations.any { it.eventId == input.eventId && it.userId == userId && it.isActive } ->
                    error("Kamu sudah terdaftar di event ini. Cek tab Registrations.")
                event.isFull -> error("Maaf, kuota event ini sudah penuh.")
            }
            val existingCancelledIndex = db.registrations.indexOfFirst {
                it.eventId == input.eventId && it.userId == userId && !it.isActive
            }
            if (existingCancelledIndex != -1) {
                val existing = db.registrations[existingCancelledIndex]
                db.registrations[existingCancelledIndex] = existing.copy(
                    fullName = input.fullName,
                    nim = input.nim,
                    email = input.email,
                    phone = input.phone,
                    status = RegistrationStatus.CONFIRMED,
                    createdAt = OffsetDateTime.now().toString(),
                )
            } else {
                db.registrations += Registration(
                    id = db.nextRegistrationId++,
                    eventId = input.eventId,
                    userId = userId,
                    fullName = input.fullName,
                    nim = input.nim,
                    email = input.email,
                    phone = input.phone,
                    status = RegistrationStatus.CONFIRMED,
                    createdAt = OffsetDateTime.now().toString(),
                )
            }
        }
    }

    override suspend fun updateRegistration(id: Long, input: RegistrationUpdate) {
        db.transaction {
            val index = indexOf(id)
            val current = db.registrations[index]
            if (current.userId != userId) error("Kamu tidak berhak mengubah pendaftaran ini.")
            db.registrations[index] = current.copy(
                fullName = input.fullName,
                email = input.email,
                phone = input.phone,
            )
        }
    }

    override suspend fun updateStatus(id: Long, status: RegistrationStatus) {
        db.transaction {
            val index = indexOf(id)
            val current = db.registrations[index]
            val event = db.findEvent(current.eventId)
            val isOwner = current.userId == userId
            val isOrganizer = event.createdBy == userId

            if (!isOwner && !isOrganizer) error("Kamu tidak berhak mengubah status ini.")
            if (isOwner && !isOrganizer && status == RegistrationStatus.PENDING) {
                error("Status Pending hanya bisa diatur oleh penyelenggara.")
            }

            val reactivating = !current.isActive && status != RegistrationStatus.CANCELLED
            if (reactivating) {
                if (event.isPast) error("Event sudah lewat, pendaftaran tidak bisa diaktifkan lagi.")
                if (event.isFull) error("Maaf, kuota event ini sudah penuh.")
            }
            db.registrations[index] = current.copy(status = status)
        }
    }

    override suspend fun deleteRegistration(id: Long) {
        db.transaction {
            val current = findById(id)
            if (current.userId != userId) error("Kamu tidak berhak menghapus pendaftaran ini.")
            db.registrations.removeAll { it.id == id }
        }
    }

    private fun indexOf(id: Long): Int {
        val index = db.registrations.indexOfFirst { it.id == id }
        if (index == -1) throw NoSuchElementException("Data pendaftaran tidak ditemukan.")
        return index
    }

    private fun findById(id: Long): Registration = db.registrations[indexOf(id)]
}
