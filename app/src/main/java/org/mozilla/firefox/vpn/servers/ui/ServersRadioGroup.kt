package org.mozilla.firefox.vpn.servers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RadioButton
import android.widget.RadioGroup
import kotlinx.android.synthetic.main.view_servers_radio_group.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.servers.data.CountryInfo
import org.mozilla.firefox.vpn.servers.data.ServerInfo

class ServersRadioGroup : RadioGroup {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val radioButtonTextPadding = resources.getDimensionPixelSize(R.dimen.radio_button_text_padding)
    private val radioButtonVerticalPadding = resources.getDimensionPixelSize(R.dimen.radio_button_vertical_padding)
    private val radioButtonLeftMargin = resources.getDimensionPixelSize(R.dimen.radio_button_left_margin)

    private var lastAddedCountry: String = ""
    private var listener: OnServerCheckListener? = null

    private val countryFolderViewMap = mutableMapOf<CountryInfo, CountryFolderView>()
    private val serverViewMap = mutableMapOf<ServerInfo, RadioButton>()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_servers_radio_group, this, true)
    }

    fun setOnServerCheckListener(listener: OnServerCheckListener) {
        this.listener = listener
    }

    fun setServers(servers: List<ServerInfo>) {
        countryFolderViewMap.clear()
        serverViewMap.clear()
        servers.forEachIndexed { index, server ->
            if (lastAddedCountry != server.country.name) {
                lastAddedCountry = server.country.name
                addCountryFolder(server.country)
            }
            addServerRadioButton(server, index)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (index in 0 until servers_group.childCount) {
            servers_group.getChildAt(index).isEnabled = enabled
        }
    }

    fun setSelectedServer(server: ServerInfo) {
        serverViewMap[server]?.let {
            it.isChecked = true
            scroll_view.post {
                scroll_view.scrollTo(0, it.y.toInt() - scroll_view.measuredHeight / 2)
            }
        }
        countryFolderViewMap[server.country]?.let {
            it.performClick()
        }
    }

    private fun addCountryFolder(countryInfo: CountryInfo) {
        val countryFolderView = CountryFolderView(context)
        countryFolderView.setCountry(countryInfo)
        countryFolderView.setOnExpandListener(onExpandListener)
        servers_group.addView(countryFolderView, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        countryFolderViewMap[countryInfo] = countryFolderView
    }

    private fun addServerRadioButton(serverInfo: ServerInfo, index: Int) {
        val radioButton = RadioButton(context).apply {
            id = index
            tag = serverInfo.country.code
            text = serverInfo.city.name
            setPadding(radioButtonTextPadding, radioButtonVerticalPadding, 0, radioButtonVerticalPadding)
            visibility = View.GONE
            setOnClickListener {
                listener?.onCheck(serverInfo)
            }
        }
        val params = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        params.setMargins(radioButtonLeftMargin, 0, 0, 0)
        servers_group.addView(radioButton, params)
        serverViewMap[serverInfo] = radioButton
    }

    private val onExpandListener = object : CountryFolderView.OnExpandListener {
        override fun onExpand(country: CountryInfo, isExpand: Boolean) {
            for (index in 0 until servers_group.childCount) {
                if (servers_group.getChildAt(index).tag == country.code) {
                    servers_group.getChildAt(index).visibility = if (isExpand) View.VISIBLE else View.GONE
                }
            }
        }
    }

    interface OnServerCheckListener {
        fun onCheck(serverInfo: ServerInfo)
    }
}
