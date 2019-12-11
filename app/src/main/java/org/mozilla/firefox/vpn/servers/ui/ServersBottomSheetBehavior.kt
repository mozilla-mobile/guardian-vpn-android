package org.mozilla.firefox.vpn.servers.ui

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ServersBottomSheetBehavior<T : View> : BottomSheetBehavior<T>() {

    private var isScrolled = false
    private var isDown = false

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (isDown && target.scrollY > 0) {
            isScrolled = true
        }

        if (dy > 0 || !isScrolled) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        target: View,
        type: Int
    ) {
        isDown = false
        super.onStopNestedScroll(coordinatorLayout, child, target, type)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: T,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        isDown = true
        isScrolled = false
        return super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }
}
