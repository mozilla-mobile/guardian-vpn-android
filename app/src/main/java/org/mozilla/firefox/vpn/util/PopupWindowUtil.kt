package org.mozilla.firefox.vpn.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow

class PopupWindowUtil(
    context: Context,
    layoutResId: Int,
    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    height: Int = WindowManager.LayoutParams.WRAP_CONTENT
) {

    private val rootView: View = LayoutInflater.from(context).inflate(layoutResId, null)
    private val popupWindow: PopupWindow = PopupWindow(rootView, width, height, true)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupWindow.isTouchModal = true
        }
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun getRootView(): View = rootView

    fun showAtLocation(parent: View, gravity: Int, x: Int = 0, y: Int = 0) {
        popupWindow.showAtLocation(parent, gravity, x, y)
        popupWindow.dimBehind()
    }

    private fun PopupWindow.dimBehind() {
        val container = contentView.rootView
        val context = contentView.context
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = container.layoutParams as WindowManager.LayoutParams
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        params.dimAmount = 0.3f
        windowManager.updateViewLayout(container, params)
    }
}
