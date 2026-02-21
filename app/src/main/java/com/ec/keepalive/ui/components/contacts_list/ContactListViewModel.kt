package com.ec.keepalive.ui.components.contacts_list

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ec.keepalive.KeepAlive
import com.ec.keepalive.data.local.Contact
import com.ec.keepalive.data.manager.CallLogManager
import com.ec.keepalive.utils.KLog
import com.ec.keepalive.utils.toFormattedDate
import com.ec.keepalive.worker.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactListViewModel(application: Application) : AndroidViewModel(application)  {
    private val dao = (application as KeepAlive).database.contactDao()
    private val CallLogManager = CallLogManager(application)

    // val contactListFlow = dao.getAllContactsSorted()

    val contactListState: StateFlow<List<Contact>?> = dao.getAllContactsSorted()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            dao.deleteContact(contact)
            WorkScheduler.scheduleNextWork(getApplication())
        }
    }
    var recentlyAddedId by mutableStateOf<Int?>(null)

    var isSplashReady = mutableStateOf(false)
    var wasSyncedInSplash = false

    fun onContactAdded(id: Int) {
        recentlyAddedId = id
    }
    fun clearHighlight() {
        recentlyAddedId = null
    }

    fun syncWithCallLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            val allContacts = dao.getAllContacts()
            KLog.d("ALLCONTACTS: ${allContacts.size}")
            if(allContacts.isEmpty()) return@launch

            val maxPeriodInList = allContacts.maxOfOrNull { it.periodAsDays } ?: 15
            KLog.d("MAXPERIOD: $maxPeriodInList")
            val lookBackDays = (maxPeriodInList + 7) //

            val phones = allContacts.map { it.phoneNumber }

            val lastDatesMap = CallLogManager.getLastContactDates(phones, lookBackDays)

            allContacts.forEach { contact ->
                val lastLogDate = lastDatesMap[contact.phoneNumber]
                KLog.d("${contact.name} !! LATEST: ${lastLogDate?.toFormattedDate()}")
                if(lastLogDate != null && lastLogDate > contact.lastSeenDate) {
                    KLog.d("${contact.name} !! ${lastLogDate?.toFormattedDate()} <---- ${contact.lastSeenDate?.toFormattedDate()}")
                    val updatedContact = contact.copy(lastSeenDate = lastLogDate)
                    dao.updateContact(updatedContact)
                }
            }

            WorkScheduler.scheduleNextWork(getApplication())
        }
    }

    fun checkAndSync(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        if(hasPermission) {
            viewModelScope.launch(Dispatchers.IO) {
                //delay(5000)
                syncWithCallLogs()
                wasSyncedInSplash = true
                isSplashReady.value = true
            }
        } else {
            isSplashReady.value = true
        }
    }

    fun updateLastSeenToNow(contact: Contact) {
        viewModelScope.launch {
            val updatedContact = contact.copy(
                lastSeenDate = System.currentTimeMillis()
            )
            dao.updateContact(updatedContact)
            WorkScheduler.scheduleNextWork(getApplication())
        }
    }

}