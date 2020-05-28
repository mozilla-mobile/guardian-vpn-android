package org.mozilla.firefox.vpn.device.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.FragmentDevicesBinding
import org.mozilla.firefox.vpn.device.DevicesComponentImpl
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.getSupportActionBar
import org.mozilla.firefox.vpn.main.setSupportActionBar
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.ui.GuardianSnackbar
import org.mozilla.firefox.vpn.ui.InAppNotificationView
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class DevicesFragment : Fragment() {

    private val component by lazy {
        DevicesComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private lateinit var deviceCountView: TextView

    private var binding: FragmentDevicesBinding by viewBinding()

    private var snackBar: GuardianSnackbar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        deviceCountView = View.inflate(context, R.layout.view_device_count, null) as TextView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initActionBar()

        viewModel.devicesUiModel.observe(viewLifecycleOwner, Observer {
            when (it) {
                is DevicesUiState.StateLoading -> showLoading()
                is DevicesUiState.StateLoaded -> showData(it.uiModel)
                is DevicesUiState.StateError -> showError(it.errorMessage)
                else -> return@Observer
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            showSnackBar(it.config, it.duration)
        })

        viewModel.dismissMessage.observe(viewLifecycleOwner, Observer {
            dismissSnackBar()
        })
    }

    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.deviceList.visibility = View.INVISIBLE
    }

    private fun showData(uiModel: DevicesUiModel) {
        binding.loadingView.visibility = View.INVISIBLE
        binding.deviceList.visibility = View.VISIBLE

        if (binding.deviceList.adapter == null) {
            binding.deviceList.adapter = DevicesAdapter(uiModel) { device ->
                val context = activity ?: return@DevicesAdapter
                showDeleteDialog(context, device) {
                    viewModel.deleteDevice(device)
                }
            }
        } else {
            (binding.deviceList.adapter as? DevicesAdapter)?.setData(uiModel)
        }

        deviceCountView.text = getString(
            R.string.devices_page_subtitle,
            uiModel.devices.size.toString(),
            uiModel.maxDevices.toString()
        )
    }

    private fun showError(errorMessage: ErrorMessage) {
        binding.loadingView.visibility = View.INVISIBLE
        binding.deviceList.visibility = View.INVISIBLE
        showSnackBar(errorMessage.config, errorMessage.duration)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_devices, menu)

        val deviceCountItem = menu.findItem(R.id.device_count)
        deviceCountItem.actionView = deviceCountView

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initActionBar() {
        setHasOptionsMenu(true)

        activity?.apply {
            setSupportActionBar(binding.toolbar)
            getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun showDeleteDialog(context: Context, device: DeviceInfo, positiveCallback: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.devices_remove_popup_title)
            .setMessage(getString(R.string.devices_remove_popup_content, device.name))
            .setPositiveButton(R.string.popup_remove_button_text) { _, _ -> positiveCallback() }
            .setNegativeButton(R.string.popup_cancel_button_text) { _, _ -> }
            .show()
    }

    private fun showSnackBar(config: InAppNotificationView.Config, duration: Int) {
        snackBar?.dismiss()
        snackBar = GuardianSnackbar.make(binding.content, config, duration)
        snackBar?.show()
    }

    private fun dismissSnackBar() {
        snackBar?.dismiss()
        snackBar = null
    }
}
