package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RoutineState(
    val dailyGoal: Int = 80,
    val wearReminderEnabled: Boolean = true,
    val wearReminderHour: Int = 9,
    val wearReminderMinute: Int = 0,
    val eveningSummaryEnabled: Boolean = true,
    val eveningSummaryHour: Int = 20,
    val eveningSummaryMinute: Int = 0,
    val hasCompletedOnboarding: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class RoutineRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("posture_routine_prefs", Context.MODE_PRIVATE)

    private fun loadThemeMode(): AppThemeMode {
        val raw = prefs.getString("theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(raw)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    private val _routineState = MutableStateFlow(
        RoutineState(
            dailyGoal = prefs.getInt("daily_goal", 80),
            wearReminderEnabled = prefs.getBoolean("wear_reminder_enabled", true),
            wearReminderHour = prefs.getInt("wear_reminder_hour", 9),
            wearReminderMinute = prefs.getInt("wear_reminder_minute", 0),
            eveningSummaryEnabled = prefs.getBoolean("evening_summary_enabled", true),
            eveningSummaryHour = prefs.getInt("evening_summary_hour", 20),
            eveningSummaryMinute = prefs.getInt("evening_summary_minute", 0),
            hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false),
            themeMode = loadThemeMode()
        )
    )
    val routineState: StateFlow<RoutineState> = _routineState.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _routineState.value = _routineState.value.copy(themeMode = mode)
    }

    fun setDailyGoal(goal: Int) {
        prefs.edit().putInt("daily_goal", goal).apply()
        _routineState.value = _routineState.value.copy(dailyGoal = goal)
    }

    fun setWearReminder(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean("wear_reminder_enabled", enabled)
            .putInt("wear_reminder_hour", hour)
            .putInt("wear_reminder_minute", minute)
            .apply()
        _routineState.value = _routineState.value.copy(
            wearReminderEnabled = enabled,
            wearReminderHour = hour,
            wearReminderMinute = minute
        )
    }

    fun setEveningSummary(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean("evening_summary_enabled", enabled)
            .putInt("evening_summary_hour", hour)
            .putInt("evening_summary_minute", minute)
            .apply()
        _routineState.value = _routineState.value.copy(
            eveningSummaryEnabled = enabled,
            eveningSummaryHour = hour,
            eveningSummaryMinute = minute
        )
    }

    fun setCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).apply()
        _routineState.value = _routineState.value.copy(hasCompletedOnboarding = completed)
    }
}
