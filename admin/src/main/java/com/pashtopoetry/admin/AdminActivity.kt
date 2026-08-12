package com.pashtopoetry.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.PashtoPoetryTheme
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.admin.AdminLoginScreen
import com.example.ui.admin.AdminViewModel

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PashtoPoetryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val adminViewModel: AdminViewModel = viewModel()
                    val isAuthenticated by adminViewModel.isAdminAuthenticated.collectAsState()

                    if (isAuthenticated) {
                        AdminDashboardScreen(
                            adminViewModel = adminViewModel,
                            onLogoutClick = {
                                adminViewModel.logoutAdmin()
                            }
                        )
                    } else {
                        AdminLoginScreen(
                            onLoginSuccess = {
                                // Handled automatically via state update in viewModel
                            },
                            onAuthenticate = { password ->
                                adminViewModel.authenticateAdmin(password)
                            }
                        )
                    }
                }
            }
        }
    }
}
