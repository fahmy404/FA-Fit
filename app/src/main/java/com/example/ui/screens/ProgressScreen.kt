package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.glassCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppState

// Vico Charts
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun ProgressScreen(state: AppState = AppState()) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("التقدم والنتائج", style = MaterialTheme.typography.displayLarge, color = OnSurface)
            Text("تابع رحلتك نحو صحة أفضل", style = MaterialTheme.typography.bodyMedium, color = Outline)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .background(SurfaceContainerHigh, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(SurfaceContainerLowest, RoundedCornerShape(50))
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("أسبوع", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text("شهر", style = MaterialTheme.typography.labelLarge, color = Outline)
                }
            }
        }

        CalorieChartCard(state)
        WeightTrendCard()
        ForecastCard()
        AchievementsSection()
    }
}

@Composable
fun CalorieChartCard(state: AppState) {
    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("السعرات الحرارية", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                Text("المتوسط: 1,850 سعرة", style = MaterialTheme.typography.bodySmall, color = Outline)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(8.dp).background(Primary, CircleShape))
                Text("الهدف: ${state.calorieGoal}", style = MaterialTheme.typography.labelSmall, color = Outline)
            }
        }
        
        // Vico Chart for Calories
        // TODO: Replace with real historical data when available
        val model = entryModelOf(0, 0, 0, 0, 0, 0, 0)
        Chart(
            chart = columnChart(),
            model = model,
            startAxis = null,
            bottomAxis = rememberBottomAxis(),
            modifier = Modifier.fillMaxWidth().height(150.dp)
        )
    }
}

@Composable
fun WeightTrendCard() {
    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text("تطور الوزن", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("72.5", style = MaterialTheme.typography.headlineLarge, color = Tertiary)
                    Text("كجم", style = MaterialTheme.typography.labelLarge, color = Outline)
                }
            }
            Box(
                modifier = Modifier
                    .background(Color.Transparent, RoundedCornerShape(50))
                    .border(1.dp, Outline.copy(alpha = 0.2f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("-1.2 كجم", style = MaterialTheme.typography.labelSmall, color = OnSurface)
            }
        }

        val model = entryModelOf(73.7, 73.5, 73.2, 72.8, 72.5)
        Chart(
            chart = lineChart(),
            model = model,
            startAxis = null,
            bottomAxis = rememberBottomAxis(),
            modifier = Modifier.fillMaxWidth().height(100.dp)
        )
    }
}

@Composable
fun ForecastCard() {
    Row(
        modifier = Modifier.fillMaxWidth().glassCard(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SecondaryContainer.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, SecondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Flag, contentDescription = null, tint = Secondary)
        }
        Column {
            Text("توقعات الوزن", style = MaterialTheme.typography.headlineSmall, color = Secondary)
            Text("ستصل لهدفك (68 كجم) في:", style = MaterialTheme.typography.bodySmall, color = OnSurface)
            Text("15 سبتمبر", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
        }
    }
}

@Composable
fun AchievementsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("الإنجازات", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AchievementCard(
                icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Tertiary, modifier = Modifier.size(32.dp)) },
                title = "أسبوع متواصل",
                desc = "3 أيام متبقية",
                isActive = false,
                modifier = Modifier.weight(1f)
            )
            AchievementCard(
                icon = { Icon(Icons.Default.Restaurant, contentDescription = null, tint = OnSurface, modifier = Modifier.size(32.dp)) },
                title = "أول وجبة",
                desc = "تم الإنجاز",
                isActive = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AchievementCard(icon: @Composable () -> Unit, title: String, desc: String, isActive: Boolean, modifier: Modifier = Modifier) {
    val borderColor = if (isActive) Primary else Outline.copy(alpha = 0.2f)
    val bgColor = if (isActive) Primary.copy(alpha = 0.05f) else Surface.copy(alpha = 0.5f)
    
    Column(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = OnSurface)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 10.sp)
        }
    }
}
