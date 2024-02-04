package com.example.cdmusicplayer.utils

import android.util.Log
import android.view.View

object ProgressBarUtills {
    fun showLoading(view: View){
        view.visibility=View.VISIBLE
        Log.d("loading","Visible")
    }
    fun hideLoading(view:View){
        view.visibility=View.GONE
        Log.d("loading","Gone")
    }
}