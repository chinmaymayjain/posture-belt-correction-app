package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DayRecordEntity::class, SnapshotEntity::class], version = 1, exportSchema = false)
abstract class PostureDatabase : RoomDatabase() {
    abstract fun postureDao(): PostureDao

    companion object {
        @Volatile
        private var INSTANCE: PostureDatabase? = null

        fun getDatabase(context: Context): PostureDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PostureDatabase::class.java,
                    "posture_belt_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
