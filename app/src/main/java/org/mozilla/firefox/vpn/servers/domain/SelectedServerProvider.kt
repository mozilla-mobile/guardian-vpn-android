package org.mozilla.firefox.vpn.servers.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.servers.data.ServerRepository

interface SelectedServerNotifier {
    fun notifyServerChanged()
}

class SelectedServerProvider(private val serverRepo: ServerRepository) : SelectedServerNotifier {
    private val _stateObservable = MutableLiveData<ServerInfo>()
    val observable: LiveData<ServerInfo?> = _stateObservable

    val selectedServer: ServerInfo?
        get() {
            return serverRepo.getSelectedServer()?.server
        }

    override fun notifyServerChanged() {
        _stateObservable.postValue(selectedServer)
    }
}
