package org.mozilla.firefox.vpn.servers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.ViewCountryFolderBinding
import org.mozilla.firefox.vpn.servers.data.CountryInfo
import org.mozilla.firefox.vpn.util.getCountryFlag

class CountryFolderView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val binding: ViewCountryFolderBinding =
        ViewCountryFolderBinding.inflate(LayoutInflater.from(context), this, true)

    private var isFolderOpen = false
    private var listener: OnExpandListener? = null

    fun setOnExpandListener(listener: OnExpandListener) {
        this.listener = listener
    }

    fun setCountry(country: CountryInfo) {

        binding.countryFlag.setImageResource(context.getCountryFlag(country.code))
        binding.countryName.text = country.name

        setOnClickListener {
            if (isFolderOpen) {
                isFolderOpen = false
                binding.dropdownArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_close))
            } else {
                isFolderOpen = true
                binding.dropdownArrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_open))
            }
            listener?.onExpand(country, isFolderOpen)
        }
    }

    interface OnExpandListener {
        fun onExpand(country: CountryInfo, isExpand: Boolean)
    }
}
