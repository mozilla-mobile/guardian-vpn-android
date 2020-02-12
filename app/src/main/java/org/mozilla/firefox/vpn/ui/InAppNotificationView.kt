package org.mozilla.firefox.vpn.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import kotlinx.android.synthetic.main.view_in_app_notification.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.util.StringResource
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.tint

object InAppNotificationView {

    fun inflate(context: Context, config: Config): View {
        return View.inflate(context, R.layout.view_in_app_notification, null).apply {
            background = context.getDrawable(config.style.bkgDrawableId)

            initText(this, config)
            initTextAction(this, config)
            initCloseButton(this, config)
        }
    }

    private fun initText(view: View, config: Config) {
        view.text.setTextColor(view.context.color(config.style.textColorId))
        view.text.text = config.text.resolve(view.context)
    }

    private fun initTextAction(view: View, config: Config) {
        val action = config.textAction ?: run {
            return
        }

        view.setOnClickListener { action.action() }

        val actionText = action.text.resolve(view.context) ?: return
        val text = "${view.text.text} $actionText"
        val spannable = SpannableString(text)
        val start = view.text.text.length + 1
        val end = start + actionText.length

        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(boldSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        view.text.text = spannable
    }

    private fun initCloseButton(view: View, config: Config) {
        val closeAction = config.closeAction ?: run {
            view.close_views.visibility = View.GONE
            return
        }

        view.close.background = view.context.getDrawable(config.style.closeAreaBkgDrawableId)
        view.close_icon.background.tint(view.context.color(config.style.closeIconColorId))
        view.close.setOnClickListener {
            closeAction()
        }
        view.close_views.visibility = View.VISIBLE
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
        object Red : Style(R.drawable.ripple_in_app_notification_red, R.color.gray50, R.drawable.ripple_in_app_notification_close_btn_red, R.color.gray50)
        object Blue : Style(R.drawable.ripple_in_app_notification_blue, android.R.color.white, R.drawable.ripple_in_app_notification_close_btn_blue, android.R.color.white)
        object Green : Style(R.drawable.ripple_in_app_notification_green, R.color.gray50, R.drawable.ripple_in_app_notification_close_btn_green, R.color.gray50)
    }
}

fun InAppNotificationView.Config.action(text: StringResource, action: () -> Unit): InAppNotificationView.Config {
    return this.apply { textAction = InAppNotificationView.TextAction(text, action) }
}

fun InAppNotificationView.Config.close(action: () -> Unit): InAppNotificationView.Config {
    return this.apply { closeAction = action }
}
