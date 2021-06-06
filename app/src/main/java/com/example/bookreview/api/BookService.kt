package com.example.bookreview.api

import com.example.bookreview.model.BestSellerDto
import com.example.bookreview.model.Book
import com.example.bookreview.model.SearchBookDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService { // 인터페이스기 때문에 실제로 구현하려면 retrofit에서 create로 구현해야 함

    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey:String,
        @Query("query") keyWord : String
    ) : Call<SearchBookDto>

    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey:String
    ): Call<BestSellerDto>
}