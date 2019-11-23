package org.mozilla.firefox.vpn.device.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_devices.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.DevicesComponentImpl
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.getSupportActionBar
import org.mozilla.firefox.vpn.main.setSupportActionBar
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.util.viewModel

class DevicesFragment : Fragment() {

    private val component by lazy {
        DevicesComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private lateinit var deviceCountView: TextView

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
            it ?: return@Observer
            if (device_list.adapter == null) {
                device_list.adapter = DevicesAdapter(it) { device ->
                    val context = activity ?: return@DevicesAdapter
                    showDeleteDialog(context, device) {
                        viewModel.deleteDevice(device)
                    }
                }
            } else {
                (device_list.adapter as? DevicesAdapter)?.setData(it)
            }

            deviceCountView.text = getString(R.string.devices_count, it.devices.size, it.maxDevices)
        })
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
        AlertDialog.Builder(context)
            .setTitle(R.string.devices_remove_dialog_title)
            .setMessage(getString(R.string.devices_remove_dialog_message, device.name))
            .setPositiveButton(R.string.remove) { _, _ -> positiveCallback() }
            .setNegativeButton(android.R.string.cancel) { _, _ ->  }
            .show()
    }
}