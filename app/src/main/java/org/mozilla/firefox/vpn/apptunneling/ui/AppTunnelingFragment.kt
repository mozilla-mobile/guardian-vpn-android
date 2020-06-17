package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.apptunneling.AppTunnelingComponentImpl
import org.mozilla.firefox.vpn.apptunneling.ui.AppTunnelingViewModel.InfoState
import org.mozilla.firefox.vpn.databinding.FragmentAppTunnelingBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.ui.GuardianSnackbar
import org.mozilla.firefox.vpn.ui.InAppNotificationView
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class AppTunnelingFragment : Fragment() {

    private val component by lazy {
        AppTunnelingComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private var binding: FragmentAppTunnelingBinding by viewBinding()

    private var snackBar: GuardianSnackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppTunnelingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiModel.observe(viewLifecycleOwner, Observer {
            when (it) {
                is AppTunnelingUiState.StateLoading -> showLoading()
                is AppTunnelingUiState.StateLoaded -> showData(it.uiModel)
                else -> return@Observer
            }
        })

        viewModel.vpnState.observe(viewLifecycleOwner, Observer { vpnState ->
            when (vpnState) {
                is VpnState.Disconnected -> updateInfoState()
                else -> {
                    if (binding.switchBtn.isChecked) {
                        updateInfoState(InfoState.SwitchOnWarning)
                    } else {
                        updateInfoState(InfoState.SwitchOffWarning)
                    }
                }
            }
        })

        viewModel.enableState.observe(viewLifecycleOwner, Observer { enableState ->
            binding.switchBtn.isEnabled = enableState
            binding.switchBtn.alpha = if (enableState) 1f else 0.5f
            (binding.expandableList.adapter as? ExpandableAdapter)?.setEnabled(enableState)
        })

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.switchBtn.setOnCheckedChangeListener { _, isChecked ->
            updateInfoState()
            viewModel.switchAppTunneling(isChecked)
        }

        binding.switchBtn.isChecked = viewModel.getAppTunnelingSwitchState()
    }

    private fun showLoading() {
        binding.loadingView.isVisible = binding.switchBtn.isChecked
    }

    private fun showData(uiModel: AppTunnelingUiModel) {
        binding.loadingView.visibility = View.GONE
        binding.expandableList.isVisible = binding.switchBtn.isChecked

        if (binding.expandableList.adapter == null) {
            binding.expandableList.adapter = ExpandableAdapter(uiModel, onExpandableItemCallback)
        } else {
            (binding.expandableList.adapter as? ExpandableAdapter)?.setData(uiModel)
        }
    }

    private fun updateInfoState(infoState: InfoState = InfoState.Normal) {
        binding.infoView.infoIcon.setImageResource(infoState.infoDrawableId)
        binding.infoView.infoText.text = getString(infoState.infoTextResId)
        binding.infoView.root.isVisible =
            !binding.switchBtn.isChecked || infoState is InfoState.Warning
        binding.expandableList.isVisible = binding.switchBtn.isChecked
    }

    private fun showSnackBar(config: InAppNotificationView.Config) {
        snackBar?.dismiss()
        snackBar = GuardianSnackbar.make(binding.content, config, GuardianSnackbar.LENGTH_SHORT)
        snackBar?.show()
    }

    private fun createProtectedNotificationConfig(appName: String = ""): InAppNotificationView.Config {
        val text =
            if (appName.isEmpty())
                StringResource(R.string.app_tunneling_notification_protected_all)
            else
                StringResource(String.format(getString(R.string.app_tunneling_notification_protected_one), appName))

        return InAppNotificationView.Config(text = text)
    }

    private fun createUnprotectedNotificationConfig(appName: String = ""): InAppNotificationView.Config {
        val text =
            if (appName.isEmpty())
                StringResource(R.string.app_tunneling_notification_unprotected_all)
            else
                StringResource(String.format(getString(R.string.app_tunneling_notification_unprotected_one), appName))

        return InAppNotificationView.Config.warning(text)
    }

    private val onExpandableItemCallback = object : ExpandableAdapter.ExpandableItemCallback {

        override fun onProtectedAppChecked(applicationInfo: ApplicationInfo) {
            viewModel.addExcludeApp(applicationInfo.packageName)
            val appName = applicationInfo.loadLabel(context!!.packageManager).toString()
            showSnackBar(createUnprotectedNotificationConfig(appName))
        }

        override fun onUnprotectedAppChecked(applicationInfo: ApplicationInfo) {
            viewModel.removeExcludeApp(applicationInfo.packageName)
            val appName = applicationInfo.loadLabel(context!!.packageManager).toString()
            showSnackBar(createProtectedNotificationConfig(appName))
        }

        override fun onProtectAllClicked(packageNameSet: Set<String>) {
            viewModel.removeExcludeApp(packageNameSet)
            showSnackBar(createProtectedNotificationConfig())
        }

        override fun onUnprotectAllClicked(packageNameSet: Set<String>) {
            viewModel.addExcludeApp(packageNameSet)
            showSnackBar(createUnprotectedNotificationConfig())
        }
    }
}
