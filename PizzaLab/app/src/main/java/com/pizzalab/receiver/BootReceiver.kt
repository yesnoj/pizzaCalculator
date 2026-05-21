package com.pizzalab.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Riceve BOOT_COMPLETED per riavviare eventuali timer attivi
 * dopo un riavvio del dispositivo.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot completed — verifico timer pendenti")
            // TODO: In futuro, leggere timer salvati da DataStore e riavviare TimerService
            // Per ora il receiver è registrato ma non riavvia timer automaticamente.
            // I timer in-memory vengono persi al riavvio del dispositivo.
        }
    }
}
