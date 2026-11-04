package com.example.transipay.service.company

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

// Like declaring your axios endpoints
interface CompanyApi {

    @GET("/api/companies")
    suspend fun getCompanies(): List<Company>

    @POST("/api/companies")
    suspend fun createCompany(@Body request: CreateCompanyRequest): Company

    @PUT("/api/companies")
    suspend fun updateCompany(@Body request: UpdateCompanyRequest): Company
}

// Like a services/companyService.ts file
object CompanyService {

    private val api = ApiClient.create<CompanyApi>()

    suspend fun getCompanies(): ApiResult<List<Company>> =
        safeApiCall { api.getCompanies() }

    suspend fun createCompany(companyName: String): ApiResult<Company> =
        safeApiCall { api.createCompany(CreateCompanyRequest(companyName)) }

    suspend fun updateCompany(id: String, companyName: String): ApiResult<Company> =
        safeApiCall { api.updateCompany(UpdateCompanyRequest(id, companyName)) }
}