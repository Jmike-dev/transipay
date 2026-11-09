package com.example.transipay.screen.fleet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiResult
import com.example.transipay.service.auth.Session
import com.example.transipay.service.vehicle.VehicleResponse
import com.example.transipay.service.vehicle.VehicleService
import kotlinx.coroutines.launch

class FleetViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var vehicles by mutableStateOf<List<VehicleResponse>>(emptyList())
        private set

    var isSaving by mutableStateOf(false)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set

    fun load() {
        val companyId = Session.companyId ?: return
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            when (val r = VehicleService.getByCompany(companyId)) {
                is ApiResult.Success -> vehicles = r.data
                is ApiResult.Error -> errorMessage =
                    if (r.code == 401) "Your session expired. Please sign in again." else r.message
            }
            isLoading = false
        }
    }

    fun addVehicle(plateInput: String, onSuccess: () -> Unit) {
        if (isSaving) return
        val companyId = Session.companyId
        if (companyId == null) {
            saveError = "You are not signed in."
            return
        }
        val plate = plateInput.trim().uppercase()
        if (plate.isBlank()) {
            saveError = "Enter the number plate."
            return
        }
        if (plate.length > 20) {
            saveError = "Number plate is too long (max 20 characters)."
            return
        }

        viewModelScope.launch {
            isSaving = true
            saveError = null
            when (val r = VehicleService.create(plate, companyId)) {
                is ApiResult.Success -> {
                    vehicles = listOf(r.data) + vehicles
                    onSuccess()
                }
                is ApiResult.Error -> saveError = when (r.code) {
                    400 -> "Invalid number plate."
                    401 -> "Your session expired. Please sign in again."
                    403 -> "You don't have permission to add vehicles."
                    409 -> "A vehicle with this number plate already exists."
                    else -> r.message
                }
            }
            isSaving = false
        }
    }

    fun clearSaveError() { saveError = null }
}