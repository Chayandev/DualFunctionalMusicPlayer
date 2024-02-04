package com.example.cdmusicplayer.utils;

import android.app.Application;
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.cdmusicplayer.model.ApiData.Data

class MyApplication : Application() {


    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
        const val STOP_SERVICE = "com.example.cdmusicplayer.STOP_SERVICE"

        var selectedPosition: Int = -1
        var dataList: List<Data> = emptyList()
        var songFromWhichFragment:String="OnlineHome"
        var prevFragment:String="OnlineHome"
        var currFragment:String="OnlineHome"
        var currentPlaylist:String=""
        var previousPlaylist:String=""
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "Now Playing song",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "This is an important channel for showing song!"

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}
