package org.mozilla.firefox.vpn.main.vpn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_country.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.main.vpn.ServerListAdapter.ServerViewHolder
import org.mozilla.firefox.vpn.service.Country

class ServerListAdapter(private val items: List<Country>) : RecyclerView.Adapter<ServerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder =
        ServerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false))

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    class ServerViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        //TODO: Need UiModel cache
        private var isExpand = true

        private val textPadding = itemView.resources.getDimensionPixelSize(R.dimen.radio_button_text_padding)
        private val verticalPadding = itemView.resources.getDimensionPixelSize(R.dimen.radio_button_vertical_padding)

        fun bind(item: Country) {
            country_name.text = item.name
            city_group.removeAllViews()

            for (city in item.cities) {
                val radioButton = RadioButton(itemView.context).apply {
                    text = city.name
                    setPadding(textPadding, verticalPadding, 0, verticalPadding)
                }
                city_group.addView(radioButton)
            }

            country_header.setOnClickListener {
                if (isExpand) {
                    isExpand = false
                    city_group.visibility = View.GONE
                    dropdown_arrow.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_close))
                } else {
                    isExpand = true
                    city_group.visibility = View.VISIBLE
                    dropdown_arrow.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_open))
                }
            }
        }
    }
}