package com.example.cdmusicplayer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val applyResumeChangesEvent = MutableLiveData<Boolean>()
}
