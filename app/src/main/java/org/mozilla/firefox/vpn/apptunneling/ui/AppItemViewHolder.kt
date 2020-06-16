package org.mozilla.firefox.vpn.apptunneling.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import org.mozilla.firefox.vpn.apptunneling.ui.ExpandableItem.AppItem
import org.mozilla.firefox.vpn.databinding.ItemAppBinding

class AppItemViewHolder(
    itemView: View,
    private val onAppItemChecked: (AppItemViewHolder) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemAppBinding.bind(itemView)

    init {
        binding.appCheckbox.setOnClickListener { onAppItemChecked(this) }
    }

    fun bind(appItem: AppItem) {
        binding.appName.text = appItem.applicationInfo.loadLabel(binding.root.context.packageManager).toString()
        binding.appPackageName.text = appItem.applicationInfo.packageName
        binding.appIcon.load("android.resource://${appItem.applicationInfo.packageName}/${appItem.applicationInfo.icon}")
        binding.appCheckbox.isChecked = appItem.type == AppGroupType.PROTECTED
        binding.appCheckbox.isEnabled = appItem.isEnabled
    }
}
