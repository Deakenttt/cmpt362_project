package com.example.matchmakers.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST

interface ClusterApiService {

    @POST("/update_clusters")
    fun updateClusters(): Call<Void>
}