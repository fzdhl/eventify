package com.example.tugasakhirpam.model

data class User(
    val id: String,
    val name: String,
    val nim: String,
    val email: String,
    val phone: String,
    val studyProgram: String = "",
    val avatarUrl: String? = null,
    /** Waktu ISO-8601 terakhir profil diubah (dari tabel profiles). */
    val updatedAt: String? = null,
) {
    val firstName: String get() = name.substringBefore(" ")

    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}
