package com.ec.keepalive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ec.keepalive.data.local.Contact
import com.ec.keepalive.data.local.ContactDao

@Database(entities = [Contact::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
