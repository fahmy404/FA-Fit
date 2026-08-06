package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.glassCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppState
import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.todayDateString
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    state: AppState = AppState(),
    viewModel: MainViewModel? = null,
    onProfileClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var newWaterGoal by remember { mutableStateOf(state.waterGoal.toString()) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("إعادة تعيين السعرات", color = OnSurface) },
            text = { Text("هل تريد إعادة تعيين سعرات اليوم إلى صفر؟", color = Outline) },
            confirmButton = {
                Button(
                    onClick = { viewModel?.resetDailyCalories(); showResetDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("إعادة تعيين", color = OnError) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("إلغاء", color = Primary) }
            },
            containerColor = SurfaceContainerHigh
        )
    }

    if (showWaterGoalDialog) {
        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            title = { Text("تعديل هدف المياه", color = OnSurface) },
            text = {
                OutlinedTextField(
                    value = newWaterGoal,
                    onValueChange = { newWaterGoal = it },
                    label = { Text("عدد الأكواب (250ml لكل كوب)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel?.setWaterGoal(newWaterGoal.toIntOrNull() ?: state.waterGoal)
                    showWaterGoalDialog = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showWaterGoalDialog = false }) { Text("إلغاء") } },
            containerColor = SurfaceContainerHigh
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopHeader(onProfileClick, state)
        WeeklyCalendar(state = state, onDateSelected = { date -> viewModel?.selectDate(date) })
        CalorieRingCard(state = state, onResetClick = { showResetDialog = true })
        MacrosCard(state)
        WaterTrackerCard(
            state = state,
            onAddWater = { viewModel?.addWater() },
            onRemoveWater = { viewModel?.removeWater() },
            onEditGoal = { newWaterGoal = state.waterGoal.toString(); showWaterGoalDialog = true }
        )
        AdaptiveTDEECard(state)
    }
}

@Composable
fun WeeklyCalendar(state: AppState, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayNameSdf = SimpleDateFormat("EEE", Locale("ar"))
    val dayNumSdf = SimpleDateFormat("d", Locale.getDefault())

    // Build 7 days (today - 3 days to today + 3 days)
    val days = (-3..3).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offset)
        Triple(
            sdf.format(cal.time),           // "2026-08-07"
            dayNameSdf.format(cal.time),    // "خم"
            dayNumSdf.format(cal.time)      // "7"
        )
    }

    val today = todayDateString()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("السجل اليومي", style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold)
            Text(SimpleDateFormat("MMMM yyyy", Locale("ar")).format(Date()), style = MaterialTheme.typography.labelSmall, color = Outline)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { (dateStr, dayName, dayNum) ->
                val isSelected = state.selectedDate == dateStr
                val isToday = dateStr == today

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isSelected -> Primary.copy(alpha = 0.2f)
                                isToday -> Primary.copy(alpha = 0.05f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) Primary else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onDateSelected(dateStr) }
                        .padding(vertical = 8.dp, horizontal = 2.dp)
                ) {
                    Text(
                        dayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Primary else Outline,
                        fontSize = 9.sp
                    )
                    Text(
                        dayNum,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isSelected) Primary else OnSurface,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    if (isToday) {
                        Box(modifier = Modifier.size(4.dp).background(Primary, CircleShape))
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeader(onProfileClick: () -> Unit, state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val userName = state.name.ifEmpty { user?.displayName ?: user?.email?.substringBefore("@") ?: "صديقي" }

        Row(
            modifier = Modifier.clickable { onProfileClick() },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(2.dp, Primary, CircleShape)
            ) {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .background(SurfaceContainerHigh, CircleShape)
                        .border(1.dp, Surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", fontSize = 8.sp)
                }
            }
            Column {
                Text("مرحباً $userName", style = MaterialTheme.typography.headlineSmall, color = Primary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Tertiary, modifier = Modifier.size(14.dp))
                    Text("5 أيام", style = MaterialTheme.typography.labelSmall, color = Outline)
                }
            }
        }

        IconButton(onClick = { /*TODO: Notifications*/ }) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Primary)
        }
    }
}

@Composable
fun CalorieRingCard(state: AppState, onResetClick: () -> Unit = {}) {
    val animationProgress = remember { Animatable(0f) }
    val progress = if (state.calorieGoal > 0)
        (state.caloriesConsumed.toFloat() / state.calorieGoal).coerceIn(0f, 1f)
    else 0f

    LaunchedEffect(state.caloriesConsumed) {
        animationProgress.animateTo(progress, animationSpec = tween(1000))
    }

    Box(
        modifier = Modifier.fillMaxWidth().glassCard(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.Center)
                .background(Primary.copy(alpha = 0.05f), CircleShape)
                .blur(30.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("السعرات الحرارية", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                IconButton(onClick = onResetClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين", tint = Outline, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(192.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(BrandPurple, Primary, BrandPurple)),
                        startAngle = -90f,
                        sweepAngle = 360f * animationProgress.value,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.caloriesConsumed}",
                        style = MaterialTheme.typography.displayLarge,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text("من ${state.calorieGoal} سعرة", style = MaterialTheme.typography.labelLarge, color = Outline)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .background(SurfaceContainerLow, RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val remaining = state.calorieGoal - state.caloriesConsumed
                val text = if (remaining >= 0) "$remaining باقي" else "${-remaining} زائد"
                Text(text, style = MaterialTheme.typography.labelLarge, color = if (remaining >= 0) Primary else Error)
            }
        }
    }
}

@Composable
fun MacrosCard(state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth().glassCard(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val proteinTarget = if (state.calorieGoal > 0) (state.calorieGoal * 0.3 / 4).toInt() else 1
        val carbsTarget = if (state.calorieGoal > 0) (state.calorieGoal * 0.4 / 4).toInt() else 1
        val fatTarget = if (state.calorieGoal > 0) (state.calorieGoal * 0.3 / 9).toInt() else 1

        MacroItem("بروتين", "${state.protein}/${proteinTarget}g", ProteinColor, state.protein.toFloat() / proteinTarget, Modifier.weight(1f))
        MacroItem("كارب", "${state.carbs}/${carbsTarget}g", CarbsColor, state.carbs.toFloat() / carbsTarget, Modifier.weight(1f))
        MacroItem("دهون", "${state.fat}/${fatTarget}g", FatColor, state.fat.toFloat() / fatTarget, Modifier.weight(1f))
    }
}

@Composable
fun MacroItem(name: String, value: String, color: Color, progress: Float, modifier: Modifier = Modifier) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animationProgress.animateTo(progress.coerceIn(0f, 1f), animationSpec = tween(1000))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name, style = MaterialTheme.typography.labelLarge, color = OnSurface)
            Text(value, style = MaterialTheme.typography.labelSmall, color = Outline)
        }
        Box(
            modifier = Modifier.fillMaxWidth().height(8.dp).background(SurfaceContainerHighest, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animationProgress.value)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun WaterTrackerCard(
    state: AppState,
    onAddWater: () -> Unit,
    onRemoveWater: () -> Unit,
    onEditGoal: () -> Unit
) {
    val progress = if (state.waterGoal > 0) state.waterConsumed.toFloat() / state.waterGoal else 0f
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(state.waterConsumed) {
        animProgress.animateTo(progress.coerceIn(0f, 1f), tween(800))
    }

    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = WaterColor, modifier = Modifier.size(20.dp))
                Text("المياه", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
            }
            Text(
                "تعديل الهدف",
                style = MaterialTheme.typography.labelSmall,
                color = WaterColor,
                modifier = Modifier.clickable { onEditGoal() }
            )
        }

        // Progress bar
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${state.waterConsumed} / ${state.waterGoal} كوب", style = MaterialTheme.typography.bodyMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                Text("${(state.waterConsumed * 250)} مل", style = MaterialTheme.typography.labelSmall, color = Outline)
            }
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(SurfaceContainerHighest, RoundedCornerShape(50))) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress.value)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(WaterColor.copy(alpha = 0.7f), WaterColor)),
                            RoundedCornerShape(50)
                        )
                )
            }
        }

        // Cups visual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(state.waterGoal.coerceAtMost(10)) { index ->
                val isFilled = index < state.waterConsumed
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .background(
                            if (isFilled) WaterColor.copy(alpha = 0.8f) else SurfaceContainerHighest,
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, if (isFilled) WaterColor else Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                ) {
                    if (isFilled) {
                        Text("💧", modifier = Modifier.align(Alignment.Center), fontSize = 14.sp)
                    }
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRemoveWater,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WaterColor.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = WaterColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إزالة كوب", color = WaterColor, style = MaterialTheme.typography.labelMedium)
            }
            Button(
                onClick = onAddWater,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaterColor, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("إضافة كوب", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun AdaptiveTDEECard(state: AppState) {
    if (!state.adaptiveTDEEEnabled) return
    Row(
        modifier = Modifier.fillMaxWidth().glassCard().padding(end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Tertiary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, Tertiary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("ثقة عالية", style = MaterialTheme.typography.labelSmall, color = Tertiary)
                }
                Text("حساب السعرات الذكي", style = MaterialTheme.typography.titleMedium, color = OnSurface)
            }
            Text("استناداً إلى نشاطك وبياناتك الحيوية", style = MaterialTheme.typography.bodySmall, color = Outline, maxLines = 2)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(state.calorieGoal.toString(), style = MaterialTheme.typography.headlineMedium, color = Primary, fontWeight = FontWeight.Bold)
            Text("kcal", style = MaterialTheme.typography.labelSmall, color = Outline)
        }
    }
}
