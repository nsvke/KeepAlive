package com.ec.keepalive.ui.components.add_contact

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
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
import com.ec.keepalive.utils.formatPhoneNumber
import com.ec.keepalive.utils.normalizeForComparison
import com.ec.keepalive.utils.toFormattedDate
import com.ec.keepalive.worker.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddContactViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as KeepAlive).database.contactDao()

    var selectedName by mutableStateOf("")
    var selectedPhoneNumber by mutableStateOf("")
    var rawPhoneNumber by mutableStateOf("")
    var displayPhoneNumber by mutableStateOf("")
    var periodAsDays by mutableStateOf(7)
    var isContactSelected by mutableStateOf(false)
    private var currentContactId by mutableStateOf(0)

    fun parseContactUri(uri: Uri) {
        val contentResolver = getApplication<Application>().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if(it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                if(nameIndex>= 0) selectedName = it.getString(nameIndex) ?: ""
                val numIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if(numIndex >= 0) {
                    val rawNumber = it.getString(numIndex) ?: ""
                    rawPhoneNumber = rawNumber
                    selectedPhoneNumber = rawNumber.formatPhoneNumber().normalizeForComparison()
                    displayPhoneNumber = rawNumber.formatPhoneNumber()
                    isContactSelected = true
                    currentContactId = 0

                }
            }
        }
    }

    fun loadContact(contact: Contact) {
        selectedName = contact.name
        selectedPhoneNumber = contact.phoneNumber
        rawPhoneNumber = contact.rawPhoneNumber
        displayPhoneNumber = contact.rawPhoneNumber.formatPhoneNumber() // Re-format for display
        periodAsDays = contact.periodAsDays
        currentContactId = contact.id
        isContactSelected = true
    }

    private fun clearCache() {
        selectedName = ""
        selectedPhoneNumber = ""
        rawPhoneNumber = ""
        isContactSelected = false
        displayPhoneNumber = ""
        currentContactId = 0
    }

    private val callLogManager = CallLogManager(application)
    fun saveContact(onSuccess: (Int) -> Unit) {
        if (!isContactSelected) return
        val period = if (periodAsDays > 0) periodAsDays else 7
        viewModelScope.launch(Dispatchers.IO) {

            var finalLastSeen = System.currentTimeMillis() // Default fallback
            
            if (currentContactId != 0) {
               // UPDATE MODE
               val existingContact = dao.getContactById(currentContactId)
               if(existingContact != null) {
                   finalLastSeen = existingContact.lastSeenDate
               }
            } else {
                // INSERT MODE
                val periodInMillis = period.toLong() * 24L * 60L * 60L * 1000L
                finalLastSeen = (System.currentTimeMillis() - periodInMillis)

                if(hasCallLogPermission()){
                    val lookBack = period + 7
                    val realLogDate = callLogManager.getLastContactDateForSingleNumber(
                        selectedPhoneNumber,
                        lookBack
                    )
                    if(realLogDate != null) {
                        finalLastSeen = realLogDate
                        KLog.d("${selectedName} !! NEW PERSON SYNCED: ${finalLastSeen.toFormattedDate()}")
                    } else {
                        KLog.d("${selectedName} !! NEW PERSON: NO CALL LOG ")
                    }
                }
            }

            val contactToSave = Contact(
                id = currentContactId, // 0 for insert, actual ID for update
                name = selectedName,
                phoneNumber = selectedPhoneNumber,
                rawPhoneNumber = rawPhoneNumber,
                periodAsDays = period,
                lastSeenDate = finalLastSeen
            )

            if (currentContactId != 0) {
                dao.updateContact(contactToSave)
                onSuccess(currentContactId)
            } else {
                val newIdLong = dao.insertContact(contactToSave)
                onSuccess(newIdLong.toInt())
            }
            
            clearCache()
            WorkScheduler.scheduleNextWork(getApplication())
        }
    }
    private fun hasCallLogPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

}