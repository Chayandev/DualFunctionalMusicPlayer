package com.example.cdmusicplayer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.repository.NetworkRepository


class MusicViewModel:ViewModel(){
    private val repository=NetworkRepository()

    fun loadMusicList(query: String):LiveData<List<Data>>{
        repository.fetchMusicList(query)
        return repository.musicList()
    }
}