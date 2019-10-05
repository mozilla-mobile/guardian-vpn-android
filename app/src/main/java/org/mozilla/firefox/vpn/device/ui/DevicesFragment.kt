package org.mozilla.firefox.vpn.device.ui

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_devices.*
import kotlinx.android.synthetic.main.item_device.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.user.data.DeviceInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.Locale

class DevicesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        device_list.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProviders.of(this).get(DevicesViewModel::class.java)

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

private class DevicesAdapter(
    val devices: List<DeviceInfo>,
    val onDeleteClicked: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        val holder = DevicesViewHolder(itemView)

        holder.itemView.delete.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(devices[holder.adapterPosition])
            }
        }

        return holder
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bind(devices[position])
    }
}

private class DevicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(device: DeviceInfo) {
        itemView.title.text = device.name
        itemView.time.text = getRelativeTime(device.createdAt)
    }
}

private fun getRelativeTime(iso8601Time: String): String? {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    val time = try {
        format.parse(iso8601Time)
    } catch (e: ParseException) {
        return null
    }.time


    val now = System.currentTimeMillis()
    return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
}
