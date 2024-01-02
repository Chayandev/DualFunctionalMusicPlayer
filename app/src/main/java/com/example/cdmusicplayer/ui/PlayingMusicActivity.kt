package com.example.cdmusicplayer.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.core.net.toUri
import com.example.cdmusicplayer.ApiData.Data
import com.example.cdmusicplayer.data.MediaPlayerManager
import com.example.cdmusicplayer.data.MyApplication
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.databinding.ActivityPlayingMusicBinding
import com.squareup.picasso.Picasso

class PlayingMusicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayingMusicBinding
    private var selectedPosition: Int = -1
    private lateinit var dataList: List<Data>
    private var isFav: Boolean = false
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var mediaPlayerManager: MediaPlayerManager = MediaPlayerManager.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayingMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

//-------------------------------------------------------------------------------------------------------------------
    //setting the window
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            window.setDecorFitsSystemWindows(false)
//        } else {
//            window.setFlags(
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            )
//        }
//-------------------------------------------------------------------------------------------------------------------
        // Access shared data in PlayingMusicActivity
        accessSharedData()
        binding.backwardBtn.setOnClickListener {
            if (selectedPosition != 0) {
                mediaPlayerManager.releaseMediaPlayer()
                (application as MyApplication).selectedPosition -= 1
                setNewSharedData()
            } else {
                binding.backwardBtn.setImageResource(R.drawable.previous_trashparent)
            }
        }
        binding.forwardBtn.setOnClickListener {
            if (selectedPosition != dataList.size - 1) {
                mediaPlayerManager.releaseMediaPlayer()
                (application as MyApplication).selectedPosition += 1
                setNewSharedData()
            } else {
                binding.forwardBtn.setImageResource(R.drawable.next_transhparent)
            }
        }
        mediaPlayerManager.mediaPlayer!!.setOnCompletionListener{
            playNewMusic()
        }
    }

    private fun playNewMusic() {
        if (selectedPosition < dataList.size - 1) {
            mediaPlayerManager.releaseMediaPlayer()
            (application as MyApplication).selectedPosition += 1
            setNewSharedData()
        } else {
            // Handle the case where the last song has finished
            // You may want to loop back to the first song or take some other action
            // For now, let's just stop the MediaPlayer
            mediaPlayerManager.releaseMediaPlayer()
        }
    }

    private fun setNewSharedData() {
        selectedPosition = (application as MyApplication).selectedPosition
        val selectedSong = dataList[selectedPosition]
        mediaPlayerManager.initializeMediaPlayer()
        mediaPlayerManager.mediaPlayer?.setDataSource(this, selectedSong.preview.toUri())
        mediaPlayerManager.mediaPlayer?.prepareAsync()
        mediaPlayerManager.mediaPlayer?.setOnPreparedListener {
            setUpUI(selectedPosition)
            setNewSeekBar()
            setUpPlayPauseButton()
            updateSeekBarProgress()
        }
    }

    private fun setNewSeekBar() {
        binding.seekBar.max = mediaPlayerManager.mediaPlayer!!.duration
        binding.totalTime.text = convertDuration(mediaPlayerManager.mediaPlayer!!.duration.toLong())
        mediaPlayerManager.mediaPlayer?.start()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
        checkUserChangeListner()
    }

    private fun accessSharedData() {
        selectedPosition = (application as MyApplication).selectedPosition
        dataList = (application as MyApplication).dataList
        if (selectedPosition != -1 && dataList.isNotEmpty()) {
            setUpUI(selectedPosition)
        }
        setUpSeekBar()
        setUpPlayPauseButton()
        updateSeekBarProgress()
    }

    private fun setUpUI(selectedPosition: Int) {
        val selectedSong = dataList[selectedPosition]
        binding.songTitle.text = selectedSong.title
        binding.singer.text = selectedSong.artist.name
        binding.singer.setHorizontallyScrolling(true)
        binding.singer.isSelected = true
        Picasso.get().load(selectedSong.album.cover_big).into(binding.musicImg)

        if (selectedPosition == 0) {
            binding.backwardBtn.setImageResource(R.drawable.previous_trashparent)
        } else {
            binding.backwardBtn.setImageResource((R.drawable.previous))
        }

        if (selectedPosition == dataList.size - 1) {
            binding.forwardBtn.setImageResource(R.drawable.next_transhparent)
        } else {
            binding.forwardBtn.setImageResource(R.drawable.next)
        }

        binding.addFav.setOnClickListener {
            isFav = if (!isFav) {
                binding.addFav.setImageResource(R.drawable.favorite)
                true
            } else {
                binding.addFav.setImageResource(R.drawable.baseline_favorite_border_24)
                false
            }
        }
//        mediaPlayer = MediaPlayer()
//        mediaPlayer?.setDataSource(this, selectedSong.preview.toUri())
//        mediaPlayer?.prepareAsync()

        // Set up UI components
//        setUpSeekBar()
//        setUpPlayPauseButton()
//        // Update seek bar progress and time text views
//        updateSeekBarProgress()
    }

    private fun setUpSeekBar() {
        if (mediaPlayerManager.mediaPlayer != null) {
            binding.seekBar.max = mediaPlayerManager.mediaPlayer!!.duration
            binding.totalTime.text = convertDuration(mediaPlayerManager.mediaPlayer!!.duration.toLong())
            if (mediaPlayerManager.mediaPlayer!!.isPlaying) {
                binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
            } else {
                binding.playPaushBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
            checkUserChangeListner()
        } else {
            // Handle the case where mediaPlayer is null (initialize or show an error)
        }
    }


    private fun checkUserChangeListner() {
        binding.seekBar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: android.widget.SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayerManager.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Not needed for this example
            }

            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {
                // Not needed for this example
            }
        })
    }

    private fun setUpPlayPauseButton() {
        binding.playPaushBtn.setOnClickListener {
            if (mediaPlayerManager.mediaPlayer?.isPlaying == true) {
                pauseMusic()
            } else {
                resumeMusic()
            }
        }
    }

    private fun updateSeekBarProgress() {
        binding.seekBar.progress=mediaPlayerManager.mediaPlayer!!.currentPosition
        binding.runTime.text = convertDuration(mediaPlayerManager.mediaPlayer!!.currentPosition.toLong())
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayerManager.mediaPlayer?.let {
                    if (it.isPlaying) {
                        binding.seekBar.progress = it.currentPosition
                        binding.runTime.text = convertDuration(it.currentPosition.toLong())
                    }
                }
                handler.postDelayed(this, 10)
            }
        }, 0)
    }

    private fun convertDuration(durationInMillis: Long): String {
        val totalSeconds = durationInMillis / 1000

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun resumeMusic() {
        mediaPlayerManager.mediaPlayer?.start()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
    }

    private fun pauseMusic() {
        mediaPlayerManager.mediaPlayer?.pause()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_play_arrow_24)
    }

    override fun onDestroy() {
        super.onDestroy()
      //  mediaPlayerManager.releaseMediaPlayer()
        handler.removeCallbacksAndMessages(null)
    }
}