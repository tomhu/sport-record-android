package com.example.sportrecord

import android.app.Application
import com.example.sportrecord.data.AppDatabase
import com.example.sportrecord.data.SportRepository

class SportRecordApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: SportRepository by lazy { SportRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SportRecordApplication
            private set
    }
}
