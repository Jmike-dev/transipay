package com.example.transipay.screen.staff

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.Session
import com.example.transipay.service.staff.StaffResponse
import com.example.transipay.service.staff.StaffService
import kotlinx.coroutines.launch

class StaffListViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var staff by mutableStateOf<List<StaffResponse>>(emptyList())
        private set

    fun load() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val r = StaffService.list()) {
                is ApiResult.Success ->
                    // Stopgap: see the backend note below. Scope on the server instead.
                    staff = r.data.filter { it.companyId.equals(Session.companyId, ignoreCase = true) }
                is ApiResult.Error -> errorMessage =
                    if (r.code == 401) "Your session expired. Please sign in again." else r.message
            }
            isLoading = false
        }
    }
}