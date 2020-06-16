package org.mozilla.firefox.vpn.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.ViewInAppNotificationBinding
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.tint

object InAppNotificationView {

    fun inflate(context: Context, config: Config): View {
        return View.inflate(context, R.layout.view_in_app_notification, null).apply {
            background = context.getDrawable(config.style.bkgDrawableId)

            val binding = ViewInAppNotificationBinding.bind(this)
            initText(binding, config)
            initTextAction(binding, config)
            initCloseButton(binding, config)
        }
    }

    private fun initText(binding: ViewInAppNotificationBinding, config: Config) {
        binding.text.setTextColor(binding.root.context.color(config.style.textColorId))
        binding.text.text = config.text.resolve(binding.root.context)
    }

    private fun initTextAction(binding: ViewInAppNotificationBinding, config: Config) {
        val action = config.textAction ?: run {
            return
        }

        binding.root.setOnClickListener { action.action() }

        val actionText = action.text.resolve(binding.root.context) ?: return
        val text = "${binding.text.text} $actionText"
        val spannable = SpannableString(text)
        val start = binding.text.text.length + 1
        val end = start + actionText.length

        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(boldSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        binding.text.text = spannable
    }

    private fun initCloseButton(binding: ViewInAppNotificationBinding, config: Config) {
        val closeAction = config.closeAction ?: run {
            binding.closeViews.visibility = View.GONE
            return
        }

        binding.close.background = binding.root.context.getDrawable(config.style.closeAreaBkgDrawableId)
        binding.closeIcon.background.tint(binding.root.context.color(config.style.closeIconColorId))
        binding.close.setOnClickListener {
            closeAction()
        }
        binding.closeViews.visibility = View.VISIBLE
    }

    data class Config(
        val style: Style = Style.Green,
        val text: StringResource,
        var textAction: TextAction? = null,
        var closeAction: (() -> Unit)? = null
    ) {
        companion object {
            fun warning(text: StringResource) = Config(Style.Red, text)
        }
    }

    data class TextAction(
        val text: StringResource,
        val action: () -> Unit
    )

    sealed class Style(
        val bkgDrawableId: Int,
        val textColorId: Int,
        val closeAreaBkgDrawableId: Int,
        val closeIconColorId: Int
    ) {
        object Red : Style(
            R.drawable.ripple_in_app_notification_red,
            android.R.color.white,
            R.drawable.ripple_in_app_notification_close_btn_red,
            android.R.color.white
        )
        object Blue : Style(
            R.drawable.ripple_in_app_notification_blue,
            android.R.color.white,
            R.drawable.ripple_in_app_notification_close_btn_blue,
            android.R.color.white
        )
        object Green : Style(
            R.drawable.ripple_in_app_notification_green,
            R.color.gray50,
            R.drawable.ripple_in_app_notification_close_btn_green,
            R.color.gray50
        )
    }
}

fun InAppNotificationView.Config.action(text: StringResource, action: () -> Unit): InAppNotificationView.Config {
    return this.apply { textAction = InAppNotificationView.TextAction(text, action) }
}

fun InAppNotificationView.Config.close(action: () -> Unit): InAppNotificationView.Config {
    return this.apply { closeAction = action }
}
