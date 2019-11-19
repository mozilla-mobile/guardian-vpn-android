package org.mozilla.firefox.vpn.device.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_device.*
import kotlinx.android.synthetic.main.item_device.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.device.ui.DevicesAdapter.DevicesViewHolder
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil
import java.text.ParseException

class DevicesAdapter(
    private val devicesModel: DevicesModel,
    private val onDeleteClicked: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<DevicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevicesViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_device, parent, false)

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

    class DevicesViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(device: DeviceInfo, currentDevice: CurrentDevice?) {
            title.text = device.name
            time.text = getRelativeTime(device.createdAt)

            val isCurrentDevice = currentDevice?.device == device
            if (isCurrentDevice) {
                time.text = itemView.context.getString(R.string.devices_current_device)
                time.setTextColor(ContextCompat.getColor(itemView.context, R.color.science_blue))
                delete.visibility = View.INVISIBLE
            } else {
                time.text = getRelativeTime(device.createdAt)
                time.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray40))
                delete.visibility = View.VISIBLE
            }
        }

        private fun getRelativeTime(iso8601Time: String): String? {
            return TimeUtil.parseOrNull(iso8601Time, TimeFormat.Iso8601)?.let {
                val now = System.currentTimeMillis()
                DateUtils.getRelativeTimeSpanString(it.time, now, DateUtils.MINUTE_IN_MILLIS).toString()
            }
        }
    }
}
