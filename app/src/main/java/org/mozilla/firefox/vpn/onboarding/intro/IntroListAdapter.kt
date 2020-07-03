package org.mozilla.firefox.vpn.onboarding.intro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.ItemIntroBinding
import org.mozilla.firefox.vpn.onboarding.intro.IntroListAdapter.IntroViewHolder

class IntroListAdapter : RecyclerView.Adapter<IntroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder =
        IntroViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_intro, parent, false))

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = 4

    class IntroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemIntroBinding.bind(itemView)

        private val introImageList = listOf(
            R.drawable.ic_intro_1,
            R.drawable.ic_intro_2,
            R.drawable.ic_intro_3,
            R.drawable.ic_intro_4
        )

        private val introTitleList = listOf(
            R.string.nux_title_1,
            R.string.nux_title_2,
            R.string.nux_title_3,
            R.string.nux_title_4
        )

        private val introDescriptionList = listOf(
            R.string.nux_content_1,
            R.string.nux_content_2,
            R.string.nux_content_3,
            R.string.nux_content_4
        )

        fun bind(position: Int) {
            binding.introImage.setImageResource(introImageList[position])
            binding.introTitle.setText(introTitleList[position])
            binding.introDescription.setText(introDescriptionList[position])
        }
    }
}
