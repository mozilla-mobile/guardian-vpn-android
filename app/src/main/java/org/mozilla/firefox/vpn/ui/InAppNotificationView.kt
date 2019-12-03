package org.mozilla.firefox.vpn.ui

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import kotlinx.android.synthetic.main.view_in_app_notification.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.tint

object InAppNotificationView {

    fun inflate(context: Context, config: Config): View {
        return View.inflate(context, R.layout.view_in_app_notification, null).apply {
            background = background.tint(context.color(config.style.bkgColorId))
            initText(this, config)
            initTextAction(this, config)
            initCloseButton(this, config)
        }
    }

    private fun initText(view: View, config: Config) {
        view.text.setTextColor(view.context.color(config.style.textColorId))
        view.text.text = config.text
    }

    private fun initTextAction(view: View, config: Config) {
        val action = config.textAction ?: run {
            view.action_text.visibility = View.GONE
            return
        }

        view.action_text.setTextColor(view.context.color(config.style.textColorId))
        val spanned = SpannableString(action.text).apply {
            setSpan(UnderlineSpan(), 0, action.text.length, 0)
        }
        view.action_text.text = spanned
        view.action_text.setOnClickListener { action.action() }
        view.action_text.visibility = View.VISIBLE
    }

    private fun initCloseButton(view: View, config: Config) {
        val closeAction = config.closeAction ?: run {
            view.close_views.visibility = View.GONE
            return
        }

        view.close.background.tint(view.context.color(config.style.closeAreaColorId))
        view.close_icon.background.tint(view.context.color(config.style.closeIconColorId))
        view.close.setOnClickListener {
            closeAction()
        }
        view.close_views.visibility = View.VISIBLE
    }

    data class Config(
        val style: Style = Style.Green,
        val text: String,
        val textAction: TextAction? = null,
        val closeAction: (() -> Unit)? = null
    )

    data class TextAction(
        val text: String,
        val action: () -> Unit
    )

    sealed class Style(
        val bkgColorId: Int,
        val textColorId: Int,
        val closeAreaColorId: Int,
        val closeIconColorId: Int
    ) {
        object Red : Style(R.color.red40, R.color.gray50, R.color.red50, R.color.gray50)
        object Blue : Style(R.color.blue50, android.R.color.white, R.color.blue60, android.R.color.white)
        object Green : Style(R.color.green40, R.color.gray50, R.color.green50, R.color.gray50)
    }
}
