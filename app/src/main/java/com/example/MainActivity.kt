package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.NavGraph
import com.example.ui.theme.PashtoPoetryTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.PoetryViewModel

class MainActivity : ComponentActivity() {
    private val poetryViewModel: PoetryViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PashtoPoetryTheme {
                NavGraph(
                    poetryViewModel = poetryViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
