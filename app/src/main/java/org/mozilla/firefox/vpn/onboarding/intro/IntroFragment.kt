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
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.FragmentIntroBinding
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.util.viewBinding

class IntroFragment : Fragment() {

    private var binding: FragmentIntroBinding by viewBinding()

    private lateinit var adapter: IntroListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.closeBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_entrance))
        binding.skipBtn.setOnClickListener {
            binding.introList.smoothScrollToPosition(adapter.itemCount - 1)
        }
        binding.authBtn.setOnClickListener {
            val activity = activity as? OnboardingActivity ?: return@setOnClickListener
            activity.startLoginFlow()
        }
    }

    private fun initRecyclerView() {
        adapter = IntroListAdapter()
        binding.introList.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.introList)
        binding.indicatorView.updateIndicatorStatus(0)

        binding.introList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (position == adapter.itemCount - 1) {
                        binding.indicatorView.visibility = View.GONE
                        binding.skipBtn.visibility = View.GONE
                        binding.authBtn.visibility = View.VISIBLE
                    } else {
                        binding.indicatorView.updateIndicatorStatus(position)
                        binding.indicatorView.visibility = View.VISIBLE
                        binding.skipBtn.visibility = View.VISIBLE
                        binding.authBtn.visibility = View.GONE
                    }
                }
            }
        })
    }
}
