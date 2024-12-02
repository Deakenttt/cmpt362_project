package com.example.matchmakers.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://recommand-sys-408256072995.us-central1.run.app"

    // Create a Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Provide the API service
    val api: ClusterApiService = retrofit.create(ClusterApiService::class.java)
}