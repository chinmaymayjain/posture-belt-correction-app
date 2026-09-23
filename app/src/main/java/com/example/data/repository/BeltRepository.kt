package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.BeltApiService
import com.example.data.api.BeltSimulator
import com.example.data.history.HistorySyncLogic
import com.example.data.local.DayRecordEntity
import com.example.data.local.PostureDao
import com.example.data.local.PostureDatabase
import com.example.data.model.BeltStatus
import com.example.data.model.CommandResult
import com.example.data.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BeltRepository(
    private val context: Context,
    val apiService: BeltApiService = BeltApiService(),
    val simulator: BeltSimulator = BeltSimulator()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences = context.getSharedPreferences("posture_belt_prefs", Context.MODE_PRIVATE)
    private val database: PostureDatabase = PostureDatabase.getDatabase(context)
    val dao: PostureDao = database.postureDao()

    private val _status = MutableStateFlow(BeltStatus())
    val status: StateFlow<BeltStatus> = _status.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.CONNECTING)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _baseUrl = MutableStateFlow(prefs.getString("base_url", "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _beltName = MutableStateFlow(prefs.getString("belt_name", "PostureBelt ESP32") ?: "PostureBelt ESP32")
    val beltName: StateFlow<String> = _beltName.asStateFlow()

    private val _useSimulator = MutableStateFlow(prefs.getBoolean("use_simulator", true))
    val useSimulator: StateFlow<Boolean> = _useSimulator.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    val allDayRecords = dao.getAllDayRecords()
    val todayRecord = dao.getDayRecordFlow(HistorySyncLogic.getTodayDateString())

    private var pollingJob: Job? = null
    private var historySyncJob: Job? = null
    private var consecutiveFailures = 0

    init {
        startPolling()
        startHistorySyncTimer()
    }

    fun setBaseUrl(url: String) {
        val clean = url.trim()
        _baseUrl.value = clean
        prefs.edit().putString("base_url", clean).apply()
        consecutiveFailures = 0
    }

    fun setBeltName(name: String) {
        _beltName.value = name
        prefs.edit().putString("belt_name", name).apply()
    }

    fun setUseSimulator(enabled: Boolean) {
        _useSimulator.value = enabled
        prefs.edit().putBoolean("use_simulator", enabled).apply()
        consecutiveFailures = 0
        if (enabled) {
            _connectionState.value = ConnectionState.CONNECTED
        }
    }

    fun clearError() {
        _lastErrorMessage.value = null
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = scope.launch {
            while (true) {
                if (!_isBusy.value) {
                    pollOnce()
                }

                // Poll interval: 1 second if connected/connecting, 5 seconds if offline (Prompt 2 requirement)
                val interval = if (_connectionState.value == ConnectionState.OFFLINE) 5000L else 1000L
                delay(interval)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollOnce() {
        if (_useSimulator.value) {
            val simStatus = simulator.getStatus()
            _status.value = simStatus
            _connectionState.value = ConnectionState.CONNECTED
            consecutiveFailures = 0
            return
        }

        // Real HTTP poll to belt
        val currentUrl = _baseUrl.value
        val result = apiService.getStatus(currentUrl)

        if (result.isSuccess) {
            _status.value = result.getOrThrow()
            _connectionState.value = ConnectionState.CONNECTED
            consecutiveFailures = 0
            _lastErrorMessage.value = null
        } else {
            consecutiveFailures++
            if (consecutiveFailures >= 3) {
                _connectionState.value = ConnectionState.OFFLINE
                _lastErrorMessage.value = result.exceptionOrNull()?.message ?: "Can't reach your belt"
            }
        }
    }

    private fun startHistorySyncTimer() {
        historySyncJob?.cancel()
        historySyncJob = scope.launch {
            // Initial sync on app open
            delay(1500)
            syncHistoryNow()

            // Routine 60-second sync while app is active
            while (true) {
                delay(60_000L)
                syncHistoryNow()
            }
        }
    }

    suspend fun syncHistoryNow() {
        try {
            val currentStatus = _status.value
            HistorySyncLogic.syncHistory(dao, currentStatus)
        } catch (_: Exception) {}
    }

    suspend fun calibrate(): Result<Unit> {
        _isBusy.value = true
        return try {
            if (_useSimulator.value) {
                simulator.executeCommand("CAL")
                _status.value = _status.value.copy(calibrated = true, angle = 0f)
                Result.success(Unit)
            } else {
                val res = apiService.sendCommand(_baseUrl.value, "CAL")
                if (res.isSuccess && res.getOrThrow().ok) {
                    _status.value = _status.value.copy(calibrated = true, angle = 0f)
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(res.getOrNull()?.message ?: "Calibration failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isBusy.value = false
        }
    }

    suspend fun sendTestBuzz(): Result<CommandResult> {
        return try {
            if (_useSimulator.value) {
                val res = simulator.executeCommand("TEST")
                Result.success(res)
            } else {
                apiService.sendCommand(_baseUrl.value, "TEST")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSettingCommands(commands: List<String>): Result<Unit> {
        _isBusy.value = true
        return try {
            if (_useSimulator.value) {
                for (cmd in commands) {
                    simulator.executeCommand(cmd)
                }
                // Immediately refresh status
                _status.value = simulator.getStatus()
                Result.success(Unit)
            } else {
                val res = apiService.sendCommandsInOrder(_baseUrl.value, commands)
                if (res.isSuccess) {
                    pollOnce()
                }
                res
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isBusy.value = false
        }
    }

    suspend fun configureBeltWifi(ssid: String, pass: String): Result<Boolean> {
        return apiService.saveWifi("http://192.168.4.1", ssid, pass)
    }

    suspend fun resetBeltWifi(): Result<CommandResult> {
        return if (_useSimulator.value) {
            simulator.executeCommand("RESETWIFI")
            Result.success(CommandResult(ok = true, message = "Belt Wi-Fi reset into setup mode"))
        } else {
            apiService.sendCommand(_baseUrl.value, "RESETWIFI")
        }
    }

    fun forgetBelt() {
        prefs.edit().remove("base_url").remove("belt_name").apply()
        _baseUrl.value = ""
        _connectionState.value = ConnectionState.OFFLINE
    }

    suspend fun seedHistoryData() {
        HistorySyncLogic.seedDemoHistory(dao)
    }

    suspend fun clearHistoryData() {
        dao.clearHistory()
    }
}
