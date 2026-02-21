package com.ec.keepalive.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)

data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val rawPhoneNumber: String,
    val periodAsDays: Int,
    val lastSeenDate: Long,
) {
    val dueTime: Long
        get() = lastSeenDate + (periodAsDays * 24 * 60 * 60 * 1000L)

}