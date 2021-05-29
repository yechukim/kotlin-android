package com.example.calculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.calculator.model.History

@Dao // 이제 룸의 dao 에 연결
interface HistoryDao {
    //entity에 대한 것 정의함

    @Query("SELECT * FROM history")
    fun getAll() : List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history") // 전체 삭제
    fun deleteAll()

    //추가 내용
   /* @Delete //하나만 삭제할 경우
    fun delete(history: History)

    //result에 따른 리스트 반환
    @Query("SELECT * FROM history WHERE result LIKE :result")
    fun findByResult(result: String) : List<History>

    //하나만 반환
    @Query("SELECT * FROM history WHERE result LIKE :result LIMIT 1")
    fun findResult(result: String) :History*/
}