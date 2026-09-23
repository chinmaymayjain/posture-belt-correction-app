package com.example

import com.example.data.history.HistorySyncLogic
import com.example.data.local.SnapshotEntity
import com.example.data.model.BeltStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySyncLogicTest {

    @Test
    fun testFirstEverSync() {
        val status = BeltStatus(
            goodSec = 300L,
            badSec = 60L,
            alerts = 2,
            uptimeSec = 360L
        )

        val delta = HistorySyncLogic.computeDelta(lastSnapshot = null, status = status)

        assertEquals(300L, delta.deltaGoodSec)
        assertEquals(60L, delta.deltaBadSec)
        assertEquals(2, delta.deltaAlerts)
    }

    @Test
    fun testNormalDelta() {
        val lastSnapshot = SnapshotEntity(
            id = 1,
            goodSec = 500L,
            badSec = 100L,
            alerts = 3,
            uptimeSec = 600L
        )
        val status = BeltStatus(
            goodSec = 560L,
            badSec = 120L,
            alerts = 4,
            uptimeSec = 680L
        )

        val delta = HistorySyncLogic.computeDelta(lastSnapshot = lastSnapshot, status = status)

        assertEquals(60L, delta.deltaGoodSec)
        assertEquals(20L, delta.deltaBadSec)
        assertEquals(1, delta.deltaAlerts)
    }

    @Test
    fun testBeltRestart() {
        val lastSnapshot = SnapshotEntity(
            id = 1,
            goodSec = 1200L,
            badSec = 300L,
            alerts = 8,
            uptimeSec = 1500L
        )
        // Belt restarted, uptimeSec is lower (e.g. 50s)
        val status = BeltStatus(
            goodSec = 40L,
            badSec = 10L,
            alerts = 1,
            uptimeSec = 50L
        )

        val delta = HistorySyncLogic.computeDelta(lastSnapshot = lastSnapshot, status = status)

        // On reboot, delta is the new values themselves
        assertEquals(40L, delta.deltaGoodSec)
        assertEquals(10L, delta.deltaBadSec)
        assertEquals(1, delta.deltaAlerts)
    }

    @Test
    fun testNeverNegativeDelta() {
        val lastSnapshot = SnapshotEntity(
            id = 1,
            goodSec = 500L,
            badSec = 100L,
            alerts = 3,
            uptimeSec = 600L
        )
        // Same or slightly out-of-order values
        val status = BeltStatus(
            goodSec = 500L,
            badSec = 90L, // lower for some edge reason
            alerts = 3,
            uptimeSec = 605L
        )

        val delta = HistorySyncLogic.computeDelta(lastSnapshot = lastSnapshot, status = status)

        assertEquals(0L, delta.deltaGoodSec)
        assertEquals(0L, delta.deltaBadSec)
        assertEquals(0, delta.deltaAlerts)
    }
}
