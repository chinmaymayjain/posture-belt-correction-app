package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_records")
data class DayRecordEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val goodSec: Long,
    val badSec: Long,
    val alerts: Int
)

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey val id: Int = 1,
    val goodSec: Long,
    val badSec: Long,
    val alerts: Int,
    val uptimeSec: Long,
    val lastSyncTime: Long = System.currentTimeMillis()
)
