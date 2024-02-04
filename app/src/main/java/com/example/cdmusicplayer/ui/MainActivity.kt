package com.example.cdmusicplayer.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.databinding.ActivityMainBinding
import com.example.cdmusicplayer.ui.fragments.OfflineMusicHomeFragment
import com.example.cdmusicplayer.ui.fragments.OnlineMusicHomeFragment
import com.example.cdmusicplayer.utils.MediaPlayerManager
import com.example.cdmusicplayer.utils.MusicService
import com.example.cdmusicplayer.utils.MyApplication


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.uiBg)
        setContentView(binding.root)
        replaceFragment(OnlineMusicHomeFragment.getInstance())
//        binding.bottomNavigation.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.online_view -> replaceFragment(OnlineMusicHomeFragment.getInstance())
//                R.id.offline_view -> replaceFragment(OfflineMusicHomeFragment())
//            }
//            true
//        }
        // Apply system UI flags and window insets handling
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars=false
        }
        window.navigationBarColor = ContextCompat.getColor(this@MainActivity,R.color.uiBg)
    }


    @SuppressLint("CommitTransaction")
   fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()

        MediaPlayerManager.getInstance().releaseMediaPlayer()
        // Use the reference to the existing MusicService instance
        val musicServiceIntent = Intent(this, MusicService::class.java)
        stopService(musicServiceIntent)
    }
}
