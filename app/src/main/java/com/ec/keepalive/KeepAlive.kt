package com.ec.keepalive

import android.app.Application
import androidx.room.Room
import com.ec.keepalive.data.local.AppDatabase

class KeepAlive: Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "db-keepalive"
        ).fallbackToDestructiveMigration()
        .build()
    }
}