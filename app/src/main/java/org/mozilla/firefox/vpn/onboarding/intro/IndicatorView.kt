package org.mozilla.firefox.vpn.onboarding.intro

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_indicator.view.*
import org.mozilla.firefox.vpn.R

class IndicatorView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val activeIndicator: Drawable?
    private val inactiveIndicator: Drawable?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_indicator, this, true)

        activeIndicator = ContextCompat.getDrawable(context,R.drawable.ic_indicator_active)
        inactiveIndicator = ContextCompat.getDrawable(context,R.drawable.ic_indicator_inactive)
    }

    fun updateIndicatorStatus(position: Int) {
        indicator_1.setImageDrawable(if (position == 0) activeIndicator else inactiveIndicator)
        indicator_2.setImageDrawable(if (position == 1) activeIndicator else inactiveIndicator)
        indicator_3.setImageDrawable(if (position == 2) activeIndicator else inactiveIndicator)
    }
}