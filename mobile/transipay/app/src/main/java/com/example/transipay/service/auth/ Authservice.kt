package com.example.transipay.service.auth

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.POST

data class GoogleLoginRequest(val idToken: String)

// TODO: match this to the real response of POST /auth/google
// (check the Logcat body logged by HttpLoggingInterceptor). Both common names are covered.
data class AuthResponse(
    val accessToken: String? = null,
    val token: String? = null,
) {
    val bearer: String? get() = accessToken ?: token
}

interface AuthApi {
    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): AuthResponse
}

object AuthService {
    private val api = ApiClient.create<AuthApi>()

    suspend fun googleLogin(idToken: String): ApiResult<AuthResponse> =
        safeApiCall { api.googleLogin(GoogleLoginRequest(idToken)) }
}