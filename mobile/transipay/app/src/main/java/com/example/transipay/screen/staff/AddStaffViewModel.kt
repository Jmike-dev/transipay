package com.example.transipay.screen.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.Role
import com.example.transipay.service.auth.Session
import com.example.transipay.service.staff.StaffRequest
import com.example.transipay.service.staff.StaffService
import kotlinx.coroutines.launch
import java.security.SecureRandom

class AddStaffViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var createdName by mutableStateOf<String?>(null)
        private set

    fun submit(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        role: Role,
        password: String,
    ) {
        if (isLoading) return

        val companyId = Session.companyId
        if (companyId == null) {
            errorMessage = "You are not signed in."
            return
        }
        if (!Session.isAdmin) {
            errorMessage = "Only admins can add staff."
            return
        }
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
            errorMessage = "First name, last name and email are required."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMessage = "Enter a valid email address."
            return
        }
        if (password.length !in 10..72) {
            errorMessage = "Password must be 10 to 72 characters."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val request = StaffRequest(
                companyId = companyId,
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = email.trim().lowercase(),
                phone = phone.trim().ifBlank { null },
                role = role.name,
                active = true,
                password = password,
            )

            when (val r = StaffService.create(request)) {
                is ApiResult.Success -> createdName = "${r.data.firstName} ${r.data.lastName}"
                is ApiResult.Error -> errorMessage = when (r.code) {
                    400 -> "Some details are invalid. Check the fields and try again."
                    401 -> "Your session expired. Please sign in again."
                    403 -> "Only admins can add staff."
                    409 -> "That email is already in use."
                    else -> r.message
                }
            }
            isLoading = false
        }
    }

    /** 14-char random password with no look-alike characters (0/O, 1/l/I) */
    fun generatePassword(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789"
        val rnd = SecureRandom()
        return (1..14).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
    }

    fun clearCreated() { createdName = null }
    fun clearError() { errorMessage = null }
}