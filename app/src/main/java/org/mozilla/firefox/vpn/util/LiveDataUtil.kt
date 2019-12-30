package org.mozilla.firefox.vpn.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

fun <T> LiveData<T>.distinctBy(selector: (v1: T, v2: T) -> Boolean) = object : MediatorLiveData<T>() {
    var current: T? = null
    init {
        addSource(this@distinctBy) { newValue ->
            if (current == null || selector(current!!, newValue)) {
                current = newValue
                value = newValue
            }
        }
    }
}

fun <T, A, B> LiveData<A>.combineWith(other: LiveData<B>, onChange: (A, B) -> T): MediatorLiveData<T> {
    var source1emitted = false
    var source2emitted = false

    val result = MediatorLiveData<T>()

    val mergeF = {
        val source1Value = this.value
        val source2Value = other.value

        if (source1emitted && source2emitted) {
            result.value = onChange.invoke(source1Value!!, source2Value!!)
        }
    }

    result.addSource(this) { source1emitted = true; mergeF.invoke() }
    result.addSource(other) { source2emitted = true; mergeF.invoke() }

    return result
}
