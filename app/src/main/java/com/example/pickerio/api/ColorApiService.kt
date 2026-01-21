package com.example.pickerio.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ColorApiService {
    @GET("id")
    suspend fun getColor(@Query("hex") hex: String): ColorApiResponse
}

data class ColorApiResponse(
    val name: ColorName,
    val image: ColorImage,
    // We can add more fields as needed, but this covers the requirements
)

data class ColorName(
    val value: String,
    val closest_named_hex: String,
    val exact_match_name: Boolean,
    val distance: Int
)

data class ColorImage(
    val bare: String,
    val named: String
)

object NetworkModule {
    private const val BASE_URL = "https://www.thecolorapi.com/"

    val api: ColorApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ColorApiService::class.java)
    }
}
