package org.mozilla.firefox.vpn.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_help.btn_contact_us
import kotlinx.android.synthetic.main.fragment_help.btn_debug
import kotlinx.android.synthetic.main.fragment_help.btn_help_and_support
import kotlinx.android.synthetic.main.fragment_help.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.coreComponent
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.report.ReportUtil
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.util.launchUrl
import org.mozilla.firefox.vpn.util.viewModel

class HelpFragment : Fragment() {
    private val component by lazy {
        HelpComponentImpl(activity!!.coreComponent, activity!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner, Observer {
            btn_debug.visibility = if (it.isDebugEntryVisible) {
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

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        btn_contact_us.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_CONTACT)
            }
        }
        btn_help_and_support.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_SUPPORT)
            }
        }
        btn_debug.setOnClickListener { viewModel.reportLogClicked() }
    }
}
