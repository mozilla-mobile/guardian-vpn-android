package org.mozilla.firefox.vpn.apptunneling.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.mozilla.firefox.vpn.apptunneling.AppTunnelingComponentImpl
import org.mozilla.firefox.vpn.databinding.FragmentAppTunnelingBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.util.viewBinding
import org.mozilla.firefox.vpn.util.viewModel

class AppTunnelingFragment : Fragment() {

    private val component by lazy {
        AppTunnelingComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel {
        component.viewModel
    }

    private var binding: FragmentAppTunnelingBinding by viewBinding()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAppTunnelingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
