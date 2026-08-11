package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.components.PashtoPoemCard
import com.example.ui.components.RtlLayout
import com.example.ui.theme.PashtoGold
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PoetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAuthScreen(
    authViewModel: AuthViewModel,
    poetryViewModel: PoetryViewModel,
    onPoemClick: (String) -> Unit,
    onAdminDashboardClick: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val myPoems by poetryViewModel.myPoems.collectAsState()

    var isRegisterMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    RtlLayout {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("زما حسـاب او پروفایل", fontWeight = FontWeight.Bold) }
                )
            }
        ) { paddingValues ->
            if (!currentUser.isLoggedIn) {
                // Auth Form Card (Login / Register)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (isRegisterMode) "نوې نوملیکنه وکړئ" else "حساب ته ننوتل",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = { nameInput = it },
                                    label = { Text("ستاسو بشپړ نوم") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("برېښنالیک (Email)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_email_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("پټنوم (Password)") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_password_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    if (emailInput.isBlank()) {
                                        Toast.makeText(context, "لطفاً برېښنالیک ولیکئ", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val isAdmin = emailInput.lowercase().contains("admin")
                                        authViewModel.login(emailInput, nameInput, isAdmin = isAdmin)
                                        poetryViewModel.currentUserId.value = "user_" + emailInput.hashCode()
                                        Toast.makeText(context, "ښه راغلاست!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("auth_submit_btn"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = if (isRegisterMode) "کتابچه ثبت کړئ" else "ننوتل",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                Text(
                                    text = if (isRegisterMode) "حساب لرئ؟ دلته ننوځئ" else "حساب نه لرئ؟ نوی جوړ کړئ"
                                )
                            }
                        }
                    }
                }
            } else {
                // Logged-in Profile View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentUser.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = currentUser.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = currentUser.bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onAdminDashboardClick,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = PashtoGold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("اداري اډمین پینل")
                                    }

                                    Button(
                                        onClick = { authViewModel.logout() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("وتل")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "زما خپاره شوي شعرونه (${myPoems.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (myPoems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "تاسو تر اوسه شعر ندی ورزیات کړی.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(myPoems, key = { it.id }) { poem ->
                            Box {
                                PashtoPoemCard(
                                    poem = poem,
                                    onPoemClick = { onPoemClick(poem.id) },
                                    onLikeClick = { poetryViewModel.toggleLike(poem.id, poem.isLikedByMe) },
                                    onFavoriteClick = { poetryViewModel.toggleFavorite(poem.id, poem.isFavoriteByMe) },
                                    onShareClick = {
                                        val shareIntent = android.content.Intent().apply {
                                            action = android.content.Intent.ACTION_SEND
                                            putExtra(android.content.Intent.EXTRA_TEXT, "${poem.title}\n\n${poem.content}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(android.content.Intent.createChooser(shareIntent, "شریکول"))
                                    }
                                )

                                IconButton(
                                    onClick = { poetryViewModel.deletePoem(poem.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete my poem",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }
}
