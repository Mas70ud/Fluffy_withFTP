package app.fluffy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("Fluffy", "Boot completed - starting service")
            context.startForegroundService(Intent(context, ForegroundService::class.java))
        }
    }
}