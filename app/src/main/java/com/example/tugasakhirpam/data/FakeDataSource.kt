package com.example.tugasakhirpam.data

import com.example.tugasakhirpam.model.Event
import com.example.tugasakhirpam.model.EventCategory
import com.example.tugasakhirpam.model.Registration
import com.example.tugasakhirpam.model.RegistrationStatus
import com.example.tugasakhirpam.model.Review
import com.example.tugasakhirpam.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.OffsetDateTime

internal object FakeDataSource {

    private const val NETWORK_DELAY = 600L
    private val mutex = Mutex()

    val currentUser = User(
        id = "u-001",
        name = "Aditya Pratama",
        nim = "221051234",
        email = "aditya.pratama@univ.ac.id",
        phone = "+6281234567890",
    )

    private val today: LocalDate = LocalDate.now()
    private fun day(offset: Long) = today.plusDays(offset).toString()
    private fun createdAt(daysAgo: Long) = OffsetDateTime.now().minusDays(daysAgo).toString()

    val events = mutableListOf(
        Event(
            id = 1, title = "Future Tech Summit 2025", category = EventCategory.SEMINAR,
            organizer = "Himpunan Mahasiswa Informatika", createdBy = "u-099",
            eventDate = day(12), startTime = "09:00", endTime = "15:00",
            location = "Auditorium Utama", quota = 150,
            description = "Bergabunglah dalam seminar teknologi terbesar di kampus. Temui praktisi industri, " +
                "eksplorasi tren AI terbaru, dan perluas jaringan bersama mahasiswa dari berbagai universitas.",
            imageUrl = "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800",
        ),
        Event(
            id = 2, title = "UI/UX Design Workshop", category = EventCategory.WORKSHOP,
            organizer = "UKM Desain Kreatif", createdBy = "u-098",
            eventDate = day(17), startTime = "13:00", endTime = "16:00",
            location = "Lab Multimedia 2", quota = 40,
            description = "Workshop intensif membahas design thinking, wireframing, hingga prototyping " +
                "menggunakan Figma bersama mentor dari industri.",
            imageUrl = "https://images.unsplash.com/photo-1531482615713-2afd69097998?w=800",
        ),
        Event(
            id = 3, title = "National Business Challenge", category = EventCategory.COMPETITION,
            organizer = "BEM Fakultas Ekonomi", createdBy = "u-097",
            eventDate = day(26), startTime = "08:00", endTime = "17:00",
            location = "Student Hall", quota = 60,
            description = "Kompetisi business plan tingkat nasional. Tunjukkan ide bisnismu dan menangkan " +
                "total hadiah jutaan rupiah.",
            imageUrl = "https://images.unsplash.com/photo-1556761175-b413da4baf72?w=800",
        ),
        Event(
            id = 4, title = "Campus Innovation Week", category = EventCategory.SEMINAR,
            organizer = "Lembaga Inovasi Kampus", createdBy = "u-096",
            eventDate = day(35), startTime = "09:00", endTime = "16:00",
            location = "Gedung Serbaguna", quota = 200, isFeatured = true,
            description = "Temukan ide, teman, dan peluang baru. Pameran startup mahasiswa, talkshow, " +
                "dan sesi networking selama satu pekan penuh.",
            imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800",
        ),
        Event(
            id = 5, title = "Public Speaking Bootcamp", category = EventCategory.WORKSHOP,
            organizer = "UKM Debat", createdBy = "u-095",
            eventDate = day(8), startTime = "10:00", endTime = "12:00",
            location = "Ruang Seminar B", quota = 30,
            description = "Latih kepercayaan diri berbicara di depan umum bersama juara debat nasional.",
            imageUrl = "https://images.unsplash.com/photo-1475721027785-f74eccf877e2?w=800",
        ),
        Event(
            id = 6, title = "Open Recruitment BEM 2026", category = EventCategory.ORGANIZATION,
            organizer = "BEM Universitas", createdBy = "u-094",
            eventDate = day(20), startTime = "13:00", endTime = "15:00",
            location = "Aula Rektorat", quota = 100,
            description = "Sesi pengenalan kabinet dan seleksi pengurus BEM periode 2026.",
            imageUrl = "https://images.unsplash.com/photo-1529070538774-1843cb3265df?w=800",
        ),
        Event(
            id = 7, title = "Kelas Android Jetpack Compose", category = EventCategory.WORKSHOP,
            organizer = "Aditya Pratama", createdBy = "u-001",
            eventDate = day(14), startTime = "09:00", endTime = "12:00",
            location = "Lab Komputer 3", quota = 30,
            description = "Belajar membuat aplikasi Android modern dengan Jetpack Compose, mulai dari " +
                "layout, state, hingga navigasi.",
            imageUrl = "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800",
        ),
        Event(
            id = 8, title = "Seminar Karier & Magang", category = EventCategory.SEMINAR,
            organizer = "Pusat Karier Kampus", createdBy = "u-093",
            eventDate = day(-10), startTime = "09:00", endTime = "12:00",
            location = "Auditorium Utama", quota = 120,
            description = "Tips menyiapkan CV, wawancara, dan peluang magang di perusahaan teknologi.",
            imageUrl = "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=800",
        ),
        Event(
            id = 9, title = "Workshop Git & GitHub untuk Pemula", category = EventCategory.WORKSHOP,
            organizer = "Google Developer Student Club", createdBy = "u-092",
            eventDate = day(-20), startTime = "13:00", endTime = "16:00",
            location = "Lab Komputer 1", quota = 40,
            description = "Belajar version control dari nol: commit, branch, merge, hingga kolaborasi di GitHub.",
            imageUrl = "https://images.unsplash.com/photo-1556075798-4825dfaaf498?w=800",
        ),
    )

    private val otherParticipants = mutableMapOf(
        1L to 107, 2L to 25, 3L to 40, 4L to 80, 5L to 30, 6L to 63, 8L to 95, 9L to 35,
    )

    val registrations = mutableListOf(
        Registration(1, 1, "u-001", "Aditya Pratama", "221051234", "aditya.pratama@univ.ac.id",
            "+6281234567890", RegistrationStatus.CONFIRMED, createdAt(3)),
        Registration(2, 2, "u-001", "Aditya Pratama", "221051234", "aditya.pratama@univ.ac.id",
            "+6281234567890", RegistrationStatus.CONFIRMED, createdAt(2)),
        Registration(3, 3, "u-001", "Aditya Pratama", "221051234", "aditya.pratama@univ.ac.id",
            "+6281234567890", RegistrationStatus.CANCELLED, createdAt(1)),
        Registration(4, 8, "u-001", "Aditya Pratama", "221051234", "aditya.pratama@univ.ac.id",
            "+6281234567890", RegistrationStatus.CONFIRMED, createdAt(20)),
        Registration(5, 7, "u-101", "Rina Maharani", "221051111", "rina@univ.ac.id",
            "+6281211112222", RegistrationStatus.PENDING, createdAt(1)),
        Registration(6, 7, "u-102", "Bima Saputra", "221052222", "bima@univ.ac.id",
            "+6281233334444", RegistrationStatus.CONFIRMED, createdAt(2)),
        Registration(7, 7, "u-103", "Siti Nurhaliza", "221053333", "siti@univ.ac.id",
            "+6281255556666", RegistrationStatus.CONFIRMED, createdAt(4)),
        Registration(8, 9, "u-001", "Aditya Pratama", "221051234", "aditya.pratama@univ.ac.id",
            "+6281234567890", RegistrationStatus.CONFIRMED, createdAt(30)),
    )

    val reviews = mutableListOf(
        Review(1, 8, "u-201", "Nadia Putri", 5,
            "Materinya relevan dan pembicaranya sangat inspiratif. Wajib ikut lagi!", createdAt(2)),
        Review(2, 8, "u-202", "Rizky Ananda", 4,
            "Event terorganisir dengan baik, hanya antrean registrasinya cukup panjang.", createdAt(2)),
        Review(3, 8, "u-001", "Aditya Pratama", 5,
            "Banyak insight baru tentang persiapan karier dan magang di industri.", createdAt(2)),
        Review(4, 8, "u-203", "Dimas Prasetyo", 3,
            "Isinya bagus, tapi sesi tanya jawab terlalu singkat.", createdAt(4)),
        Review(5, 8, "u-204", "Laras Wulandari", 5,
            "Pembicaranya ramah dan memberi contoh CV yang nyata. Sangat membantu!", createdAt(5)),
        Review(6, 9, "u-205", "Fajar Nugroho", 4,
            "Penjelasannya runtut, cocok untuk yang baru pertama kali pakai Git.", createdAt(15)),
    )

    val favoriteEventIds = mutableSetOf(2L)

    var nextEventId = 10L
    var nextRegistrationId = 9L
    var nextReviewId = 7L

    fun activeCount(eventId: Long): Int =
        (otherParticipants[eventId] ?: 0) +
            registrations.count { it.eventId == eventId && it.isActive }

    fun withState(event: Event): Event {
        val eventReviews = reviews.filter { it.eventId == event.id }
        return event.copy(
            registeredCount = activeCount(event.id),
            isFavorite = event.id in favoriteEventIds,
            ratingAverage = if (eventReviews.isEmpty()) 0.0 else eventReviews.map { it.rating }.average(),
            reviewCount = eventReviews.size,
        )
    }

    fun findEvent(id: Long): Event =
        events.firstOrNull { it.id == id }?.let(::withState)
            ?: throw NoSuchElementException("Event tidak ditemukan.")

    fun withEvent(registration: Registration): Registration =
        registration.copy(event = events.firstOrNull { it.id == registration.eventId }?.let(::withState))

    fun removeEventData(eventId: Long) {
        events.removeAll { it.id == eventId }
        registrations.removeAll { it.eventId == eventId }
        reviews.removeAll { it.eventId == eventId }
        favoriteEventIds.remove(eventId)
        otherParticipants.remove(eventId)
    }

    suspend fun <T> transaction(block: () -> T): T = withContext(Dispatchers.IO) {
        mutex.withLock {
            delay(NETWORK_DELAY)
            block()
        }
    }
}
