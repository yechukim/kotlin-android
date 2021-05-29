package com.example.calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.calculator.dao.HistoryDao
import com.example.calculator.model.History

@Database(entities = [History::class], version = 1) //db에 history라는 테이블 사용하겠다고 등록을 해야 db 만들어짐
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao //db 생성시 함수 가져와서 쓸 수 있게
}