package com.example.pickerio.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient

interface VersionApiService {
    @GET("version")
    suspend fun checkVersion(
        @Query("app") app: String,
        @Query("version") version: String
    ): VersionApiResponse
}

data class VersionApiResponse(
    val status: String,
    val message: String,
    val download_url: String?,
    val latest_version: String
)

object VersionNetworkModule {
    private const val BASE_URL = "https://v0-pickerio.vercel.app/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Pickerio-Android/1.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: VersionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VersionApiService::class.java)
    }
}
