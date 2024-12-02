package com.example.matchmakers.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Ensure the recommandation model is accessible from the Android device  (make it public)
    private const val BASE_URL = "https://recommand-sys-408256072995.us-central1.run.app"
    val api: ClusterApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClusterApiService::class.java)
    }
}