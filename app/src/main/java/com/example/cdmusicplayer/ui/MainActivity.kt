package com.example.cdmusicplayer.ui


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cdmusicplayer.ApiData.Data
import com.example.cdmusicplayer.data.ApiInterface
import com.example.cdmusicplayer.data.MediaPlayerManager
import com.example.cdmusicplayer.adapter.MusicAdapter
import com.example.cdmusicplayer.data.MyApplication
import com.example.cdmusicplayer.data.MyData
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.adapter.FamousArtistAdapter
import com.example.cdmusicplayer.data.FamousArtistDataManager
import com.example.cdmusicplayer.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var famousArtistAdapter: FamousArtistAdapter
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var selectedPos: Int = -1
    private lateinit var songList: List<Data>
    private lateinit var mediaPlayerManager: MediaPlayerManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        networkCheckAndProceed()
        checkForSearchResult()
    }

    private fun checkForSearchResult() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Tag:OnSUbmitpressKyeboard","Search Pressed")
               query?.takeIf { it.isNotBlank() }?.let { handelQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun handelQuery(it: String) {
        if (isNetworkAvailable()) {
            val intent = Intent(this, PlaySongByRequestActivity::class.java)

            // Put the string data into the Intent
            intent.putExtra("QUERY", it)
            intent.putExtra("FLAG", "S")
            mediaPlayerManager.releaseMediaPlayer()
            handler.removeCallbacksAndMessages(null)
            binding.playerBottomLL.visibility = View.GONE
            // Start the new activity
            startActivity(intent)
        } else {
            showSnackBar()
        }
    }

    private fun networkCheckAndProceed() {
        // Check network connectivity
        showLoading()
        if (isNetworkAvailable()) {
            binding.noInternetConnectionLL.visibility = View.GONE
            binding.availableInternetLL.visibility = View.VISIBLE
            mediaPlayerManagerInitialize()
            famousArtistRecyclerViewSetUp()
            buildRetrofit()
        } else {
            hideLoading()
            // No network available, handle accordingly (e.g., show a message to the user)
            binding.noInternetConnectionLL.visibility = View.VISIBLE
            binding.availableInternetLL.visibility = View.GONE
            binding.tryAgainBtn.setOnClickListener {
                networkCheckAndProceed()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun buildRetrofit() {
        //create retrofit Builder
        val retrofitBuilder = Retrofit.Builder()
            .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
        searchGetAndDisplayDatA(retrofitBuilder, "Famous Songs")
    }

    private fun searchGetAndDisplayDatA(retrofitBuilder: ApiInterface, query: String) {
        val retrofitData = retrofitBuilder.getData(query)
        showLoading() // Show loading indicator before making the API call
        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                val dataList = response.body()?.data!!
                hideLoading()
                Log.d("Tag:Albam items", "$dataList")
                if (dataList.isNotEmpty())
                    playMusic(dataList)
            }

            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error Occurred", Toast.LENGTH_LONG).show()
                Log.d("TAG : onFailure", "onFailure: " + t.message)
            }

        })
    }

    private fun playMusic(dataList: List<Data>) {
        // Pass a lambda expression as the higher-order function
        musicAdapter = MusicAdapter(this@MainActivity, dataList) { position, dataList ->
            // Handle item click here
            if (isNetworkAvailable()) {
                val selectedMusic = dataList[position]
                // Set or get shared data in MainActivity
                (application as MyApplication).selectedPosition = position
                (application as MyApplication).dataList = dataList
                //playing song on the bottom palyer
                updateUiOfBottomPlayer()
                // Inside your MainActivity
                musicAdapter.setSelectedPosition(position)
            } else {
                showSnackBar()
            }
        }

        binding.recyclerView.adapter = musicAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        // Log.d("TAG : onResponse", "onResponse: " + response.body())
    }

    private fun showSnackBar() {
        val snackbar = Snackbar.make(
            binding.mainActivityId, "No internet",
            Snackbar.LENGTH_SHORT
        ).setAction("Action", null)
        snackbar.show()
    }


    private fun mediaPlayerManagerInitialize() {
        mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.initializeMediaPlayer()
    }

    private fun famousArtistRecyclerViewSetUp() {
        val artistData = FamousArtistDataManager.famousArtistData
        famousArtistAdapter = FamousArtistAdapter(this@MainActivity, artistData) { position ->
            // Create an Intent
            if (isNetworkAvailable()) {
                val intent = Intent(this, PlaySongByRequestActivity::class.java)

                // Put the string data into the Intent
                intent.putExtra("QUERY", artistData[position].name)
                intent.putExtra("FLAG", "A")
                mediaPlayerManager.releaseMediaPlayer()
                handler.removeCallbacksAndMessages(null)
                binding.playerBottomLL.visibility = View.GONE
                // Start the new activity
                startActivity(intent)
                // mediaPlayerManager.releaseMediaPlayer()
                //handler.removeCallbacksAndMessages(null)
            } else {
                showSnackBar()
            }
        }
        binding.famousArtistRcView.adapter = famousArtistAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(this@MainActivity, 1)
    }

    private fun updateUiOfBottomPlayer() {
        binding.playerBottomLL.visibility = View.VISIBLE
        selectedPos = (application as MyApplication).selectedPosition
        songList = (application as MyApplication).dataList
        val selectedSong = songList[selectedPos]

        setMusicDetails(selectedSong)
        if (mediaPlayerManager.mediaPlayer != null && mediaPlayerManager.mediaPlayer != null && mediaPlayerManager.mediaPlayer!!.isPlaying) {
            mediaPlayerManager.releaseMediaPlayer()
        }
        //  val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.initializeMediaPlayer()
        mediaPlayerManager.mediaPlayer?.setDataSource(this, selectedSong.preview.toUri())
        mediaPlayerManager.mediaPlayer?.prepareAsync()
        Log.d("Tag:UpdateBottomUi after setMusic", "Hello bottom middle")
        setUpSeekBar()
        updateSeekBarProgress()
        setUpPlayPauseButton()

        binding.playerBottomLL.setOnClickListener {
            //musicAdapter.setSelectedPosition(position) r
            val intent = Intent(this@MainActivity, PlayingMusicActivity::class.java)
            startActivity(intent)
        }
    }

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

    private fun setMusicDetails(selectedSong: Data) {
        binding.musicTitle.text = selectedSong.title
        binding.artistName.text = selectedSong.artist.name
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
        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
            playNextSong()
        }
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

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

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

    private fun resumeMusic() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.start()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_stop_24)
    }

    private fun pauseMusic() {
        //   val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.pause()
        binding.playPaushBtn.setImageResource(R.drawable.baseline_play_arrow_24)
    }

    override fun onDestroy() {
        super.onDestroy()
        // mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)
    }
}