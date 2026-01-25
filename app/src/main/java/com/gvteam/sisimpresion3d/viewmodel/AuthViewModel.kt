package com.gvteam.sisimpresion3d.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gvteam.sisimpresion3d.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Idle
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Idle
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al registrarse")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Idle
            try {
                SupabaseClient.client.auth.signInWith(IDToken) {
                    this.idToken = token
                    this.provider = Google
                }
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Fallo Google: ${e.message}")
                Log.e("MiError", "Aquí falló algo: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}