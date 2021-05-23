package com.example.secretgarden

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.addTextChangedListener

class DiaryActivity : AppCompatActivity() {

    /* private val diaryEditText:EditText by lazy {
         findViewById(R.id.diaryEditText)
     }*/

    //메인 루퍼를 넣어주면 메인 스레드와 연결된 핸들러가 하나 만들어짐
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)
        //로컬로 선언해도 무방
        //on Create 나갈일이 없으니깐..
        val diaryEditText = findViewById<EditText>(R.id.diaryEditText)
        val detailPreferences = getSharedPreferences("diary", Context.MODE_PRIVATE)

        diaryEditText.setText(detailPreferences.getString("detail", ""))

        val runnable = Runnable {
            getSharedPreferences("diary",Context.MODE_PRIVATE).edit {
                //백그라운드에서 글 저장 --> commit으로 기다리지 않고 비동기로 넘길 것
                putString("detail", diaryEditText.text.toString())
            }
            Log.d("Diary Activity", "SAVE !!${diaryEditText.text}")
        }
        //수정될때마다 저장해보기
        //이것만 있을 경우 너무 빈번하게 저장됨
        diaryEditText.addTextChangedListener{
          // 아래 부분을 백그라운드 스레드에서 저장되도록 함 , runnable을 사용하여
           /* detailPreferences.edit {
                putString("detail", diaryEditText.text.toString())
            }*/
            handler.removeCallbacks(runnable) // pending된 runnable 있다면 제거
            handler.postDelayed(runnable, 500) // 0.5초
        }
    }
}