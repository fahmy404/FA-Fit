package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppState
import com.example.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(onSetupComplete: () -> Unit, userSetupViewModel: com.example.ui.viewmodels.UserSetupViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val scrollState = rememberScrollState()
    
    val setupState by userSetupViewModel.state.collectAsState()
    
    var name by remember(setupState.name) { mutableStateOf(setupState.name) }
    var gender by remember { mutableStateOf("ذكر") }
    var age by remember { mutableStateOf("25") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("70") }
    var goal by remember { mutableStateOf("خسارة وزن") }
    var activityLevel by remember { mutableStateOf("نشاط معتدل") }

    val calculateTDEE = {
        val w = weight.toDoubleOrNull() ?: 70.0
        val h = height.toDoubleOrNull() ?: 175.0
        val a = age.toDoubleOrNull() ?: 25.0
        
        // Mifflin-St Jeor Equation
        var bmr = (10 * w) + (6.25 * h) - (5 * a)
        bmr += if (gender == "ذكر") 5 else -161
        
        val activityMultiplier = when (activityLevel) {
            "قليل الحركة" -> 1.2
            "نشاط خفيف" -> 1.375
            "نشاط معتدل" -> 1.55
            "نشاط عالٍ / نشاط شديد" -> 1.725
            else -> 1.55
        }
        
        var tdee = bmr * activityMultiplier
        
        when (goal) {
            "خسارة وزن" -> tdee -= 500
            "تنشيف" -> tdee -= 300
            "زيادة عضل / تضخيم" -> tdee += 300
            // ثبات -> no change
        }
        
        tdee.toInt()
    }
    
    val estimatedCalories = calculateTDEE()

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHigh.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("احتياجك اليومي التقديري:", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(estimatedCalories.toString(), style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Bold)
                            Text("سعرة", style = MaterialTheme.typography.labelSmall, color = Primary)
                        }
                    }
                    Button(
                        onClick = {
                            val profile = UserProfile(
                                name = name.ifBlank { "مستخدم" },
                                gender = gender,
                                age = age.toIntOrNull() ?: 25,
                                height = height.toIntOrNull() ?: 175,
                                weight = weight.toIntOrNull() ?: 70,
                                goal = goal,
                                activityLevel = activityLevel,
                                calorieGoal = estimatedCalories
                            )
                            userSetupViewModel.saveProfile(profile) {
                                onSetupComplete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("حفظ والمتابعة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("إعداد ملفك الشخصي", style = MaterialTheme.typography.headlineLarge, color = Primary, fontWeight = FontWeight.Bold)
                Text("دعنا نتعرف عليك لتقديم خطة غذائية تناسب أهدافك.", style = MaterialTheme.typography.bodyLarge, color = Outline)
            }
            
            // Name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("الاسم", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("أدخل اسمك") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color(0xFF08080A),
                        unfocusedContainerColor = Color(0xFF08080A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            
            // Gender
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("الجنس", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenderButton("ذكر", gender == "ذكر", Icons.Default.Male, Modifier.weight(1f)) { gender = "ذكر" }
                    GenderButton("أنثى", gender == "أنثى", Icons.Default.Female, Modifier.weight(1f)) { gender = "أنثى" }
                }
            }
            
            // Metrics (Age, Height, Weight)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricInput("العمر", age, "سنة", Modifier.weight(1f)) { age = it }
                MetricInput("الطول", height, "سم", Modifier.weight(1f)) { height = it }
                MetricInput("الوزن", weight, "كجم", Modifier.weight(1f)) { weight = it }
            }
            
            // Goal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("الهدف", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                GoalOption("خسارة وزن", Icons.Default.TrendingDown, goal == "خسارة وزن") { goal = "خسارة وزن" }
                GoalOption("تنشيف", Icons.Default.FitnessCenter, goal == "تنشيف") { goal = "تنشيف" }
                GoalOption("ثبات", Icons.Default.HorizontalRule, goal == "ثبات") { goal = "ثبات" }
                GoalOption("زيادة عضل / تضخيم", Icons.Default.TrendingUp, goal == "زيادة عضل / تضخيم") { goal = "زيادة عضل / تضخيم" }
            }
            
            // Activity Level
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("مستوى النشاط", style = MaterialTheme.typography.titleMedium, color = OnSurface)
                ActivityOption("قليل الحركة", activityLevel == "قليل الحركة") { activityLevel = "قليل الحركة" }
                ActivityOption("نشاط خفيف", activityLevel == "نشاط خفيف") { activityLevel = "نشاط خفيف" }
                ActivityOption("نشاط معتدل", activityLevel == "نشاط معتدل") { activityLevel = "نشاط معتدل" }
                ActivityOption("نشاط عالٍ / نشاط شديد", activityLevel == "نشاط عالٍ / نشاط شديد") { activityLevel = "نشاط عالٍ / نشاط شديد" }
            }
            
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun GenderButton(text: String, isSelected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) Primary.copy(alpha = 0.1f) else SurfaceContainer
    val borderColor = if (isSelected) Primary else Color.White.copy(alpha = 0.1f)
    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun MetricInput(label: String, value: String, unit: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Column(
        modifier = modifier
            .background(SurfaceContainerLow, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Outline)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center, color = OnSurface),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Text(unit, style = MaterialTheme.typography.labelMedium, color = Outline)
    }
}

@Composable
fun GoalOption(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Primary.copy(alpha = 0.1f) else SurfaceContainer
    val borderColor = if (isSelected) Primary else Color.White.copy(alpha = 0.1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
        Icon(icon, contentDescription = null, tint = Outline, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ActivityOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Primary.copy(alpha = 0.1f) else SurfaceContainer
    val borderColor = if (isSelected) Primary else Color.White.copy(alpha = 0.1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
    }
}
