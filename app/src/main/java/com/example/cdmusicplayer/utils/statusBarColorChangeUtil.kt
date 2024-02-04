package com.example.cdmusicplayer.utils

import android.app.Activity
import android.graphics.Color

object statusBarColorChangeUtil {
    fun setStatusBarColor(dominantColor: Int,activity:Activity) {
        if(MyApplication.currFragment.lowercase()=="OnlineHome".lowercase()){
            activity.window.statusBarColor=Color.TRANSPARENT
        }else
        activity.window.statusBarColor = dominantColor
    }
}