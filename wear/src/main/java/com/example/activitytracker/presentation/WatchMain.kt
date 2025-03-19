package com.example.activitytracker.presentation

import ActivityTracker.common.Act
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.activitytracker.presentation.theme.ActivityTrackerTheme
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class WatchMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            // Set up your theme, etc.
            ActivityTrackerTheme {
                // Replace WearApp(...) with your new composable
                SpeechRecognitionButton()
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    SpeechRecognitionButton()
}

@Composable
fun SpeechRecognitionButton() {
    val context = LocalContext.current

    // This will hold the recognized speech text
    var recognizedText by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf<Long?>(null) }

    // Activity launcher to handle speech recognition result
    val speechRecognizerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // The recognized text is returned as a list of possible transcriptions
                val matches = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                // Take the first result if there is one
                recognizedText = matches?.firstOrNull().orEmpty()
                if (recognizedText != "") {
                    startTime = System.currentTimeMillis();
                }

            }
        }

    // Function to start speech recognition
    fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something...")
            // You could also set EXTRA_LANGUAGE, etc.
        }
        speechRecognizerLauncher.launch(intent)
    }

    // Your UI that includes the speech button and recognized text
    WearSpeechScreen(
        context = context,
        recognizedText = recognizedText,
        startTime = startTime,
        onButtonClick = { startSpeechRecognition() }
    )
}

// A composable to show the button and recognized text in the layout
@Composable
fun WearSpeechScreen(
    context: Context,
    recognizedText: String,
    startTime: Long?,
    onButtonClick: () -> Unit
) {
    var act: Act? = null;
    if (startTime != null) {
        act = Act(recognizedText, startTime);
        sendActivityToPhone(context, act);
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        TimeText()

        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.wear.compose.material.Button(
            onClick = onButtonClick,
                modifier = androidx.compose.ui.Modifier.size(100.dp),
                colors = androidx.wear.compose.material.ButtonDefaults.buttonColors()
            ) {
                androidx.wear.compose.material.Text(text = "\uD83C\uDF99") // Microphone emoji
            }

            androidx.compose.foundation.layout.Spacer(
                modifier = androidx.compose.ui.Modifier.size(
                    16.dp
                )
            )

            var text = "";
            if (act != null) {
//                val now = Date.from(Instant.now())
//                val start = Date.from(Instant.ofEpochMilli(act.timestamp))
//                val durationMillis = now.time - start.time
//                val totalSeconds = durationMillis / 1000
//                val minutes = totalSeconds / 60
//                val seconds = totalSeconds % 60
//                val duration = "$minutes:${if (seconds < 10) "0$seconds" else seconds}"
//                text = "${act.label}: $duration"
                text = act.toString();
            }
                androidx.wear.compose.material.Text(
                    text = text,
                    style = androidx.wear.compose.material.MaterialTheme.typography.body1
                )
        }
    }
}

fun sendActivityToPhone(context: Context, act: Act) {
    val dataMapRequest = PutDataMapRequest.create("/activity_data").apply {
        dataMap.putString("label", act.label)
        dataMap.putLong("timestamp", act.timestamp)
    }

    val putDataRequest = dataMapRequest.asPutDataRequest().setUrgent()

    Wearable.getDataClient(context)
        .putDataItem(putDataRequest)
        .addOnSuccessListener {

        }
        .addOnFailureListener { e ->

        }
}