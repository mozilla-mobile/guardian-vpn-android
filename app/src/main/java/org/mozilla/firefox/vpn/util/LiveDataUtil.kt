package org.mozilla.firefox.vpn.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observerUntilOnDestroy(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            removeObserver(observer)
            owner.lifecycle.removeObserver(this)
        }
    })

    observeForever(observer)
}
