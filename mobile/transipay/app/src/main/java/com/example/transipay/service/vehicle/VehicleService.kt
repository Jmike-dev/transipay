package com.example.transipay.service.vehicle

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class CreateVehicleRequest(val numberPlate: String, val companyId: String)
data class UpdateVehicleRequest(val numberPlate: String)

data class VehicleResponse(
    val id: String,
    val code: String,
    val numberPlate: String,
    val companyId: String,
)

interface VehicleApi {
    @POST("api/vehicles")
    suspend fun create(@Body body: CreateVehicleRequest): VehicleResponse

    @GET("api/companies/{companyId}/vehicles")
    suspend fun getByCompany(@Path("companyId") companyId: String): List<VehicleResponse>

    @PUT("api/vehicles/{id}")
    suspend fun update(@Path("id") id: String, @Body body: UpdateVehicleRequest): VehicleResponse
}

object VehicleService {
    private val api = ApiClient.create<VehicleApi>()

    suspend fun create(numberPlate: String, companyId: String): ApiResult<VehicleResponse> =
        safeApiCall { api.create(CreateVehicleRequest(numberPlate, companyId)) }

    suspend fun getByCompany(companyId: String): ApiResult<List<VehicleResponse>> =
        safeApiCall { api.getByCompany(companyId) }

    suspend fun update(id: String, numberPlate: String): ApiResult<VehicleResponse> =
        safeApiCall { api.update(id, UpdateVehicleRequest(numberPlate)) }
}