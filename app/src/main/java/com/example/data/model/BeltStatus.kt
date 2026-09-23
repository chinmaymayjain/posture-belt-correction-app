package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BeltStatus(
    @Json(name = "angle") val angle: Float = 0f,
    @Json(name = "calibrated") val calibrated: Boolean = true,
    @Json(name = "alerting") val alerting: Boolean = false,
    @Json(name = "thr") val thr: Float = 20f,
    @Json(name = "hold") val hold: Int = 3,
    @Json(name = "pulses") val pulses: Int = 2,
    @Json(name = "pulse") val pulse: Int = 200,
    @Json(name = "gap") val gap: Int = 150,
    @Json(name = "cool") val cool: Int = 10,
    @Json(name = "goodSec") val goodSec: Long = 0,
    @Json(name = "badSec") val badSec: Long = 0,
    @Json(name = "alerts") val alerts: Int = 0,
    @Json(name = "rssi") val rssi: Int = -60,
    @Json(name = "uptimeSec") val uptimeSec: Long = 0
)

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    OFFLINE
}

data class CommandResult(
    val ok: Boolean,
    val message: String
)
