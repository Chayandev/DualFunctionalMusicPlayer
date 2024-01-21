package com.example.cdmusicplayer.repository

import com.example.cdmusicplayer.model.MyData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiServices {
    @Headers("X-RapidAPI-Key: 68a1104f7emsh4fff69afe79840fp1b1032jsna2b5ee2a9694",
        "X-RapidAPI-Host: deezerdevs-deezer.p.rapidapi.com")
    @GET("search")
    fun getData(@Query("q") query: String): Call<MyData>
}