package com.example.slots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Using MaterialTheme instead of CounterTheme to avoid "Unresolved reference" errors
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SlotMachineScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotMachineScreen() {
    // Counts for the 3 slots
    var count1 by remember { mutableIntStateOf(0) }
    var count2 by remember { mutableIntStateOf(0) }
    var count3 by remember { mutableIntStateOf(0) }

    // Coroutine Jobs
    var job1: Job? by remember { mutableStateOf(null) }
    var job2: Job? by remember { mutableStateOf(null) }
    var job3: Job? by remember { mutableStateOf(null) }

    // Game state
    var isRunning by remember { mutableStateOf(false) }
    var stoppedCount by remember { mutableIntStateOf(0) }
    var resultMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Slot Machine") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Step 4: Win/Loss Message
            Text(
                text = resultMessage,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Step 2 & 5: Three Images/Counters in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SlotDisplay(count1)
                SlotDisplay(count2)
                SlotDisplay(count3)
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Step 3 & 6: Control Logic
            if (!isRunning) {
                Button(onClick = {
                    // Reset game
                    resultMessage = ""
                    stoppedCount = 0
                    isRunning = true

                    // Step 1: Cycle 0-3 with different delays
                    job1 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) {
                            delay(100)
                            count1 = (count1 + 1) % 4
                        }
                    }
                    job2 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) {
                            delay(130)
                            count2 = (count2 + 1) % 4
                        }
                    }
                    job3 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) {
                            delay(160)
                            count3 = (count3 + 1) % 4
                        }
                    }
                }) {
                    Text("SPIN", fontSize = 20.sp)
                }
            } else {
                Button(onClick = {
                    // Step 6: Stop one coroutine per click
                    when (stoppedCount) {
                        0 -> job1?.cancel()
                        1 -> job2?.cancel()
                        2 -> {
                            job3?.cancel()
                            isRunning = false

                            // Step 4: Winning Logic (All 3 match)
                            resultMessage = if (count1 == count2 && count2 == count3) {
                                "JACKPOT! 💰"
                            } else if (count1 == count2 || count2 == count3 || count1 == count3) {
                                "Small Win! ✨"
                            } else {
                                "Better luck next time!"
                            }
                        }
                    }
                    stoppedCount++
                }) {
                    // Dynamic text for the stop button
                    Text("STOP REEL ${stoppedCount + 1}", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun SlotDisplay(value: Int) {
    // Step 5: Map counts 0-3 to Images
    val imageRes = when (value) {
        0 -> R.drawable.cherry
        1 -> R.drawable.grape
        2 -> R.drawable.pear
        else -> R.drawable.strawberry
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.size(90.dp)
        )

        Text(text = "$value", fontSize = 16.sp)
    }
}