package com.example.activitytracker

import ActivityTracker.common.Act
import ActivityTracker.common.ActDao
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import sendPostRequest
import serverURL
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MobileMain : AppCompatActivity() {

    private lateinit var database: ActivityDatabase
    private lateinit var actDao: ActDao

    private val activities = mutableListOf<Act>()

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MyWearableListenerService.ACTION_SPEECH_DATA_RECEIVED) {
                val label = intent.getStringExtra(MyWearableListenerService.EXTRA_TEXT) ?: "No String"
                val timestamp = intent.getLongExtra(MyWearableListenerService.EXTRA_TIMESTAMP, 0L)
                val act = Act(label, timestamp)
                addNewRecord(act)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = ActivityDatabase.getDatabase(this)
        actDao = database.actDao()

        lifecycleScope.launch {
            val actsFromDb = actDao.getAllActs()
            activities.addAll(actsFromDb)
            updateUI()
        }

        val filter = IntentFilter(MyWearableListenerService.ACTION_SPEECH_DATA_RECEIVED)
        registerReceiver(dataReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    fun addNewRecord(act: Act) {
        activities.add(act)
        updateUI()
        lifecycleScope.launch {
            actDao.insert(act)
        }
        uploadRecord(act)
    }

    fun uploadRecord(act: Act) {
        val endpoint = "/logUpload"
        val url = serverURL + endpoint;
        val payload = """${act.label},${act.timestamp}"""

        sendPostRequest(url, payload)

    }

    fun updateUI() {
        val combinedString = buildActivityListString()
        val dataTextView = findViewById<TextView>(R.id.text)
        dataTextView.text = combinedString
    }

    private fun buildActivityListString(): String {
        val today = LocalDate.now()

        // Filter activities where the timestamp's date is equal to today's date.
        val todaysActivities = activities.filter { act ->
            val actDate = Instant.ofEpochMilli(act.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            actDate == today
        }


        return todaysActivities.joinToString(separator = "\n") { act -> act.toString() };
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dataReceiver)
    }
}