package org.mozilla.firefox.vpn.servers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.view_servers_radio_group.view.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.servers.data.CountryInfo
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.util.color
import org.mozilla.firefox.vpn.util.colorStateList
import org.mozilla.firefox.vpn.util.tint

class ServersRadioGroup : RadioGroup {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    private val radioButtonTextPadding = resources.getDimensionPixelSize(R.dimen.radio_button_text_padding)
    private val radioButtonVerticalPadding = resources.getDimensionPixelSize(R.dimen.radio_button_vertical_padding)
    private val radioButtonLeftMargin = resources.getDimensionPixelSize(R.dimen.radio_button_left_margin)

    private val countryFolderViewMap = mutableMapOf<CountryInfo, CountryFolderView>()
    private val serverViewMap = mutableMapOf<ServerInfo, RadioButton>()
    private val serverStateIcon = ContextCompat.getDrawable(context, R.drawable.ic_error)

    private var listener: OnServerCheckListener? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_servers_radio_group, this, true)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (index in 0 until servers_group.childCount) {
            servers_group.getChildAt(index).isEnabled = enabled
        }
    }

    fun setOnServerCheckListener(listener: OnServerCheckListener) {
        this.listener = listener
    }

    fun setServers(servers: List<ServerInfo>) {
        countryFolderViewMap.clear()
        serverViewMap.clear()
        servers.groupBy { it.country }
            .forEach { (countryInfo, serverInfoList) ->
                addCountryFolder(countryInfo)
                serverInfoList.forEach { serverInfo ->
                    addServerRadioButton(serverInfo)
                }
            }
    }

    fun setSelectedServer(server: ServerInfo) {
        serverViewMap[server]?.let {
            it.isChecked = true
            scroll_view.post {
                scroll_view.scrollTo(0, it.y.toInt() - scroll_view.measuredHeight / 2)
            }
        }
        countryFolderViewMap[server.country]?.performClick()
    }

    fun setSelectedServerWithState(selectedServer: ServerInfo?, state: VpnState) {
        selectedServer?.let {
            serverViewMap[it]?.setCompoundDrawablesWithIntrinsicBounds(
                null, null,
                when (state) {
                    is VpnState.Unstable -> serverStateIcon?.tint(context.color(R.color.yellow50))
                    is VpnState.NoSignal -> serverStateIcon?.tint(context.color(R.color.red50))
                    else -> null
                }, null)
        }
    }

    private fun addCountryFolder(countryInfo: CountryInfo) {
        val countryFolderView = CountryFolderView(context)
        countryFolderView.setCountry(countryInfo)
        countryFolderView.setOnExpandListener(onExpandListener)
        servers_group.addView(countryFolderView, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        countryFolderViewMap[countryInfo] = countryFolderView
    }

    private fun addServerRadioButton(serverInfo: ServerInfo) {
        val radioButton = RadioButton(context).apply {
            tag = serverInfo.country.code
            text = serverInfo.city.name
            visibility = View.GONE
            buttonTintList = context.colorStateList(R.color.radio_button)
            setTextAppearance(R.style.TextAppearance_Guardian_Body10)
            setPadding(radioButtonTextPadding, radioButtonVerticalPadding, radioButtonTextPadding, radioButtonVerticalPadding)
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
