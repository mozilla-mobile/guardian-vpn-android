package org.mozilla.firefox.vpn.main.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.databinding.FragmentVpnBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.servers.ui.ServersFragment
import org.mozilla.firefox.vpn.service.Version
import org.mozilla.firefox.vpn.ui.GuardianSnackbar
import org.mozilla.firefox.vpn.ui.InAppNotificationView
import org.mozilla.firefox.vpn.util.GooglePlayUtil
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.getCountryFlag
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class VpnFragment : Fragment() {

    private val component by lazy {
        VpnComponentImpl(context!!.coreComponent, context!!.guardianComponent)
    }

    private val vpnViewModel by viewModel { component.viewModel }

    private val serversFragment = ServersFragment.newInstance()

    private val snackBars = ArrayDeque<GuardianSnackbar>()

    private var currentSnackBar: GuardianSnackbar? = null

    private val durationObserver = Observer<Long> {
        val duration = String.format(
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(it),
            TimeUnit.MILLISECONDS.toMinutes(it) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(it) % TimeUnit.MINUTES.toSeconds(1)
        )
        binding.connectionStateView.setDuration(duration)
    }

    private var binding: FragmentVpnBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentVpnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
        observeServers()

        vpnViewModel.snackBar.observe(viewLifecycleOwner, Observer {
            val snackBar = GuardianSnackbar.make(binding.content, it, GuardianSnackbar.LENGTH_SHORT)
            showSnackBar(snackBar, false)
        })

        vpnViewModel.updateAvailable.observe(viewLifecycleOwner, Observer { version ->
            version
                ?.let { showUpdateMessage(view.context, it) }
                ?: dismissUpdateMessage()
        })

        binding.connectionStateView.onSwitchListener = { isChecked ->
            if (isChecked) {
                vpnViewModel.executeAction(VpnViewModel.Action.Connect)
            } else {
                vpnViewModel.executeAction(VpnViewModel.Action.Disconnect)
            }
        }

        binding.vpnServerSwitch.setOnClickListener {
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
        binding.connectionStateView.applyUiModel(model)
    }

    private fun showConnectedState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
        startObserveDuration()
    }

    private fun showDisconnectingState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
    }

    private fun showDisconnectedState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
        stopObserveDuration()
    }

    private fun showSwitchingState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
    }

    private fun showNoSignalState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
    }

    private fun showUnstableState(model: VpnViewModel.UIModel) {
        binding.connectionStateView.applyUiModel(model)
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
                binding.countryFlag.setImageResource(context!!.getCountryFlag(it.country.code))
                binding.countryName.text = it.city.name
            }
        })
    }

    private fun showUpdateMessage(context: Context, version: Version) {
        val messageView = InAppNotificationView.inflate(
            context,
            InAppNotificationView.Config(
                style = InAppNotificationView.Style.Blue,
                text = StringResource(R.string.toast_update_version_message_1),
                textAction = InAppNotificationView.TextAction(
                    text = StringResource(R.string.update_update_button_text),
                    action = {
                        GooglePlayUtil.launchPlayStore(context)
                    }),
                closeAction = {
                    vpnViewModel.onUpdateMessageDismiss(version)
                    dismissUpdateMessage()
                }
            )
        )
        binding.messageContainer.addView(messageView)
        binding.messageContainer.visibility = View.VISIBLE
    }

    private fun dismissUpdateMessage() {
        binding.messageContainer.visibility = View.GONE
        binding.messageContainer.removeAllViews()
    }

    @Suppress("SameParameterValue")
    private fun showSnackBar(snackBar: GuardianSnackbar, queueIfOccupied: Boolean) {
        currentSnackBar
            ?.let { if (queueIfOccupied) { snackBars.offer(snackBar) } }
            ?: showSnackBar(snackBar)
    }

    private fun showSnackBar(snackBar: GuardianSnackbar) {
        currentSnackBar = snackBar
        snackBar.addCallback(object : BaseTransientBottomBar.BaseCallback<GuardianSnackbar>() {
            override fun onDismissed(transientBottomBar: GuardianSnackbar?, event: Int) {
                currentSnackBar = null
                snackBars.poll()?.let {
                    showSnackBar(it, true)
                }
            }
        })
        snackBar.show()
    }
}
