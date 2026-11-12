package com.example.transipay.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.AuthResponse
import com.example.transipay.service.auth.AuthService
import com.example.transipay.service.auth.Role
import com.example.transipay.service.auth.Session
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var signedInRole by mutableStateOf<Role?>(null)
        private set

    fun signInWithPassword(email: String, password: String) {
        if (isLoading) return
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Enter your email and password."
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            handle(AuthService.login(email.trim(), password))
            isLoading = false
        }
    }

    fun signInWithGoogle(getIdToken: suspend () -> String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                handle(AuthService.googleLogin(getIdToken()))
            } catch (e: GetCredentialCancellationException) {
                // User closed the picker: not an error.
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Google sign-in failed"
            }
            isLoading = false
        }
    }

    private fun handle(result: ApiResult<AuthResponse>) {
        when (result) {
            is ApiResult.Success -> {
                ApiClient.authToken = result.data.accessToken
                Session.start(result.data)
                signedInRole = result.data.roleEnum
            }
            is ApiResult.Error -> errorMessage = friendly(result.message)
        }
    }

    private fun friendly(msg: String?): String = when {
        msg == null -> "Sign-in failed. Try again."
        msg.contains("Invalid email or password", true) -> "Incorrect email or password."
        msg.contains("Too many attempts", true) ->
            "Too many failed attempts. Please wait 15 minutes and try again."
        msg.contains("No staff account", true) ->
            "This email isn't registered. Ask your Sacco admin to add you first."
        msg.contains("disabled", true) -> "Your account has been disabled. Contact your admin."
        msg.contains("another Google account", true) ->
            "This email is linked to a different Google account."
        else -> msg
    }

    fun clearSignedIn() { signedInRole = null }
}