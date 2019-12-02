package org.mozilla.firefox.vpn.ui

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.ContentViewCallback

class GuardianSnackbar(
    parentView: ViewGroup,
    contentView: View,
    callback: com.google.android.material.snackbar.ContentViewCallback
) : BaseTransientBottomBar<GuardianSnackbar>(parentView, contentView, callback) {

    init {
        view.setPadding(0, 0, 0, 0)
        view.elevation = 0f
    }

    companion object {
        const val LENGTH_INDEFINITE = -2
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0

        fun make(parentView: ViewGroup, config: InAppNotificationView.Config, duration: Int): GuardianSnackbar {
            val contentView = InAppNotificationView.inflate(parentView.context, config)
            val callback = ContentViewCallback(contentView)
            val snackBar = GuardianSnackbar(findSuitableParent(parentView)!!, contentView, callback)
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
