package com.example.cdmusicplayer.model

import com.example.cdmusicplayer.model.ApiData.Data

data class MyData(
    val `data`: List<Data>,
    val next: String,
    val total: Int
)