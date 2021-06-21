package com.example.airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/2c50ae57-345a-4f27-8ce5-44440706bd97")
    fun getHouseList(): Call<HouseDto>
}