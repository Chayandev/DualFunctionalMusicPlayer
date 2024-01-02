package com.example.cdmusicplayer.data

import com.example.cdmusicplayer.ApiData.Data

data class MyData(
    val `data`: List<Data>,
    val next: String,
    val total: Int
)