package org.mozilla.firefox.vpn.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.databinding.FragmentHelpBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.report.ReportUtil
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.util.launchUrl
import org.mozilla.firefox.vpn.util.viewLifecycle
import org.mozilla.firefox.vpn.util.viewModel

class HelpFragment : Fragment() {

    private val component by lazy {
        HelpComponentImpl(activity!!.coreComponent, activity!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    private var binding: FragmentHelpBinding by viewLifecycle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner, Observer {
            binding.debugBtn.visibility = if (it.isDebugEntryVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })

        viewModel.reportLog.observe(viewLifecycleOwner, Observer { info ->
            val activity = activity ?: return@Observer

            ReportUtil.sendLog(
                activity = activity,
                email = info.email,
                subject = info.subject,
                body = info.body
            )
        })

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.contactUsBtn.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_CONTACT)
            }
        }
        binding.helpAndSupportBtn.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_SUPPORT)
            }
        }
        binding.debugBtn.setOnClickListener { viewModel.reportLogClicked() }
    }
}
