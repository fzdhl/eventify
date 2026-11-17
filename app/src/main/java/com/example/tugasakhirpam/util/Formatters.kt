package com.example.tugasakhirpam.util

import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import io.github.jan.supabase.exceptions.RestException

object DateFormatter {
    private val locale: Locale = Locale.forLanguageTag("id-ID")
    private val fullFormat = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale)
    private val shortFormat = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM", locale)
    private val inputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale)
    private val timestampFormat = DateTimeFormatter.ofPattern("d MMM yyyy, HH.mm", locale)

    fun parseDate(raw: String?): LocalDate? =
        raw?.takeIf { it.length >= 10 }?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }

    fun parseTime(raw: String?): LocalTime? =
        raw?.takeIf { it.length >= 5 }?.let { runCatching { LocalTime.parse(it.take(5)) }.getOrNull() }

    fun full(raw: String?): String = parseDate(raw)?.format(fullFormat) ?: "-"

    fun short(raw: String?): String = parseDate(raw)?.format(shortFormat) ?: "-"

    fun dayMonth(raw: String?): String = parseDate(raw)?.format(dayMonthFormat) ?: "-"

    fun input(raw: String?): String = parseDate(raw)?.format(inputFormat) ?: ""

    fun time(raw: String?): String =
        parseTime(raw)?.let { String.format(Locale.ROOT, "%02d.%02d", it.hour, it.minute) } ?: ""

    fun timeRange(start: String?, end: String?): String {
        val s = time(start)
        val e = time(end)
        return when {
            s.isEmpty() -> "-"
            e.isEmpty() -> "$s WIB"
            else -> "$s – $e WIB"
        }
    }

    fun timestamp(raw: String?): String =
        raw?.let {
            runCatching {
                OffsetDateTime.parse(it).atZoneSameInstant(ZoneId.systemDefault()).format(timestampFormat)
            }.getOrNull()
        } ?: "-"

    fun relative(raw: String?): String {
        val time = raw?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() } ?: return "-"
        val diff = Duration.between(time, OffsetDateTime.now())
        return when {
            diff.toMinutes() < 1 -> "Baru saja"
            diff.toHours() < 1 -> "${diff.toMinutes()} menit lalu"
            diff.toDays() < 1 -> "${diff.toHours()} jam lalu"
            diff.toDays() < 30 -> "${diff.toDays()} hari lalu"
            else -> short(raw)
        }
    }

    fun rating(value: Double): String = String.format(Locale.ROOT, "%.1f", value)

    fun isUpcoming(raw: String?): Boolean =
        parseDate(raw)?.let { !it.isBefore(LocalDate.now()) } ?: true

    fun toUtcMillis(raw: String?): Long? =
        parseDate(raw)?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()

    fun fromUtcMillis(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()

    fun todayUtcMillis(): Long =
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun greeting(now: LocalTime = LocalTime.now()): String = when (now.hour) {
        in 4..10 -> "Selamat pagi,"
        in 11..14 -> "Selamat siang,"
        in 15..17 -> "Selamat sore,"
        else -> "Selamat malam,"
    }
}

inline fun <T> runSafely(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Throwable.toUserMessage(): String = when {
    this is IOException -> "Tidak dapat terhubung ke server. Periksa koneksi internetmu."
    this is RestException -> friendlyMessage()
    !message.isNullOrBlank() -> message!!.take(200)
    else -> "Terjadi kesalahan. Silakan coba lagi."
}

/**
 * RestException.message memuat URL dan seluruh header (termasuk token). Jangan pernah ditampilkan.
 * Yang dipakai hanya field error dan description dari Supabase, lalu diterjemahkan.
 */
private fun RestException.friendlyMessage(): String {
    val detail = description?.takeIf { it.isNotBlank() } ?: error
    return when {
        detail.contains("row-level security", ignoreCase = true) ->
            "Akses ditolak oleh aturan keamanan Supabase (RLS). Pastikan SQL setup sudah dijalankan."
        detail.contains("bucket not found", ignoreCase = true) ->
            "Bucket penyimpanan belum dibuat di Supabase. Jalankan SQL setup."
        detail.contains("mime type", ignoreCase = true) ->
            "Format file tidak diizinkan. Gunakan foto JPG atau PNG."
        detail.contains("maximum allowed size", ignoreCase = true) ||
                detail.contains("too large", ignoreCase = true) ->
            "Ukuran foto melebihi batas 5 MB."
        statusCode == 401 -> "Sesi berakhir. Silakan keluar lalu masuk lagi."
        else -> "Server menolak permintaan (kode $statusCode): ${detail.take(120)}"
    }
}