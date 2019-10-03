package org.mozilla.guardian.main.vpn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_country.*
import org.mozilla.guardian.R
import org.mozilla.guardian.main.vpn.ServerListAdapter.ServerViewHolder
import org.mozilla.guardian.user.data.Country

class ServerListAdapter(private val items: List<Country>) : RecyclerView.Adapter<ServerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder =
        ServerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false))

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ServerViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: Country) {
            country_name.text = item.name
        }
    }
}