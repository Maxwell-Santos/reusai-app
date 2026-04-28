package com.example.reusai.data.network

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ReusaiApiService {
    @POST("item")
    suspend fun createItem(@Body item: ItemRequest): ItemResponse

    @Multipart
    @POST("item/upload-image")
    suspend fun uploadImage(@Part image: MultipartBody.Part): UploadImageResponse

    @PUT("item/{itemId}")
    suspend fun updateItem(@Path("itemId") itemId: String, @Body item: ItemRequest)
}

object RetrofitClient {
    // 10.0.2.2 is the special IP for Android Emulator to access localhost of the host machine
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BASE_URL = "http://192.168.15.7:8080/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val instance: ReusaiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ReusaiApiService::class.java)
    }
}
