package org.mozilla.firefox.vpn.onboarding.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.FragmentIntroBinding
import org.mozilla.firefox.vpn.util.viewBinding

class IntroFragment : BottomSheetDialogFragment() {

    private var binding: FragmentIntroBinding by viewBinding()

    private lateinit var adapter: IntroAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupIntroPager()
        setupIndicators()
        setCurrentIndicator(0)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.skipBtn.setOnClickListener {
            binding.introPager.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) ?: return@let
            val params = (bottomSheet.layoutParams as? CoordinatorLayout.LayoutParams) ?: return@let
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.behavior = BottomSheetBehavior<FrameLayout>()

            (params.behavior as? BottomSheetBehavior)?.apply {
                addBottomSheetCallback(bottomSheetBehaviorCallback)
                peekHeight = 0
                state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (isAdded) {
            return
        } else {
            super.show(manager, tag)
        }
    }

    private fun setupIntroPager() {
        adapter = IntroAdapter()
        binding.introPager.adapter = adapter
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.introPager)
        binding.introPager.isNestedScrollingEnabled = false

        binding.introPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                setCurrentIndicator(position)
            }
        })
    }

    private fun setupIndicators() {
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(8, 0, 8, 0)
        for (i in 0 until adapter.itemCount) {
            val imageView = ImageView(context)
            imageView.layoutParams = params
            binding.indicatorLayout.addView(imageView)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorLayout[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_indicator_inactive))
            }
        }
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dismissAllowingStateLoss()
            }
        }
    }

    companion object {
        fun newInstance(): IntroFragment = IntroFragment()
    }
}
