package com.fktimp.news.activities

import android.app.Application
import androidx.room.Room
import com.fktimp.news.models.database.AppDatabase
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler


lateinit var instance: VKApplication
var database: AppDatabase? = null

class VKApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        VK.addTokenExpiredHandler(tokenTracker)
        database = Room.databaseBuilder(this, AppDatabase::class.java, "database")
            .build()
    }

    private val tokenTracker = object : VKTokenExpiredHandler {
        override fun onTokenExpired() {
            WelcomeActivity.startFrom(this@VKApplication)
        }
    }

    fun getDb(): AppDatabase = database!!
}

object VKState {
    var isVKExist = true
}