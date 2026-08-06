package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.glassCard(): Modifier = composed {
    this
        .background(Color(0xFF1A1A1F).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
        .padding(20.dp)
}
