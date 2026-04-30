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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}

// --- ROOT: controls which screen is shown ---
@Composable
fun AppRoot() {
    var currentScreen by remember { mutableStateOf("title") }

    when (currentScreen) {
        "title" -> TitleScreen(onPlay = { currentScreen = "game" })
        "game"  -> SlotMachineScreen(onBack = { currentScreen = "title" })
    }
}

// --- SCREEN 1: Title / Info Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleScreen(onPlay: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Welcome") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "🎰",
                fontSize = 80.sp
            )

            Text(
                text = "Slot Machine",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "How to Play",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. Press SPIN to start the reels.", fontSize = 16.sp)
                Text("2. Press STOP REEL to freeze each reel one at a time.", fontSize = 16.sp)
                Text("3. Match all 3 symbols for a JACKPOT! 💰", fontSize = 16.sp)
                Text("4. Match any 2 symbols for a Small Win! ✨", fontSize = 16.sp)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "🍒  🍇  🍐  🍓",
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Four fruits, endless chances — good luck!",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("PLAY", fontSize = 20.sp)
            }
        }
    }
}

// --- SCREEN 2: Slot Machine Game Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotMachineScreen(onBack: () -> Unit) {
    var count1 by remember { mutableIntStateOf(0) }
    var count2 by remember { mutableIntStateOf(0) }
    var count3 by remember { mutableIntStateOf(0) }

    var job1: Job? by remember { mutableStateOf(null) }
    var job2: Job? by remember { mutableStateOf(null) }
    var job3: Job? by remember { mutableStateOf(null) }

    var isRunning by remember { mutableStateOf(false) }
    var stoppedCount by remember { mutableIntStateOf(0) }
    var resultMessage by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Slot Machine") },
                navigationIcon = {
                    // Back button to return to title screen
                    TextButton(onClick = onBack) {
                        Text("← Info")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = resultMessage,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SlotDisplay(count1)
                SlotDisplay(count2)
                SlotDisplay(count3)
            }

            Spacer(modifier = Modifier.height(60.dp))

            if (!isRunning) {
                Button(onClick = {
                    resultMessage = ""
                    stoppedCount = 0
                    isRunning = true

                    job1 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) { delay(100); count1 = (count1 + 1) % 4 }
                    }
                    job2 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) { delay(130); count2 = (count2 + 1) % 4 }
                    }
                    job3 = coroutineScope.launch(Dispatchers.Default) {
                        while (true) { delay(160); count3 = (count3 + 1) % 4 }
                    }
                }) {
                    Text("SPIN", fontSize = 20.sp)
                }
            } else {
                Button(onClick = {
                    when (stoppedCount) {
                        0 -> job1?.cancel()
                        1 -> job2?.cancel()
                        2 -> {
                            job3?.cancel()
                            isRunning = false
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
                    Text("STOP REEL ${stoppedCount + 1}", fontSize = 20.sp)
                }
            }
        }
    }
}

// --- Slot symbol display (unchanged) ---
@Composable
fun SlotDisplay(value: Int) {
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
