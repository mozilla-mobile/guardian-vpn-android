package org.mozilla.firefox.vpn.apptunneling.ui

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.request.RequestDisposable
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.apptunneling.isSystemApp
import org.mozilla.firefox.vpn.apptunneling.ui.ExpandableItem.AppItem
import org.mozilla.firefox.vpn.databinding.ItemAppBinding

class AppItemViewHolder(
    itemView: View,
    private val onAppItemChecked: (AppItemViewHolder) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemAppBinding.bind(itemView)

    private var imageRequest: RequestDisposable? = null

    init {
        binding.appCheckbox.setOnClickListener { onAppItemChecked(this) }
    }

    fun bind(appItem: AppItem) {
        binding.appName.text = appItem.applicationInfo.loadLabel(binding.root.context.packageManager).toString()
        binding.appPackageName.text = appItem.applicationInfo.packageName
        imageRequest = binding.appIcon.load("android.resource://${appItem.applicationInfo.packageName}/${appItem.applicationInfo.icon}") {
            error(R.drawable.ic_sys_def_app)
        }
        binding.appCheckbox.isChecked = appItem.groupType == AppGroupType.PROTECTED
        binding.appCheckbox.isEnabled = appItem.isEnabled

        if (BuildConfig.DEBUG) {
            bindDebugInfo(appItem)
        }
    }

    private fun bindDebugInfo(appItem: AppItem) {
        if (appItem.applicationInfo.isSystemApp()) {
            val appName = binding.appName.text
            val postfix = "  system app "
            val spanBuilder = SpannableStringBuilder("$appName$postfix")

            fun setSpan(what: Any) = spanBuilder.setSpan(
                what,
                appName.length + 1,
                spanBuilder.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )

            setSpan(BackgroundColorSpan(binding.root.context.getColor(R.color.red50)))
            setSpan(ForegroundColorSpan(Color.WHITE))
            binding.appName.text = spanBuilder
        }
    }

    fun unbind() {
        imageRequest?.dispose()
    }
}
