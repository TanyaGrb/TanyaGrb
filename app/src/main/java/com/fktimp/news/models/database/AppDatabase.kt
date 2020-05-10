package com.fktimp.news.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fktimp.news.models.VKAttachments
import com.fktimp.news.models.VKLink
import com.fktimp.news.models.VKSize
import com.fktimp.news.models.VKWallPostModel

@Database(entities = [ VKWallPostModel::class, VKAttachments::class, VKSize::class, VKLink::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): VKDao
}