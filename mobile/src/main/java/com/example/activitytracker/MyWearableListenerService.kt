package com.example.activitytracker

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class MyWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            println(event)
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == "/activity_data") {
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val label = dataMap.getString("label") ?: ""
                    val timestamp = dataMap.getLong("timestamp", 0L)

                    Log.d("MyWearableListener", "Received text=$label time=$timestamp")

                    // Broadcast an Intent so that your Activity can pick it up
                    val intent = Intent(ACTION_SPEECH_DATA_RECEIVED).apply {
                        putExtra(EXTRA_TEXT, label)
                        putExtra(EXTRA_TIMESTAMP, timestamp)
                    }
                    sendBroadcast(intent)
                }
            }
        }
    }

    companion object {
        const val ACTION_SPEECH_DATA_RECEIVED = "com.example.activitytracker.SPEECH_DATA_RECEIVED"
        const val EXTRA_TEXT = "extra_text"
        const val EXTRA_TIMESTAMP = "extra_timestamp"
    }
}