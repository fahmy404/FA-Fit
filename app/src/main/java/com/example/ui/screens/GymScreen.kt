package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppState
import com.example.ui.viewmodels.Exercise
import com.example.ui.viewmodels.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

data class WorkoutWeekDay(
    val nameAr: String,
    val dateStr: String,
    val isToday: Boolean
)

@Composable
fun GymScreen(
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    state: AppState = AppState()
) {
    var selectedDay by remember { mutableStateOf<WorkoutWeekDay?>(null) }

    if (selectedDay != null) {
        GymDayScreen(
            day = selectedDay!!,
            viewModel = viewModel,
            state = state,
            onBack = { selectedDay = null }
        )
    } else {
        GymWeekScreen(
            state = state,
            onDaySelected = { day ->
                viewModel.loadWorkoutForDate(day.dateStr)
                selectedDay = day
            }
        )
    }
}

@Composable
fun GymWeekScreen(
    state: AppState,
    onDaySelected: (WorkoutWeekDay) -> Unit
) {
    val scrollState = rememberScrollState()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())

    val arabicDays = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    // Build current week (Saturday to Friday)
    val calendar = Calendar.getInstance()
    // Go to start of current week (Saturday)
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
    val daysFromSat = (dayOfWeek - Calendar.SATURDAY + 7) % 7
    calendar.add(Calendar.DAY_OF_YEAR, -daysFromSat)

    val weekDays = (0..6).map { offset ->
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, offset)
        WorkoutWeekDay(
            nameAr = arabicDays[offset],
            dateStr = sdf.format(cal.time),
            isToday = sdf.format(cal.time) == today
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("جدول التدريب", style = MaterialTheme.typography.displayLarge, color = OnSurface)
            Text("اختر اليوم لعرض أو إضافة تمارينك", style = MaterialTheme.typography.bodyMedium, color = Outline)
        }

        // Weekly stats
        val totalWorkoutDays = state.workoutSchedule.values.count { it.isNotEmpty() }
        val totalExercises = state.workoutSchedule.values.sumOf { it.size }
        val totalSets = state.workoutSchedule.values.sumOf { exList -> exList.sumOf { it.sets.size } }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeekStatCard("$totalWorkoutDays", "أيام تدريب", Primary, Modifier.weight(1f))
            WeekStatCard("$totalExercises", "تمارين", Secondary, Modifier.weight(1f))
            WeekStatCard("$totalSets", "سِتّ", Tertiary, Modifier.weight(1f))
        }

        // Week days grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الأسبوع الحالي", style = MaterialTheme.typography.headlineSmall, color = OnSurface)

            weekDays.forEach { day ->
                val dayExercises = state.workoutSchedule[day.dateStr] ?: emptyList()
                val hasWorkout = dayExercises.isNotEmpty()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (day.isToday) Primary.copy(alpha = 0.08f)
                            else SurfaceContainerLow,
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (day.isToday) Primary.copy(alpha = 0.5f)
                            else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onDaySelected(day) }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Day icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (hasWorkout) Primary.copy(alpha = 0.15f)
                                    else SurfaceContainerHigh,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (hasWorkout) Primary.copy(alpha = 0.4f)
                                    else Color.White.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = if (hasWorkout) Primary else Outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(day.nameAr, style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                                if (day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .background(Primary.copy(alpha = 0.2f), RoundedCornerShape(50))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("اليوم", style = MaterialTheme.typography.labelSmall, color = Primary)
                                    }
                                }
                            }
                            Text(
                                if (hasWorkout) "${dayExercises.size} تمرين • ${dayExercises.sumOf { it.sets.size }} سِتّ"
                                else "لا يوجد تمارين مسجلة",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasWorkout) Secondary else Outline
                            )
                        }
                    }

                    // Completion indicator
                    if (hasWorkout) {
                        val completedSets = dayExercises.sumOf { ex -> ex.sets.count { it.isDone } }
                        val totalDaySets = dayExercises.sumOf { it.sets.size }
                        val isComplete = completedSets == totalDaySets && totalDaySets > 0
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isComplete) SuccessColor.copy(alpha = 0.2f) else SurfaceContainerHigh,
                                    CircleShape
                                )
                                .border(1.dp, if (isComplete) SuccessColor else Color.White.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isComplete) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SuccessColor, modifier = Modifier.size(18.dp))
                            } else {
                                Text("$completedSets/$totalDaySets", style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GymDayScreen(
    day: WorkoutWeekDay,
    viewModel: MainViewModel,
    state: AppState,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    val exercises = state.workoutSchedule[day.dateStr] ?: emptyList()

    // Update state.exercises to match selected day
    LaunchedEffect(day.dateStr) {
        viewModel.loadWorkoutForDate(day.dateStr)
    }

    if (showAddExerciseDialog) {
        var exerciseName by remember { mutableStateOf("") }
        var targetMuscles by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("إضافة تمرين جديد", color = OnSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("اسم التمرين") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetMuscles,
                        onValueChange = { targetMuscles = it },
                        label = { Text("العضلات المستهدفة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (exerciseName.isNotBlank()) {
                        viewModel.addExercise(exerciseName, targetMuscles)
                    }
                    showAddExerciseDialog = false
                }) { Text("إضافة") }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) { Text("إلغاء") }
            },
            containerColor = SurfaceContainerHigh
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceContainerLow)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Primary)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day.nameAr, style = MaterialTheme.typography.titleLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                Text(
                    if (day.isToday) "اليوم" else day.dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) Primary else Outline
                )
            }
            Box(modifier = Modifier.size(40.dp)) // Spacer
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Stats for this day
            val completedSets = exercises.sumOf { ex -> ex.sets.count { it.isDone } }
            val totalSets = exercises.sumOf { it.sets.size }
            val volume = exercises.sumOf { ex -> ex.sets.sumOf { s -> if (s.isDone) s.weight * s.reps else 0 } }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DayStatCard("${exercises.size}", "تمرين", Primary, Modifier.weight(1f))
                DayStatCard("$completedSets/$totalSets", "سِتّ مكتملة", Secondary, Modifier.weight(1f))
                DayStatCard("${volume / 1000.0}k", "كجم حجم", Tertiary, Modifier.weight(1f))
            }

            // Add Exercise Button
            Button(
                onClick = { showAddExerciseDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة تمرين", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // Exercise List
            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(SurfaceContainerLow, RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Outline, modifier = Modifier.size(48.dp))
                        Text("لا توجد تمارين لهذا اليوم", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        Text("اضغط على إضافة تمرين للبدء", style = MaterialTheme.typography.labelSmall, color = OutlineVariant)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    exercises.forEach { exercise ->
                        ExerciseCard(exercise = exercise, viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Exercise Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(SurfaceContainerHigh, RoundedCornerShape(8.dp)).border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text(exercise.targets, style = MaterialTheme.typography.labelSmall, color = Outline)
                }
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Outline)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(SurfaceContainerHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("إضافة مجموعة", color = OnSurface) },
                        onClick = { viewModel.addExerciseSet(exercise.id); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("حذف التمرين", color = Error) },
                        onClick = { viewModel.deleteExercise(exercise.id); expanded = false }
                    )
                }
            }
        }

        // Sets Table Header
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text("المجموعة", style = MaterialTheme.typography.labelSmall, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("الوزن (كجم)", style = MaterialTheme.typography.labelSmall, color = Outline, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            Text("التكرار", style = MaterialTheme.typography.labelSmall, color = Outline, modifier = Modifier.weight(2f), textAlign = TextAlign.Center)
            Text("تم", style = MaterialTheme.typography.labelSmall, color = Outline, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }

        // Sets
        exercise.sets.forEach { setItem ->
            var showEditSetDialog by remember { mutableStateOf(false) }

            if (showEditSetDialog) {
                var weightInput by remember { mutableStateOf(setItem.weight.toString()) }
                var repsInput by remember { mutableStateOf(setItem.reps.toString()) }

                AlertDialog(
                    onDismissRequest = { showEditSetDialog = false },
                    title = { Text("تعديل المجموعة ${setItem.setNum}", color = OnSurface) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("الوزن (كجم)") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = repsInput,
                                onValueChange = { repsInput = it },
                                label = { Text("التكرار") },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.updateExerciseSet(
                                exercise.id, setItem.setNum,
                                weightInput.toIntOrNull() ?: 0,
                                repsInput.toIntOrNull() ?: 0
                            )
                            showEditSetDialog = false
                        }) { Text("حفظ") }
                    },
                    dismissButton = { TextButton(onClick = { showEditSetDialog = false }) { Text("إلغاء") } },
                    containerColor = SurfaceContainerHigh
                )
            }

            val isCompleted = setItem.isDone
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isCompleted) SuccessColor.copy(alpha = 0.08f) else Primary.copy(alpha = 0.05f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, if (isCompleted) SuccessColor.copy(alpha = 0.3f) else Primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    setItem.setNum.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCompleted) SuccessColor else Primary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier.weight(2f).padding(horizontal = 4.dp)
                        .background(SurfaceContainer, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isCompleted) SuccessColor.copy(alpha = 0.2f) else Primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable { showEditSetDialog = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${setItem.weight}", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
                Box(
                    modifier = Modifier.weight(2f).padding(horizontal = 4.dp)
                        .background(SurfaceContainer, RoundedCornerShape(6.dp))
                        .border(1.dp, if (isCompleted) SuccessColor.copy(alpha = 0.2f) else Primary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .clickable { showEditSetDialog = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${setItem.reps}", style = MaterialTheme.typography.bodyMedium, color = OnSurface)
                }
                Box(
                    modifier = Modifier.weight(1f).clickable { viewModel.toggleExerciseSet(exercise.id, setItem.setNum) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Box(
                            modifier = Modifier.size(26.dp).background(SuccessColor, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Box(modifier = Modifier.size(26.dp).border(1.5.dp, Outline, RoundedCornerShape(6.dp)))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Add Set Button
        TextButton(
            onClick = { viewModel.addExerciseSet(exercise.id) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text("إضافة مجموعة", style = MaterialTheme.typography.labelMedium, color = Primary)
        }
    }
}

@Composable
fun WeekStatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(SurfaceContainerLow.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Outline)
    }
}

@Composable
fun DayStatCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Outline, textAlign = TextAlign.Center)
    }
}
