package org.mozilla.firefox.vpn.servers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_country_folder.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.servers.data.CountryInfo
import org.mozilla.firefox.vpn.util.EmojiUtil

class CountryFolderView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var isFolderOpen = true

    private var listener: OnExpandListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_country_folder, this, true)
    }

    fun setOnExpandListener(listener: OnExpandListener) {
        this.listener = listener
    }

    fun setCountry(country: CountryInfo) {

        country_emoji.text = EmojiUtil.loadEmoji(EmojiUtil.getCountryFlagCodePoint(country.code))
        country_name.text = country.name

        country_folder.setOnClickListener {
            if (isFolderOpen) {
                isFolderOpen = false
                dropdown_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_close))
            } else {
                isFolderOpen = true
                dropdown_arrow.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_open))
            }
            listener?.onExpand(country, isFolderOpen)
        }
    }

    interface OnExpandListener {
        fun onExpand(country: CountryInfo, isExpand: Boolean)
    }
}
