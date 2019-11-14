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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wireguard.android.backend.GoBackend
import kotlinx.android.synthetic.main.bottom_sheet_servers.*
import kotlinx.android.synthetic.main.fragment_vpn.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.util.viewModel

class VpnFragment : Fragment() {

    private lateinit var behavior: BottomSheetBehavior<View>

    private val component by lazy {
        VpnComponentImpl(context!!.coreComponent, context!!.guardianComponent)
    }

    private val vpnViewModel by viewModel { component.viewModel }

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

        behavior = BottomSheetBehavior.from(bottom_sheet)

        vpn_server_switch.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        btn_cancel.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        val intent = GoBackend.VpnService.prepare(context)
        startActivityForResult(intent, 0)
    }

    private fun showConnectingState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.VISIBLE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
    }

    private fun showConnectedState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.VISIBLE
        vpn_state_switching.visibility = View.GONE
        vpn_switch.isChecked = true
    }

    private fun showDisconnectingState() {
        vpn_state_offline.visibility = View.GONE
        vpn_state_disconnecting.visibility = View.VISIBLE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
    }

    private fun showDisconnectedState() {
        vpn_state_offline.visibility = View.VISIBLE
        vpn_state_disconnecting.visibility = View.GONE
        vpn_state_connecting.visibility = View.GONE
        vpn_state_online.visibility = View.GONE
        vpn_state_switching.visibility = View.GONE
        vpn_switch.isChecked = false
    }

    private fun observeServers() {
        vpnViewModel.servers.observe(viewLifecycleOwner, Observer { servers ->
            servers?.let {
                server_list.adapter = ServerListAdapter(it)
                city_name.text = it[0].name
            }
        })
    }
}