package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.calculator.model.History
import java.lang.NumberFormatException
import kotlin.math.exp

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById(R.id.resultTextView)
    }
    private val historyLayout: View by lazy {
        findViewById<View>(R.id.historyLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    lateinit var db: AppDatabase

    private var isOperator = false
    private var hasOperator = false

    //DB에서 기록 저장시 어떻게 저장하는게 좋을지 생각을 먼저 해야함
    //result 버튼 누를 때 저장할 것

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder( // 빌더를 만들고
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build() // 빌드를 해서 실제로 app database를 반환하도록 설정
    }

    fun buttonOnClicked(v: View) {
        when (v.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonModular -> operatorButtonClicked("%")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonDivider -> operatorButtonClicked("/")
            R.id.buttonMulti -> operatorButtonClicked("*")
        }
    }

    private fun numberButtonClicked(number: String) {

        if (isOperator) {
            //연산자 기준으로
            expressionTextView.append(" ")
        }
        isOperator = false

        val expressionText = expressionTextView.text.split(" ")

        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15자리까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (expressionText.last().isEmpty() && number == "0") {
            Toast.makeText(this, "0은 먼저 올 수 없습니다", Toast.LENGTH_SHORT).show()
            return
        }
        expressionTextView.append(number)
        resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        //연산자 먼저 들어왔을 때 무시
        if (expressionTextView.text.isEmpty()) {
            return
        }

        when {
            //isOperator = true
            isOperator -> {
                val text = expressionTextView.text.toString()
                // 끝한자리 지우고
                expressionTextView.text = text.dropLast(1) + operator
            }
            //hasOperator = true
            hasOperator -> {
                Toast.makeText(this, "연산자는 한번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            else -> { //한칸 띄고 연산자 붙임
                expressionTextView.append(" $operator")
            }
        }

        //spannable String Builder를 사용하여 연산자일 때 다른 색
        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            expressionTextView.text.length - 1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        expressionTextView.text = ssb
        isOperator = true
        hasOperator = true
    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTextView.text.split(" ")

        //비어있거나 숫자만 있는 경우
        if (expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }
        //완성되지 않은 수식일 때
        if (expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()
        }
        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        //db에 넣어주는 부분 -> 메인스레드 아닌 새로운 스레드에서 실행해야 함
        //db에 넣기
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTextView.text = resultText
        //연산결과 끝나고 초기화
        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        val expressionTexts = expressionTextView.text.split(" ")

        if (hasOperator.not() || expressionTexts.size != 3) {
            return ""
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }
        //연산할 수 있는 숫자로 분류된 상황
        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when (op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "*" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(v: View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews() // 리니어 레이아웃 하위에있는 모든 뷰들 삭제 (있다면)

        //새로운 스레드
        //디비에서 모든 기록 가져오기
        //뷰에 모든 기록 할당하기
        Thread(Runnable {
            //최신것을 가장 위에 보여주기 위해 리스트 reverse 했음
            db.historyDao().getAll().reversed().forEach {
                //하나씩 꺼내고
                //ui스레드 열기
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(
                        R.layout.history_row, null, false
                    ) // root가 linear 이지만 나중에 addview로 붙일 것이니까 null, false로 주었음
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    //마지막으로 addview로 붙이기
                    //linear 니까 위에서 아래로 착착 달라붙음
                    //뷰가 많아져도 스크롤 뷰 안에 들어가 있어서 스크롤 가능
                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()
    }

    fun closeHistoryButtonClicked(view: View) {
        historyLayout.isVisible = false
    }

    fun historyClearButtonClicked(view: View) {
        //뷰메서 모드 기록 삭제
        historyLinearLayout.removeAllViews()
        // 디비에서 모든 기록 삭제
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()

    }
}

//사용자 지정 객체 함수
//. 붙이고 이름 만들면 그 객체를 확장하는 함수라는 걸 표현함
fun String.isNumber(): Boolean {
    return try {
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        false
    }

}