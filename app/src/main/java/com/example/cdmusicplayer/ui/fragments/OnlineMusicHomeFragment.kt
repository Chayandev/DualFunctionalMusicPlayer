package com.example.cdmusicplayer.ui.fragments

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.adapter.FamousArtistAdapter
import com.example.cdmusicplayer.adapter.MusicAdapter
import com.example.cdmusicplayer.databinding.FragmentOnlineMusicHomeBinding
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.utils.FamousArtistDataManager
import com.example.cdmusicplayer.utils.MediaPlayerManager
import com.example.cdmusicplayer.utils.MusicService
import com.example.cdmusicplayer.utils.MyApplication
import com.example.cdmusicplayer.utils.NotificationReceiver
import com.example.cdmusicplayer.viewmodel.MusicViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 * Use the [OnlineMusicHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnlineMusicHomeFragment private constructor() : Fragment(), ServiceConnection,
    PlayingMusicBottomSheetFragment.BottomSheetDismissListener {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: OnlineMusicHomeFragment? = null

        fun getInstance(): OnlineMusicHomeFragment {
            if (instance == null) {
                instance = OnlineMusicHomeFragment()
            }
            return instance!!
        }

        fun releaseInstance() {
            instance = null
        }
    }

    private lateinit var binding: FragmentOnlineMusicHomeBinding
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var famousArtistAdapter: FamousArtistAdapter
    var handler: Handler = Handler(Looper.getMainLooper())
    private var selectedPos: Int = -1
    private lateinit var songList: List<Data>
    lateinit var mediaPlayerManager: MediaPlayerManager
    private var musicService: MusicService? = null
    private lateinit var musicViewModel: MusicViewModel
    lateinit var playerBottomLL: ConstraintLayout
    private lateinit var musicPlayingLL: ConstraintLayout
    private lateinit var bottomLLMusicTitle: TextView
    private lateinit var bottomLLArtistName: TextView
    lateinit var bottomLLMusicImage: ImageView
    lateinit var bottomLLMusicPgBar: me.tankery.lib.circularseekbar.CircularSeekBar
    private lateinit var bottomLLPlayPauseBtn: ImageButton
    private lateinit var bottomNavBar: BottomNavigationView
    private var isPlayingMusicFragmentAdded = false
    private fun isMusicAdapterInitialized(): Boolean {
        return ::musicAdapter.isInitialized
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OnCreate", "im in On Create method")
        //initializing the view-model
        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        changeStatusBarColor()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("OnCreateView", "im in On CreateView method")
        binding = FragmentOnlineMusicHomeBinding.inflate(inflater, container, false)
        bottomNavBar = requireActivity().findViewById(R.id.bottomNavigation)
        playerBottomLL = requireActivity().findViewById(R.id.playerBottomLL)
        bottomLLMusicTitle = playerBottomLL.findViewById(R.id.music_title)
        bottomLLArtistName = playerBottomLL.findViewById(R.id.artist_name)
        bottomLLMusicImage = playerBottomLL.findViewById(R.id.musicImg)
        bottomLLMusicPgBar = playerBottomLL.findViewById(R.id.ProgressSeekbar)
        bottomLLPlayPauseBtn = playerBottomLL.findViewById(R.id.play_paush_btn)
        musicPlayingLL = playerBottomLL.findViewById(R.id.MusicPlayingLL)
        bottomNavBar.visibility = View.VISIBLE
        playerBottomLL.visibility = View.GONE


        // Assuming your NavHostFragment is in the 'fragmentContainer' FrameLayout

        // Check if savedInstanceState is not null
        if (savedInstanceState != null) {
            // Retrieve the saved state information
            isPlayingMusicFragmentAdded = savedInstanceState.getBoolean("isInStack", false)
        }
        return binding.root


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("OnCreate", "im in On ViewCreated method")

        //starting service
        val intent = Intent(requireContext(), MusicService::class.java)
        requireActivity().bindService(
            intent,
            this@OnlineMusicHomeFragment,
            Context.BIND_AUTO_CREATE
        )
        requireActivity().startService(intent)

        //-------------------------------------------------------------

        networkCheckAndProceed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save important state information here
        super.onSaveInstanceState(outState)
        outState.putBoolean("isInStack", isPlayingMusicFragmentAdded)
    }

    /*
    private fun checkForSearchResult() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("Tag:OnSubmitpressKyeboard", "Search Pressed")
                query?.takeIf { it.isNotBlank() }?.let { handelQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    */

    /*
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
    */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun networkCheckAndProceed() {
        // Check network connectivity
        showLoading()
        if (isNetworkAvailable()) {
            binding.noInternetConnectionLL.visibility = View.GONE
            binding.availableInternetLL.visibility = View.VISIBLE
            if (!isPlayingMusicFragmentAdded) {
                mediaPlayerManagerInitialize()
                famousArtistRecyclerViewSetUp()
                loadDataByViewModel()
            }
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadDataByViewModel() {
        showLoading()
        musicViewModel.loadMusicList("Arijit Singh")
            .observe(viewLifecycleOwner, Observer { musicList ->
                playMusic(musicList)
                hideLoading()
            })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playMusic(dataList: List<Data>) {
        // Pass a lambda expression as the higher-order function
        musicAdapter = MusicAdapter(requireActivity(), dataList) { position, dataList ->
            // Handle item click here
            MyApplication.songFromWhichActivity = "MainActivity"
            if (isNetworkAvailable()) {
                //val selectedMusic = dataList[position]
                // Set or get shared data in MainActivity
                MyApplication.selectedPosition = position
                MyApplication.dataList = dataList
                //playing song on the bottom palyer
                updateUiOfBottomPlayer()
                // Inside your MainActivity
                setMusicAdapterView()
            } else {
                showSnackBar()
            }
        }

        binding.recyclerView.adapter = musicAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        // Log.d("TAG : onResponse", "onResponse: " + response.body())
    }

    private fun showSnackBar() {
        val snackbar = Snackbar.make(
            binding.homeOnlineFrgment, "No internet",
            Snackbar.LENGTH_SHORT
        ).setAction("Action", null)
        snackbar.show()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun mediaPlayerManagerInitialize() {
        mediaPlayerManager = MediaPlayerManager.getInstance()
        if (mediaPlayerManager.mediaPlayer != null) {
            setMusicAdapterView()
            singleTimeExecutionBottomUi()
        } else {
            // mediaPlayerManager.initializeMediaPlayer()
        }
    }

    private fun famousArtistRecyclerViewSetUp() {
        val artistData = FamousArtistDataManager.famousArtistData
        famousArtistAdapter = FamousArtistAdapter(requireActivity(), artistData) { position ->
            /*
              // Create an Intent
              if (isNetworkAvailable()) {
                  val intent = Intent(this, PlaySongByRequestActivity::class.java)

                  // Put the string data into the Intent
                  intent.putExtra("QUERY", artistData[position].name)
                  intent.putExtra("FLAG", "A")
                  mediaPlayerManager.releaseMediaPlayer()
                  handler.removeCallbacksAndMessages(null)
                  binding.playerBottomLL.visibility = View.GONE
                  musicAdapter.setSelectedPosition(RecyclerView.NO_POSITION)
                  (application as MyApplication).prevActivity = "MainActivity"
                  // Start the new activity
                  startActivity(intent)
                  // mediaPlayerManager.releaseMediaPlayer()
                  //handler.removeCallbacksAndMessages(null)
              } else {
                  showSnackBar()
              }
              */
        }


        binding.famousArtistRcView.adapter = famousArtistAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity(), 1)


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateUiOfBottomPlayer() {
//        playerBottomLL.visibility = View.VISIBLE
        selectedPos = MyApplication.selectedPosition
        songList = MyApplication.dataList
        val selectedSong = songList[selectedPos]

        setMusicDetails(selectedSong)

        if (mediaPlayerManager.mediaPlayer != null && mediaPlayerManager.mediaPlayer!!.isPlaying) {
            mediaPlayerManager.releaseMediaPlayer()
        }
        //  val mediaPlayerManager = MediaPlayerManager.getInstance()
        createMediaPlayer(selectedSong)
        Log.d("Tag:UpdateBottomUi after setMusic", "Hello bottom middle")
        setUpSeekBar()
        updateSeekBarProgress()
        setUpPlayPauseButton()

        playerBottomLL.setOnClickListener {
            // Inside your OnlineMusicHomeFragment
            val playingMusicBottomSheetFragment = PlayingMusicBottomSheetFragment.getInstance()

            // Set the listener
            playingMusicBottomSheetFragment.onSetListener(this)

            // Show the bottom sheet
            playingMusicBottomSheetFragment.show(requireActivity().supportFragmentManager, "tag")

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createMediaPlayer(selectedSong: Data) {
        mediaPlayerManager.initializeMediaPlayer()
        mediaPlayerManager.mediaPlayer?.setDataSource(
            requireContext(),
            selectedSong.preview.toUri()
        )
        mediaPlayerManager.mediaPlayer?.prepareAsync()
        mediaPlayerManager.mediaPlayer!!.setOnCompletionListener {
            playNextSong(selectedPos)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun playNextSong(currentPosition: Int) {
        songList = MyApplication.dataList
        if (currentPosition >= 0 && currentPosition < songList.size - 1) {
            mediaPlayerManager.releaseMediaPlayer()
            MyApplication.selectedPosition += 1
            setMusicAdapterView()
            updateUiOfBottomPlayer()
        } else {
            // Handle the case where the last song has finished
            // You may want to loop back to the first song or take some other action
            // For now, let's just stop the MediaPlayer
            Toast.makeText(requireContext(), "No more Songs", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setMusicAdapterView() {
        if (isMusicAdapterInitialized()) {
            Log.d(
                "SongFromWhichActivity",
                "${MyApplication.songFromWhichActivity.lowercase()} and ${"MainActivity".lowercase()}"
            )
            if (MyApplication.songFromWhichActivity.lowercase() == "MainActivity".lowercase()) {
                musicAdapter.setSelectedPosition(MyApplication.selectedPosition)
                Log.d(
                    "AdapterView-selected pos",
                    "${MyApplication.selectedPosition}"
                )
            } else {
                musicAdapter.setSelectedPosition(RecyclerView.NO_POSITION)
            }
        }
    }

    private fun setMusicDetails(selectedSong: Data) {
        bottomLLMusicTitle.text = selectedSong.title
        bottomLLArtistName.text = selectedSong.artist.name
        Picasso.get().load(selectedSong.album.cover_big).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                /*
              bitmap.let {albumArtWorkBitmap->
                  Palette.from(albumArtWorkBitmap!!).generate {palette->
                      val defaultColor = ContextCompat.getColor(requireContext(), R.color.uiBg)
                      val dominantColor = palette?.getDarkMutedColor(defaultColor)

                      val gradientDrawable=ContextCompat.getDrawable(
                          requireActivity(),
                          R.drawable.bottom_music_paying_ll_bg
                      ) as GradientDrawable

                      gradientDrawable.setColor(dominantColor!!)
                      musicPlayingLL.background=gradientDrawable

                  }
                  }
                  */
                bottomLLMusicImage.setImageBitmap(bitmap)
                playerBottomLL.visibility = View.VISIBLE

            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                bottomLLMusicImage.setImageResource(R.drawable.headphone)
                playerBottomLL.visibility = View.VISIBLE
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                playerBottomLL.visibility = View.VISIBLE
            }

        })

        bottomLLMusicPgBar.progress = 0.0F
    }


    private fun setUpSeekBar() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.setOnPreparedListener {
            bottomLLMusicPgBar.max = mediaPlayerManager.mediaPlayer!!.duration.toFloat()
            mediaPlayerManager.mediaPlayer?.start()
            musicService?.notificationUiUpdate()
            bottomLLPlayPauseBtn.setImageResource(R.drawable.ic_pause)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateSeekBarProgress() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // val mediaPlayerManager = MediaPlayerManager.getInstance()
                mediaPlayerManager.mediaPlayer?.let {
                    if (it.isPlaying) {
                        bottomLLMusicPgBar.progress = it.currentPosition.toFloat()
                    }
                }
                handler.postDelayed(this, 10)
            }
        }, 0)
//        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
//            playNextSong()
//        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpPlayPauseButton() {
        bottomLLPlayPauseBtn.setOnClickListener {
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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        applyChangesOnResume()
        Log.d("Resume", "Im onResume")
    }

    private fun changeStatusBarColor() {
        activity?.let {
            val desiredStatusBarColor = ContextCompat.getColor(requireContext(), R.color.uiBg)
            it.window.statusBarColor = desiredStatusBarColor
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun applyChangesOnResume() {
        if (isMusicAdapterInitialized()) {
            Log.d("ResumeApply", "Changes")
            if (mediaPlayerManager.mediaPlayer != null) {
                Log.d("ResumeApply", "Changes")
                updateActivityResumeData()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateActivityResumeData() {
        if (MyApplication.prevActivity.lowercase() == "PlayingMusicActivity".lowercase() || MyApplication.prevActivity.lowercase() == "MainActivity".lowercase()) {
            Log.d("Tag:From Main or PlayingMusic Activity", "hi")
            setMusicAdapterView()
            playerBottomLL.visibility = View.VISIBLE
            selectedPos = MyApplication.selectedPosition

            if (selectedPos != -1) {
                resumeMusicBottomUiSetUp(selectedPos)
                mediaPlayerManager.mediaPlayer!!.setOnCompletionListener {
                    playNextSong(selectedPos)
                }
            }
        } else {
            Log.d("Tag:From not Main or PlayingMusic Activity", "hi")
            musicAdapter.setSelectedPosition(RecyclerView.NO_POSITION)
            selectedPos = MyApplication.selectedPosition
            resumeMusicBottomUiSetUp(selectedPos)
            singleTimeExecutionBottomUi()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun singleTimeExecutionBottomUi() {
        playerBottomLL.visibility = View.VISIBLE
        selectedPos = MyApplication.selectedPosition
        songList = MyApplication.dataList
//        val selectedSong = songList[selectedPos]
        //setUpSeekBar()
        if (selectedPos != -1) {
            updateSeekBarProgress()
            setUpPlayPauseButton()

            playerBottomLL.setOnClickListener {
                PlayingMusicBottomSheetFragment.getInstance().show(
                    requireActivity().supportFragmentManager,
                    "tag"
                )
            }
            mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
                playNextSong(selectedPos)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("Pause", "In in OnPause")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun resumeMusicBottomUiSetUp(selectedPos: Int) {
        val selectedSong = MyApplication.dataList[selectedPos]
        setMusicDetails(selectedSong)
        bottomLLMusicPgBar.progress =
            mediaPlayerManager.mediaPlayer?.currentPosition!!.toFloat()
        bottomLLMusicPgBar.max = mediaPlayerManager.mediaPlayer!!.duration.toFloat()
        if (mediaPlayerManager.mediaPlayer!!.isPlaying) {
            bottomLLPlayPauseBtn.setImageResource(R.drawable.ic_pause)
            //   updateSeekBarProgress()
        } else {
            bottomLLPlayPauseBtn.setImageResource(R.drawable.ic_play)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun resumeMusic() {
        // val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.start()
        bottomLLPlayPauseBtn.setImageResource(R.drawable.ic_pause)
        musicService?.notificationUiUpdate()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun pauseMusic() {
        //   val mediaPlayerManager = MediaPlayerManager.getInstance()
        mediaPlayerManager.mediaPlayer?.pause()
        bottomLLPlayPauseBtn.setImageResource(R.drawable.ic_play)
        musicService?.notificationUiUpdate()
    }


    override fun onDestroy() {
        super.onDestroy()
        // mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBottomSheetDismissed() {
        // This method will be called when the bottom sheet is dismissed
        // Perform any necessary actions here
        Log.d("BottomSheetDismissed", "Bottom sheet dismissed")
        // Apply changes on resume if needed
        applyChangesOnResume()
        changeStatusBarColor()
    }
}