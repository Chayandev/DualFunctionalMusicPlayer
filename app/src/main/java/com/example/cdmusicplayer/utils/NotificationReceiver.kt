package com.example.cdmusicplayer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cdmusicplayer.ui.fragments.OnlineMusicHomeFragment
import com.example.cdmusicplayer.ui.fragments.PlayingMusicBottomSheetFragment
import kotlin.system.exitProcess


class NotificationReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

            MyApplication.NEXT -> {
                updateNextClick()
            }

            MyApplication.PREVIOUS -> {
                updatePreviousClick()
            }

            MyApplication.PLAY -> {
                updatePlayaPauseClick()
            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updatePlayaPauseClick() {
        if (MediaPlayerManager.getInstance().mediaPlayer?.isPlaying == true) {
            OnlineMusicHomeFragment.getInstance().pauseMusic()
            try {
                PlayingMusicBottomSheetFragment.getInstance().pauseMusic()
            } catch (e: Exception) {
                Log.e("Exception", "Fragment is not created")
            }
        } else {
            OnlineMusicHomeFragment.getInstance().resumeMusic()
            try {
                PlayingMusicBottomSheetFragment.getInstance().resumeMusic()
            } catch (e: Exception) {
                Log.e("Exception", "Fragment is not created")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updatePreviousClick() {
        val currentPos = MyApplication.selectedPosition - 2
        OnlineMusicHomeFragment.getInstance().playNextSong(currentPos)
        try {
            PlayingMusicBottomSheetFragment.getInstance().playNewMusic(currentPos)
        } catch (e: Exception) {
            Log.e("Exception", "Fragment is not created")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateNextClick() {
        OnlineMusicHomeFragment.getInstance().playNextSong(MyApplication.selectedPosition)
        try {
            PlayingMusicBottomSheetFragment.getInstance()
                .playNewMusic(MyApplication.selectedPosition)
        } catch (e: Exception) {
            Log.e("Exception", "Fragment is not created")
        }
    }

}