package com.example.airbnb

data class HouseModel(
    val id: Int,
    val title: String,
    val price: String,
    val lat: Double,
    val lng: Double,
    val imageUrl: String,
)
