package com.example.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RtlLayout
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.theme.PashtoGold
import com.example.ui.theme.PashtoPoetryTheme
import com.example.ui.viewmodel.AdminViewModel

class AdminActivity : ComponentActivity() {
    private val adminViewModel: AdminViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PashtoPoetryTheme {
                AdminAppMainContent(adminViewModel = adminViewModel, onExit = { finish() })
            }
        }
    }
}

@Composable
fun AdminAppMainContent(
    adminViewModel: AdminViewModel,
    onExit: () -> Unit
) {
    var isAuthenticated by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        AdminDashboardScreen(
            adminViewModel = adminViewModel,
            onBackClick = onExit
        )
    } else {
        AdminLoginScreen(
            onLoginSuccess = { isAuthenticated = true },
            onExit = onExit
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    onExit: () -> Unit
) {
    var email by remember { mutableStateOf("admin@pashtopoetry.af") }
    var password by remember { mutableStateOf("admin123") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    RtlLayout {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("د اډمین ننوتل (Admin Portal)", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onExit) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PashtoGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = PashtoGold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Text(
                            text = "د اډمین پینل اختصاصي ننوتل",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "د پښتو شعرونو او دیتابیس د مدیریت لپاره خپلو پیژندپاڼو سره داخل شئ.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("د اډمین ایمیل (Email)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("پټه نوم (Password)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        AnimatedVisibility(visible = errorMessage != null) {
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    errorMessage = "مهرباني وکړئ ایمیل او پاسورډ دننه کړئ."
                                } else if (password.length < 4) {
                                    errorMessage = "پاسورډ سم ندی."
                                } else {
                                    isLoading = true
                                    Toast.makeText(context, "اډمین سیسټم ته په بریالیتوب سره ننوتلئ!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PashtoGold)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("پینل ته داخلیدل", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        TextButton(
                            onClick = {
                                onLoginSuccess()
                            }
                        ) {
                            Text("د ازموینې ننوتل (Quick Demo Access)", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
