package org.mozilla.guardian.insights

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InsightsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Insights Fragment"
    }
    val text: LiveData<String> = _text
}