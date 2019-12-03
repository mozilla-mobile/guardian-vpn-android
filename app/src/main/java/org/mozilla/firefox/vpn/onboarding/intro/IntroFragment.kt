package org.mozilla.firefox.vpn.onboarding.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_intro.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity

class IntroFragment : Fragment() {

    private lateinit var adapter: IntroListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        btn_close.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_entrance))
        btn_skip.setOnClickListener {
            intro_list.smoothScrollToPosition(adapter.itemCount - 1)
        }
        btn_auth.setOnClickListener {
            val activity = activity as? OnboardingActivity ?: return@setOnClickListener
            activity.startLoginFlow()
        }
    }

    private fun initRecyclerView() {
        adapter = IntroListAdapter()
        intro_list.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(intro_list)
        indicator_view.updateIndicatorStatus(0)

        intro_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (position == adapter.itemCount - 1) {
                        indicator_view.visibility = View.GONE
                        btn_skip.visibility = View.GONE
                        btn_auth.visibility = View.VISIBLE
                    } else {
                        indicator_view.updateIndicatorStatus(position)
                        indicator_view.visibility = View.VISIBLE
                        btn_skip.visibility = View.VISIBLE
                        btn_auth.visibility = View.GONE
                    }
                }
            }
        })
    }
}
