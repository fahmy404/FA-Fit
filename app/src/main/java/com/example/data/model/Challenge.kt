package com.example.data.model

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val participants: List<String> = emptyList(),
    val endDate: Long = 0L
)
