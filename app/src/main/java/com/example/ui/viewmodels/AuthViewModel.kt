package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    val currentUser = repository.currentUser

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "الرجاء إدخال البريد الإلكتروني وكلمة المرور") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.loginWithEmail(email, password)
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل الدخول") }
            }
        }
    }

    fun register(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "الرجاء إدخال البريد الإلكتروني وكلمة المرور") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.registerWithEmail(email, password)
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء إنشاء الحساب") }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = repository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _state.update { it.copy(isLoading = false, error = result.exceptionOrNull()?.message ?: "حدث خطأ أثناء تسجيل الدخول بحساب Google") }
            }
        }
    }
    
    fun setError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun logout() {
        repository.logout()
        _state.update { AuthState() }
    }
}
