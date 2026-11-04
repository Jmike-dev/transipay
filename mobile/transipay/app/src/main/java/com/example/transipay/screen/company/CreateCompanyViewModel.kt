package com.example.transipay.screen.company

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiResult
import com.example.transipay.service.company.Company
import com.example.transipay.service.company.CompanyService
import kotlinx.coroutines.launch

class CreateCompanyViewModel : ViewModel() {

    // Like const [companyName, setCompanyName] = useState("")
    var companyName by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var createdCompany by mutableStateOf<Company?>(null)
        private set

    fun onCompanyNameChange(value: String) {
        companyName = value
        errorMessage = null
    }

    fun createCompany() {
        if (companyName.isBlank()) {
            errorMessage = "Company name is required"
            return
        }

        viewModelScope.launch {
            isLoading = true
            when (val result = CompanyService.createCompany(companyName.trim())) {
                is ApiResult.Success -> {
                    createdCompany = result.data
                    companyName = ""
                }
                is ApiResult.Error -> errorMessage = result.message
            }
            isLoading = false
        }
    }

    fun clearCreated() {
        createdCompany = null
    }
}