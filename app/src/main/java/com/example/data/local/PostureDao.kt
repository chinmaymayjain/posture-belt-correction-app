package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostureDao {
    @Query("SELECT * FROM day_records ORDER BY date DESC LIMIT 90")
    fun getAllDayRecords(): Flow<List<DayRecordEntity>>

    @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
    suspend fun getDayRecord(date: String): DayRecordEntity?

    @Query("SELECT * FROM day_records WHERE date = :date LIMIT 1")
    fun getDayRecordFlow(date: String): Flow<DayRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDayRecord(record: DayRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDayRecords(records: List<DayRecordEntity>)

    @Query("DELETE FROM day_records WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: String)

    @Query("SELECT * FROM snapshots WHERE id = 1 LIMIT 1")
    suspend fun getSnapshot(): SnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: SnapshotEntity)

    @Query("DELETE FROM day_records")
    suspend fun clearHistory()
}
