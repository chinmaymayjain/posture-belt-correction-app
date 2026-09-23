package com.example.data.api

import com.example.data.model.BeltStatus
import com.example.data.model.CommandResult
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

class BeltSimulator {
    var isEnabled: Boolean = true
    private var baseUptimeSec: Long = 1840L
    private var simulatedStartTime: Long = System.currentTimeMillis()

    // Configurable state
    var thr: Float = 20f
    var hold: Int = 3
    var pulses: Int = 2
    var pulse: Int = 200
    var gap: Int = 150
    var cool: Int = 10
    var calibrated: Boolean = true

    // Tracking
    private var goodSec: Long = 1920L
    private var badSec: Long = 240L
    private var alerts: Int = 5

    // Dynamic tilt simulation
    var userManualAngle: Float? = null // if user overrides in dev/demo mode
    private var overThresholdSeconds: Int = 0
    private var coolDownRemaining: Int = 0
    private var isAlerting: Boolean = false

    fun getStatus(): BeltStatus {
        val now = System.currentTimeMillis()
        val elapsedSec = ((now - simulatedStartTime) / 1000L).coerceAtLeast(0L)
        val uptime = baseUptimeSec + elapsedSec

        // Calculate dynamic angle if manual angle not set
        val angle: Float = if (userManualAngle != null) {
            userManualAngle!!
        } else {
            // Realistic breathing wave between 6° and 24°
            val t = (now / 1000.0)
            val wave = (sin(t * 0.4) * 8.0 + sin(t * 0.15) * 4.0 + 13.0).toFloat()
            // Add tiny jitter
            (wave + (Random.nextFloat() * 0.8f - 0.4f)).coerceIn(0f, 60f)
        }

        // Alert logic simulation
        if (angle > thr) {
            badSec += 1
            if (coolDownRemaining > 0) {
                coolDownRemaining--
                isAlerting = false
            } else {
                overThresholdSeconds++
                if (overThresholdSeconds >= hold) {
                    isAlerting = true
                    alerts++
                    overThresholdSeconds = 0
                    coolDownRemaining = cool
                }
            }
        } else {
            goodSec += 1
            overThresholdSeconds = 0
            if (coolDownRemaining > 0) coolDownRemaining--
            isAlerting = false
        }

        val rssi = -55 - (uptime % 10).toInt()

        return BeltStatus(
            angle = Math.round(angle * 10f) / 10f,
            calibrated = calibrated,
            alerting = isAlerting,
            thr = thr,
            hold = hold,
            pulses = pulses,
            pulse = pulse,
            gap = gap,
            cool = cool,
            goodSec = goodSec,
            badSec = badSec,
            alerts = alerts,
            rssi = rssi,
            uptimeSec = uptime
        )
    }

    suspend fun executeCommand(cmd: String): CommandResult {
        delay(120) // simulated network delay
        val upper = cmd.trim().uppercase()

        when {
            upper == "CAL" -> {
                delay(800) // CAL takes ~1 sec on belt
                calibrated = true
                userManualAngle = 0f
                return CommandResult(ok = true, message = "CAL DONE")
            }
            upper == "TEST" -> {
                return CommandResult(ok = true, message = "TEST BUZZ")
            }
            upper.startsWith("THR=") -> {
                val value = upper.removePrefix("THR=").toFloatOrNull() ?: thr
                thr = value.coerceIn(10f, 45f)
                return CommandResult(ok = true, message = "THR SET")
            }
            upper.startsWith("HOLD=") -> {
                val value = upper.removePrefix("HOLD=").toIntOrNull() ?: hold
                hold = value.coerceIn(1, 10)
                return CommandResult(ok = true, message = "HOLD SET")
            }
            upper.startsWith("PULSES=") -> {
                val value = upper.removePrefix("PULSES=").toIntOrNull() ?: pulses
                pulses = value.coerceIn(1, 5)
                return CommandResult(ok = true, message = "PULSES SET")
            }
            upper.startsWith("PULSE=") -> {
                val value = upper.removePrefix("PULSE=").toIntOrNull() ?: pulse
                pulse = value.coerceIn(100, 500)
                return CommandResult(ok = true, message = "PULSE SET")
            }
            upper.startsWith("GAP=") -> {
                val value = upper.removePrefix("GAP=").toIntOrNull() ?: gap
                gap = value.coerceIn(50, 500)
                return CommandResult(ok = true, message = "GAP SET")
            }
            upper.startsWith("COOL=") -> {
                val value = upper.removePrefix("COOL=").toIntOrNull() ?: cool
                cool = value.coerceIn(5, 60)
                return CommandResult(ok = true, message = "COOL SET")
            }
            upper == "RESETWIFI" -> {
                return CommandResult(ok = true, message = "WIFI RESET")
            }
            else -> {
                return CommandResult(ok = false, message = "Unknown command: $cmd")
            }
        }
    }
}
