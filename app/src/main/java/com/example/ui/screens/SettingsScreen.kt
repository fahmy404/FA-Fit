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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.components.glassCard
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

import com.example.ui.viewmodels.MainViewModel
import com.example.ui.viewmodels.AppState

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    state: AppState,
    onLogout: () -> Unit = {},
    onNavigateToChallenges: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("تسجيل الخروج", color = OnSurface) },
            text = { Text("هل أنت متأكد من تسجيل الخروج؟", color = Outline) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("خروج", color = OnError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("إلغاء", color = Primary)
                }
            },
            containerColor = SurfaceContainerHigh
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
        // Header
        Column {
            Text("الإعدادات", style = MaterialTheme.typography.displayLarge, color = OnSurface)
            Text("إدارة حسابك وتخصيص تجربتك", style = MaterialTheme.typography.bodyMedium, color = Outline)
        }

        // Profile Card
        ProfileSection(state)

        AppearanceSection(viewModel, state)
        DietSection(viewModel, state)
        AdvancedSection(viewModel, state)

        // Challenges shortcut
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                Text("التحديات", style = MaterialTheme.typography.headlineSmall, color = Primary)
            }
            Column(modifier = Modifier.fillMaxWidth().glassCard()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChallenges() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Tertiary)
                        }
                        Column {
                            Text("تحدياتي النشطة", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                            Text("${state.challenges.size} تحدي نشط", style = MaterialTheme.typography.bodySmall, color = Outline)
                        }
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Primary)
                }
            }
        }

        // Logout
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().glassCard()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(ErrorContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Error)
                        }
                        Text("تسجيل الخروج", style = MaterialTheme.typography.labelLarge, color = Error)
                    }
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Error)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileSection(state: AppState) {
    val user = FirebaseAuth.getInstance().currentUser
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SurfaceContainerHigh, CircleShape)
                .border(2.dp, Primary, CircleShape)
        ) {
            if (user?.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.align(Alignment.Center).size(32.dp)
                )
            }
        }
        Column {
            Text(
                state.name.ifEmpty { user?.displayName ?: user?.email ?: "المستخدم" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                user?.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Outline
            )
            Text(
                "هدف: ${state.calorieGoal} سعرة • المياه: ${state.waterGoal} كوب",
                style = MaterialTheme.typography.labelSmall,
                color = Primary
            )
        }
    }
}

@Composable
fun AppearanceSection(viewModel: MainViewModel, state: AppState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Text("المظهر", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        Column(modifier = Modifier.fillMaxWidth().glassCard()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = OnSurface)
                    }
                    Column {
                        Text("السمة (Theme)", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                        Text(if (state.isDarkTheme) "وضع ليلي" else "وضع نهاري", style = MaterialTheme.typography.bodySmall, color = Outline)
                    }
                }
                Switch(
                    checked = state.isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryContainer)
                )
            }

            Divider(color = Outline.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth().clickable {
                    android.widget.Toast.makeText(context, "عذراً، سيتم إضافة دعم اللغة الإنجليزية في التحديث القادم.", android.widget.Toast.LENGTH_SHORT).show()
                }.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).background(Surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = OnSurface)
                    }
                    Column {
                        Text("اللغة (Language)", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                        Text("العربية", style = MaterialTheme.typography.bodySmall, color = Outline)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("تغيير", style = MaterialTheme.typography.labelLarge, color = Primary)
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Primary)
                }
            }
        }
    }
}

@Composable
fun DietSection(viewModel: MainViewModel, state: AppState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Text("نظام الأكل", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        Column(
            modifier = Modifier.fillMaxWidth().glassCard(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("اختر النمط الغذائي الأنسب لأهدافك", style = MaterialTheme.typography.bodySmall, color = Outline)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DietCard("متوازن", "كارب 50% | بروتين 25% | دهون 25%", state.selectedDiet == 0, { viewModel.selectDiet(0) }, Modifier.weight(1f))
                DietCard("بروتين عالي", "كارب 40% | بروتين 35% | دهون 25%", state.selectedDiet == 1, { viewModel.selectDiet(1) }, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DietCard("كارب منخفض", "كارب 25% | بروتين 35% | دهون 40%", state.selectedDiet == 2, { viewModel.selectDiet(2) }, Modifier.weight(1f))
                DietCard("كيتو", "كارب 5% | بروتين 20% | دهون 75%", state.selectedDiet == 3, { viewModel.selectDiet(3) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun DietCard(title: String, desc: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val bgColor = if (isSelected) Primary.copy(alpha = 0.1f) else Surface.copy(alpha = 0.5f)
    val borderColor = if (isSelected) Primary else Outline.copy(alpha = 0.2f)
    val textColor = if (isSelected) Primary else OnSurface

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = textColor)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = Outline, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun AdvancedSection(viewModel: MainViewModel, state: AppState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Text("خيارات متقدمة", style = MaterialTheme.typography.headlineSmall, color = Primary)
        }

        Column(modifier = Modifier.fillMaxWidth().glassCard()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(40.dp).background(Surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = OnSurface)
                    }
                    Column {
                        Text("تدوير السعرات", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                        Text("توزيع السعرات بشكل متغير خلال الأسبوع (Zigzag)", style = MaterialTheme.typography.bodySmall, color = Outline)
                    }
                }
                Switch(checked = state.zigzagEnabled, onCheckedChange = { viewModel.toggleZigzag(it) })
            }

            Divider(color = Outline.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(40.dp).background(Surface, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = OnSurface)
                    }
                    Column {
                        Text("حساب السعرات الذكي", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                        Text("تعديل تلقائي للسعرات بناءً على معدل نزول الوزن (Adaptive TDEE)", style = MaterialTheme.typography.bodySmall, color = Outline)
                    }
                }
                Switch(checked = state.adaptiveTDEEEnabled, onCheckedChange = { viewModel.toggleAdaptive(it) })
            }
        }
    }
}
