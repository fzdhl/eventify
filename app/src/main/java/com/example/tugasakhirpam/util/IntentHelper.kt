package com.example.tugasakhirpam.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.net.toUri
import com.example.tugasakhirpam.model.Event
import java.time.LocalDateTime
import java.time.ZoneId

object IntentHelper {

    fun openMaps(context: Context, location: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, ("geo:0,0?q=" + Uri.encode(location)).toUri())
        return launch(context, intent)
    }

    fun shareEvent(context: Context, event: Event): Boolean {
        val text = buildString {
            appendLine("Yuk ikut ${event.title}!")
            appendLine("Tanggal : ${DateFormatter.full(event.eventDate)}")
            appendLine("Waktu   : ${DateFormatter.timeRange(event.startTime, event.endTime)}")
            append("Lokasi  : ${event.location}")
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, event.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        return launch(context, Intent.createChooser(send, "Bagikan event"))
    }

    fun addToCalendar(context: Context, event: Event): Boolean {
        val date = DateFormatter.parseDate(event.eventDate) ?: return false
        val start = DateFormatter.parseTime(event.startTime) ?: return false
        val end = DateFormatter.parseTime(event.endTime) ?: start.plusHours(1)
        val zone = ZoneId.systemDefault()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, LocalDateTime.of(date, start).atZone(zone).toInstant().toEpochMilli())
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, LocalDateTime.of(date, end).atZone(zone).toInstant().toEpochMilli())
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.Events.DESCRIPTION, event.description)
        }
        return launch(context, intent)
    }

    fun dialPhone(context: Context, phone: String): Boolean {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = "tel:$phone".toUri()
        }
        return launch(context, intent)
    }

    fun sendEmail(context: Context, email: String, subject: String): Boolean {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:$email".toUri()
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        val chooser = Intent.createChooser(intent, "Pilih aplikasi email")
        return launch(context, chooser)
    }

    private fun launch(context: Context, intent: Intent): Boolean =
        try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
}
