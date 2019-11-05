package org.mozilla.firefox.vpn.device.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_devices.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.DevicesComponentImpl
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.util.viewModel

class DevicesFragment : Fragment() {

    private val component by lazy {
        DevicesComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.devices.observe(viewLifecycleOwner, Observer {
            device_list.adapter = DevicesAdapter(it) { device ->
                val context = activity ?: return@DevicesAdapter
                showDeleteDialog(context, device) {
                    viewModel.deleteDevice(device)
                }
            }
        })

        viewModel.isAuthorized.observe(viewLifecycleOwner, Observer { isAuthorized ->
            val context = activity ?: return@Observer
            if (!isAuthorized) {
                Toast.makeText(context, "unauthorized!!!!", Toast.LENGTH_SHORT).show()
            }
        })
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