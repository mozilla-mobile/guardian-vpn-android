package org.mozilla.firefox.vpn.servers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_servers.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.util.viewModel

class ServersFragment : BottomSheetDialogFragment() {

    private val component by lazy {
        ServersComponentImpl(context!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_servers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeServers()

        btn_cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.setBottomSheetCallback(bottomSheetBehaviorCallback)
            behavior.peekHeight = 0
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun observeServers() {
        viewModel.servers.observe(viewLifecycleOwner, Observer { servers ->
            servers?.let {
                server_list.adapter =
                    ServerListAdapter(it)
            }
        })
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(): ServersFragment = ServersFragment()
    }
}