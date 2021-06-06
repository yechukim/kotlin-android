package com.example.bookreview

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.bookreview.adapter.BookAdapter
import com.example.bookreview.adapter.HistoryAdapter
import com.example.bookreview.api.BookService
import com.example.bookreview.databinding.ActivityMainBinding
import com.example.bookreview.model.BestSellerDto
import com.example.bookreview.model.History
import com.example.bookreview.model.SearchBookDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bookService: BookService

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //뷰 바인딩
        //메인에는 인플레이터 존대
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) //바인딩된 루트를 넣어줌

        initBookRecyclerView()
        initHistoryRecyclerView()
        initSearchEditText()

        db = getAppDatabase(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)
        bookService.getBestSellerBooks(getString(R.string.interparkAPIKey))
            .enqueue(object : Callback<BestSellerDto> {
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if (response.isSuccessful.not()) {
                        return
                    }
                    response.body()?.let {
                        //리스트가 바뀜
                        adapter.submitList(it.books)
                    }
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {

                }

            })

    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }

    //전역으로 쓸 api 키 같은 경우
    //xml로 사용하는 것이 좋다.value에 만들었음
    private fun search(keyword: String) {

        bookService.getBooksByName(getString(R.string.interparkAPIKey), keyword)
            .enqueue(object : Callback<SearchBookDto> {
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    hideHistoryView()
                    saveSearchKeyword(keyword)

                    if (response.isSuccessful.not()) return

                    adapter.submitList(response.body()?.books.orEmpty())
                    /*    response.body()?.let {
                            adapter.submitList(it.books)
                        }*/

                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryView()
                }

            })
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    fun showHistoryView() {
        Thread {
            //리스트
            val keywords = db.historyDao().getAll().reversed()
            runOnUiThread {
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords.orEmpty()) //혹시 모르는 널 처리까지
            }

        }.start()
        binding.historyRecyclerView.isVisible = true
    }

    fun hideHistoryView() {
        binding.historyRecyclerView.isVisible = false
    }

    private fun initBookRecyclerView() {
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this,DetailActivity::class.java)
           //클래스를 직렬화 시켜서 클래스 자체를 넘겨보기
            //오브젝트를 직렬화 시키려면 ? parcelize 어노테이션 달아주기 !
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }

    private fun initSearchEditText() {
        // 책 검색
        binding.searchEditText.setOnKeyListener { _, keyCode, event ->
            //enter될때 search하게 구현
            //edittext 줄을 1줄로 설정하면 엔터시 다음줄로 안넘어가고 엔터키로 눌림
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true // 처리했음을 의미
            }
            return@setOnKeyListener false

        }
        // edit text 터치시 검색한 기록 뜨도록
        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                showHistoryView()
            }
            return@setOnTouchListener false
        }

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}