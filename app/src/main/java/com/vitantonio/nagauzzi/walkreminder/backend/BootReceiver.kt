package com.vitantonio.nagauzzi.walkreminder.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vitantonio.nagauzzi.walkreminder.LOG_TAG

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d(LOG_TAG, "Boot completed")
        }
    }
}
