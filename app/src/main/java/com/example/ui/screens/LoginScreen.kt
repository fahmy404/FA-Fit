package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: com.example.ui.viewmodels.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        // Background
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDU_AHbf5RKAquGZkC-9eYfWF3RlC_enI7DXqZULuArQJdcyWfd36gHTlNc4LtXkaY38HL2pJKyh967QWFMeQrRmaAYu2FbuRAqFH_KQDOX1JwCwuS9oTONd8oJzcdCIEXHz0BWyLekuFKC0NVJL_5qb9ttYiPDg3deXC-HY9LcCZ8QGER3-rtduL-SqOpDosFcRfv6dWAVWjlgb5T3kaxXy0SLHChZwm9wiytzUa43dgp-hDuLyGc",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.2f
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface.copy(alpha = 0.8f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1F).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Header
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Primary.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Primary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("FA² Fit", style = MaterialTheme.typography.displayLarge, color = OnSurface)
                            Text("مرحباً بك مجدداً", style = MaterialTheme.typography.bodyMedium, color = Outline)
                        }
                    }

                    // Form
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        // Email
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("البريد الإلكتروني أو اسم المستخدم", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("أدخل بريدك الإلكتروني", color = OutlineVariant) },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Outline) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFF08080A),
                                    focusedContainerColor = Color(0xFF08080A),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                    focusedBorderColor = BrandPurple,
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface
                                )
                            )
                        }

                        // Password
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("كلمة المرور", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text("نسيت كلمة المرور؟", style = MaterialTheme.typography.labelSmall, color = Primary)
                            }
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("أدخل كلمة المرور", color = OutlineVariant) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Outline) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = Outline
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFF08080A),
                                    focusedContainerColor = Color(0xFF08080A),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                                    focusedBorderColor = BrandPurple,
                                    focusedTextColor = OnSurface,
                                    unfocusedTextColor = OnSurface
                                )
                            )
                        }
                    }

                    // Buttons
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        state.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple, contentColor = Color.White),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("تسجيل الدخول", style = MaterialTheme.typography.labelLarge)
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current
                        val coroutineScope = rememberCoroutineScope()
                        val googleAuthHelper = remember { com.example.utils.GoogleAuthHelper(context) }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val result = googleAuthHelper.getGoogleIdToken()
                                    result.onSuccess { token ->
                                        viewModel.loginWithGoogle(token)
                                    }.onFailure { e ->
                                        viewModel.setError(e.message ?: "خطأ في تسجيل الدخول بحساب جوجل")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Surface),
                            enabled = !state.isLoading
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("تسجيل الدخول بواسطة جوجل", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.clickable {
                        if (!state.isLoading) viewModel.register(email, password)
                    }) {
                        Text("ليس لديك حساب؟", style = MaterialTheme.typography.bodySmall, color = Outline)
                        Text("سجل الآن", style = MaterialTheme.typography.labelSmall, color = Primary)
                    }
                }
            }
        }
    }
}
