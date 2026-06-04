package com.example.sportrecord

import android.app.Application

class SportRecordApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { SportRepository(database) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SportRecordApplication
            private set
    }
}
