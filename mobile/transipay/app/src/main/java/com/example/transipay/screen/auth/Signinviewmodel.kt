package com.example.transipay.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.AuthService
import kotlinx.coroutines.launch

class SignInViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isSignedIn by mutableStateOf(false)
        private set

    /** [getIdToken] shows the Google picker; we then exchange the idToken at /auth/google. */
    fun signInWithGoogle(getIdToken: suspend () -> String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val idToken = getIdToken()
                when (val result = AuthService.googleLogin(idToken)) {
                    is ApiResult.Success -> {
                        ApiClient.authToken = result.data.bearer
                        isSignedIn = true
                    }
                    is ApiResult.Error -> errorMessage = result.message
                }
            } catch (e: GetCredentialCancellationException) {
                // User closed the picker: not an error.
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Google sign-in failed"
            }
            isLoading = false
        }
    }

    fun clearSignedIn() {
        isSignedIn = false
    }
}