package com.example.transipay.service.staff

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class StaffRequest(
    val companyId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val role: String = "STAFF",   // "ADMIN" or "STAFF"
    val active: Boolean = true,
    val password: String? = null, // initial password, create only
) {
    // keep the password out of Logcat if this object is ever printed
    override fun toString() = "StaffRequest(email=$email, role=$role)"
}

data class StaffResponse(
    val id: String,
    val companyId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

interface StaffApi {
    @POST("api/staff")
    suspend fun create(@Body body: StaffRequest): StaffResponse

    @GET("api/staff")
    suspend fun list(): List<StaffResponse>

    @GET("api/staff/{id}")
    suspend fun get(@Path("id") id: String): StaffResponse
}

object StaffService {
    private val api = ApiClient.create<StaffApi>()

    suspend fun create(req: StaffRequest): ApiResult<StaffResponse> =
        safeApiCall { api.create(req) }

    suspend fun list(): ApiResult<List<StaffResponse>> =
        safeApiCall { api.list() }

    suspend fun get(id: String): ApiResult<StaffResponse> =
        safeApiCall { api.get(id) }
}