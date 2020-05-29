package org.mozilla.firefox.vpn.servers.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.databinding.FragmentServersBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.main.vpn.domain.VpnState
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class ServersFragment : BottomSheetDialogFragment() {

    private val component by lazy {
        ServersComponentImpl(context!!.coreComponent, context!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    private var binding: FragmentServersBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentServersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.radioGroup.setOnServerCheckListener(onServerCheckListener)

        binding.cancelBtn.setOnClickListener {
            dismiss()
        }

        observeServers()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet = it.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) ?: return@let
            val params = (bottomSheet.layoutParams as? CoordinatorLayout.LayoutParams) ?: return@let
            params.height = ViewGroup.LayoutParams.MATCH_PARENT

            if (params.behavior !is ServersBottomSheetBehavior) {
                params.behavior = ServersBottomSheetBehavior<FrameLayout>()
            }

            (params.behavior as? BottomSheetBehavior)?.apply {
                setBottomSheetCallback(bottomSheetBehaviorCallback)
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

    private fun observeServers() {
        viewModel.servers.observe(viewLifecycleOwner, Observer { servers ->
            servers?.let {
                binding.radioGroup.setServers(it)
                viewModel.updateSelectedServer()
            }
        })

        viewModel.selectedServer.observe(viewLifecycleOwner, Observer {
            binding.radioGroup.setSelectedServer(it)
        })

        viewModel.vpnState.observe(viewLifecycleOwner, Observer { vpnState ->
            binding.connectionText.text = when (vpnState) {
                is VpnState.Connecting -> getString(R.string.hero_text_connecting)
                is VpnState.Disconnecting -> getString(R.string.hero_text_disconnecting)
                is VpnState.Switching -> getString(R.string.hero_text_switching)
                else -> getString(R.string.connection_page_title)
            }

            binding.radioGroup.isEnabled = when (vpnState) {
                is VpnState.Connecting,
                is VpnState.Disconnecting,
                is VpnState.Switching -> false
                else -> true
            }
        })

        viewModel.selectedServerWithVpnState.observe(viewLifecycleOwner, Observer { pair ->
            binding.radioGroup.setSelectedServerWithState(pair.first, pair.second)
        })
    }

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dismissAllowingStateLoss()
            }
        }
    }

    private val onServerCheckListener = object : ServersRadioGroup.OnServerCheckListener {
        override fun onCheck(serverInfo: ServerInfo) {
            viewModel.executeAction(ServersViewModel.Action.Switch(serverInfo))
            view?.postDelayed({ collapseSmoothly() }, DURATION_RADIO_CHECK_ANIM)
        }
    }

    private fun collapseSmoothly() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }

        val bottomSheet = dialog?.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val behavior = try {
            BottomSheetBehavior.from(bottomSheet)
        } catch (e: IllegalArgumentException) {
            return
        }

        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    companion object {
        fun newInstance(): ServersFragment = ServersFragment()

        /** Refer to btn_radio_material_anim.xml */
        private const val DURATION_RADIO_CHECK_ANIM = 483L
    }
}
