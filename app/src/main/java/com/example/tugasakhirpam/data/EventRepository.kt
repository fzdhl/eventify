package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.EventInput
import com.example.tugasakhirpam.model.Registration

interface EventRepository {
    suspend fun getEvents(): List<Event>
    suspend fun getEvent(id: Long): Event
    suspend fun getMyEvents(): List<Event>
    suspend fun getFavoriteEvents(): List<Event>
    suspend fun getParticipants(eventId: Long): List<Registration>
    suspend fun createEvent(input: EventInput): Long
    suspend fun updateEvent(id: Long, input: EventInput)
    suspend fun deleteEvent(id: Long)
    suspend fun toggleFavorite(id: Long): Boolean
}

class FakeEventRepository : EventRepository {

    private val db = FakeDataSource
    private val userId get() = db.currentUser.id

    override suspend fun getEvents(): List<Event> = db.transaction {
        db.events.map(db::withState).sortedBy { it.eventDate }
    }

    override suspend fun getEvent(id: Long): Event = db.transaction { db.findEvent(id) }

    override suspend fun getMyEvents(): List<Event> = db.transaction {
        db.events.filter { it.createdBy == userId }
            .map(db::withState)
            .sortedByDescending { it.eventDate }
    }

    override suspend fun getFavoriteEvents(): List<Event> = db.transaction {
        db.events.filter { it.id in db.favoriteEventIds }
            .map(db::withState)
            .sortedBy { it.eventDate }
    }

    override suspend fun getParticipants(eventId: Long): List<Registration> = db.transaction {
        db.registrations.filter { it.eventId == eventId }.sortedBy { it.id }
    }

    override suspend fun createEvent(input: EventInput): Long = db.transaction {
        val id = db.nextEventId++
        db.events += Event(
            id = id,
            title = input.title,
            category = input.category,
            organizer = db.currentUser.name,
            createdBy = userId,
            eventDate = input.eventDate,
            startTime = input.startTime,
            endTime = input.endTime,
            location = input.location,
            quota = input.quota,
            description = input.description,
            imageUrl = input.imageUrl,
        )
        id
    }

    override suspend fun updateEvent(id: Long, input: EventInput) {
        db.transaction {
            val index = db.events.indexOfFirst { it.id == id }
            if (index == -1) throw NoSuchElementException("Event tidak ditemukan.")
            val current = db.events[index]
            if (current.createdBy != userId) error("Kamu tidak berhak mengubah event ini.")

            val active = db.activeCount(id)
            if (input.quota < active) error("Kuota tidak boleh kurang dari $active peserta terdaftar.")

            db.events[index] = current.copy(
                title = input.title,
                category = input.category,
                description = input.description,
                eventDate = input.eventDate,
                startTime = input.startTime,
                endTime = input.endTime,
                location = input.location,
                quota = input.quota,
                imageUrl = input.imageUrl,
            )
        }
    }

    override suspend fun deleteEvent(id: Long) {
        db.transaction {
            val event = db.events.firstOrNull { it.id == id } ?: throw NoSuchElementException("Event tidak ditemukan.")
            if (event.createdBy != userId) error("Kamu tidak berhak menghapus event ini.")
            db.removeEventData(id)
        }
    }

    override suspend fun toggleFavorite(id: Long): Boolean = db.transaction {
        if (db.events.none { it.id == id }) throw NoSuchElementException("Event tidak ditemukan.")
        if (!db.favoriteEventIds.add(id)) {
            db.favoriteEventIds.remove(id)
            false
        } else {
            true
        }
    }
}
