package com.example.transipay.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.AuthService
import com.example.transipay.service.auth.RegisterRequest
import com.example.transipay.service.auth.Session
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var signedUp by mutableStateOf(false)
        private set

    fun submit(
        companyId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        confirm: String,
    ) {
        if (isLoading) return
        errorMessage = when {
            firstName.isBlank() || lastName.isBlank() || email.isBlank() ->
                "First name, last name and email are required."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Enter a valid email address."
            password.length !in 10..72 -> "Password must be 10 to 72 characters."
            password != confirm -> "Passwords don't match."
            else -> null
        }
        if (errorMessage != null) return

        viewModelScope.launch {
            isLoading = true
            val req = RegisterRequest(
                companyId = companyId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim().lowercase(),
                phone = phone.trim().ifBlank { null },
                password = password,
            )
            when (val r = AuthService.register(req)) {
                is ApiResult.Success -> {
                    ApiClient.authToken = r.data.accessToken
                    Session.start(r.data)
                    signedUp = true
                }
                is ApiResult.Error -> errorMessage = when (r.code) {
                    409 -> "This email or company is already registered. Try signing in."
                    404 -> "Company not found. Go back and create it again."
                    400 -> "Some details are invalid. Check the fields and try again."
                    else -> r.message
                }
            }
            isLoading = false
        }
    }
}