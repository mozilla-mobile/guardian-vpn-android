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
import java.util.concurrent.TimeUnit
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

    private val durationObserver = Observer<Long> {
        val duration = String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(it),
            TimeUnit.MILLISECONDS.toMinutes(it) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(it) % TimeUnit.MINUTES.toSeconds(1)
        )
        connection_state_view.setDuration(duration)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        observeServers()

        connection_state_view.onSwitchListener = { isChecked ->
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
                is VpnViewModel.UIState.Connecting -> showConnectingState(state.uiModel)
                is VpnViewModel.UIState.Connected -> showConnectedState(state.uiModel)
                is VpnViewModel.UIState.Disconnecting -> showDisconnectingState(state.uiModel)
                is VpnViewModel.UIState.Disconnected -> showDisconnectedState(state.uiModel)
                is VpnViewModel.UIState.Switching -> showSwitchingState(state.uiModel)
                is VpnViewModel.UIState.Unstable -> showUnstableState(state.uiModel)
                is VpnViewModel.UIState.NoSignal -> showNoSignalState(state.uiModel)
            }
        })
    }

    private fun requestPermission() {
        val intent = android.net.VpnService.prepare(context)
        startActivityForResult(intent, 0)
    }

    private fun showConnectingState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
    }

    private fun showConnectedState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
        startObserveDuration()
    }

    private fun showDisconnectingState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
    }

    private fun showDisconnectedState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
        stopObserveDuration()
    }

    private fun showSwitchingState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
    }

    private fun showNoSignalState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
    }

    private fun showUnstableState(model: VpnViewModel.UIModel) {
        connection_state_view.applyUiModel(model)
    }

    private fun startObserveDuration() {
        vpnViewModel.duration.observe(viewLifecycleOwner, durationObserver)
    }

    private fun stopObserveDuration() {
        vpnViewModel.duration.removeObserver(durationObserver)
    }

    private fun observeServers() {
        vpnViewModel.selectedServer.observe(viewLifecycleOwner, Observer { servers ->
            servers?.let {
                country_emoji.text = EmojiUtil.loadEmoji(EmojiUtil.getCountryFlagCodePoint(it.country.code))
                country_name.text = it.city.name
            }
        })
    }
}
