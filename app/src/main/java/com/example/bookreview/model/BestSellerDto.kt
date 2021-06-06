package com.example.bookreview.model

import com.google.gson.annotations.SerializedName

//서버에서 원하는 값만 모델에 제대로 뽑기 위해
//데이터에 접근하는 dto 클래스를 만든다.
//전체 api response 받아옴
data class BestSellerDto(
    @SerializedName("title") val title: String,
    @SerializedName("item") val books: List<Book>
)