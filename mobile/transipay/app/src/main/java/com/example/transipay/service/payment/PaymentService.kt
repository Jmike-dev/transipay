package com.example.transipay.service.payment

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class InitiatePaymentRequest(
    val vehicleId: String,
    val phoneNumber: String,
    val amount: Double,
)

// status kept as String so a new backend status never crashes Gson parsing
data class PaymentResponse(
    val id: String,
    val vehicleId: String,
    val vehicleCode: String,
    val numberPlate: String,
    val phoneNumber: String,
    val amount: Double,
    val status: String,
    val checkoutRequestId: String?,
    val mpesaReceiptNumber: String?,
    val resultCode: Int?,
    val resultDesc: String?,
    val transactionDate: String?,
    val createdAt: String?,
)

/** Mirrors Spring's Page<T> */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val last: Boolean,
)

object PaymentStatus {
    const val PENDING = "PENDING"
    const val SUCCESS = "SUCCESS"
}

interface PaymentApi {
    @POST("api/payments")
    suspend fun initiate(@Body body: InitiatePaymentRequest): PaymentResponse

    @GET("api/payments/{id}")
    suspend fun getById(@Path("id") id: String): PaymentResponse

    @POST("api/payments/{id}/sync")
    suspend fun sync(@Path("id") id: String): PaymentResponse

    @GET("api/payments")
    suspend fun list(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponse<PaymentResponse>

    @GET("api/vehicles/{vehicleId}/payments")
    suspend fun listByVehicle(
        @Path("vehicleId") vehicleId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PageResponse<PaymentResponse>
}

object PaymentService {
    private val api = ApiClient.create<PaymentApi>()

    suspend fun initiate(vehicleId: String, phoneNumber: String, amount: Double): ApiResult<PaymentResponse> =
        safeApiCall { api.initiate(InitiatePaymentRequest(vehicleId, phoneNumber, amount)) }

    suspend fun getById(id: String): ApiResult<PaymentResponse> =
        safeApiCall { api.getById(id) }

    suspend fun sync(id: String): ApiResult<PaymentResponse> =
        safeApiCall { api.sync(id) }

    suspend fun list(status: String? = null, page: Int = 0): ApiResult<PageResponse<PaymentResponse>> =
        safeApiCall { api.list(status, page) }

    suspend fun listByVehicle(vehicleId: String, page: Int = 0): ApiResult<PageResponse<PaymentResponse>> =
        safeApiCall { api.listByVehicle(vehicleId, page) }
}