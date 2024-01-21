package com.example.cdmusicplayer.utils

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.model.ApiData.Data
import com.squareup.picasso.Picasso
import java.lang.Exception


class MusicService : Service() {
    private var myBinder = MyBinder()

    private lateinit var mediaSession: MediaSessionCompat

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun showNotification(
        playPauseDrawable: Int,
        previousDrawable: Int,
        nextDrawable: Int,
        playBackSpeed: Float
    ) {

        val dataList = MyApplication.dataList
        val selectedPosition = MyApplication.selectedPosition

        if (selectedPosition >= 0 && selectedPosition < dataList.size) {
            val selectedSong = dataList[selectedPosition]
            var bitMap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.headphone)

            Picasso.get().load(selectedSong.album.cover_big)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        // Update the bitmap when the image is successfully loaded
                        bitMap = bitmap!!

                        // Build the notification with the updated bitmap
                        val notification = buildNotification(
                            selectedSong,
                            bitMap,
                            playPauseDrawable,
                            previousDrawable,
                            nextDrawable,
                            playBackSpeed
                        )
                        startForeground(
                            7,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        // Handle bitmap loading failure, if needed
                        bitMap = BitmapFactory.decodeResource(resources, R.drawable.headphone)

                        // Build the notification with the default bitmap
                        val notification = buildNotification(
                            selectedSong,
                            bitMap,
                            playPauseDrawable,
                            previousDrawable,
                            nextDrawable,
                            playBackSpeed
                        )
                        startForeground(
                            7,
                            notification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                        )
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // This method is called before loading the image,
                        // you can keep the default bitmap or update it if needed
                        bitMap = BitmapFactory.decodeResource(resources, R.drawable.headphone)
                    }
                })

        }
    }

    private fun buildNotification(
        selectedSong: Data,
        bitMap: Bitmap,
        playPauseDrawable: Int,
        previousDrawable: Int,
        nextDrawable: Int,
        playBackSpeed: Float
    ): Notification {

        val prevIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val exitIntent =
            Intent(baseContext, NotificationReceiver::class.java).setAction(MyApplication.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder().putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    MediaPlayerManager.getInstance().mediaPlayer?.duration!!.toLong()
                ).build()
            )
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    MediaPlayerManager.getInstance().mediaPlayer?.currentPosition!!.toLong(),
                    playBackSpeed
                )
                    .build()
            )
        }
        return NotificationCompat.Builder(baseContext, MyApplication.CHANNEL_ID)
            .setContentTitle(selectedSong.title)
            .setContentText(selectedSong.artist.name)
            .setSmallIcon(R.drawable.itunes)
            .setLargeIcon(bitMap)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(previousDrawable, "Previous", prevPendingIntent)
            .addAction(playPauseDrawable, "Play", playPendingIntent)
            .addAction(nextDrawable, "Next", nextPendingIntent)
            //.addAction(R.drawable.ic_clear, "Exit", exitPendingIntent)
            .build()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun notificationUiUpdate() {
        val playPauseDrawable: Int
        val previousDrawable: Int
        val nextDrawable: Int
        val playBackSpeed: Float
        val mediaPlayer = MediaPlayerManager.getInstance().mediaPlayer
        if (mediaPlayer?.isPlaying == true) {
            playPauseDrawable = R.drawable.ic_pause
            playBackSpeed = 1F
        } else {
            playPauseDrawable = R.drawable.ic_play
            playBackSpeed = 0F
        }
        when (MyApplication.selectedPosition) {
            0 -> {
                previousDrawable = R.drawable.ic_go_previous_transparent
                nextDrawable = R.drawable.ic_go_next
            }

            MyApplication.dataList.size - 1 -> {
                previousDrawable = R.drawable.ic_go_previous
                nextDrawable = R.drawable.ic_go_next_transparent
            }

            else -> {
                previousDrawable = R.drawable.ic_go_previous
                nextDrawable = R.drawable.ic_go_next
            }
        }
        showNotification(playPauseDrawable, previousDrawable, nextDrawable, playBackSpeed)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == MyApplication.STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()

        }
        // Return a non-sticky service type
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf() // Stop the service
    }

}
