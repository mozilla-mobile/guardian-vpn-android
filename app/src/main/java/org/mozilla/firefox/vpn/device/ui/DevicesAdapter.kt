package org.mozilla.firefox.vpn.device.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_device.*
import kotlinx.android.synthetic.main.item_device.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.device.data.CurrentDevice
import org.mozilla.firefox.vpn.service.DeviceInfo
import org.mozilla.firefox.vpn.util.TimeFormat
import org.mozilla.firefox.vpn.util.TimeUtil

class DevicesAdapter(
    private var uiModel: DevicesUiModel,
    private val onDeleteClicked: (DeviceInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<DeviceListItem>

    init {
        items = uiModel.toDeviceList()
    }

    fun setData(uiModel: DevicesUiModel) {
        this.uiModel = uiModel
        val newList = uiModel.toDeviceList()
        getDiffResult(items, newList).dispatchUpdatesTo(this)
        items = newList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_LIMIT_REACH -> createLimitReachViewHolder(parent)
            else -> createDeviceViewHolder(parent)
        }
    }

    private fun createLimitReachViewHolder(parent: ViewGroup): LimitReachHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_device_limit_reached, parent, false)

        return LimitReachHolder(itemView)
    }

    private fun createDeviceViewHolder(parent: ViewGroup): DevicesViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_device, parent, false)

        val holder = DevicesViewHolder(itemView)

        holder.itemView.delete.setOnClickListener {
            onDeleteClicked(holder)
        }

        return holder
    }

    private fun onDeleteClicked(holder: DevicesViewHolder) {
        if (holder.adapterPosition == RecyclerView.NO_POSITION) {
            return
        }

        val item = items[holder.adapterPosition]
        if (item is DeviceListItem.Device) {
            onDeleteClicked(item.uiModel.info)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int) = items[position].type

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DevicesViewHolder -> bindDeviceItem(items[position], holder)
        }
    }

    private fun bindDeviceItem(item: DeviceListItem, holder: DevicesViewHolder) {
        if (item is DeviceListItem.Device) {
            holder.bind(item.uiModel, uiModel.currentDevice)
        }
    }

    private fun DevicesUiModel.toDeviceList(): List<DeviceListItem> {
        val list = mutableListOf<DeviceListItem>()
        if (isLimitReached) {
            list.add(DeviceListItem.LimitReach())
            list.addAll(devices.map { DeviceListItem.Device(it) })
        } else {
            list.addAll(devices.map { DeviceListItem.Device(it) })
        }
        return list
    }

    private fun getDiffResult(
        oldList: List<DeviceListItem>,
        newList: List<DeviceListItem>
    ): DiffUtil.DiffResult {

        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val newItem = newList[newItemPosition]
                val oldItem = oldList[oldItemPosition]

                if (newItem.type != oldItem.type) {
                    return false
                }

                if (newItem.type == TYPE_LIMIT_REACH && oldItem.type == TYPE_LIMIT_REACH) {
                    return true
                }

                val newDevice = (newItem as DeviceListItem.Device).uiModel.info
                val oldDevice = (oldItem as DeviceListItem.Device).uiModel.info
                return newDevice == oldDevice
            }

            override fun getOldListSize(): Int {
                return oldList.size
            }

            override fun getNewListSize(): Int {
                return newList.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val newItem = newList[newItemPosition]
                val oldItem = oldList[oldItemPosition]
                if (newItem is DeviceListItem.Device && oldItem is DeviceListItem.Device) {
                    return newItem == oldItem
                }
                return true
            }
        })
    }

    class LimitReachHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DevicesViewHolder(
        override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(deviceUiMode: DeviceItemUiModel, currentDevice: CurrentDevice?) {
            val device = deviceUiMode.info
            title.text = device.name
            time.text = getRelativeTime(device.createdAt)

            val isCurrentDevice = currentDevice?.device == device
            if (isCurrentDevice) {
                time.text = itemView.context.getString(R.string.devices_current_device)
                time.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue50))
                delete.visibility = View.INVISIBLE
                loading.visibility = View.INVISIBLE
            } else {
                time.text = getRelativeTime(device.createdAt)
                time.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray40))

                if (deviceUiMode.isLoading) {
                    loading.visibility = View.VISIBLE
                    delete.visibility = View.INVISIBLE
                    item_holder.alpha = 0.25f
                } else {
                    loading.visibility = View.INVISIBLE
                    delete.visibility = View.VISIBLE
                    item_holder.alpha = 1f
                }
            }
        }

        private fun getRelativeTime(iso8601Time: String): String? {
            return TimeUtil.parseOrNull(iso8601Time, TimeFormat.Iso8601)?.let {
                val now = System.currentTimeMillis()
                DateUtils.getRelativeTimeSpanString(it.time, now, DateUtils.MINUTE_IN_MILLIS).toString()
            }
        }
    }

    sealed class DeviceListItem(val type: Int) {
        data class Device(val uiModel: DeviceItemUiModel) : DeviceListItem(TYPE_DEVICE)
        class LimitReach : DeviceListItem(TYPE_LIMIT_REACH)
    }

    companion object {
        private const val TYPE_LIMIT_REACH = 0
        private const val TYPE_DEVICE = 1
    }
}
