package com.example.stopwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*
import kotlin.concurrent.timer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()

            val sec = viewModel.sec.value
            val milli = viewModel.milli.value
            val isRunning = viewModel.isRunning.value
            val lapTimes = viewModel.lapTimes.value

            MainScreen(
                sec = sec,
                milli = milli,
                isRunning = isRunning,
                lapTimes = lapTimes,
                onReset = { viewModel.reset() },
                onToggle = { running ->
                    if (running) {
                        viewModel.pause()
                    } else {
                        viewModel.start()
                    }
                },
                onLapTime = { viewModel.recordLapTime() },
            )
        }
    }
}

class MainViewModel : ViewModel() {
    // 스톱워치에서 초 표시할 변수
    private val _sec = mutableStateOf(0)
    val sec: State<Int> = _sec

    // 스톱워치에서 밀리초 표시할 변수
    private val _milli = mutableStateOf(0)
    val milli: State<Int> = _milli

    // 타이머 작동 중인지 확인할 변수
    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    // 랩 타임 저장할 리스트
    private val _lapTimes = mutableStateOf(mutableListOf<String>())
    val lapTimes: State<List<String>> = _lapTimes

    // 랩 카운트
    private var lap = 1

    // 시간 저장할 변수
    private var time = 0

    // 타이머(초기화시 null)
    private var timerTask: Timer? = null

    fun start() {
        _isRunning.value = true

        timerTask = timer(period = 10) {
            time++
            _sec.value = time / 100
            _milli.value = time % 100
        }
    }

    fun pause() {
        _isRunning.value = false
        timerTask?.cancel()
    }

    fun reset() {
        timerTask?.cancel()

        time = 0
        _isRunning.value = false
        _sec.value = 0
        _milli.value = 0

        _lapTimes.value.clear()
        lap = 1
    }

    fun recordLapTime() {
        _lapTimes.value.add(0, "$lap LAP : ${sec.value}.${milli.value}")
        lap++
    }
}

@Composable
fun MainScreen(
    sec: Int,
    milli: Int,
    isRunning: Boolean,
    lapTimes: List<String>,
    onReset: () -> Unit, // 리셋 버튼 콜백
    onToggle: (Boolean) -> Unit, // 시작, 일시 정지 버튼 콜백
    onLapTime: () -> Unit, // 랩 타임 버튼 콜백
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "StopWatch") })
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(text = "$sec", fontSize = 100.sp)
                Text(text = "$milli")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                lapTimes.forEach { lapTime ->
                    Text(text = lapTime)
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingActionButton(
                    onClick = { onReset() },
                    backgroundColor = Color.Red
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_refresh_24),
                        contentDescription = "reset"
                    )
                }

                FloatingActionButton(
                    onClick = { onToggle(isRunning) },
                    backgroundColor = Color.Green
                ) {
                    Image(
                        painter = painterResource(
                            id =
                            if (isRunning) R.drawable.ic_baseline_pause_24
                            else R.drawable.ic_baseline_play_arrow_24
                        ),
                        contentDescription = "start/pause"
                    )
                }
                
                Button(onClick = { onLapTime()} ) {
                    Text(text = "랩 타임")
                }
            }
        }
    }
}