package com.example.cdmusicplayer.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.model.MyData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkRepository {
    private val apiService: ApiServices
    private val musicList = MutableLiveData<List<Data>>()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiServices::class.java)
    }

    fun musicList(): LiveData<List<Data>> {
        return musicList
    }

    fun fetchMusicList(query: String) {
        val musicData = apiService.getData(query)

        musicData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        musicList.value = response.body()!!.data
                    }
                } else {
                    Log.e("Retrofit", "response unsuccessful")
                }
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                t.printStackTrace()
            }

        })
    }
}