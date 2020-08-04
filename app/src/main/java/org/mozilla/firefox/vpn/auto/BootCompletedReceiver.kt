package org.mozilla.firefox.vpn.auto

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.guardianComponent

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val vpnManager = context?.guardianComponent?.vpnManager ?: return
        Log.d("mmmmmmmm", "Guardian: onReceive ACTION_BOOT_COMPLETED")
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            GlobalScope.launch {
                val selectedServer = context.guardianComponent.selectedServerProvider.observable.value ?: return@launch
                val connectionConfig = context.guardianComponent.connectionConfigProvider.getCurrentConnectionConfig() ?: return@launch
                vpnManager.connect(selectedServer, connectionConfig)
                Log.d("mmmmmmmm", "Guardian: onReceive ACTION_BOOT_COMPLETED")
            }
        }
    }

}
