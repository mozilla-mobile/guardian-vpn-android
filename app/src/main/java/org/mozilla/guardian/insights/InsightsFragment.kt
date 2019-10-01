package org.mozilla.guardian.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.mozilla.guardian.R

class InsightsFragment : Fragment() {

    private lateinit var insightsViewModel: InsightsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        insightsViewModel = ViewModelProviders.of(this).get(InsightsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_insights, container, false)
        val textView: TextView = root.findViewById(R.id.title)
        insightsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}