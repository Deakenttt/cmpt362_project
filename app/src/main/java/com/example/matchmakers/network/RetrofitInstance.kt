package com.example.matchmakers.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Ensure the Flask server is accessible from the Android device  (make it public)
    private const val BASE_URL = "http://0.0.0.0:5000"
    val api: ClusterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClusterApiService::class.java)
    }
}