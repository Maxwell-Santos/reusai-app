package com.example.reusai.data.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ItemsApi {
    @GET("item")
    suspend fun getItems(): List<ItemResponse>

    @POST("item")
    suspend fun createItem(@Body item: ItemRequest): ItemResponse

    @GET("item/{itemId}")
    suspend fun getItem(@Path("itemId") itemId: String): ItemResponse

    @PUT("item/{itemId}")
    suspend fun updateItem(@Path("itemId") itemId: String, @Body item: ItemRequest)

    @DELETE("item/{itemId}")
    suspend fun deleteItem(@Path("itemId") itemId: String)

    @GET("item/user/{userId}")
    suspend fun getItemsByUser(@Path("userId") userId: String): List<ItemResponse>
}

interface AuthApi {
    @POST("auth/signin")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse

    @PUT("auth/refresh/{username}")
    suspend fun refreshToken(@Path("username") username: String): AuthResponse

    @POST("logout")
    suspend fun logout()
}

interface UserApi {
    @POST("user")
    suspend fun createUser(@Body user: UserRequest): UserResponse
}

interface ImageApi {
    @Multipart
    @POST("upload-image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadImageResponse
}

interface ReusaiApiService : ItemsApi, AuthApi, UserApi, ImageApi

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val path = originalRequest.url.encodedPath
        if (!path.contains("auth/signin")) {
            tokenManager.getAccessToken()?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.15.7:8080/"
    private var tokenManager: TokenManager? = null

    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }

    fun getTokenManager(): TokenManager? = tokenManager

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
        
        tokenManager?.let {
            builder.addInterceptor(AuthInterceptor(it))
        }
        
        builder.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    val instance: ReusaiApiService by lazy { retrofit.create(ReusaiApiService::class.java) }
}
