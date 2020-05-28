package org.mozilla.firefox.vpn.onboarding.intro

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.ViewIndicatorBinding

class IndicatorView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: ViewIndicatorBinding =
        ViewIndicatorBinding.inflate(LayoutInflater.from(context), this, true)
    private val activeIndicator: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_indicator_active)
    private val inactiveIndicator: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_indicator_inactive)

    fun updateIndicatorStatus(position: Int) {
        binding.indicator1.setImageDrawable(if (position == 0) activeIndicator else inactiveIndicator)
        binding.indicator2.setImageDrawable(if (position == 1) activeIndicator else inactiveIndicator)
        binding.indicator3.setImageDrawable(if (position == 2) activeIndicator else inactiveIndicator)
    }
}
