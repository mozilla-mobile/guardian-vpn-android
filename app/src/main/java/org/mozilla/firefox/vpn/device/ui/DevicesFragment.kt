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
import kotlinx.android.synthetic.main.fragment_devices.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.DevicesComponentImpl
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.getSupportActionBar
import org.mozilla.firefox.vpn.main.setSupportActionBar
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.ui.GuardianSnackbar
import org.mozilla.firefox.vpn.ui.InAppNotificationView
import org.mozilla.firefox.vpn.util.viewModel

class DevicesFragment : Fragment() {

    private val component by lazy {
        DevicesComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private lateinit var deviceCountView: TextView
    private var snackBar: GuardianSnackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
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
        loading_view.visibility = View.VISIBLE
        device_list.visibility = View.INVISIBLE
    }

    private fun showData(uiModel: DevicesUiModel) {
        loading_view.visibility = View.INVISIBLE
        device_list.visibility = View.VISIBLE

        if (device_list.adapter == null) {
            device_list.adapter = DevicesAdapter(uiModel) { device ->
                val context = activity ?: return@DevicesAdapter
                showDeleteDialog(context, device) {
                    viewModel.deleteDevice(device)
                }
            }
        } else {
            (device_list.adapter as? DevicesAdapter)?.setData(uiModel)
        }

        deviceCountView.text = getString(
            R.string.devices_page_subtitle,
            uiModel.devices.size.toString(),
            uiModel.maxDevices.toString()
        )
    }

    private fun showError(errorMessage: ErrorMessage) {
        loading_view.visibility = View.INVISIBLE
        device_list.visibility = View.INVISIBLE
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
            setSupportActionBar(toolbar)
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
        snackBar = GuardianSnackbar.make(content, config, duration)
        snackBar?.show()
    }

    private fun dismissSnackBar() {
        snackBar?.dismiss()
        snackBar = null
    }
}
