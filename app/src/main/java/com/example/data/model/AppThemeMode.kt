package com.example.data.model

enum class AppThemeMode(val title: String, val subtitle: String) {
    SYSTEM(
        title = "Auto (System)",
        subtitle = "Matches your Android device's system theme automatically"
    ),
    LIGHT(
        title = "Light Mode",
        subtitle = "Crisp, bright, high-contrast interface"
    ),
    DARK(
        title = "Dark Mode",
        subtitle = "Sleek, eye-friendly dark interface with neon accents"
    )
}
