package com.example.tugasakhirpam.util

/** Aturan validasi form auth dan profil. Fungsi mengembalikan pesan error, atau null jika valid. */
object Validators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val PHONE_REGEX = Regex("^\\+?[0-9 ()-]{9,20}$")

    const val PASSWORD_MIN_LENGTH = 8

    fun fullName(raw: String): String? = when {
        raw.isBlank() -> "Nama lengkap wajib diisi"
        raw.trim().length < 3 -> "Nama minimal 3 karakter"
        else -> null
    }

    fun nim(raw: String): String? {
        val value = raw.trim()
        return when {
            value.isEmpty() -> "NIM wajib diisi"
            !value.all { it.isDigit() } -> "NIM hanya boleh berisi angka"
            value.length !in 8..20 -> "NIM harus 8 sampai 20 digit"
            else -> null
        }
    }

    fun email(raw: String): String? = when {
        raw.isBlank() -> "Email wajib diisi"
        !EMAIL_REGEX.matches(raw.trim()) -> "Format email tidak valid"
        else -> null
    }

    fun password(raw: String): String? = when {
        raw.isEmpty() -> "Password wajib diisi"
        raw.length < PASSWORD_MIN_LENGTH -> "Password minimal $PASSWORD_MIN_LENGTH karakter"
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Konfirmasi password wajib diisi"
        confirm != password -> "Konfirmasi password tidak sama"
        else -> null
    }

    /** Nomor HP opsional, tetapi jika diisi harus berformat wajar. */
    fun phoneOptional(raw: String): String? {
        val value = raw.trim()
        return if (value.isEmpty() || PHONE_REGEX.matches(value)) null else "Format nomor HP tidak valid"
    }
}
