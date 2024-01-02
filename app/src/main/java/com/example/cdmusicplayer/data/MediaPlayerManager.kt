package com.example.cdmusicplayer.data

import android.media.MediaPlayer

class MediaPlayerManager private constructor() {

    companion object {
        private var instance: MediaPlayerManager? = null

        fun getInstance(): MediaPlayerManager {
            if (instance == null) {
                instance = MediaPlayerManager()
            }
            return instance as MediaPlayerManager
        }
    }

    var mediaPlayer: MediaPlayer? = null
        private set

    fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer()
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
