package com.example.bbomotimer

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }
    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }
    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }
    private val soundPool = SoundPool.Builder().build()
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    //시작하자마자 생기는거 아니니까 초기에는 null 값으로 세팅함
    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //윈도우는 하얀색이기 때문에 거기에 set view를 해서 하얀색이 나타났다가 빨간색 나타남
        // 윈도우 백그라운드를 빨강으로 하는 것이 깔끔함
        //theme 에서 설정했음
        bindViews()
        initSounds() // sound pool 사용지 짧은 오디오 추천.. 메모리에 로드하고 하는거라서..?
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() // 로드됬던 사운드 풀 해제됨
    }

    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) { // 사용자가 건드렸을 때만
                        updateRemainTime(progress * 60 * 1000L)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //널이 아니면 현재 카운트 다운 cancel
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    //엘비스 연산자
                    //좌측이 null일 경우 우측에 있는 값을 리턴한다.
                    seekBar ?: return // seekbar 가 null 이면 실행하지 않는다.
                    if (seekBar.progress != 0) {
                        startCountDown()
                    } else {
                        stopCountDown()
                    }

                }
            }
        )
    }

    private fun initSounds() {
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()

        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 0, -1, 1f)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        currentCountDownTimer = null
        soundPool.autoPause()
    }

    private fun createCountDownTimer(initialMillis: Long) =
        //코틀린 반환타입, return 키워드 제거 가능
        object : CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTime(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
        }

    private fun completeCountDown() {
        updateRemainTime(0)
        updateSeekBar(0)
        //끝나면 벨소리로 종료
        soundPool.autoPause()
        bellSoundId?.let { soundPool.play(it, 1f, 1f, 0, 0, 1f) }

    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTime(remainMillis: Long) {
        val remainSeconds = remainMillis / 1000
        //한자리 수 일때 앞에 0 붙도록 포맷 설정
        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60) // 60초로 나눈 나머지...가 표현되어야
    }

    //받는 값을 통일 시키는게 가독성이 훨씬 좋다
    private fun updateSeekBar(remainMillis: Long) {
        seekBar.progress = (remainMillis / 1000 / 60).toInt()
    }
}