package com.example.transipay.screen.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.transipay.service.ApiResult
import com.example.transipay.service.payment.PaymentResponse
import com.example.transipay.service.payment.PaymentService
import com.example.transipay.service.payment.PaymentStatus
import com.example.transipay.service.vehicle.VehicleResponse
import com.example.transipay.service.vehicle.VehicleService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PaymentPhase { FORM, WAITING, SUCCESS, FAILED }

data class PaymentUiState(
    val showDialog: Boolean = false,
    val phase: PaymentPhase = PaymentPhase.FORM,
    val vehicles: List<VehicleResponse> = emptyList(),
    val loadingVehicles: Boolean = false,
    val selectedVehicle: VehicleResponse? = null,
    val phoneNumber: String = "",
    val amount: String = "",
    val submitting: Boolean = false,
    val formError: String? = null,
    val payment: PaymentResponse? = null,
    val resultMessage: String? = null,
)

data class PaymentHistoryState(
    val vehicles: List<VehicleResponse> = emptyList(),
    val filterVehicle: VehicleResponse? = null,   // null = all vehicles
    val items: List<PaymentResponse> = emptyList(),
    val page: Int = 0,
    val endReached: Boolean = false,
    val loading: Boolean = false,                 // first page
    val loadingMore: Boolean = false,
    val error: String? = null,
)

class PaymentViewModel : ViewModel() {

    private val _state = MutableStateFlow(PaymentUiState())
    val state: StateFlow<PaymentUiState> = _state.asStateFlow()

    private val _history = MutableStateFlow(PaymentHistoryState())
    val history: StateFlow<PaymentHistoryState> = _history.asStateFlow()

    private var pollJob: Job? = null
    private var loadJob: Job? = null
    private var historyCompanyId: String? = null

    /* ------------------------- History list ------------------------- */

    fun initHistory(companyId: String) {
        if (historyCompanyId == companyId) return
        historyCompanyId = companyId

        viewModelScope.launch {
            val r = VehicleService.getByCompany(companyId)
            if (r is ApiResult.Success) _history.update { it.copy(vehicles = r.data) }
        }
        loadPage(0)
    }

    fun onFilterSelected(vehicle: VehicleResponse?) {
        if (_history.value.filterVehicle?.id == vehicle?.id) return
        _history.update { it.copy(filterVehicle = vehicle, items = emptyList(), page = 0, endReached = false) }
        loadPage(0)
    }

    fun refresh() = loadPage(0)

    fun loadMore() {
        val h = _history.value
        if (h.loading || h.loadingMore || h.endReached || h.items.isEmpty()) return
        loadPage(h.page + 1)
    }

    private fun loadPage(page: Int) {
        loadJob?.cancel()
        val vehicleId = _history.value.filterVehicle?.id

        _history.update {
            if (page == 0) it.copy(loading = true, loadingMore = false, error = null)
            else it.copy(loadingMore = true, error = null)
        }

        loadJob = viewModelScope.launch {
            val result = if (vehicleId == null) {
                PaymentService.list(page = page)
            } else {
                PaymentService.listByVehicle(vehicleId, page)
            }
            when (result) {
                is ApiResult.Success -> _history.update {
                    it.copy(
                        items = if (page == 0) result.data.content else it.items + result.data.content,
                        page = page,
                        endReached = result.data.last,
                        loading = false,
                        loadingMore = false,
                    )
                }
                is ApiResult.Error -> _history.update {
                    it.copy(loading = false, loadingMore = false, error = result.message)
                }
            }
        }
    }

    /* ------------------------- Collect dialog ------------------------- */

    fun openDialog(companyId: String) {
        _state.value = PaymentUiState(showDialog = true, loadingVehicles = true)
        viewModelScope.launch {
            when (val r = VehicleService.getByCompany(companyId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(vehicles = r.data, loadingVehicles = false)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(loadingVehicles = false, formError = r.message)
                }
            }
        }
    }

    fun dismiss() {
        pollJob?.cancel()
        _state.value = PaymentUiState()
        if (historyCompanyId != null) refresh()   // pick up any payment made in the dialog
    }

    fun onVehicleSelected(v: VehicleResponse) = _state.update { it.copy(selectedVehicle = v, formError = null) }
    fun onPhoneChanged(v: String) = _state.update { it.copy(phoneNumber = v.filter { c -> c.isDigit() || c == '+' }, formError = null) }
    fun onAmountChanged(v: String) = _state.update { it.copy(amount = v.filter { c -> c.isDigit() || c == '.' }, formError = null) }

    fun submit() {
        val s = _state.value
        val vehicle = s.selectedVehicle
        val amount = s.amount.toDoubleOrNull()

        val error = when {
            vehicle == null -> "Select a vehicle"
            s.phoneNumber.length < 10 -> "Enter a valid phone number"
            amount == null || amount < 1 -> "Amount must be at least 1"
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(formError = error) }
            return
        }

        _state.update { it.copy(submitting = true, formError = null) }
        viewModelScope.launch {
            when (val r = PaymentService.initiate(vehicle!!.id, s.phoneNumber, amount!!)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(submitting = false, phase = PaymentPhase.WAITING, payment = r.data)
                    }
                    startPolling(r.data.id)
                }
                is ApiResult.Error -> _state.update {
                    it.copy(submitting = false, formError = r.message)
                }
            }
        }
    }

    private fun startPolling(paymentId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            repeat(MAX_ATTEMPTS) { attempt ->
                delay(POLL_INTERVAL_MS)

                val result = if (attempt == SYNC_AT_1 || attempt == SYNC_AT_2) {
                    PaymentService.sync(paymentId)
                } else {
                    PaymentService.getById(paymentId)
                }

                if (result is ApiResult.Success && result.data.status != PaymentStatus.PENDING) {
                    finish(result.data)
                    return@launch
                }
            }
            _state.update {
                it.copy(
                    phase = PaymentPhase.FAILED,
                    resultMessage = "We didn't get a confirmation. If money was deducted, check the payments list shortly.",
                )
            }
        }
    }

    private fun finish(p: PaymentResponse) {
        val ok = p.status == PaymentStatus.SUCCESS
        _state.update {
            it.copy(
                payment = p,
                phase = if (ok) PaymentPhase.SUCCESS else PaymentPhase.FAILED,
                resultMessage = if (ok) null else (p.resultDesc ?: "Payment ${p.status.lowercase()}"),
            )
        }
        refresh()
    }

    fun retry() = _state.update {
        it.copy(phase = PaymentPhase.FORM, payment = null, resultMessage = null)
    }

    override fun onCleared() {
        pollJob?.cancel()
        loadJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 3_000L
        const val MAX_ATTEMPTS = 20
        const val SYNC_AT_1 = 9
        const val SYNC_AT_2 = 15
    }
}