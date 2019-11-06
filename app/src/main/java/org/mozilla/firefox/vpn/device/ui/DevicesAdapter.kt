package org.mozilla.firefox.vpn.device.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_device.*
import kotlinx.android.synthetic.main.item_device.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.ui.DevicesAdapter.DevicesViewHolder
import org.mozilla.firefox.vpn.service.DeviceInfo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DevicesAdapter(
    private val devicesModel: DevicesModel,
    private val onDeleteClicked: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        val holder = DevicesViewHolder(itemView)

        holder.itemView.delete.setOnClickListener {
            if (holder.adapterPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(devicesModel.devices[holder.adapterPosition])
            }
        }

        return holder
    }

    override fun getItemCount(): Int = devicesModel.devices.size

    override fun onBindViewHolder(holder: DevicesViewHolder, position: Int) {
        holder.bind(devicesModel.devices[position], devicesModel.currentDevice)
    }

    class DevicesViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(device: DeviceInfo, currentDevice: CurrentDevice?) {
            title.text = device.name
            time.text = getRelativeTime(device.createdAt)

            val isCurrentDevice = currentDevice?.device == device
            delete.visibility = if (isCurrentDevice) {
                View.INVISIBLE
            } else {
                View.VISIBLE
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
    }
}