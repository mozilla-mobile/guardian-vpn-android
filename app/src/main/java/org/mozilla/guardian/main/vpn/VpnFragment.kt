package org.mozilla.guardian.main.vpn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.mozilla.guardian.R

class VpnFragment : Fragment() {

    private lateinit var vpnViewModel: VpnViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        vpnViewModel = ViewModelProviders.of(this).get(VpnViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_vpn, container, false)
        val textView: TextView = root.findViewById(R.id.title)
        vpnViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}