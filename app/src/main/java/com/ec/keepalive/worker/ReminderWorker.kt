package com.ec.keepalive.worker

import android.content.Context
import androidx.compose.ui.text.toUpperCase
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ec.keepalive.data.manager.CallLogManager
import com.ec.keepalive.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ec.keepalive.R
import com.ec.keepalive.utils.SyncPreferences
import com.ec.keepalive.utils.isOverDueByDay

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
): CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContext = applicationContext

        val database = (appContext as com.ec.keepalive.KeepAlive).database
        val dao = database.contactDao()
        val callLogManager = CallLogManager(appContext)

        val allContacts = dao.getAllContacts()
        if(allContacts.isNotEmpty()) {
            val maxPeriod = allContacts.maxOfOrNull { it.periodAsDays } ?: 15
            val lookback = (maxPeriod + 7)


            val phoneNumbers = allContacts.map { it.phoneNumber }
            val lastDates = callLogManager.getLastContactDates(phoneNumbers,lookback)

            allContacts.forEach { contact ->
                val lastLogDate = lastDates[contact.phoneNumber]
                if(lastLogDate != null && lastLogDate > contact.lastSeenDate){
                    dao.updateContact(contact.copy(lastSeenDate = lastLogDate))
                }
            }
        }

        val updatedContacts = dao.getAllContacts()

        val contactsToNotify = updatedContacts.filter { contact ->
            isOverDueByDay(contact.lastSeenDate, contact.periodAsDays)
        }

        contactsToNotify.forEach { contact ->
            NotificationHelper.sendNotification(
                appContext,
                contact.name.substring(0,1).uppercase() + contact.name.substring(1),
                appContext.getString(R.string.notifiy_reminder_text),
                contact.id,
                contact.rawPhoneNumber
            )
        }

//        if(contactsToNotify.size > 1) {
//            NotificationHelper.sendNotificationSummary(appContext, contactsToNotify.size)
//        }

        WorkScheduler.scheduleNextWork(appContext)

        Result.success()
    }
}