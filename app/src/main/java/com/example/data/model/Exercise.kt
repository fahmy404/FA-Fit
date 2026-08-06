package com.example.data.model

data class Exercise(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
