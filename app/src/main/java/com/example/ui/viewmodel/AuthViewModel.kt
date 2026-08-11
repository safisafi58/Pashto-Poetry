package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository(AppDatabase.getDatabase(application))

    val currentUser: StateFlow<UserProfile> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val allUsers: StateFlow<List<UserProfile>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun login(email: String, name: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            repository.login(email, name, isAdmin)
        }
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String?) {
        viewModelScope.launch {
            repository.updateProfile(name, bio, avatarUrl)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
