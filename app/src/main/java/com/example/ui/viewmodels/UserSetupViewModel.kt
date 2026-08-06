package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.UserProfile
import com.example.api.UserService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserSetupState(
    val isLoading: Boolean = false,
    val name: String = "",
    val error: String? = null,
    val isSuccess: Boolean = false
)

class UserSetupViewModel : ViewModel() {
    private val _state = MutableStateFlow(UserSetupState())
    val state: StateFlow<UserSetupState> = _state.asStateFlow()
    
    private val userService = UserService()
    private val auth = FirebaseAuth.getInstance()
    
    init {
        val user = auth.currentUser
        if (user != null) {
            _state.update { it.copy(name = user.displayName ?: "") }
        }
    }
    
    fun updateName(newName: String) {
        _state.update { it.copy(name = newName) }
    }
    
    fun saveProfile(profile: UserProfile, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Start fire-and-forget save so we don't block on Firestore offline issues
            launch(kotlinx.coroutines.Dispatchers.IO) {
                userService.saveProfile(profile)
            }
            
            _state.update { it.copy(isLoading = false, isSuccess = true) }
            onSuccess()
        }
    }
}
