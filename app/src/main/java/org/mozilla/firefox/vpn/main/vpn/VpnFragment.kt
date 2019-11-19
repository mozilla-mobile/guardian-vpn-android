package org.mozilla.firefox.vpn.main.vpn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_vpn.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.servers.ui.ServersFragment
import org.mozilla.firefox.vpn.util.EmojiUtil
import org.mozilla.firefox.vpn.util.viewModel

class VpnFragment : Fragment() {

    private val component by lazy {
        VpnComponentImpl(context!!.coreComponent, context!!.guardianComponent)
    }

    private val vpnViewModel by viewModel { component.viewModel }

    private val serversFragment = ServersFragment.newInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        observeServers()

        vpn_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vpnViewModel.executeAction(VpnViewModel.Action.Connect)
            } else {
                vpnViewModel.executeAction(VpnViewModel.Action.Disconnect)
            }
        }

        vpn_server_switch.setOnClickListener {
            serversFragment.show(childFragmentManager, serversFragment.tag)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            vpnViewModel.executeAction(VpnViewModel.Action.Connect)
        } else {
            Toast.makeText(context, "Permission denied by user", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeState() {
        vpnViewModel.uiState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                is VpnViewModel.UIState.RequestPermission -> requestPermission()
                is VpnViewModel.UIState.Connecting -> showConnectingState()
                is VpnViewModel.UIState.Connected -> showConnectedState()
                is VpnViewModel.UIState.Disconnecting -> showDisconnectingState()
                is VpnViewModel.UIState.Disconnected -> showDisconnectedState()
            }
        })
    }

    private fun requestPermission() {
        val intent = android.net.VpnService.prepare(context)
        startActivityForResult(intent, 0)
    }

    private fun showConnectingState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.VISIBLE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
        updateStatePanelElevation(true)
    }

    private fun showConnectedState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.VISIBLE
        vpn_state_switching.visibility = View.GONE
        vpn_switch.isChecked = true
        updateStatePanelElevation(true)
    }

    private fun showDisconnectingState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.VISIBLE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
        updateStatePanelElevation(false)
    }

    private fun showDisconnectedState() {
        vpn_state_offline.visibility = View.VISIBLE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
        vpn_switch.isChecked = false
        updateStatePanelElevation(false)
    }

    private fun updateStatePanelElevation(isSecure: Boolean) {
        val elevation = vpn_state_panel.context.resources.getDimensionPixelSize(if (isSecure) {
            R.dimen.vpn_panel_elevation_secure
        } else {
            R.dimen.vpn_panel_elevation_insecure
        })
        vpn_state_panel.cardElevation = elevation.toFloat()
    }

    private fun observeServers() {
        vpnViewModel.servers.observe(viewLifecycleOwner, Observer { servers ->
            servers?.let {
                country_emoji.text = EmojiUtil.loadEmoji(EmojiUtil.getCountryFlagCodePoint(it[0].country.code))
                country_name.text = it[0].country.name
            }
        })
    }
}