package com.fktimp.news.models.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fktimp.news.models.*

@Database(
    entities = [VKWallPostModel::class, VKAttachments::class, VKSize::class, VKLink::class, VKGroupModel::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): VKDao
}