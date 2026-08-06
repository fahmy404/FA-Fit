package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.components.glassCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.MainViewModel
import kotlinx.coroutines.launch
import com.example.api.MealAnalysisResult
import com.example.api.MealService

enum class MealInputMode { AI, MANUAL, BARCODE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    val state by viewModel.state.collectAsState()
    var mealText by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var analyzedCalories by remember { mutableStateOf("0") }
    var analyzedProtein by remember { mutableStateOf("0") }
    var analyzedCarbs by remember { mutableStateOf("0") }
    var analyzedFat by remember { mutableStateOf("0") }
    var mealName by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(MealInputMode.AI) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val mealService = remember { MealService() }

    // Barcode scanner
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                // Got barcode - use Gemini to analyze it
                coroutineScope.launch {
                    isAnalyzing = true
                    val barcodeText = "باركود منتج: ${result.contents} - قدم المعلومات الغذائية التقريبية لهذا الباركود"
                    val barcodeResult = mealService.analyzeMeal(barcodeText)
                    isAnalyzing = false
                    if (barcodeResult != null) {
                        analyzedCalories = barcodeResult.calories.toString()
                        analyzedProtein = barcodeResult.protein.toString()
                        analyzedCarbs = barcodeResult.carbs.toString()
                        analyzedFat = barcodeResult.fat.toString()
                        mealName = barcodeResult.name
                        showEditDialog = true
                    }
                }
            }
        }
    )

    // Confirm/Edit dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تأكيد وتعديل الوجبة", color = OnSurface) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = mealName, onValueChange = { mealName = it }, label = { Text("اسم الوجبة") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = analyzedCalories,
                        onValueChange = { analyzedCalories = it },
                        label = { Text("السعرات الحرارية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = analyzedProtein, onValueChange = { analyzedProtein = it }, label = { Text("بروتين (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = analyzedCarbs, onValueChange = { analyzedCarbs = it }, label = { Text("كارب (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = analyzedFat, onValueChange = { analyzedFat = it }, label = { Text("دهون (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val calories = analyzedCalories.toIntOrNull() ?: 0
                        val protein = analyzedProtein.toIntOrNull() ?: 0
                        val carbs = analyzedCarbs.toIntOrNull() ?: 0
                        val fat = analyzedFat.toIntOrNull() ?: 0
                        val meal = MealAnalysisResult(calories, protein, carbs, fat, mealName.ifBlank { "وجبة" })
                        viewModel.addFavoriteMeal(meal)
                        android.widget.Toast.makeText(context, "تمت الإضافة للمفضلة ✓", android.widget.Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مفضلة")
                    }
                    Button(
                        onClick = {
                            val calories = analyzedCalories.toIntOrNull() ?: 0
                            val protein = analyzedProtein.toIntOrNull() ?: 0
                            val carbs = analyzedCarbs.toIntOrNull() ?: 0
                            val fat = analyzedFat.toIntOrNull() ?: 0
                            viewModel.addCalories(calories)
                            viewModel.addMacros(protein, carbs, fat)
                            coroutineScope.launch {
                                mealService.saveMealToFirestore(MealAnalysisResult(calories, protein, carbs, fat, mealName))
                            }
                            showEditDialog = false
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("إضافة", color = OnPrimary) }
                }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("إلغاء", color = Outline) } },
            containerColor = SurfaceContainerHigh
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة وجبة", style = MaterialTheme.typography.headlineSmall, color = Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface.copy(alpha = 0.9f))
            )
        },
        containerColor = Surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Mode Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerHigh.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ModeTabButton("تحليل ذكي", selectedMode == MealInputMode.AI, Icons.Default.SmartToy, Modifier.weight(1f)) {
                    selectedMode = MealInputMode.AI
                }
                ModeTabButton("يدوي", selectedMode == MealInputMode.MANUAL, Icons.Default.Edit, Modifier.weight(1f)) {
                    selectedMode = MealInputMode.MANUAL
                }
                ModeTabButton("باركود", selectedMode == MealInputMode.BARCODE, Icons.Default.QrCodeScanner, Modifier.weight(1f)) {
                    selectedMode = MealInputMode.BARCODE
                }
            }

            // Content per mode
            when (selectedMode) {
                MealInputMode.AI -> {
                    AIMealInput(
                        mealText = mealText,
                        onMealTextChange = { mealText = it },
                        isAnalyzing = isAnalyzing,
                        onAnalyze = {
                            if (mealText.isNotBlank()) {
                                isAnalyzing = true
                                coroutineScope.launch {
                                    val result = mealService.analyzeMeal(mealText)
                                    isAnalyzing = false
                                    if (result != null) {
                                        analyzedCalories = result.calories.toString()
                                        analyzedProtein = result.protein.toString()
                                        analyzedCarbs = result.carbs.toString()
                                        analyzedFat = result.fat.toString()
                                        mealName = result.name
                                        showEditDialog = true
                                    }
                                }
                            }
                        }
                    )
                }

                MealInputMode.MANUAL -> {
                    ManualMealInput(
                        onConfirm = { name, cal, prot, carb, fat ->
                            mealName = name
                            analyzedCalories = cal.toString()
                            analyzedProtein = prot.toString()
                            analyzedCarbs = carb.toString()
                            analyzedFat = fat.toString()
                            showEditDialog = true
                        }
                    )
                }

                MealInputMode.BARCODE -> {
                    BarcodeScanCard(
                        isAnalyzing = isAnalyzing,
                        onScan = {
                            val options = com.journeyapps.barcodescanner.ScanOptions()
                            options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.ALL_CODE_TYPES)
                            options.setPrompt("امسح باركود المنتج")
                            options.setBeepEnabled(true)
                            options.setBarcodeImageEnabled(false)
                            options.setOrientationLocked(false)
                            scanLauncher.launch(options)
                        }
                    )
                }
            }

            // Favorites Section
            if (state.favoriteMeals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Tertiary, modifier = Modifier.size(18.dp))
                        Text("المفضلة", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                    }

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.favoriteMeals.forEach { favoriteMeal ->
                            QuickAddChip(favoriteMeal.name, "${favoriteMeal.calories} س", Icons.Default.Favorite) {
                                analyzedCalories = favoriteMeal.calories.toString()
                                analyzedProtein = favoriteMeal.protein.toString()
                                analyzedCarbs = favoriteMeal.carbs.toString()
                                analyzedFat = favoriteMeal.fat.toString()
                                mealName = favoriteMeal.name
                                showEditDialog = true
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AIMealInput(
    mealText: String,
    onMealTextChange: (String) -> Unit,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
            Text("ماذا أكلت؟", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        OutlinedTextField(
            value = mealText,
            onValueChange = onMealTextChange,
            placeholder = { Text("اكتب اللي أكلته... مثلاً: أكلت صحن فول وطعمية وبيض مسلوق", color = Outline) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = SurfaceContainerHighest.copy(alpha = 0.5f),
                unfocusedContainerColor = SurfaceContainerHighest.copy(alpha = 0.5f),
                focusedTextColor = OnSurface,
                unfocusedTextColor = OnSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onAnalyze,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isAnalyzing && mealText.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("جاري التحليل...", style = MaterialTheme.typography.labelLarge, color = OnPrimary)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("تحليل بالذكاء الاصطناعي", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = OnPrimary)
            }
        }
    }
}

@Composable
fun ManualMealInput(onConfirm: (String, Int, Int, Int, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
            Text("إدخال يدوي", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم الوجبة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface)
        )

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("السعرات الحرارية") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, focusedLabelColor = Primary, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
            trailingIcon = { Text("كيلو كالوري", style = MaterialTheme.typography.labelSmall, color = Outline, modifier = Modifier.padding(end = 8.dp)) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("بروتين") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ProteinColor, focusedLabelColor = ProteinColor, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
                trailingIcon = { Text("g", style = MaterialTheme.typography.labelSmall, color = Outline) }
            )
            OutlinedTextField(
                value = carbs,
                onValueChange = { carbs = it },
                label = { Text("كارب") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CarbsColor, focusedLabelColor = CarbsColor, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
                trailingIcon = { Text("g", style = MaterialTheme.typography.labelSmall, color = Outline) }
            )
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("دهون") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FatColor, focusedLabelColor = FatColor, focusedTextColor = OnSurface, unfocusedTextColor = OnSurface),
                trailingIcon = { Text("g", style = MaterialTheme.typography.labelSmall, color = Outline) }
            )
        }

        Button(
            onClick = {
                onConfirm(
                    name.ifBlank { "وجبة" },
                    calories.toIntOrNull() ?: 0,
                    protein.toIntOrNull() ?: 0,
                    carbs.toIntOrNull() ?: 0,
                    fat.toIntOrNull() ?: 0
                )
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = calories.isNotBlank()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("تأكيد وإضافة", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = OnPrimary)
        }
    }
}

@Composable
fun BarcodeScanCard(isAnalyzing: Boolean, onScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().glassCard(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Primary, modifier = Modifier.size(22.dp))
            Text("مسح الباركود", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        Box(
            modifier = Modifier
                .size(180.dp)
                .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                .border(2.dp, Primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Primary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                Text("اضغط للمسح", style = MaterialTheme.typography.labelMedium, color = Outline)
            }
        }

        Text(
            "سيتم استخدام الذكاء الاصطناعي لتحليل المنتج\nوتقدير قيمته الغذائية",
            style = MaterialTheme.typography.bodySmall,
            color = Outline,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Button(
            onClick = onScan,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            enabled = !isAnalyzing
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnPrimary, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("جاري التحليل...", color = OnPrimary)
            } else {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("مسح الباركود", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = OnPrimary)
            }
        }
    }
}

@Composable
fun ModeTabButton(
    text: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Primary else Outline, modifier = Modifier.size(14.dp))
            Text(
                text = text,
                color = if (isSelected) Primary else Outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun QuickAddChip(name: String, calories: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .glassCard()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).background(SurfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Tertiary)
        }
        Text(name, style = MaterialTheme.typography.labelLarge, color = OnSurface)
        Text(calories, style = MaterialTheme.typography.labelSmall, color = Outline)
    }
}