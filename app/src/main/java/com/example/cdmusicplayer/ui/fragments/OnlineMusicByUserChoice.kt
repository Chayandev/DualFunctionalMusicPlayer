package com.example.cdmusicplayer.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.adapter.MusicAdapter
import com.example.cdmusicplayer.databinding.FragmentOnlineMusicByUserChoiceBinding
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.utils.MyApplication
import com.example.cdmusicplayer.utils.NetworkCheckingUtill
import com.example.cdmusicplayer.utils.ProgressBarUtills
import com.example.cdmusicplayer.utils.RecyclerViewLastItemMarginDecoration
import com.example.cdmusicplayer.utils.statusBarColorChangeUtil
import com.example.cdmusicplayer.viewmodel.MusicViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar


class OnlineMusicByUserChoice private constructor() : Fragment(),PlayingMusicBottomSheetFragment.BottomSheetDismissListener{
    private lateinit var binding: FragmentOnlineMusicByUserChoiceBinding
    private lateinit var query: String
    private lateinit var queryImg: String
    private lateinit var tag: String
    private lateinit var musicViewModel: MusicViewModel
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var playerBottomLL: ConstraintLayout
    private lateinit var musicPlayingLL: ConstraintLayout
    private lateinit var bottomLLMusicTitle: TextView
    private lateinit var bottomLLArtistName: TextView
    private lateinit var bottomLLMusicImage: ImageView
    private lateinit var bottomLLMusicPgBar: me.tankery.lib.circularseekbar.CircularSeekBar
    private lateinit var bottomLLPlayPauseBtn: ImageButton
    private lateinit var bottomNavBar: BottomNavigationView
    private lateinit var dataList: List<Data>
    private val fragmentTag = "UserChoice"
    private lateinit var playlistName: String
    private var statusBarColor:Int=Color.TRANSPARENT
    private fun isMusicAdapterInitialized(): Boolean {
        return ::musicAdapter.isInitialized
    }

    companion object {
        private var instance: OnlineMusicByUserChoice? = null

        fun getInstance(): OnlineMusicByUserChoice {
            if (instance == null) {
                instance = OnlineMusicByUserChoice()
            }
            return instance!!
        }

        fun relaseInstace() {
            instance = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getString("QUERY").toString()
        queryImg = arguments?.getString("QUERY_IMAGE").toString()
        tag = arguments?.getString("TAG").toString()
        musicViewModel = ViewModelProvider(this)[MusicViewModel::class.java]
        Log.d("Query", "$query and $queryImg")
        playlistName = query
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playerBottomLL = requireActivity().findViewById(R.id.playerBottomLL)
        bottomLLMusicTitle = playerBottomLL.findViewById(R.id.music_title)
        bottomLLArtistName = playerBottomLL.findViewById(R.id.artist_name)
        bottomLLMusicImage = playerBottomLL.findViewById(R.id.musicImg)
        bottomLLMusicPgBar = playerBottomLL.findViewById(R.id.ProgressSeekbar)
        bottomLLPlayPauseBtn = playerBottomLL.findViewById(R.id.play_paush_btn)
        musicPlayingLL = playerBottomLL.findViewById(R.id.MusicPlayingLL)
        binding = FragmentOnlineMusicByUserChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        WindowInsetsControllerCompat(
            requireActivity().window,
            requireActivity().window.decorView
        ).apply {

        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //  uiGone()
        binding.progressBar.visibility = View.VISIBLE
        if (NetworkCheckingUtill.isNetworkAvailable(requireContext())) {
            loadDataByViewModel()
        }
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.playPauseBtn.setOnClickListener {
            setUpPlayPause()
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpPlayPause() {
        if (MyApplication.songFromWhichFragment.lowercase()!=fragmentTag) {
            setAndPlayMusic(dataList, 0)
            Log.d("if", "in")
        } else {
            OnlineMusicHomeFragment.getInstance().setPlayAndPause()
            Log.d("else", "in")
        }
//        Log.d("currentActivity", MyApplication..lowercase())
//        Log.d("tag", tag.lowercase())
    }

    private fun setupInitialUi() {
        binding.musicName.text = query
        if (tag == "SearchQuery") {
            uiElementSetUp()
        } else {
            glideImageLoad(queryImg)
        }
    }

    private fun glideImageLoad(queryImg: String) {
        Glide.with(requireContext()).asBitmap().load(queryImg)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Log.d("OnBitmapLoad", "BitmapLoadingSuccess")
                    Palette.from(resource).generate { palette ->
                        Log.d("PaletteSetup", "PaletteSetupWork")
                        val defaultColor =
                            ContextCompat.getColor(requireContext(), R.color.uiBg)
                        val dominantColor = palette?.getDominantColor(defaultColor)
                        val transparentColor = Color.argb(
                            150,
                            Color.red(dominantColor!!),
                            Color.green(dominantColor),
                            Color.blue(dominantColor)
                        )
                        val gradientDrawable = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.custom_gradient_bg
                        ) as GradientDrawable
                        gradientDrawable.colors = intArrayOf(defaultColor, dominantColor)
                        gradientDrawable.shape = GradientDrawable.RECTANGLE
                        uiElementSetUp()
                        binding.topBarLL.background = gradientDrawable
                        statusBarColorChangeUtil.setStatusBarColor(
                            dominantColor,
                            requireActivity()
                        )
                        statusBarColor=dominantColor

                        binding.artistImg.setImageBitmap(resource)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Implement if needed
                }
            })
    }

    private fun plateUiSetUp(albumArtworkBitmap: Bitmap?) {

    }

    private fun uiElementSetUp() {
        Log.d("uiElementSetup", "Making All Visible")
        binding.topBarLL.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.custom_gradient_bg)
        binding.artistImg.setImageResource(R.drawable.headphone)
        binding.artistName.text = query
        binding.playPauseBtn.let {
            it.background = ContextCompat.getDrawable(requireContext(), R.drawable.pl_ps_btn)
            it.setImageResource(R.drawable.ic_play)
            it.isClickable=true
        }
        binding.backBtn.let {
            it.setImageResource(R.drawable.baseline_arrow_back_24)
            it.isClickable = true
        }
        binding.progressBar.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadDataByViewModel() {
        // ProgressBarUtills.showLoading(binding.progressBar)
        musicViewModel.loadMusicList(query)
            .observe(viewLifecycleOwner, Observer { musicList ->
                if (musicList.size != 0) {
                    dataList = musicList
                    playMusic(musicList)
                    setupInitialUi()
                    //ProgressBarUtills.hideLoading(binding.progressBar)
                } else {
                    Log.d("Zero", "NoResult")
                    binding.noResult.setImageResource(R.drawable.ic_no_result)
                    ProgressBarUtills.hideLoading(binding.progressBar)
                }
            })
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun playMusic(dataList: List<Data>) {
        // Pass a lambda expression as the higher-order function
        musicAdapter = MusicAdapter(requireActivity(), dataList) { position, dataList ->
            // Handle item click here
            setAndPlayMusic(dataList, position)
        }

        binding.recyclerView.adapter = musicAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        MyApplication.currentPlaylist = playlistName
        setMusicAdapterView()
        val marginInDp = 100
        val lastItemMarginDecoration =
            RecyclerViewLastItemMarginDecoration(marginInDp, requireContext())
        binding.recyclerView.addItemDecoration(lastItemMarginDecoration)
        // Log.d("TAG : onResponse", "onResponse: " + response.body())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setAndPlayMusic(dataList: List<Data>, position: Int) {
        MyApplication.songFromWhichFragment = fragmentTag
        MyApplication.currentPlaylist = playlistName
        MyApplication.previousPlaylist = playlistName
        if (NetworkCheckingUtill.isNetworkAvailable(requireContext())) {
            MyApplication.selectedPosition = position
            MyApplication.dataList = dataList
            OnlineMusicHomeFragment.getInstance().updateUiOfBottomPlayer()
            setMusicAdapterView()
            setResumeBtn()
            //checkForMusicCompletion()
        } else {
            showSnackBar()
        }
    }

    fun extraUiModification(position: Int) {
        if (tag == "SearchQuery") {
            if(::dataList.isInitialized) {
                // binding.artistName.text = dataList[position].artist.name
                binding.musicName.text = dataList[position].title
                queryImg = dataList[position].album.cover_big
                glideImageLoad(queryImg)
            }
        }
    }

    private fun showSnackBar() {
        val snackBar = Snackbar.make(
            binding.userChoiceFg, "No internet",
            Snackbar.LENGTH_SHORT
        ).setAction("Action", null)
        snackBar.show()
    }

    fun setPauseBtn() {
        if(MyApplication.currentPlaylist.lowercase()==playlistName.lowercase())
        binding.playPauseBtn.setImageResource(R.drawable.ic_play)
    }

    fun setResumeBtn() {
        if(MyApplication.currentPlaylist.lowercase()==playlistName.lowercase())
        binding.playPauseBtn.setImageResource(R.drawable.ic_pause)
    }

    fun setMusicAdapterView() {
        if (isMusicAdapterInitialized()) {
            Log.d(
                "PlayList",
                "${MyApplication.currentPlaylist.lowercase()} and ${MyApplication.previousPlaylist.lowercase()}"
            )
            if (MyApplication.currentPlaylist.lowercase() == MyApplication.previousPlaylist.lowercase() && MyApplication.songFromWhichFragment.lowercase()==fragmentTag.lowercase()) {
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

    override fun onResume() {
        super.onResume()
        MyApplication.currFragment = fragmentTag
        setMusicAdapterView()
        //uiUpdate()
    }

    fun uiUpdate() {
       if(MyApplication.songFromWhichFragment.lowercase()==fragmentTag.lowercase()){
           if(tag=="SearchQuery"){
               extraUiModification(MyApplication.selectedPosition)
           }else{
               statusBarColorChangeUtil.setStatusBarColor(statusBarColor,requireActivity())
           }
       }
    }

    override fun onDestroy() {
        super.onDestroy()
        statusBarColorChangeUtil.setStatusBarColor(Color.TRANSPARENT, requireActivity())
        MyApplication.previousPlaylist = MyApplication.currentPlaylist
        relaseInstace()
        Log.d("destroy", "Destroyed")
    }

    override fun onBottomSheetDismissed() {
        Log.d("BottomSheetDismissedUser", "Bottom sheet dismissed")
        uiUpdate()
    }
}
