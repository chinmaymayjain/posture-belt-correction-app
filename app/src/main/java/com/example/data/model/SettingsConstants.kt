package com.example.data.model

data class BeltSettingItem(
    val key: String,
    val label: String,
    val helperText: String,
    val unit: String,
    val min: Float,
    val max: Float,
    val step: Float
)

data class PresetConfig(
    val name: String,
    val thr: Float,
    val hold: Int,
    val pulses: Int,
    val pulse: Int,
    val gap: Int,
    val cool: Int
)

object SettingsConstants {
    val SETTINGS = listOf(
        BeltSettingItem(
            key = "THR",
            label = "Alert angle",
            helperText = "How far you can lean before the belt reminds you.",
            unit = "°",
            min = 10f,
            max = 45f,
            step = 1f
        ),
        BeltSettingItem(
            key = "HOLD",
            label = "Wait before buzzing",
            helperText = "Short leans, like picking something up, are ignored.",
            unit = "s",
            min = 1f,
            max = 10f,
            step = 1f
        ),
        BeltSettingItem(
            key = "PULSES",
            label = "Buzzes per alert",
            helperText = "How many gentle buzzes each reminder has.",
            unit = "",
            min = 1f,
            max = 5f,
            step = 1f
        ),
        BeltSettingItem(
            key = "PULSE",
            label = "Buzz length",
            helperText = "How long each buzz lasts.",
            unit = "ms",
            min = 100f,
            max = 500f,
            step = 50f
        ),
        BeltSettingItem(
            key = "GAP",
            label = "Gap between buzzes",
            helperText = "Pause between buzzes.",
            unit = "ms",
            min = 50f,
            max = 500f,
            step = 25f
        ),
        BeltSettingItem(
            key = "COOL",
            label = "Quiet time after alert",
            helperText = "Rest time before the belt can remind you again.",
            unit = "s",
            min = 5f,
            max = 60f,
            step = 5f
        )
    )

    val PRESET_GENTLE = PresetConfig(
        name = "Gentle",
        thr = 25f,
        hold = 5,
        pulses = 1,
        pulse = 150,
        gap = 200,
        cool = 15
    )

    val PRESET_NORMAL = PresetConfig(
        name = "Normal",
        thr = 20f,
        hold = 3,
        pulses = 2,
        pulse = 200,
        gap = 150,
        cool = 10
    )

    val PRESET_STRICT = PresetConfig(
        name = "Strict",
        thr = 15f,
        hold = 2,
        pulses = 3,
        pulse = 250,
        gap = 100,
        cool = 5
    )

    val PRESETS = listOf(PRESET_GENTLE, PRESET_NORMAL, PRESET_STRICT)

    fun matchesPreset(thr: Float, hold: Int, pulses: Int, pulse: Int, gap: Int, cool: Int): String? {
        for (preset in PRESETS) {
            if (preset.thr == thr &&
                preset.hold == hold &&
                preset.pulses == pulses &&
                preset.pulse == pulse &&
                preset.gap == gap &&
                preset.cool == cool
            ) {
                return preset.name
            }
        }
        return null
    }
}
