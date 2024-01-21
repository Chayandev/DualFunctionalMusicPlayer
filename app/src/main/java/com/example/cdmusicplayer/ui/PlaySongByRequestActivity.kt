package com.example.cdmusicplayer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.adapter.MusicAdapter
import com.example.cdmusicplayer.repository.ApiServices
import com.example.cdmusicplayer.utils.MediaPlayerManager
import com.example.cdmusicplayer.utils.MyApplication
import com.example.cdmusicplayer.model.MyData
import com.example.cdmusicplayer.databinding.ActivityPlaysongByRequestBinding
import com.example.cdmusicplayer.utils.MusicService
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PlaySongByRequestActivity : AppCompatActivity(),ServiceConnection {
    /*
    private lateinit var binding: ActivityPlaysongByRequestBinding
    private lateinit var musicAdapter: MusicAdapter
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var selectedPos: Int = -1
    private lateinit var songList: List<Data>
    private lateinit var dataList: List<Data>
    private var mediaPlayerManager: MediaPlayerManager = MediaPlayerManager.getInstance()
    private lateinit var receivedQuery: String
    private lateinit var uiFlag: String
    private var flag = false
    private var musicService:MusicService?=null
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Your custom logic here
          //  mediaPlayerManager.releaseMediaPlayer()
         MyApplication.prevActivity="PlaySongByRequestActivity"
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

// Make the activity full screen
        makeFullScreen()
        binding = ActivityPlaysongByRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, callback)


        // Get the Intent that started this activity
        // Retrieve the string data
        receivedQuery = intent.getStringExtra("QUERY").toString()
        uiFlag = intent.getStringExtra("FLAG").toString()
        //checkMediaPlayingAndSetUpBottomUi()
        // Initialize the MediaPlayer in the MediaPlayerManager singleton
      //  mediaPlayerManagerInitialize()
        buildRetrofit()
        // Example: Toggle visibility of PlayingBottomLL
        togglePlayingBottomLLVisibility()
        //starting service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this@PlaySongByRequestActivity, BIND_AUTO_CREATE)
        startService(intent)

        binding.playPauseBtn.setOnClickListener() {
            setUpTheOnImagePlayPauseBtn()
        }
    }

    private fun makeFullScreen() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpTheOnImagePlayPauseBtn() {
        Log.d("Tag:OnImagePlPsBtn", "In the FUnction call")
        if (flag && mediaPlayerManager.mediaPlayer != null) {
            if (mediaPlayerManager.mediaPlayer!!.isPlaying) {
                pauseMusic()
                binding.playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            } else {
                resumeMusic()
                binding.playPauseBtn.setImageResource(R.drawable.baseline_stop_24)
            }
            Log.d("Tag:OnImagePlPsBtn", "In the if block")
        } else {
            if (::dataList.isInitialized) {
                playSong(dataList, position = 0)
                binding.playPauseBtn.setImageResource(R.drawable.baseline_stop_24)
                Log.d("Tag:OnImagePlPsBtn", "In the else block")
            }
        }
    }

    private fun togglePlayingBottomLLVisibility() {
        val recyclerView = binding.recyclerView
        val musicPlayingLL = binding.playerBottomLL

        val params = recyclerView.layoutParams as ConstraintLayout.LayoutParams

        if (musicPlayingLL.visibility == View.VISIBLE) {
            // MusicPlayingLL is visible, set bottom constraint to the top of MusicPlayingLL
            params.bottomToTop = musicPlayingLL.id
        } else {
            // MusicPlayingLL is not visible, set bottom constraint to the bottom of parent
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        }
        recyclerView.layoutParams = params
    }


    private fun buildRetrofit() {
        //create retrofit Builder
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServices::class.java)
        searchGetAndDisplayDatA(retrofitBuilder, receivedQuery)
    }

    private fun searchGetAndDisplayDatA(retrofitBuilder: ApiServices, query: String) {
        val retrofitData = retrofitBuilder.getData(query)
        showLoading() // Show loading indicator before making the API call
        retrofitData.enqueue(object : Callback<MyData?> {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                dataList = response.body()?.data!!
                (application as MyApplication).songFromWhichActivity="PlaySongByRequestActivity"
                if (uiFlag == "A") {
                    setUpUiA(dataList[1].artist.name, dataList[1].artist.picture_big)
                } else {
                    setUpUiS()
                }
                hideLoading()
                Log.d("Tag:Albam items", "$dataList")
                // Pass a lambda expression as the higher-order function
                musicAdapter =
                    MusicAdapter(this@PlaySongByRequestActivity, dataList) { position, dataList ->
                        // Handle item click here
                        val selectedMusic = dataList[position]
                        playSong(dataList, position)
                    }

                binding.recyclerView.adapter = musicAdapter
                binding.recyclerView.layoutManager =
                    LinearLayoutManager(this@PlaySongByRequestActivity)

                Log.d("TAG : onResponse", "onResponse: " + response.body())
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Toast.makeText(this@PlaySongByRequestActivity, "Error Occurred", Toast.LENGTH_LONG)
                    .show()
                Log.d("TAG : onFailure", "onFailure: " + t.message)
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playSong(dataList: List<Data>, position: Int) {
        // Set or get shared data in MainActivity
        (application as MyApplication).selectedPosition = position
        (application as MyApplication).dataList = dataList
        //playing song on the bottom palyer
        flag = true
        updateUiOfBottomPlayer()
        togglePlayingBottomLLVisibility()
        binding.playPauseBtn.setImageResource(R.drawable.baseline_stop_24)
        musicAdapter.setSelectedPosition(position)
    }

    private fun setUpUiA(name: String, pictureBig: String) {
        binding.Name.text = name
        Picasso.get().load(pictureBig).into(binding.artistImg)
        binding.view.visibility = View.VISIBLE
        binding.playPauseBtn.visibility = View.VISIBLE
    }

    private fun setUpUiS() {
        binding.artistImg.setImageResource(R.drawable.headphone)
        binding.view.visibility = View.VISIBLE
        binding.playPauseBtn.visibility = View.VISIBLE
    }

    private fun setUpUiS(mName: String, aName: String, pictureBig: String) {
        binding.artistNameMusicNameLL.visibility = View.VISIBLE
        binding.artistId.text = aName
        binding.musicName.text = mName
        Picasso.get().load(pictureBig).into(binding.artistImg)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun mediaPlayerManagerInitialize(selectedSong: Data) {
        mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.initializeMediaPlayer()
        mediaPlayerManager.mediaPlayer?.setDataSource(this, selectedSong.preview.toUri())
        mediaPlayerManager.mediaPlayer?.prepareAsync()
        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
            playNextSong()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateUiOfBottomPlayer() {
        binding.playerBottomLL.visibility = View.VISIBLE
        selectedPos = (application as MyApplication).selectedPosition
        songList = (application as MyApplication).dataList
        val selectedSong = songList[selectedPos]

        //if activity came from the search of user
        if (uiFlag == "S") {
            setUpUiS(selectedSong.title, selectedSong.artist.name, selectedSong.album.cover_big)
        }//----------------------------------

        setMusicDetails(selectedSong)
        if (mediaPlayerManager.mediaPlayer != null && mediaPlayerManager.mediaPlayer!!.isPlaying) {
            mediaPlayerManager.releaseMediaPlayer()
        }
        mediaPlayerManagerInitialize(selectedSong)
        //  val mediaPlayerManager = MediaPlayerManager.getInstance()
//        mediaPlayerManager.initializeMediaPlayer()
//        mediaPlayerManager.mediaPlayer?.setDataSource(this, selectedSong.preview.toUri())
//        mediaPlayerManager.mediaPlayer?.prepareAsync()
        Log.d("Tag:UpdateBottomUi after setMusic", "Hello bottom middle")
        setUpSeekBar()
        updateSeekBarProgress()
        setUpPlayPauseButton()

        binding.playerBottomLL.setOnClickListener {
            //musicAdapter.setSelectedPosition(position) r
         //     val intent = Intent(this@PlaySongByRequestActivity, PlayingMusicActivity::class.java)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setMusicDetails(selectedSong: Data) {
        musicService?.showNotification()
        binding.musicTitle.text = selectedSong.title
        binding.artistName.text = selectedSong.artist.name
        Log.d("Tag_artistName in bottom LL", selectedSong.artist.name)
        Picasso.get().load(selectedSong.album.cover_big).into(binding.musicImg)
        binding.ProgressSeekbar.progress = 0.0F
    }


    private fun setUpSeekBar() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.setOnPreparedListener {
            binding.ProgressSeekbar.max = mediaPlayerManager.mediaPlayer!!.duration.toFloat()
            mediaPlayerManager.mediaPlayer?.start()
            binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
        }
    }

    private fun updateSeekBarProgress() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // val mediaPlayerManager = MediaPlayerManager.getInstance()
                mediaPlayerManager.mediaPlayer?.let {
                    if (it.isPlaying) {
                        binding.ProgressSeekbar.progress = it.currentPosition.toFloat()
                    }
                }
                handler.postDelayed(this, 10)
            }
        }, 0)
//        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
//            playNextSong()
//        }
    }

    private fun setUpPlayPauseButton() {
        binding.playPaushBtn.setOnClickListener {
            //   val mediaPlayerManager = MediaPlayerManager.getInstance()
            if (mediaPlayerManager.mediaPlayer?.isPlaying == true) {
                pauseMusic()
            } else {
                resumeMusic()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playNextSong() {
        if (selectedPos != -1 && selectedPos < songList.size - 1) {
            mediaPlayerManager.releaseMediaPlayer()
            (application as MyApplication).selectedPosition += 1
            musicAdapter.setSelectedPosition((application as MyApplication).selectedPosition)
            updateUiOfBottomPlayer()
        } else {
            // Handle the case where the last song has finished
            // You may want to loop back to the first song or take some other action
            // For now, let's just stop the MediaPlayer
            mediaPlayerManager.releaseMediaPlayer()
        }
    }


    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    private fun resumeMusic() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.start()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
        binding.playPauseBtn.setImageResource(R.drawable.baseline_stop_24)
    }

    private fun pauseMusic() {
        //   val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.pause()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_play_arrow_24)
        binding.playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (::musicAdapter.isInitialized) {
            if (mediaPlayerManager.mediaPlayer != null) {
                musicAdapter.setSelectedPosition((application as MyApplication).selectedPosition)
                updateActivityResumeData()
            } else {
                musicAdapter.setSelectedPosition(RecyclerView.NO_POSITION)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateActivityResumeData() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        binding.playerBottomLL.visibility = View.VISIBLE
        selectedPos = (application as MyApplication).selectedPosition

        if (selectedPos != -1) {
            val selectedSong = (application as MyApplication).dataList[selectedPos]
            setMusicDetails(selectedSong)
            binding.ProgressSeekbar.progress =
                mediaPlayerManager.mediaPlayer?.currentPosition!!.toFloat()
            binding.ProgressSeekbar.max = mediaPlayerManager.mediaPlayer!!.duration.toFloat()
            if (mediaPlayerManager.mediaPlayer!!.isPlaying) {
                binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
                //   updateSeekBarProgress()
            } else {
                binding.playPaushBtn.setImageResource(R.drawable.baseline_play_arrow_24)
            }
            mediaPlayerManager.mediaPlayer!!.setOnCompletionListener {
                playNextSong()
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
     //   mediaPlayerManager.releaseMediaPlayer()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService=null
    }
    */
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
       //
    }

    override fun onServiceDisconnected(name: ComponentName?) {
       //
    }

}