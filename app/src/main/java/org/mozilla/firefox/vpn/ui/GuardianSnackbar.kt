package org.mozilla.firefox.vpn.ui

import android.graphics.Color
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.ContentViewCallback
import kotlinx.android.synthetic.main.view_guardian_snackbar.view.*
import org.mozilla.firefox.vpn.R
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout

class GuardianSnackbar(
    parentView: ViewGroup,
    contentView: View,
    config: Config,
    callback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<GuardianSnackbar>(parentView, contentView, callback) {

    init {
        view.setPadding(0, 0, 0, 0)

        val bkgColor: Int
        val textColor: Int
        when (config.style) {
            Style.Red -> {
                bkgColor = ContextCompat.getColor(parentView.context, R.color.red40)
                textColor = ContextCompat.getColor(parentView.context, R.color.gray50)
            }
            Style.Blue -> {
                bkgColor = ContextCompat.getColor(parentView.context, R.color.blue50)
                textColor = Color.WHITE
            }
            Style.Green -> {
                bkgColor = ContextCompat.getColor(parentView.context, R.color.green50)
                textColor = ContextCompat.getColor(parentView.context, R.color.gray50)
            }
        }
        val bkgDrawable = ContextCompat.getDrawable(parentView.context, R.drawable.bg_snackbar)
        val wrapped = bkgDrawable?.let { DrawableCompat.wrap(it) }
        wrapped?.setTint(bkgColor)

        view.apply {
            background = wrapped
            elevation = 0f
        }

        contentView.text.apply {
            setTextColor(textColor)
            text = config.text
        }

        config.textAction?.let { textAction ->
            contentView.action_text.apply {
                setTextColor(textColor)
                val spanned = SpannableString(textAction.text).apply {
                    setSpan(UnderlineSpan(), 0, textAction.text.length, 0)
                }
                text = spanned
                setOnClickListener { textAction.action() }
            }
        }
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

    enum class Style {
        Red, Green, Blue
    }

    companion object {
        const val LENGTH_INDEFINITE = -2
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0

        fun make(parent: ViewGroup, config: Config, duration: Int): GuardianSnackbar {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.view_guardian_snackbar, parent, false)
            val callback = ContentViewCallback(view)
            val snackBar = GuardianSnackbar(findSuitableParent(parent)!!, view, config, callback)
            snackBar.duration = duration
            return snackBar
        }

        private fun findSuitableParent(view: View?): ViewGroup? {
            var suitableView = view
            var fallback: ViewGroup? = null

            do {
                if (suitableView is CoordinatorLayout) {
                    return suitableView
                }

                if (suitableView is FrameLayout) {
                    if (suitableView.id == android.R.id.content) {
                        return suitableView
                    }

                    fallback = suitableView
                }

                if (suitableView != null) {
                    val parent = suitableView.parent
                    suitableView = if (parent is View) parent else null
                }
            } while (view != null)

            return fallback
        }
    }
}

private class ContentViewCallback(private val messageView: View) : ContentViewCallback {

    override fun animateContentIn(delay: Int, duration: Int) {
        this.messageView.alpha = 0.0f
        this.messageView.animate().alpha(1.0f).setDuration(duration.toLong())
            .setStartDelay(delay.toLong()).start()
    }

    override fun animateContentOut(delay: Int, duration: Int) {
        this.messageView.alpha = 1.0f
        this.messageView.animate().alpha(0.0f).setDuration(duration.toLong())
            .setStartDelay(delay.toLong()).start()
    }
}
