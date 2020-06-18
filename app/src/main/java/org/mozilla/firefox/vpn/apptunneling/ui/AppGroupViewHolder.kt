package org.mozilla.firefox.vpn.apptunneling.ui

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.firefox.vpn.apptunneling.ui.ExpandableItem.AppGroup
import org.mozilla.firefox.vpn.databinding.ItemAppGroupBinding

class AppGroupViewHolder(
    itemView: View,
    private val onAppGroupClicked: (AppGroupViewHolder) -> Unit,
    private val onAppGroupActionClicked: (AppGroupViewHolder) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemAppGroupBinding.bind(itemView)

    init {
        binding.root.setOnClickListener { onAppGroupClicked(this) }
        binding.groupAction.setOnClickListener { onAppGroupActionClicked(this) }
    }

    fun bind(appGroupItem: AppGroup) {
        when (appGroupItem.groupType) {
            AppGroupType.PROTECTED -> {
                if (appGroupItem.isExpanded) {
                    updateAppGroupState(appGroupItem, AppGroupConfig.ProtectedGroupExpanded)
                } else {
                    updateAppGroupState(appGroupItem, AppGroupConfig.ProtectedGroupCollapse)
                }
            }
            AppGroupType.UNPROTECTED -> {
                if (appGroupItem.isExpanded) {
                    updateAppGroupState(appGroupItem, AppGroupConfig.UnprotectedGroupExpanded)
                } else {
                    updateAppGroupState(appGroupItem, AppGroupConfig.UnprotectedGroupCollapse)
                }
            }
        }
    }

    private fun updateAppGroupState(appGroupItem: AppGroup, config: AppGroupConfig) {
        val context = binding.root.context
        binding.dropdownArrow.setImageDrawable(ContextCompat.getDrawable(context, config.arrowDrawableId))
        binding.groupDescription.text = HtmlCompat.fromHtml(context.getString(config.descriptionResId), 0)
        binding.groupAction.text = context.getString(config.actionResId)

        if (appGroupItem.isExpanded) {
            binding.groupTitle.text = context.getString(config.titleResId)
            binding.groupDescription.visibility = View.VISIBLE
            binding.groupAction.visibility = View.VISIBLE

            if (appGroupItem.appItems.isEmpty()) {
                binding.infoView.root.visibility = View.VISIBLE
                binding.infoView.infoText.text = context.getString(config.infoResId)
            } else {
                binding.infoView.root.visibility = View.GONE
            }
        } else {
            binding.groupTitle.text = String.format(context.getString(config.titleResId), appGroupItem.appItems.size)
            binding.groupDescription.visibility = View.GONE
            binding.groupAction.visibility = View.GONE
            binding.infoView.root.visibility = View.GONE
        }

        if (appGroupItem.isEnabled) {
            binding.groupAction.setOnClickListener { onAppGroupActionClicked(this) }
            binding.groupAction.alpha = 1f
        } else {
            binding.groupAction.setOnClickListener(null)
            binding.groupAction.alpha = 0.5f
        }
    }
}
