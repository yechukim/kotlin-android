package com.example.tinder

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.tinder.DBKey.Companion.DISLIKE
import com.example.tinder.DBKey.Companion.LIKE
import com.example.tinder.DBKey.Companion.LIKED_BY
import com.example.tinder.DBKey.Companion.MATCH
import com.example.tinder.DBKey.Companion.NAME
import com.example.tinder.DBKey.Companion.USERS
import com.example.tinder.DBKey.Companion.USER_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity(), CardStackListener {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference

    private val adapter = CardItemAdapter()
    private val cardItems = mutableListOf<CardItem>() // 카드 아이템 리스트

    //사용될 때 사용하도록 만들려면  lazy하게 선언..?
    private val manager by lazy{
        CardStackLayoutManager(this, this) // this에 리스너 implement
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        userDB = Firebase.database.reference.child(USERS)

        val currentUserDB = userDB.child(getCurrentUserID()) // Users 라는 db에 child가 id 인 DB
        //db에서 값을 가져오는 방법은 리스너를 다는 것
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //유저 아이디 정보
                if (snapshot.child(NAME).value == null) {
                    showNameInputPopup()
                    return
                }
                //유저정보 갱신
                getUnselectedUsers()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        initCardStackView()
        initSignOutButton()
        initMatchedListButton()
    }

    private fun initMatchedListButton() {
       val matchedListButton = findViewById<Button>(R.id.matchedListButton)
        matchedListButton.setOnClickListener {
            startActivity(Intent(this, MatchedUserActivity::class.java))
        }
    }

    private fun initSignOutButton() {
     val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun initCardStackView() {
        val stackView = findViewById<CardStackView>(R.id.cardStackView)
        stackView.layoutManager = manager
        stackView.adapter = adapter
    }

    private fun getUnselectedUsers() {
        //말그대로 child 가 추가/ 변경 / 삭제 등 될때 호출되는 메소드
        userDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 사용자가 자신이 아니고, 해당 사용자를 선택하지 않았다면(like & dislike 한적 없으면)  가져오기
                if (snapshot.child(USER_ID).value != getCurrentUserID()
                    && snapshot.child(LIKED_BY).child(LIKE).hasChild(getCurrentUserID()).not()
                    && snapshot.child(LIKED_BY).child(DISLIKE).hasChild(getCurrentUserID()).not()
                ) {

                    val userId = snapshot.child(USER_ID).value.toString()
                    var name = "undecided"

                    if (snapshot.child(NAME).value != null) {
                        name = snapshot.child(NAME).value.toString()
                    }
                    cardItems.add(CardItem(userId, name))
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // 데이터 변경시
                cardItems.find { it.userId == snapshot.key }?.let {
                    it.name = snapshot.child(NAME).value.toString()
                }
                //데이터 수정된 경우 데이터 갱신
                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun showNameInputPopup() {
        val editText = EditText(this) // 간단하게 다이얼로그에 editText 뷰를 만들고

        AlertDialog.Builder(this)
            .setTitle("이름을 입력하세요")
            .setView(editText) // setView로 붙여줌
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveUserName(editText.text.toString())
                }
            }
            .setCancelable(false)//취소 불가
            .show()
    }

    private fun saveUserName(name: String) {
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId) //없으면 생성 있으면 가져옴
        val user = mutableMapOf<String, Any>() // key value 형식
        user["userId"] = userId // key는 string, value는 any
        user["name"] = name
        currentUserDB.updateChildren(user)

        // 유저 정보 가져오기
        getUnselectedUsers()

    }

    private fun like() { // 카드는 1부터 시작인데 인덱스는 0부터 시작하니까 1 빼줌
        val card = cardItems[manager.topPosition -1] // 항상 카드가 top 에 있으니까 항상 카드는 포지션 0 ??
        cardItems.removeFirst() // 카드 아이템에서 제외
        userDB.child(card.userId)
            .child(LIKED_BY)
            .child(LIKE)
            .child(getCurrentUserID())
            .setValue(true) // setValue를 하면 위의 id 가 key가 되고 true 가 value가 됨

        saveMatchIfOtherUserLikedMe(card.userId)
        // 매칭된 시점을 봐야 한다..
        Toast.makeText(this, "you liked ${card.name}!", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchIfOtherUserLikedMe(otherUserId : String){
        val otherUserDB  = userDB.child(getCurrentUserID()).child(LIKED_BY).child(LIKE).child(otherUserId)
        //가져온값이 true 면 상대방이 나를 좋아요 한 것
        //값을 한번만 알아보는
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if(snapshot.value == true){
                   userDB.child(getCurrentUserID())
                       .child(LIKED_BY)
                       .child(MATCH)
                       .child(otherUserId)
                       .setValue(true)

                   // 상대방의 db에도 저장하고
                   userDB.child(otherUserId)
                       .child(LIKED_BY)
                       .child(MATCH)
                       .child(getCurrentUserID())
                       .setValue(true)
               }
            }

            override fun onCancelled(error: DatabaseError) {}

        })


    }
   private fun dislike() {
        val card = cardItems[manager.topPosition -1]
        cardItems.removeFirst()
        userDB.child(card.userId)
            .child(LIKED_BY)
            .child(DISLIKE)
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this, "you disliked ${card.name}!", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentUserID(): String {

        if (auth.currentUser == null) {
            Toast.makeText(this, "로그인 안됨", Toast.LENGTH_SHORT).show()
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> like()
            Direction.Left -> dislike()
            else -> {

            }
        }
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {}
    override fun onCardRewound() {}
    override fun onCardCanceled() {}
    override fun onCardAppeared(view: View?, position: Int) {}
    override fun onCardDisappeared(view: View?, position: Int) {}
}