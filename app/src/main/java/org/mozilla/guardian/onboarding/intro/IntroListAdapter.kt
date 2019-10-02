package org.mozilla.guardian.onboarding.intro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_intro.*
import org.mozilla.guardian.R
import org.mozilla.guardian.onboarding.intro.IntroListAdapter.IntroViewHolder

class IntroListAdapter : RecyclerView.Adapter<IntroViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder =
        IntroViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_intro, parent, false))

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) = holder.bind(position)

    override fun getItemCount(): Int = 4

    class IntroViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        private val introImageList = listOf(
            R.drawable.ic_intro_1,
            R.drawable.ic_intro_2,
            R.drawable.ic_intro_3,
            R.drawable.ic_intro_4
        )

        private val introTitleList = listOf(
            R.string.intro_title_1,
            R.string.intro_title_2,
            R.string.intro_title_3,
            R.string.intro_title_4
        )

        private val introDescriptionList = listOf(
            R.string.intro_description_1,
            R.string.intro_description_2,
            R.string.intro_description_3,
            R.string.intro_description_4
        )

        fun bind(position: Int) {
            intro_image.setImageResource(introImageList[position])
            intro_title.setText(introTitleList[position])
            intro_description.setText(introDescriptionList[position])
        }
    }
}