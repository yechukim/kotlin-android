package com.example.lotto

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private val clearButton: Button by lazy {
        findViewById<Button>(R.id.clearButton)
    }
    private val addButton: Button by lazy {
        findViewById<Button>(R.id.addButton)
    }
    private val runButton: Button by lazy {
        findViewById<Button>(R.id.runButton)
    }
    private val numberPicker by lazy {
        findViewById<NumberPicker>(R.id.numberPicker)
    }
    private val numberTextViewList: List<TextView> by lazy {
        listOf<TextView>(
            findViewById<TextView>(R.id.tv1),
            findViewById<TextView>(R.id.tv2),
            findViewById<TextView>(R.id.tv3),
            findViewById<TextView>(R.id.tv4),
            findViewById<TextView>(R.id.tv5),
            findViewById<TextView>(R.id.tv6),
        )
    }

    private var didRun = false

    //HastSet 중복 저장 x
    private val pickNumberSet = hashSetOf<Int>() // mutableSetOf 도 무관

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberPicker.minValue = 1
        numberPicker.maxValue = 45

        initRunButton()
        initAddButton()
        initClearButton()
    }

    private fun initClearButton() {
        clearButton.setOnClickListener {
            pickNumberSet.clear()
            // 순서대로 꺼내주는 애
            numberTextViewList.forEach {
                it.isVisible = false
            }
            didRun = false
        }
    }

    private fun initRunButton() {
        runButton.setOnClickListener {
            val list = getRandomNumber()

            didRun = true
            //텍스트 초기화 위해 인덱스가 필요함
            //인덱스와 넘버 둘다 리턴하는 함수 사용
            list.forEachIndexed { index, number ->
                val textView = numberTextViewList[index]
                textView.text = number.toString()
                textView.isVisible = true

                setNumberBackground(number,textView)
            }
        }
    }

    private fun initAddButton() {
        addButton.setOnClickListener {
            if (didRun) {
                Toast.makeText(this, "초기화 후에 시도해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener //리스너 안의 함수만을 리턴하기 위해서 붙여줌
            }
            if (pickNumberSet.size >= 6) {
                Toast.makeText(this, "번호는 6개까지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (pickNumberSet.contains(numberPicker.value)) {
                Toast.makeText(this, "이미 선택한 번호입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val textView = numberTextViewList[pickNumberSet.size]
            textView.isVisible = true
            textView.text = numberPicker.value.toString()

            setNumberBackground(numberPicker.value,textView)
            pickNumberSet.add(numberPicker.value)
        }
    }

    private fun setNumberBackground(number: Int, textView: TextView) {
        when (number) {
            in 1..10 -> textView.background =
                ContextCompat.getDrawable(this, R.drawable.circle_blue)
            in 11..20 -> textView.background =
                ContextCompat.getDrawable(this, R.drawable.circle_gray)
            in 21..30 -> textView.background =
                ContextCompat.getDrawable(this, R.drawable.circle_green)
            in 31..40 -> textView.background =
                ContextCompat.getDrawable(this, R.drawable.circle_red)
            else -> textView.background =
                ContextCompat.getDrawable(this, R.drawable.circle_yellow)
        }
    }

    //int 형 리스트 반환
    private fun getRandomNumber(): List<Int> {
        val numberList = mutableListOf<Int>()
            .apply {
                for (i in 1..45) {
                    if (pickNumberSet.contains(i)) {
                        continue // 이미 있는 번호는 넘어가기
                    }
                    this.add(i)
                }
            }
        numberList.shuffle()
        val newList =
            pickNumberSet.toList() + numberList.subList(0, 6 - pickNumberSet.size)// from , to
        return newList.sorted() // 오름차순 정렬
    }
}