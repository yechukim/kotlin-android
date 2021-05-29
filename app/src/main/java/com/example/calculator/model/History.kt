package com.example.calculator.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//모델이니까 코틀린 데이터 클래스 이용
//데이터 클래스는 생성자에 변수를 입력해서 손쉽게 작성 가능

@Entity // 이제 룸의 data class가 되었음
data class History(
    @PrimaryKey val uid: Int?, // unique id -> primary key 태그 달아줄것 (history data class를 db table로 사용하기 위함 )
    @ColumnInfo(name="expression") val expression: String?,
    @ColumnInfo(name = "result") val result : String?
)
//val로 선언 -> setter 생성 x
//data 클래스 -> toString = hashcode ... 그 4가지 함수 자동으로 생성됨?