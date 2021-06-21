package com.example.tinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.tinder.DBKey.Companion.USERS
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth //firebaseAuth.getInstance()랑 똑같은 코드, 코틀린스러울뿐
        callbackManager = CallbackManager.Factory.create()

        initFacebookLoginButton()
        //ctrl alt m -> 범위 지정 -> 함수로 리팩토링
        initLoginButton()
        initSignUpButton()
        initEmailAndPasswordEditText()
    }

    private fun initFacebookLoginButton() {
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLoginButton)
        // 페이스북에서 가져올 권한?
        facebookLoginButton.setPermissions(
            "email",
            "public_profile"
        )//유저에게 받아올 정보 , 페이스북에서 어떤 걸 가져올꺼냐
        facebookLoginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    //로그인 성공 -> 토큰 받아옴
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth.signInWithCredential(credential) // 토큰 받아와서 로그인하기
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if (task.isSuccessful) {
                                handleSuccessLogin()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity, "페이스북 로그인 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                }

                override fun onCancel() {}

                override fun onError(error: FacebookException?) {
                    //에러 토스트
                    Toast.makeText(
                        this@LoginActivity, "페이스북 로그인 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    private fun initLoginButton() {
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        handleSuccessLogin()
                    } else {
                        Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }

    private fun initSignUpButton() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun getInputEmail(): String {
        return findViewById<EditText>(R.id.emailEditText).text.toString()
    }

    private fun getInputPassword(): String {
        return findViewById<EditText>(R.id.passwordEditText).text.toString()
    }

    private fun initEmailAndPasswordEditText() {
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        emailEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            //비어있지 않을 때만 TRUE 가 됨
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
        passwordEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()

            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
    }

    //페이스북으로 로그인 끝나고 호출되는 메소드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //콜백매니저에 있는 메소드로, 액티비티, 프래그먼트의 onActivityResult 에서 호출되어야 하는 메소드
        //페이스북에서 받은 결과를 다 넣어줌
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSuccessLogin(){
        if(auth.currentUser == null){
            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            return
        }
        //currentUser 가 null 일수 있기 때문에 위에 예외처리 한 것
        val userId = auth.currentUser?.uid.orEmpty()
        //레퍼런스에서 child 라는 유저 선택
        val currentUserDB = Firebase.database.reference.child(USERS).child(userId) //없으면 생성, 있으면 가져옴
        val user = mutableMapOf<String, Any>() // key value 형식 key가 String, value는 Any ( 자바로 치면 Object )
        user["userId"] = userId // "userId" 라는 String key 에 userId 를 value로 넣어줌
        currentUserDB.updateChildren(user)

        finish()

    }

}