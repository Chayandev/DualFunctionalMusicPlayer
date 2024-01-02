package com.example.cdmusicplayer.data;

import android.app.Application;
import android.media.MediaPlayer
import com.example.cdmusicplayer.ApiData.Data

class MyApplication : Application(){
    var selectedPosition: Int = -1
    var dataList: List<Data> = emptyList()
}
