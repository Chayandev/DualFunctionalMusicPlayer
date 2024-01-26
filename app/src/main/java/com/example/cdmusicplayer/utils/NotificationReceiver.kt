package com.example.cdmusicplayer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.cdmusicplayer.ui.fragments.OnlineMusicHomeFragment
import com.example.cdmusicplayer.ui.fragments.PlayingMusicBottomSheetFragment


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

        } else {
            OnlineMusicHomeFragment.getInstance().resumeMusic()

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updatePreviousClick() {
        Log.d("NotificationReciver","current Position ${MyApplication.selectedPosition}")
        val position=--MyApplication.selectedPosition
        OnlineMusicHomeFragment.getInstance().playNextSong(position)
        val playingMusicBottomSheetFragment=PlayingMusicBottomSheetFragment.getInstance()
        if(playingMusicBottomSheetFragment.isAdded && playingMusicBottomSheetFragment.activity!=null){
            playingMusicBottomSheetFragment.changeMusicOnBackward(position)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateNextClick() {
        val position=++MyApplication.selectedPosition
        OnlineMusicHomeFragment.getInstance().playNextSong(position)
        val playingMusicBottomSheetFragment=PlayingMusicBottomSheetFragment.getInstance()
        if(playingMusicBottomSheetFragment.isAdded && playingMusicBottomSheetFragment.activity!=null){
            playingMusicBottomSheetFragment.changeMusicOnForward(position)
        }
    }

}