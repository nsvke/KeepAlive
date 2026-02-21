package com.ec.keepalive.utils

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

object KLog {
    private const val TAG = "KeepAliveDev"
    fun d(message: String)
    {
        // Log.d(TAG, message)
    }
    fun e(message: String, error: Throwable? = null) {
        // Log.e(TAG, message, error)
    }
}



fun String.normalizeForComparison(): String {
    val digits = this.filter { it.isDigit() }
    return if (digits.length> 0) {
        digits.takeLast(10)
    } else {
        digits
    }
}

fun String.formatPhoneNumber(): String {
    var digits = this.filter { it.isDigit() }

    if (digits.startsWith("90") && digits.length == 12) {
        digits = digits.substring(2)
    } else if (digits.startsWith("0") && digits.length == 11) {
        digits = digits.substring(1)
    }
    return if (digits.length == 10) {
        "0${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6, 8)} ${digits.substring(8, 10)}"
    } else {
        this
    }
}

fun Long.toFormattedDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd.MM.yyyy: HH.mm", Locale.getDefault())
    return format.format(date)
}

fun plannedAsLong(lastseen: Long, periodDays: Int): Long {
    return lastseen + (periodDays * 86400000)

}

fun calculateNextDueMillis(lastSeen: Long, periodDays: Int): Long {
    val zoneId = ZoneId.systemDefault()

    val lastSeenDate = Instant.ofEpochMilli(lastSeen)
        .atZone(zoneId)
        .toLocalDate()

    val dueDate = lastSeenDate.plusDays(periodDays.toLong())

    val dueDateTime = LocalDateTime.of(dueDate, LocalTime.of(18,0))

    return dueDateTime.atZone(zoneId).toInstant().toEpochMilli()
}

fun isOverDueByDay(lastSeen: Long, periodDays: Int): Boolean {
    var zoneId = ZoneId.systemDefault()
    val lastSeenDate = Instant.ofEpochMilli(lastSeen)
        .atZone(zoneId)
        .toLocalDate()

    val dueDate = lastSeenDate.plusDays(periodDays.toLong())
    val today = LocalDate.now(zoneId)

    return !dueDate.isAfter(today)
}

fun getRequiredPermission(): Array<String> {
    val permissions = mutableListOf(
        Manifest.permission.READ_CALL_LOG
    )
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    return permissions.toTypedArray()
}

object SyncPreferences {
    private const val PREF_NAME = "keepalive_prefs"
    private const val KEY_LAST_SYNC = "last_sync_timestamp"

    private const val KEY_REMIND_HOUR = "reminder_hour"
    private const val KEY_REMIND_MINUTE = "reminder_minute"

    fun getRemindTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_REMIND_HOUR, 17)
        val minute = prefs.getInt(KEY_REMIND_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setRemindTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REMIND_HOUR, hour)
            .putInt(KEY_REMIND_MINUTE, minute)
            .apply()
    }

    fun getLastSyncTime(context: Context) : Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_SYNC, 0)
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
}

