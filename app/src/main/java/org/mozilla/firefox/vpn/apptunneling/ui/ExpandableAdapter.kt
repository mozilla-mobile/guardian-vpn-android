package org.mozilla.firefox.vpn.apptunneling.ui

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            APP_ITEM ->
                AppItemViewHolder(
                    LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.item_app, parent, false),
                    ::onAppGroupActionClicked
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
        val packageNameSet = appGroupItem.appItems.map { it.applicationInfo.packageName }.toSet()

        if (appGroupItem.type == AppGroupType.UNPROTECTED) {
            expandableItemCallback.onProtectAllClicked(packageNameSet)
        } else {
            expandableItemCallback.onUnprotectAllClicked(packageNameSet)
        }
    }

    private fun onAppGroupActionClicked(holder: AppItemViewHolder) {
        val appItem = items[holder.adapterPosition] as AppItem
        val packageName = appItem.applicationInfo.packageName

        if (appItem.type == AppGroupType.UNPROTECTED) {
            expandableItemCallback.onUnprotectedAppChecked(packageName)
        } else {
            expandableItemCallback.onProtectedAppChecked(packageName)
        }
    }

    private fun AppTunnelingUiModel.toExpandableList(): MutableList<ExpandableItem> {
        val list = mutableListOf<ExpandableItem>()
        val excludeAppItems =
            packageList
                .filter { excludeList.contains(it.packageName) }
                .map { AppItem(AppGroupType.UNPROTECTED, it) }
        val includeAppItems =
            packageList
                .filterNot { excludeList.contains(it.packageName) }
                .map { AppItem(AppGroupType.PROTECTED, it) }
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

                return newItem.itemType == oldItem.itemType
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
                    return newItem.type == oldItem.type && newItem.isExpanded == oldItem.isExpanded
                }
                if (newItem is AppItem && oldItem is AppItem) {
                    return newItem.type == oldItem.type && newItem.applicationInfo == oldItem.applicationInfo
                }
                return true
            }
        })
    }

    interface ExpandableItemCallback {
        fun onProtectedAppChecked(packageName: String)
        fun onProtectAllClicked(packageNameSet: Set<String>)
        fun onUnprotectedAppChecked(packageName: String)
        fun onUnprotectAllClicked(packageNameSet: Set<String>)
    }
}
