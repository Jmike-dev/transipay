
package com.example.transipay.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

//const val BASE_URL = "https://transipay-develop.up.railway.app"
const val BASE_URL = "http://localhost:8080"

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null
    ) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> =
    withContext(Dispatchers.IO) {
        try {
            ApiResult.Success(call())
        } catch (e: HttpException) {
            // Server responded with 4xx / 5xx
            val body = e.response()?.errorBody()?.string()
            ApiResult.Error(
                message = if (!body.isNullOrBlank()) body else e.message(),
                code = e.code()
            )
        } catch (e: IOException) {
            // No internet, timeout, DNS failure, etc.
            ApiResult.Error("Network error. Check your connection.")
        } catch (e: Exception) {
            ApiResult.Error(e.localizedMessage ?: "Something went wrong")
        }
    }

/* ---------------------------------------------------------------
 * 3. Retrofit client (singleton)
 * --------------------------------------------------------------- */
object ApiClient {

    @Volatile
    var authToken: String? = null

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            authToken?.let { addHeader("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // switch to NONE in release builds
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /** Create any API interface: ApiClient.create<AuthApi>() */
    fun <T> create(service: Class<T>): T = retrofit.create(service)

    inline fun <reified T> create(): T = create(T::class.java)
}

