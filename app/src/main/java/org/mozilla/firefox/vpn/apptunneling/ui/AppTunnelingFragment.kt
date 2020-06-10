package org.mozilla.firefox.vpn.apptunneling.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
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
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.uiModel.observe(viewLifecycleOwner, Observer {
            if (binding.expandableList.adapter == null) {
                binding.expandableList.adapter = ExpandableAdapter(it, onExpandableItemCallback)
            } else {
                (binding.expandableList.adapter as? ExpandableAdapter)?.setData(it)
            }
        })
    }

    private val onExpandableItemCallback = object : ExpandableAdapter.ExpandableItemCallback {

        override fun onProtectedAppChecked(packageName: String) {
            viewModel.addExcludeApp(packageName)
        }

        override fun onProtectAllClicked(packageNameSet: Set<String>) {
            viewModel.removeExcludeApp(packageNameSet)
        }

        override fun onUnprotectedAppChecked(packageName: String) {
            viewModel.removeExcludeApp(packageName)
        }

        override fun onUnprotectAllClicked(packageNameSet: Set<String>) {
            viewModel.addExcludeApp(packageNameSet)
        }
    }
}
