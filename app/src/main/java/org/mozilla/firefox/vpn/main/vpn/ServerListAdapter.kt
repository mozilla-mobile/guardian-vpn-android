package org.mozilla.firefox.vpn.main.vpn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_country.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.ServerListAdapter.ServerViewHolder
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.util.EmojiUtil

class ServerListAdapter(private val items: List<ServerInfo>) : RecyclerView.Adapter<ServerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder =
        ServerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false))

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ServerViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: ServerInfo) {
            country_emoji.text = EmojiUtil.loadEmoji(EmojiUtil.getCountryFlagCodePoint(item.country.code))
            country_name.text = item.country.name
        }
    }
}