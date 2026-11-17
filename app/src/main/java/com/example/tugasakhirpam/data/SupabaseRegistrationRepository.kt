package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationInput
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.RegistrationUpdate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseRegistrationRepository(
    private val client: SupabaseClient,
    private val eventRepository: EventRepository,
) : RegistrationRepository {

    private fun requireUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: error("Silakan login terlebih dahulu.")

    override suspend fun getMyRegistrations(): List<Registration> {
        val userId = requireUserId()

        val rows = client.from("registrations")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<RegistrationDto>()

        return rows
            .sortedByDescending { it.id }
            .map { it.toRegistration(eventRepository.getEvent(it.eventId)) }
    }

    override suspend fun getRegistration(id: Long): Registration {
        requireUserId()

        val row = client.from("registrations")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull<RegistrationDto>()
            ?: throw NoSuchElementException("Data pendaftaran tidak ditemukan.")

        val event = eventRepository.getEvent(row.eventId)
        val userId = requireUserId()

        if (row.userId != userId && event.createdBy != userId) {
            error("Kamu tidak berhak melihat pendaftaran ini.")
        }

        return row.toRegistration(event)
    }

    override suspend fun findMyRegistration(eventId: Long): Registration? {
        val userId = requireUserId()

        val row = client.from("registrations")
            .select {
                filter {
                    eq("event_id", eventId)
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<RegistrationDto>()
            ?: return null

        return row.toRegistration(eventRepository.getEvent(eventId))
    }

    override suspend fun createRegistration(input: RegistrationInput) {
        val userId = requireUserId()
        val event = eventRepository.getEvent(input.eventId)

        when {
            event.createdBy == userId ->
                error("Kamu tidak bisa mendaftar ke event milikmu sendiri.")

            event.isPast ->
                error("Pendaftaran ditutup karena event sudah lewat.")

            findMyRegistration(input.eventId) != null ->
                error("Kamu sudah pernah mendaftar di event ini. Cek tab Registrations.")

            event.isFull ->
                error("Maaf, kuota event ini sudah penuh.")

            input.fullName.isBlank() ->
                error("Nama lengkap wajib diisi.")

            input.email.isBlank() ->
                error("Email wajib diisi.")

            input.phone.isBlank() ->
                error("Nomor HP wajib diisi.")
        }

        client.from("registrations").insert(
            RegistrationInsertDto(
                eventId = input.eventId,
                fullName = input.fullName.trim(),
                nim = input.nim.trim(),
                email = input.email.trim(),
                phone = input.phone.trim(),
            )
        )
    }

    override suspend fun updateRegistration(
        id: Long,
        input: RegistrationUpdate,
    ) {
        val userId = requireUserId()
        val current = getRegistration(id)

        if (current.userId != userId) {
            error("Kamu tidak berhak mengubah pendaftaran ini.")
        }

        if (!current.isActive) {
            error("Pendaftaran yang sudah dibatalkan tidak dapat diedit.")
        }

        require(input.fullName.isNotBlank()) {
            "Nama lengkap wajib diisi."
        }
        require(input.email.isNotBlank()) {
            "Email wajib diisi."
        }
        require(input.phone.isNotBlank()) {
            "Nomor HP wajib diisi."
        }

        client.from("registrations").update({
            set("full_name", input.fullName.trim())
            set("email", input.email.trim())
            set("phone", input.phone.trim())
            set("updated_at", "now()")
        }) {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun updateStatus(
        id: Long,
        status: RegistrationStatus,
    ) {
        val userId = requireUserId()
        val current = getRegistration(id)
        val isOwner = current.userId == userId
        val isOrganizer = current.event?.createdBy == userId

        if (!isOwner && !isOrganizer) {
            error("Kamu tidak berhak mengubah status ini.")
        }

        if (isOwner && !isOrganizer && status == RegistrationStatus.PENDING) {
            error("Status Pending hanya bisa diatur oleh penyelenggara.")
        }

        if (!current.isActive && status != RegistrationStatus.CANCELLED) {
            val event = current.event
                ?: eventRepository.getEvent(current.eventId)

            if (event.isPast) {
                error("Event sudah lewat, pendaftaran tidak bisa diaktifkan lagi.")
            }

            if (event.isFull) {
                error("Maaf, kuota event ini sudah penuh.")
            }
        }

        client.from("registrations").update({
            set("status", status.name)
            set("updated_at", "now()")
        }) {
            filter {
                eq("id", id)
            }
        }
    }

    override suspend fun deleteRegistration(id: Long) {
        val userId = requireUserId()
        val current = getRegistration(id)

        if (current.userId != userId) {
            error("Kamu tidak berhak menghapus pendaftaran ini.")
        }

        client.from("registrations").delete {
            filter {
                eq("id", id)
                eq("user_id", userId)
            }
        }
    }
}

// ============================================================
// DTO
// ============================================================

@Serializable
private data class RegistrationDto(
    val id: Long,
    @SerialName("event_id") val eventId: Long,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val nim: String,
    val email: String,
    val phone: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

@Serializable
private data class RegistrationInsertDto(
    @SerialName("event_id") val eventId: Long,
    @SerialName("full_name") val fullName: String,
    val nim: String,
    val email: String,
    val phone: String,
)

private fun RegistrationDto.toRegistration(
    event: com.example.tugasakhirpam.model.Event,
) = Registration(
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
    event = event,
)
