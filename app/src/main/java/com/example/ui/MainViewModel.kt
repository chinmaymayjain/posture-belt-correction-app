package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.history.HistorySyncLogic
import com.example.data.local.DayRecordEntity
import com.example.data.model.AppThemeMode
import com.example.data.model.BeltStatus
import com.example.data.model.ConnectionState
import com.example.data.model.SettingsConstants
import com.example.data.repository.BeltRepository
import com.example.data.repository.RoutineRepository
import com.example.data.repository.RoutineState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DraftSettings(
    val thr: Float = 20f,
    val hold: Int = 3,
    val pulses: Int = 2,
    val pulse: Int = 200,
    val gap: Int = 150,
    val cool: Int = 10
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val beltRepo = BeltRepository(application)
    val routineRepo = RoutineRepository(application)

    val status: StateFlow<BeltStatus> = beltRepo.status
    val connectionState: StateFlow<ConnectionState> = beltRepo.connectionState
    val isBusy: StateFlow<Boolean> = beltRepo.isBusy
    val baseUrl: StateFlow<String> = beltRepo.baseUrl
    val beltName: StateFlow<String> = beltRepo.beltName
    val useSimulator: StateFlow<Boolean> = beltRepo.useSimulator
    val routineState: StateFlow<RoutineState> = routineRepo.routineState

    val allDayRecords: StateFlow<List<DayRecordEntity>> = beltRepo.allDayRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRecord: StateFlow<DayRecordEntity?> = beltRepo.todayRecord
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val themeMode: StateFlow<AppThemeMode> = routineRepo.routineState
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), routineRepo.routineState.value.themeMode)

    // UI state
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isCalibrationOpen = MutableStateFlow(false)
    val isCalibrationOpen: StateFlow<Boolean> = _isCalibrationOpen.asStateFlow()

    private val _isOnboardingOpen = MutableStateFlow(false)
    val isOnboardingOpen: StateFlow<Boolean> = _isOnboardingOpen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // History period ("7d" or "30d")
    private val _historyPeriod = MutableStateFlow("7d")
    val historyPeriod: StateFlow<String> = _historyPeriod.asStateFlow()

    private val _selectedHistoryDayIndex = MutableStateFlow(0)
    val selectedHistoryDayIndex: StateFlow<Int> = _selectedHistoryDayIndex.asStateFlow()

    // Editable settings draft for Settings screen
    private val _draftSettings = MutableStateFlow(DraftSettings())
    val draftSettings: StateFlow<DraftSettings> = _draftSettings.asStateFlow()

    // Tracks if sliders differ from current belt status
    val hasPendingChanges: StateFlow<Boolean> = combine(status, _draftSettings) { current, draft ->
        current.thr != draft.thr ||
        current.hold != draft.hold ||
        current.pulses != draft.pulses ||
        current.pulse != draft.pulse ||
        current.gap != draft.gap ||
        current.cool != draft.cool
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val changedCount: StateFlow<Int> = combine(status, _draftSettings) { current, draft ->
        var count = 0
        if (current.thr != draft.thr) count++
        if (current.hold != draft.hold) count++
        if (current.pulses != draft.pulses) count++
        if (current.pulse != draft.pulse) count++
        if (current.gap != draft.gap) count++
        if (current.cool != draft.cool) count++
        count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        // Sync draft settings when belt status first loads
        viewModelScope.launch {
            status.collect { s ->
                if (!hasPendingChanges.value) {
                    resetDraftsToStatus(s)
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setCalibrationOpen(open: Boolean) {
        _isCalibrationOpen.value = open
    }

    fun setOnboardingOpen(open: Boolean) {
        _isOnboardingOpen.value = open
    }

    fun setHistoryPeriod(period: String) {
        _historyPeriod.value = period
        _selectedHistoryDayIndex.value = 0
    }

    fun selectHistoryDay(index: Int) {
        _selectedHistoryDayIndex.value = index
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun updateDraftThr(value: Float) {
        _draftSettings.value = _draftSettings.value.copy(thr = value)
    }

    fun updateDraftHold(value: Int) {
        _draftSettings.value = _draftSettings.value.copy(hold = value)
    }

    fun updateDraftPulses(value: Int) {
        _draftSettings.value = _draftSettings.value.copy(pulses = value)
    }

    fun updateDraftPulse(value: Int) {
        _draftSettings.value = _draftSettings.value.copy(pulse = value)
    }

    fun updateDraftGap(value: Int) {
        _draftSettings.value = _draftSettings.value.copy(gap = value)
    }

    fun updateDraftCool(value: Int) {
        _draftSettings.value = _draftSettings.value.copy(cool = value)
    }

    fun resetDraftsToStatus(s: BeltStatus = status.value) {
        _draftSettings.value = DraftSettings(
            thr = s.thr,
            hold = s.hold,
            pulses = s.pulses,
            pulse = s.pulse,
            gap = s.gap,
            cool = s.cool
        )
    }

    fun applyPreset(presetName: String) {
        val preset = SettingsConstants.PRESETS.find { it.name == presetName } ?: return
        _draftSettings.value = DraftSettings(
            thr = preset.thr,
            hold = preset.hold,
            pulses = preset.pulses,
            pulse = preset.pulse,
            gap = preset.gap,
            cool = preset.cool
        )
        triggerHaptic()
    }

    fun saveDraftSettings() {
        viewModelScope.launch {
            val s = status.value
            val draft = _draftSettings.value
            val commands = mutableListOf<String>()
            if (draft.thr != s.thr) commands.add("THR=${draft.thr.toInt()}")
            if (draft.hold != s.hold) commands.add("HOLD=${draft.hold}")
            if (draft.pulses != s.pulses) commands.add("PULSES=${draft.pulses}")
            if (draft.pulse != s.pulse) commands.add("PULSE=${draft.pulse}")
            if (draft.gap != s.gap) commands.add("GAP=${draft.gap}")
            if (draft.cool != s.cool) commands.add("COOL=${draft.cool}")

            if (commands.isNotEmpty()) {
                val res = beltRepo.saveSettingCommands(commands)
                if (res.isSuccess) {
                    showToast("Settings updated")
                    triggerHaptic()
                } else {
                    showToast(res.exceptionOrNull()?.message ?: "Failed to save settings")
                }
            }
        }
    }

    suspend fun calibrate(): Result<Unit> {
        val res = beltRepo.calibrate()
        if (res.isSuccess) {
            triggerHaptic()
        }
        return res
    }

    fun triggerTestBuzz() {
        viewModelScope.launch {
            // Vibrate phone for immediate tactile feedback
            vibratePhone()
            showToast("Buzzed")
            beltRepo.sendTestBuzz()
        }
    }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50L)
            }
        } catch (_: Exception) {}
    }

    fun vibratePhone() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200L)
            }
        } catch (_: Exception) {}
    }

    fun setDailyGoal(goal: Int) {
        routineRepo.setDailyGoal(goal)
        triggerHaptic()
    }

    fun setThemeMode(mode: AppThemeMode) {
        routineRepo.setThemeMode(mode)
        triggerHaptic()
        showToast("Theme: ${mode.title}")
    }

    fun cycleThemeMode() {
        val current = routineRepo.routineState.value.themeMode
        val next = when (current) {
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    suspend fun testBeltConnection(url: String): Result<BeltStatus> {
        return beltRepo.apiService.getStatus(url)
    }

    fun pairWithBelt(url: String, name: String = "PostureBelt") {
        beltRepo.setBaseUrl(url)
        beltRepo.setUseSimulator(false)
        completeOnboarding()
        showToast("Paired with $name")
        triggerHaptic()
    }

    fun setWearReminder(enabled: Boolean, hour: Int, minute: Int) {
        routineRepo.setWearReminder(enabled, hour, minute)
    }

    fun setEveningSummary(enabled: Boolean, hour: Int, minute: Int) {
        routineRepo.setEveningSummary(enabled, hour, minute)
    }

    fun completeOnboarding() {
        routineRepo.setCompletedOnboarding(true)
        _isOnboardingOpen.value = false
    }

    fun setBaseUrl(url: String) {
        beltRepo.setBaseUrl(url)
    }

    fun toggleSimulator(enabled: Boolean) {
        beltRepo.setUseSimulator(enabled)
        showToast(if (enabled) "Switched to Simulator mode" else "Switched to Network Belt mode")
    }

    fun resetBeltWifi() {
        viewModelScope.launch {
            beltRepo.resetBeltWifi()
            showToast("Belt Wi-Fi reset into setup mode")
        }
    }

    fun forgetBelt() {
        beltRepo.forgetBelt()
        _isOnboardingOpen.value = true
        showToast("Belt forgotten")
    }

    fun seedDemoHistory() {
        viewModelScope.launch {
            beltRepo.seedHistoryData()
            showToast("Seeded 30 days of posture history")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            beltRepo.clearHistoryData()
            showToast("History cleared")
        }
    }

    suspend fun configureBeltWifi(ssid: String, pass: String): Boolean {
        val res = beltRepo.configureBeltWifi(ssid, pass)
        return res.isSuccess && res.getOrThrow()
    }
}
