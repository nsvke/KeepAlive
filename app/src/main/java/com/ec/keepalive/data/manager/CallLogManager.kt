package com.ec.keepalive.data.manager

import android.content.Context
import android.provider.CallLog
import androidx.compose.runtime.mutableStateOf
import com.ec.keepalive.utils.normalizeForComparison

class CallLogManager(private val context: Context) {
    fun getLastContactDateForSingleNumber(
        number: String,
        lookBackDays: Int,
    ) : Long? {
        val normalizedTarget = number.normalizeForComparison()
        val minDate = System.currentTimeMillis() - (lookBackDays * 24L * 60 * 60 * 1000)

        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(minDate.toString())

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while(it.moveToNext()) {
                val logNumber = it.getString(numberIndex) ?: continue
                val logType = it.getInt(typeIndex)

                val isValid = logType == CallLog.Calls.INCOMING_TYPE ||
                        logType == CallLog.Calls.OUTGOING_TYPE

                if(isValid && logNumber.normalizeForComparison() == normalizedTarget){
                    return it.getLong(dateIndex)
                }
            }
        }
        return null
    }


    fun getLastContactDates(
        targetNumbers: List<String>,
        lookBackDays: Int
        ): Map<String, Long> {
        val results = mutableMapOf<String, Long>()
        if(targetNumbers.isEmpty()) return results

        val normalizedTargets = targetNumbers.associateBy { it }

        val minDate = System.currentTimeMillis() - (lookBackDays * 24L * 60 * 60 * 1000)
        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(minDate.toString())

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE),
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            while(it.moveToNext()) {
                val logNumber = it.getString(numberIndex) ?: continue
                val normalizedLogNumber = logNumber.normalizeForComparison()

                val logType = it.getInt(typeIndex)
                val isValidType = logType == CallLog.Calls.INCOMING_TYPE ||
                        logType == CallLog.Calls.OUTGOING_TYPE
                if (normalizedTargets.containsKey(normalizedLogNumber) && isValidType) {
                    val originalNumber = normalizedTargets[normalizedLogNumber]!!
                    val logDate = it.getLong(dateIndex)

                    if(!results.containsKey(originalNumber)){
                        results[originalNumber] = logDate
                    }

                }
            }
        }
        return results
    }

}
