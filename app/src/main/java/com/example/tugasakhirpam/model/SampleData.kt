package com.example.tugasakhirpam.model

import java.time.LocalDate
import java.time.OffsetDateTime

object SampleData {

    val user = User(
        id = "u-001",
        name = "Aditya Pratama",
        nim = "221051234",
        email = "aditya.pratama@univ.ac.id",
        phone = "+6281234567890",
    )

    val event = Event(
        id = 1,
        title = "Future Tech Summit 2025",
        categoryId = 1,
        categoryName = "Seminar",
        organizer = "Himpunan Mahasiswa Informatika",
        createdBy = "u-099",
        eventDate = LocalDate.now().plusDays(12).toString(),
        startTime = "09:00",
        endTime = "15:00",
        location = "Auditorium Utama",
        quota = 150,
        registeredCount = 108,
        description = "Bergabunglah dalam seminar teknologi terbesar di kampus. Temui praktisi industri, " +
            "eksplorasi tren AI terbaru, dan perluas jaringan bersama mahasiswa dari berbagai universitas.",
    )

    val events = listOf(
        event.copy(id = 4, title = "Campus Innovation Week", isFeatured = true, location = "Gedung Serbaguna"),
        event,
        event.copy(id = 2, title = "UI/UX Design Workshop", categoryId = 2, categoryName = "Workshop", location = "Lab Multimedia 2"),
        event.copy(id = 3, title = "National Business Challenge", categoryId = 3, categoryName = "Competition", location = "Student Hall"),
    )

    val registration = Registration(
        id = 1,
        eventId = event.id,
        userId = user.id,
        fullName = user.name,
        nim = user.nim,
        email = user.email,
        phone = user.phone,
        status = RegistrationStatus.CONFIRMED,
        createdAt = OffsetDateTime.now().minusDays(2).toString(),
        event = event,
    )

    val registrations = listOf(
        registration,
        registration.copy(id = 2, eventId = 2, event = events[2]),
        registration.copy(id = 3, eventId = 3, status = RegistrationStatus.CANCELLED, event = events[3]),
    )
}
