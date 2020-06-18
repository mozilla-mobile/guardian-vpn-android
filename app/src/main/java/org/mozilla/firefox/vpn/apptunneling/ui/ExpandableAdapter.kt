package org.mozilla.firefox.vpn.apptunneling.ui

import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.apptunneling.ui.ExpandableItem.AppGroup
import org.mozilla.firefox.vpn.apptunneling.ui.ExpandableItem.AppItem

class ExpandableAdapter(
    private var uiModel: AppTunnelingUiModel,
    private val expandableItemCallback: ExpandableItemCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: MutableList<ExpandableItem> = uiModel.toExpandableList()

    fun setData(uiModel: AppTunnelingUiModel) {
        this.uiModel = uiModel
        val newList = uiModel.toExpandableList()
        getDiffResult(items, newList).dispatchUpdatesTo(this)
        items = newList
    }

    fun setEnabled(isEnabled: Boolean) {
        items.forEach { it.isEnabled = isEnabled }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            APP_ITEM ->
                AppItemViewHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.item_app, parent, false),
                    ::onAppItemActionClicked
                )
            else ->
                AppGroupViewHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.item_app_group, parent, false),
                    ::onAppGroupClicked,
                    ::onAppGroupActionClicked
                )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int) = items[position].itemType

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            APP_ITEM -> {
                (holder as AppItemViewHolder).bind(items[position] as AppItem)
            }
            else -> {
                (holder as AppGroupViewHolder).bind(items[position] as AppGroup)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder.itemViewType) {
            APP_ITEM -> {
                (holder as AppItemViewHolder).unbind()
            }
        }
        super.onViewRecycled(holder)
    }

    private fun onAppGroupClicked(holder: AppGroupViewHolder) {
        val appGroupItem = items[holder.adapterPosition] as AppGroup
        val startPosition = holder.adapterPosition + 1
        val count = appGroupItem.appItems.size

        if (appGroupItem.isExpanded) {
            items.removeAll(appGroupItem.appItems)
            notifyItemRangeRemoved(startPosition, count)
            (items[holder.adapterPosition] as AppGroup).isExpanded = false
        } else {
            items.addAll(startPosition, appGroupItem.appItems)
            notifyItemRangeInserted(startPosition, count)
            (items[holder.adapterPosition] as AppGroup).isExpanded = true
        }
        notifyItemChanged(holder.adapterPosition)
    }

    private fun onAppGroupActionClicked(holder: AppGroupViewHolder) {
        val appGroupItem = items[holder.adapterPosition] as AppGroup

        if (appGroupItem.appItems.isEmpty()) return

        val packageNameSet = appGroupItem.appItems.map { it.applicationInfo.packageName }.toSet()

        if (appGroupItem.groupType == AppGroupType.UNPROTECTED) {
            expandableItemCallback.onProtectAllClicked(packageNameSet)
        } else {
            expandableItemCallback.onUnprotectAllClicked(packageNameSet)
        }
    }

    private fun onAppItemActionClicked(holder: AppItemViewHolder) {
        val appItem = items[holder.adapterPosition] as AppItem

        if (appItem.groupType == AppGroupType.UNPROTECTED) {
            expandableItemCallback.onUnprotectedAppChecked(appItem.applicationInfo)
        } else {
            expandableItemCallback.onProtectedAppChecked(appItem.applicationInfo)
        }
    }

    private fun AppTunnelingUiModel.toExpandableList(): MutableList<ExpandableItem> {
        val list = mutableListOf<ExpandableItem>()
        val (exclude, include) = packageList.partition { excludeList.contains(it.packageName) }
        val excludeAppItems = exclude.map { AppItem(AppGroupType.UNPROTECTED, it) }
        val includeAppItems = include.map { AppItem(AppGroupType.PROTECTED, it) }

        list.add(AppGroup(AppGroupType.UNPROTECTED, excludeAppItems))
        list.addAll(excludeAppItems)
        list.add(AppGroup(AppGroupType.PROTECTED, includeAppItems))
        list.addAll(includeAppItems)
        return list
    }

    private fun getDiffResult(oldList: List<ExpandableItem>, newList: List<ExpandableItem>): DiffUtil.DiffResult {

        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val newItem = newList[newItemPosition]
                val oldItem = oldList[oldItemPosition]
                if (newItem is AppGroup && oldItem is AppGroup) {
                    return newItem.groupType == oldItem.groupType
                }
                if (newItem is AppItem && oldItem is AppItem) {
                    return newItem.applicationInfo.packageName == oldItem.applicationInfo.packageName
                }
                return false
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
                if (newItem is AppGroup && oldItem is AppGroup) {
                    return newItem.appItems == oldItem.appItems &&
                            newItem.isExpanded == oldItem.isExpanded &&
                            newItem.isEnabled == oldItem.isEnabled
                }
                if (newItem is AppItem && oldItem is AppItem) {
                    return newItem.groupType == oldItem.groupType &&
                            newItem.isEnabled == oldItem.isEnabled
                }
                return false
            }
        })
    }

    interface ExpandableItemCallback {
        fun onProtectedAppChecked(applicationInfo: ApplicationInfo)
        fun onUnprotectedAppChecked(applicationInfo: ApplicationInfo)
        fun onProtectAllClicked(packageNameSet: Set<String>)
        fun onUnprotectAllClicked(packageNameSet: Set<String>)
    }
}
