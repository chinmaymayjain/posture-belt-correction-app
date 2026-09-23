package com.example.data.history

import com.example.data.local.DayRecordEntity
import com.example.data.local.PostureDao
import com.example.data.local.SnapshotEntity
import com.example.data.model.BeltStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

data class HistoryDelta(
    val deltaGoodSec: Long,
    val deltaBadSec: Long,
    val deltaAlerts: Int
)

object HistorySyncLogic {

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun computeDelta(lastSnapshot: SnapshotEntity?, status: BeltStatus): HistoryDelta {
        if (lastSnapshot == null) {
            // First-ever sync: delta is the current belt values
            return HistoryDelta(
                deltaGoodSec = max(0L, status.goodSec),
                deltaBadSec = max(0L, status.badSec),
                deltaAlerts = max(0, status.alerts)
            )
        }

        return if (status.uptimeSec < lastSnapshot.uptimeSec) {
            // Belt restarted: delta is the new values themselves
            HistoryDelta(
                deltaGoodSec = max(0L, status.goodSec),
                deltaBadSec = max(0L, status.badSec),
                deltaAlerts = max(0, status.alerts)
            )
        } else {
            // Normal progression: new minus last (never negative)
            HistoryDelta(
                deltaGoodSec = max(0L, status.goodSec - lastSnapshot.goodSec),
                deltaBadSec = max(0L, status.badSec - lastSnapshot.badSec),
                deltaAlerts = max(0, status.alerts - lastSnapshot.alerts)
            )
        }
    }

    suspend fun syncHistory(
        dao: PostureDao,
        status: BeltStatus,
        todayDate: String = getTodayDateString()
    ): DayRecordEntity {
        val lastSnapshot = dao.getSnapshot()
        val delta = computeDelta(lastSnapshot, status)

        val existingToday = dao.getDayRecord(todayDate) ?: DayRecordEntity(
            date = todayDate,
            goodSec = 0L,
            badSec = 0L,
            alerts = 0
        )

        val updatedRecord = existingToday.copy(
            goodSec = existingToday.goodSec + delta.deltaGoodSec,
            badSec = existingToday.badSec + delta.deltaBadSec,
            alerts = existingToday.alerts + delta.deltaAlerts
        )

        dao.insertOrUpdateDayRecord(updatedRecord)

        val newSnapshot = SnapshotEntity(
            id = 1,
            goodSec = status.goodSec,
            badSec = status.badSec,
            alerts = status.alerts,
            uptimeSec = status.uptimeSec,
            lastSyncTime = System.currentTimeMillis()
        )
        dao.saveSnapshot(newSnapshot)

        // Prune older than 90 days
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -90)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cutoff = sdf.format(cal.time)
        dao.deleteOlderThan(cutoff)

        return updatedRecord
    }

    suspend fun seedDemoHistory(dao: PostureDao) {
        val records = mutableListOf<DayRecordEntity>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // Generate past 30 days
        for (i in 29 downTo 0) {
            val recordCal = Calendar.getInstance()
            recordCal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(recordCal.time)

            // Realistic wear time: 4h to 7.5h (14400s to 27000s)
            val totalSec = Random.nextLong(14400, 27000)
            // Good percentage 72% to 94%
            val goodRatio = (72 + Random.nextInt(23)) / 100.0
            val goodSec = (totalSec * goodRatio).toLong()
            val badSec = totalSec - goodSec
            val alerts = Random.nextInt(3, 16)

            records.add(
                DayRecordEntity(
                    date = dateStr,
                    goodSec = goodSec,
                    badSec = badSec,
                    alerts = alerts
                )
            )
        }

        dao.insertAllDayRecords(records)
    }
}
