package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import coil.compose.AsyncImage
import com.example.ui.components.QRCodeImage
import com.example.ui.components.glassCard
import com.example.ui.theme.*
import com.example.ui.viewmodels.AppState
import com.example.ui.viewmodels.Challenge
import com.example.ui.viewmodels.MainViewModel


@Composable
fun ChallengesScreen(viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(), state: AppState = AppState()) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var showQRDialog by remember { mutableStateOf(false) }
    var qrCode by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var challengeTitle by remember { mutableStateOf("") }
    var challengeType by remember { mutableStateOf("calories") }
    var challengeTarget by remember { mutableStateOf("") }
    
    var selectedChallengeForQr by remember { mutableStateOf<Challenge?>(null) }
    
    if (selectedChallengeForQr != null) {
        AlertDialog(
            onDismissRequest = { selectedChallengeForQr = null },
            title = { Text("دعوة أصدقاء", color = OnSurface) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("شارك كود التحدي مع أصدقائك", color = Outline, textAlign = TextAlign.Center)
                    QRCodeImage(content = selectedChallengeForQr!!.code, size = 200.dp)
                    Text("كود التحدي: ${selectedChallengeForQr!!.code}", style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "انضم لتحدي ${selectedChallengeForQr!!.title} باستخدام الكود: ${selectedChallengeForQr!!.code}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة كود التحدي"))
                    }) {
                        Text("مشاركة الكود")
                    }
                    Button(onClick = { selectedChallengeForQr = null }) { Text("إغلاق") }
                }
            },
            containerColor = Surface
        )
    }
    
    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = com.journeyapps.barcodescanner.ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                viewModel.joinChallenge(result.contents)
                showQRDialog = false
            }
        }
    )

    if (showQRDialog) {
        AlertDialog(
            onDismissRequest = { showQRDialog = false },
            title = { Text("الانضمام لتحدي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = qrCode,
                        onValueChange = { qrCode = it },
                        placeholder = { Text("أدخل الكود يدويًا") }
                    )
                    Button(
                        onClick = { 
                            val options = com.journeyapps.barcodescanner.ScanOptions()
                            options.setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
                            options.setPrompt("امسح كود التحدي")
                            options.setBeepEnabled(false)
                            options.setBarcodeImageEnabled(false)
                            scanLauncher.launch(options)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مسح كود QR")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.joinChallenge(qrCode); showQRDialog = false }) { Text("انضمام") }
            },
            dismissButton = {
                TextButton(onClick = { showQRDialog = false }) { Text("إلغاء") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("إنشاء تحدي جديد", color = OnSurface) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = challengeTitle,
                        onValueChange = { challengeTitle = it },
                        placeholder = { Text("اسم التحدي") },
                        label = { Text("اسم التحدي") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.Primary,
                            focusedLabelColor = com.example.ui.theme.Primary,
                            focusedTextColor = com.example.ui.theme.OnSurface,
                            unfocusedTextColor = com.example.ui.theme.OnSurface
                        )
                    )
                    
                    // Challenge type selection
                    Text("نوع التحدي:", style = MaterialTheme.typography.labelSmall, color = com.example.ui.theme.Outline)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Triple("calories", "🔥 سعرات", "الهدف: اجمع سعرات"),
                            Triple("workouts", "💪 سِتّ", "الهدف: أكمل سِتّ"),
                            Triple("volume", "⚖️ وزن", "الهدف: ارفع وزن")
                        ).forEach { (type, label, _) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (challengeType == type) com.example.ui.theme.Primary.copy(alpha = 0.2f)
                                        else com.example.ui.theme.SurfaceContainerHigh,
                                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (challengeType == type) com.example.ui.theme.Primary
                                        else Color.White.copy(alpha = 0.1f),
                                        androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                    )
                                    .clickable { challengeType = type }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (challengeType == type) com.example.ui.theme.Primary else com.example.ui.theme.Outline
                                )
                            }
                        }
                    }
                    
                    // Target value
                    val targetLabel = when (challengeType) {
                        "calories" -> "الهدف (سعرة)"
                        "workouts" -> "الهدف (عدد سِتّ مكتملة)"
                        "volume" -> "الهدف (كجم إجمالي مرفوع)"
                        else -> "الهدف"
                    }
                    OutlinedTextField(
                        value = challengeTarget,
                        onValueChange = { challengeTarget = it },
                        label = { Text(targetLabel) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = com.example.ui.theme.Primary,
                            focusedLabelColor = com.example.ui.theme.Primary,
                            focusedTextColor = com.example.ui.theme.OnSurface,
                            unfocusedTextColor = com.example.ui.theme.OnSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = challengeTarget.toIntOrNull() ?: 0
                        viewModel.createChallenge(challengeTitle, challengeType, target) { code ->
                            val newlyCreated = Challenge(
                                id = state.challenges.size + 1,
                                title = challengeTitle,
                                timeLeft = "7 أيام متبقية",
                                rank = 1,
                                code = code,
                                type = challengeType,
                                targetValue = target
                            )
                            selectedChallengeForQr = newlyCreated
                        }
                        showCreateDialog = false
                        challengeTitle = ""
                        challengeTarget = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.Primary)
                ) { Text("إنشاء", color = com.example.ui.theme.OnPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("إلغاء", color = com.example.ui.theme.Outline) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
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
        LevelCard(state)
        ActiveChallenges(state, viewModel = viewModel, onQrClick = { selectedChallengeForQr = it })
        ActionButtons(
            onCreateClick = { showCreateDialog = true },
            onJoinClick = { showQRDialog = true }
        )
        Leaderboard(state)
    }
}

@Composable
fun LevelCard(state: AppState = AppState()) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(0.8f, animationSpec = tween(1000))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("المستوى ${state.userLevel}", style = MaterialTheme.typography.headlineSmall, color = Primary)
                Text("محارب السعرات", style = MaterialTheme.typography.bodySmall, color = Outline)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${state.totalPoints}", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Text("نقطة إجمالية", style = MaterialTheme.typography.labelSmall, color = OutlineVariant)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("التقدم للمستوى 6", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text("80%", style = MaterialTheme.typography.labelSmall, color = PrimaryFixedDim)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(SurfaceContainerHigh, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animationProgress.value)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(BrandPurple, Primary)), RoundedCornerShape(50))
                )
            }
            
            Text("550 نقطة متبقية", style = MaterialTheme.typography.labelSmall, color = Outline, fontSize = 10.sp)
        }
    }
}

@Composable
fun ActiveChallenges(state: AppState, viewModel: MainViewModel, onQrClick: (Challenge) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("التحديات النشطة", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
            Text("عرض الكل", style = MaterialTheme.typography.labelSmall, color = Primary)
        }

        if (state.challenges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏆", style = MaterialTheme.typography.displayLarge)
                    Text("لا توجد تحديات نشطة", style = MaterialTheme.typography.bodyMedium, color = Outline)
                    Text("أنشئ تحدياً أو انضم لتحدي أصدقائك!", style = MaterialTheme.typography.labelSmall, color = OutlineVariant)
                }
            }
        }

        state.challenges.forEach { challenge ->
            val progress = viewModel.getChallengeProgress(challenge)
            val progressFraction = if (challenge.targetValue > 0) {
                (progress.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val progressPercent = (progressFraction * 100).toInt()

            val typeLabel = when (challenge.type) {
                "calories" -> "🔥 سعرات"
                "workouts" -> "💪 سِتّ مكتملة"
                "volume" -> "⚖️ وزن مرفوع"
                else -> "🏆 تحدي"
            }
            val progressLabel = when (challenge.type) {
                "calories" -> "$progress / ${challenge.targetValue} سعرة"
                "workouts" -> "$progress / ${challenge.targetValue} سِتّ"
                "volume" -> "$progress / ${challenge.targetValue} كجم"
                else -> "$progress / ${challenge.targetValue}"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard()
                    .padding(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(if (progressFraction >= 1f) SuccessColor else Tertiary)
                )
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(SurfaceContainer, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Tertiary)
                            }
                            Column {
                                Text(challenge.title, style = MaterialTheme.typography.labelLarge, color = OnSurface)
                                Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = Outline)
                                if (challenge.code.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.clickable { onQrClick(challenge) }.padding(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = Primary, modifier = Modifier.size(16.dp))
                                        Text("كود: ${challenge.code}", style = MaterialTheme.typography.labelSmall, color = Primary)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .background(SurfaceContainerHigh, androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(14.dp))
                            Text(challenge.timeLeft, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                    }

                    // Real progress bar
                    if (challenge.targetValue > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(progressLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text(
                                    if (progressFraction >= 1f) "✅ مكتمل!" else "$progressPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (progressFraction >= 1f) SuccessColor else PrimaryFixedDim
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(SurfaceContainerHigh, androidx.compose.foundation.shape.RoundedCornerShape(50))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .background(
                                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                                if (progressFraction >= 1f) listOf(SuccessColor, SuccessColor)
                                                else listOf(BrandPurple, Primary)
                                            ),
                                            androidx.compose.foundation.shape.RoundedCornerShape(50)
                                        )
                                )
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuBBdxfM1bBbmt_60dbfw8Bm7qZ5zYmgqEQwdoC8uJ23c0TrLiq5gR9It95fb9SiDV9H3UGtfpjff53oIcXanYeUDDQsUDNSFxUKalol7aE_TypDlQJ0oBnhsfEnjaArPOpXVt7Dq_Xeoxj47OqzM9E0CO3v_2dRWQecjCZ33IodjFgAxqwk88DmTZV7UmKFCC7FgTDCPy0xWS2wQSXEt0Y89u1yq4qmRfBguecH8uctXSVrNkmqNQg",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, SurfaceContainerHigh, CircleShape)
                            )
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDQbvFsxpsuxI_Bn2joSAthwTy-b9hjGiEUVYfNqOWMtYGT33O_hl7uy252C7XzTA4sVt1BVOIm8StLB6kgab-UXm_pIVI9NbwJvMh71HlWBlvVf9w_rjte-hMYRYh4cuNZKb6y68_GccV7ElyXP07J1nNA6bQ-TAstBKNKG44Hr3IO_YG3o4HhjU-MQXUXd5qz0e8sZKms414Qn0XO7I0QFFEvQn4YQxEYX9Bv831cnVgu9ub-it4",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .offset(x = (-8).dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, SurfaceContainerHigh, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .offset(x = (-16).dp)
                                    .size(32.dp)
                                    .background(SurfaceVariant, CircleShape)
                                    .border(2.dp, SurfaceContainerHigh, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+3", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ترتيبك", style = MaterialTheme.typography.labelSmall, color = OutlineVariant)
                            Text("الـ ${challenge.rank}", style = MaterialTheme.typography.headlineMedium, color = Tertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onCreateClick: () -> Unit, onJoinClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCreateClick,
            modifier = Modifier.weight(1f).height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(32.dp))
                Text("إنشاء تحدي", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
        
        OutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.weight(1f).height(100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(32.dp)) // qr code scanner
                Text("انضمام بكود", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun Leaderboard(state: AppState = AppState()) {
    val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userName = state.name.ifEmpty { user?.displayName ?: "أنت" }
    val userPoints = state.totalPoints
    val userLevel = state.userLevel

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("لوحة الشرف", style = MaterialTheme.typography.headlineSmall, color = OnSurface)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1F).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
        ) {
            // Current user row
            LeaderboardRow(
                rank = 1,
                name = userName,
                level = userLevel,
                xp = "$userPoints",
                imageUrl = user?.photoUrl?.toString() ?: "",
                isCurrentUser = true
            )
            Divider(color = Color.White.copy(alpha = 0.05f))
            // Placeholder rows
            Box(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ادعُ أصدقاءك للانضمام لترى ترتيبك بينهم!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: Int, name: String, level: Int, xp: String, imageUrl: String, isCurrentUser: Boolean) {
    val bgColor = if (isCurrentUser) Primary.copy(alpha = 0.05f) else Color.Transparent
    val nameColor = if (isCurrentUser) Primary else OnSurface
    val rankColor = when (rank) {
        1 -> Tertiary
        2 -> Outline
        else -> OutlineVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
                Text(rank.toString(), style = MaterialTheme.typography.headlineMedium, color = rankColor)
            }
            
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(if (isCurrentUser) Modifier.border(1.dp, Primary, CircleShape) else Modifier)
            )
            
            Column {
                Text(name, style = MaterialTheme.typography.labelLarge, color = nameColor)
                Text("المستوى $level", style = MaterialTheme.typography.labelSmall, color = if(isCurrentUser) PrimaryFixedDim else Outline)
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(xp, style = MaterialTheme.typography.headlineSmall, color = if(rank==1) PrimaryFixedDim else OnSurface)
            if (!isCurrentUser) {
                Icon(Icons.Default.Report, contentDescription = "Report", tint = OutlineVariant, modifier = Modifier.size(20.dp))
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}
