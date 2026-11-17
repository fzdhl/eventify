package com.example.tugasakhirpam.util

import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException

/**
 * Terjemahan error Supabase Auth ke pesan Bahasa Indonesia.
 * Error jaringan (IOException) ditangani oleh toUserMessage() bawaan proyek.
 */
fun Throwable.toAuthMessage(): String = when (this) {
    is AuthWeakPasswordException ->
        "Password terlalu lemah. Gunakan kombinasi huruf, angka, dan simbol."

    is AuthRestException -> when (errorCode) {
        AuthErrorCode.InvalidCredentials -> "Email atau password salah."
        AuthErrorCode.EmailNotConfirmed -> "Email belum diverifikasi. Buka tautan verifikasi di kotak masukmu."
        AuthErrorCode.UserAlreadyExists -> "Email ini sudah terdaftar. Silakan masuk."
        AuthErrorCode.OverEmailSendRateLimit -> "Terlalu banyak email terkirim. Coba lagi beberapa menit lagi."
        AuthErrorCode.SignupDisabled -> "Pendaftaran akun baru sedang dinonaktifkan."
        else -> toUserMessage()
    }

    else -> toUserMessage()
}
