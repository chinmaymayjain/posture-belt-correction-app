package com.example.data.api

import com.example.data.model.BeltStatus
import com.example.data.model.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class BeltError(val friendlyMessage: String) : Exception(friendlyMessage) {
    class Timeout(msg: String = "Belt took too long to respond. Make sure it is close to your Wi-Fi.") : BeltError(msg)
    class Unreachable(msg: String = "Can't reach your belt. Make sure it's switched on and on the same Wi-Fi as your phone.") : BeltError(msg)
    class BadResponse(msg: String = "Received unexpected reply from your belt.") : BeltError(msg)
}

class BeltApiService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()
) {

    private fun normalizeBaseUrl(baseUrl: String): String {
        var trimmed = baseUrl.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        return trimmed.removeSuffix("/")
    }

    suspend fun getStatus(baseUrl: String): Result<BeltStatus> = withContext(Dispatchers.IO) {
        val root = normalizeBaseUrl(baseUrl)
        val request = Request.Builder()
            .url("$root/api/status")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(BeltError.BadResponse("Status error: ${response.code}"))
                }
                val bodyStr = response.body?.string() ?: return@withContext Result.failure(BeltError.BadResponse("Empty response"))
                val json = JSONObject(bodyStr)
                val status = BeltStatus(
                    angle = json.optDouble("angle", 0.0).toFloat(),
                    calibrated = json.optBoolean("calibrated", true),
                    alerting = json.optBoolean("alerting", false),
                    thr = json.optDouble("thr", 20.0).toFloat(),
                    hold = json.optInt("hold", 3),
                    pulses = json.optInt("pulses", 2),
                    pulse = json.optInt("pulse", 200),
                    gap = json.optInt("gap", 150),
                    cool = json.optInt("cool", 10),
                    goodSec = json.optLong("goodSec", 0L),
                    badSec = json.optLong("badSec", 0L),
                    alerts = json.optInt("alerts", 0),
                    rssi = json.optInt("rssi", -60),
                    uptimeSec = json.optLong("uptimeSec", 0L)
                )
                Result.success(status)
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(BeltError.Timeout())
        } catch (e: IOException) {
            Result.failure(BeltError.Unreachable())
        } catch (e: Exception) {
            Result.failure(BeltError.BadResponse("Failed to parse status: ${e.message}"))
        }
    }

    suspend fun sendCommand(baseUrl: String, command: String): Result<CommandResult> = withContext(Dispatchers.IO) {
        val root = normalizeBaseUrl(baseUrl)
        val encodedCmd = URLEncoder.encode(command, "UTF-8")
        val request = Request.Builder()
            .url("$root/api/cmd?c=$encodedCmd")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(BeltError.BadResponse("Command returned HTTP ${response.code}"))
                }
                val bodyStr = response.body?.string()?.trim() ?: ""
                val isUnknown = bodyStr.startsWith("Unknown", ignoreCase = true) || bodyStr.startsWith("ERR", ignoreCase = true)
                if (isUnknown) {
                    Result.success(CommandResult(ok = false, message = bodyStr))
                } else {
                    Result.success(CommandResult(ok = true, message = bodyStr.ifEmpty { "OK" }))
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(BeltError.Timeout())
        } catch (e: IOException) {
            Result.failure(BeltError.Unreachable())
        } catch (e: Exception) {
            Result.failure(BeltError.BadResponse("Error sending command: ${e.message}"))
        }
    }

    suspend fun sendCommandsInOrder(baseUrl: String, commands: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        for (cmd in commands) {
            val res = sendCommand(baseUrl, cmd)
            if (res.isFailure) {
                return@withContext Result.failure(res.exceptionOrNull() ?: BeltError.Unreachable())
            }
            val cmdRes = res.getOrNull()
            if (cmdRes?.ok == false) {
                return@withContext Result.failure(BeltError.BadResponse("Command '$cmd' failed: ${cmdRes.message}"))
            }
        }
        Result.success(Unit)
    }

    suspend fun saveWifi(hotspotBaseUrl: String, ssid: String, pass: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val root = normalizeBaseUrl(hotspotBaseUrl)
        val formBody = FormBody.Builder()
            .add("ssid", ssid)
            .add("pass", pass)
            .build()

        val request = Request.Builder()
            .url("$root/api/wifi")
            .post(formBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(BeltError.BadResponse("Wi-Fi configuration failed: ${response.code}"))
                }
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(BeltError.Timeout("Belt did not respond to Wi-Fi setup in time."))
        } catch (e: IOException) {
            Result.failure(BeltError.Unreachable("Could not reach setup Wi-Fi network at $root."))
        } catch (e: Exception) {
            Result.failure(BeltError.BadResponse(e.message ?: "Unknown setup error"))
        }
    }
}
