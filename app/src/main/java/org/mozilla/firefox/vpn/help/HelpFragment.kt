package org.mozilla.firefox.vpn.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_help.btn_contact_us
import kotlinx.android.synthetic.main.fragment_help.btn_debug
import kotlinx.android.synthetic.main.fragment_help.btn_help_and_support
import kotlinx.android.synthetic.main.fragment_help.toolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.report.ReportUtil
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.util.launchUrl

class HelpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        btn_debug.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                activity?.let { ReportUtil.sendLog(it) }
            }
        }
    }
}
