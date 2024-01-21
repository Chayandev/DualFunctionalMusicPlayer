package com.example.cdmusicplayer.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.palette.graphics.Palette
import com.example.cdmusicplayer.model.ApiData.Data
import com.example.cdmusicplayer.utils.MediaPlayerManager
import com.example.cdmusicplayer.utils.MyApplication
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.databinding.FragmentPlayingMusicBinding
import com.example.cdmusicplayer.utils.MusicService
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

class PlayingMusicBottomSheetFragment private constructor() : BottomSheetDialogFragment(),
    ServiceConnection {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: PlayingMusicBottomSheetFragment? = null
        fun getInstance(): PlayingMusicBottomSheetFragment {
            if (instance == null) {
                instance = PlayingMusicBottomSheetFragment()
            }
            return instance!!
        }
    }

    private lateinit var binding: FragmentPlayingMusicBinding
    private var selectedPosition: Int = -1
    private lateinit var dataList: List<Data>
    private var isFav: Boolean = false
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var mediaPlayerManager: MediaPlayerManager = MediaPlayerManager.getInstance()
    private var musicService: MusicService? = null
    private var bottomSheetDismissListener: BottomSheetDismissListener? = null
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var bottomSheet: FrameLayout? = null

    // Function to set the listener
    fun onSetListener(listener: BottomSheetDismissListener) {
        bottomSheetDismissListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        return dialog.apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayingMusicBinding.inflate(inflater, container, false)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = Intent(requireContext(), MusicService::class.java)
        requireActivity().bindService(
            intent,
            this@PlayingMusicBottomSheetFragment,
            Context.BIND_AUTO_CREATE
        )
        requireActivity().startService(intent)

        //now customise the bottom sheet appearance
        //-------------------------------------------
        bottomSheet = dialog.findViewById(R.id.design_bottom_sheet)
        bottomSheet?.let {
            it.layoutParams.height = LayoutParams.MATCH_PARENT
            it.requestLayout()
            it.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
            bottomSheetBehavior = BottomSheetBehavior.from(it)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.isShouldRemoveExpandedCorners = true
            bottomSheetBehavior.isDraggable = false
            bottomSheetBehavior.isFitToContents = true
            bottomSheetBehavior.setPeekHeight(Resources.getSystem().displayMetrics.heightPixels)
        }
        //-------------------------------

        accessSharedData()
        binding.backwardBtn.setOnClickListener {
            if (selectedPosition >= 0) {
                changeMusicOnForward()
            } else {
                binding.backwardBtn.setImageResource(R.drawable.ic_go_previous_transparent)
            }
        }
        binding.forwardBtn.setOnClickListener {
            if (selectedPosition < dataList.size - 1) {
                changeMusicOnBackward()
            } else {
                binding.forwardBtn.setImageResource(R.drawable.ic_go_next_transparent)
            }
        }
        mediaPlayerManager.mediaPlayer!!.setOnCompletionListener {
            playNewMusic(selectedPosition)
        }
        MyApplication.prevActivity = "PlayingMusicActivity"

        binding.dropDownBtn.setOnClickListener {
            dismissWithSlidingAnimation(bottomSheet)
        }
        setUpOnBackPress()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun changeMusicOnBackward() {
        mediaPlayerManager.releaseMediaPlayer()
        MyApplication.selectedPosition += 1
        setNewSharedData()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun changeMusicOnForward() {
        mediaPlayerManager.releaseMediaPlayer()
        MyApplication.selectedPosition -= 1
        setNewSharedData()
    }

    private fun setUpOnBackPress() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEnabled) {
                    Toast.makeText(requireContext(), "GoBack", Toast.LENGTH_SHORT).show()
                    dismissWithSlidingAnimation(bottomSheet)
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

            }
        }
        callback.isEnabled = true
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }


    private fun dismissWithSlidingAnimation(bottomSheet: FrameLayout?) {
        val translationAnimator = ObjectAnimator.ofFloat(
            bottomSheet,
            View.TRANSLATION_Y,
            bottomSheet?.height?.toFloat() ?: 0F
        )
        setStatusBarColor(Color.TRANSPARENT)
        translationAnimator.apply {
            duration = 100
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    dismiss()
                }
            })
        }
            .start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        Log.d("OnDismiss", "Override OnDismiss")
        setStatusBarColor(Color.TRANSPARENT)
        // Delay the execution by 3 seconds
        handler.postDelayed({
            super.onDismiss(dialog)
            bottomSheetDismissListener?.onBottomSheetDismissed()
            // Remove the fragment transaction to destroy the fragment
            parentFragmentManager.beginTransaction().remove(this).commit()
            handler.removeCallbacksAndMessages(null)
        }, 100) // 3000 milliseconds (3 seconds)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun playNewMusic(currentPosition: Int) {
        if (currentPosition >= 0 && currentPosition < dataList.size - 1) {
            mediaPlayerManager.releaseMediaPlayer()
            MyApplication.selectedPosition += 1
            setNewSharedData()
        } else {
            // Handle the case where the last song has finished
            // You may want to loop back to the first song or take some other action
            // For now, let's just stop the MediaPlayer
            Toast.makeText(requireContext(), "No more songs", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setNewSharedData() {
        selectedPosition = MyApplication.selectedPosition
        val selectedSong = dataList[selectedPosition]
        mediaPlayerManager.initializeMediaPlayer()
        mediaPlayerManager.mediaPlayer?.setDataSource(
            requireContext(),
            selectedSong.preview.toUri()
        )
        mediaPlayerManager.mediaPlayer?.prepareAsync()
        mediaPlayerManager.mediaPlayer?.setOnPreparedListener {
            Log.d("media", "MediaPrepareListener")
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
        musicService?.notificationUiUpdate()
        binding.playPaushBtn.setImageResource(R.drawable.ic_pause)
        checkUserChangeListner()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun accessSharedData() {
        Log.d("accessSharedData", "Calling from AccessSharedData")
        selectedPosition = MyApplication.selectedPosition
        dataList = MyApplication.dataList
        if (selectedPosition != -1 && dataList.isNotEmpty()) {
            setUpUI(selectedPosition)
        }
        setUpSeekBar()
        setUpPlayPauseButton()
        updateSeekBarProgress()
    }

    private fun setUpUI(selectedPosition: Int) {
        Log.d("setUp UI", "Setting UI")
        val selectedSong = dataList[selectedPosition]
        binding.songTitle.text = selectedSong.title
        binding.songTitle.setHorizontallyScrolling(true)
        binding.songTitle.isSelected = true
        binding.singer.text = selectedSong.artist.name
        Picasso.get().load(selectedSong.album.cover_big).into(object : Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap.let { albumArtworkBitmap ->
                    Palette.from(albumArtworkBitmap!!).generate { palette ->

                        val defaultColor = ContextCompat.getColor(requireContext(), R.color.uiBg)
                        // Get the dominant color from the Palette
                        val dominantColor = palette?.getVibrantColor(defaultColor)
                        val transparentColor = Color.argb(
                            150,
                            Color.red(dominantColor!!),
                            Color.green(dominantColor),
                            Color.blue(dominantColor)
                        )
                        // Apply the dominant color as the background
                        val gradientDrawable = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.custom_bottom_sheet_bg,
                        ) as GradientDrawable
                        gradientDrawable.colors = intArrayOf(defaultColor, dominantColor)
                        gradientDrawable.shape = GradientDrawable.RECTANGLE
                        binding.designBottomSheet.background = gradientDrawable
                        setStatusBarColor(dominantColor)
                        binding.musicImg.setImageBitmap(albumArtworkBitmap)
                    }
                }
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                binding.musicImg.setImageResource(R.drawable.headphone)
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                //
            }

        })
        if (selectedPosition == 0) {
            binding.backwardBtn.setImageResource(R.drawable.ic_go_previous_transparent)
        } else {
            binding.backwardBtn.setImageResource((R.drawable.ic_go_previous))
        }

        if (selectedPosition == dataList.size - 1) {
            binding.forwardBtn.setImageResource(R.drawable.ic_go_next_transparent)
        } else {
            binding.forwardBtn.setImageResource(R.drawable.ic_go_next)
        }

        binding.addFav.setOnClickListener {
            isFav = if (!isFav) {
                binding.addFav.setImageResource(R.drawable.favorite)
                true
            } else {
                binding.addFav.setImageResource(R.drawable.ic_favorite)
                false
            }
        }
    }

    private fun setStatusBarColor(dominantColor: Int) {
        activity?.let {
            it.window.statusBarColor = dominantColor
        }
    }

    private fun setUpSeekBar() {
        if (mediaPlayerManager.mediaPlayer != null) {
            binding.seekBar.max = mediaPlayerManager.mediaPlayer!!.duration
            binding.totalTime.text =
                convertDuration(mediaPlayerManager.mediaPlayer!!.duration.toLong())
            if (mediaPlayerManager.mediaPlayer!!.isPlaying) {
                binding.playPaushBtn.setImageResource(R.drawable.ic_pause)
            } else {
                binding.playPaushBtn.setImageResource(R.drawable.ic_play)
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpPlayPauseButton() {
        binding.playPaushBtn.setOnClickListener {
            if (mediaPlayerManager.mediaPlayer?.isPlaying == true) {
                pauseMusic()
            } else {
                resumeMusic()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateSeekBarProgress() {
        binding.seekBar.progress = mediaPlayerManager.mediaPlayer!!.currentPosition
        binding.runTime.text =
            convertDuration(mediaPlayerManager.mediaPlayer!!.currentPosition.toLong())
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
        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
            playNewMusic(MyApplication.selectedPosition)
            Log.d("MusicComplete", "setonCompleteListner")
        }
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

    @RequiresApi(Build.VERSION_CODES.Q)
    fun resumeMusic() {
        mediaPlayerManager.mediaPlayer?.start()
        binding.playPaushBtn.setImageResource(R.drawable.ic_pause)
        musicService?.notificationUiUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun pauseMusic() {
        mediaPlayerManager.mediaPlayer?.pause()
        binding.playPaushBtn.setImageResource(R.drawable.ic_play)
        musicService?.notificationUiUpdate()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        Log.d("Resume BottomSheetFragment", "BottomSheetOnresume")
        mediaPlayerManager.mediaPlayer?.setOnCompletionListener {
            playNewMusic(MyApplication.selectedPosition)
        }

    }

    override fun show(manager: FragmentManager, tag: String?) {
        super.show(manager, tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release resources or perform cleanup if needed
        // Example: mediaPlayerManager.releaseMediaPlayer()
        handler.removeCallbacksAndMessages(null)
    }

    interface BottomSheetDismissListener {
        fun onBottomSheetDismissed()
    }

}


