package com.example.tinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser==null){
            //로그인 안되었을 때 로그인 액티비티 띄워줌
            startActivity(Intent(this,LoginActivity::class.java))
        }else {
            //로그인되어 있을 때 계속 LikeActivity를 띄워주니까 백버튼을 눌러도 해당 화면을 나갈 수 없기 때문에 finish() 해준다.
            startActivity(Intent(this, LikeActivity::class.java))
            finish()
        }
    }
}