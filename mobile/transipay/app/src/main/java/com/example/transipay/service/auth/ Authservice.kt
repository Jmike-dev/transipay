package com.example.transipay.service.auth

import com.example.transipay.service.ApiClient
import com.example.transipay.service.ApiResult
import com.example.transipay.service.safeApiCall
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

data class GoogleLoginRequest(val idToken: String)
data class PasswordLoginRequest(val email: String, val password: String)
data class SetPasswordRequest(val password: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

data class AuthResponse(
    val accessToken: String,
    val expiresInSeconds: Long,
    val staffId: String,
    val companyId: String,
    val role: String,
) {
    val roleEnum: Role get() = runCatching { Role.valueOf(role) }.getOrDefault(Role.STAFF)
}
data class RegisterRequest(
    val companyId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String? = null,
    val password: String,
) {
    override fun toString() = "RegisterRequest(email=$email)"
}

interface AuthApi {
    @POST("auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: PasswordLoginRequest): AuthResponse

    // 204 No Content, requires Bearer token
    @POST("auth/password")
    suspend fun createPassword(@Body body: SetPasswordRequest)

    // 204 No Content, requires Bearer token
    @PUT("auth/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest)

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse
}

object AuthService {
    private val api = ApiClient.create<AuthApi>()

    suspend fun googleLogin(idToken: String): ApiResult<AuthResponse> =
        safeApiCall { api.googleLogin(GoogleLoginRequest(idToken)) }

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
        safeApiCall { api.login(PasswordLoginRequest(email, password)) }

    suspend fun createPassword(password: String): ApiResult<Unit> =
        safeApiCall { api.createPassword(SetPasswordRequest(password)) }

    suspend fun changePassword(current: String, new: String): ApiResult<Unit> =
        safeApiCall { api.changePassword(ChangePasswordRequest(current, new)) }

    suspend fun register(req: RegisterRequest): ApiResult<AuthResponse> =
        safeApiCall { api.register(req) }
}