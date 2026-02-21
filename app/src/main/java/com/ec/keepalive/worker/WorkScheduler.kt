package com.ec.keepalive.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ec.keepalive.utils.KLog
import com.ec.keepalive.utils.SyncPreferences
import com.ec.keepalive.utils.toFormattedDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val WORK_NAME = "keepalive_next_reminder"

    fun scheduleNextWork(context: Context) {
        // TODO scheduler optimization
        CoroutineScope(Dispatchers.IO).launch {
            val database = (context.applicationContext as com.ec.keepalive.KeepAlive).database
            val dao = database.contactDao()
            val contacts = dao.getAllContacts()

            if(contacts.isEmpty()) {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                return@launch
            }

            val priorityContact = contacts.minByOrNull { it.dueTime } ?: return@launch

            val zoneId = ZoneId.systemDefault()
            val nowDateTime = LocalDateTime.now(zoneId)
            val todayDate = nowDateTime.toLocalDate()

            val personDueDate = Instant.ofEpochMilli(priorityContact.dueTime).atZone(zoneId).toLocalDate()

            val targetDate = if(personDueDate.isBefore(todayDate)) todayDate else personDueDate

            val (userHour, userMinute) = SyncPreferences.getRemindTime(context)
            var targetDateTime = targetDate.atTime(userHour, userMinute)

            if(targetDateTime.isBefore(nowDateTime)) {
                targetDateTime = targetDateTime.plusDays(1)
            }

            val targetMillis = targetDateTime.atZone(zoneId).toInstant().toEpochMilli()
            val nowMillis = System.currentTimeMillis()
            val delay = (targetMillis - nowMillis).coerceAtLeast(0)

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                ).build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

            KLog.d("WORK_SCHEDULER: ${targetMillis.toFormattedDate()}")
        }
    }
}