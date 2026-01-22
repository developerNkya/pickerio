package com.example.pickerio.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

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
    private const val BASE_URL = "https://versioner.gamer.gd/"

    val api: VersionApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VersionApiService::class.java)
    }
}
