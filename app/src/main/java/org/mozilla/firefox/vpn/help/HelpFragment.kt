package org.mozilla.firefox.vpn.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_help.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.service.GuardianService

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
            CustomTabsIntent.Builder().apply {
                enableUrlBarHiding()
            }.build().launchUrl(context, GuardianService.HOST_CONTACT.toUri())
        }
        btn_help_and_support.setOnClickListener {
            CustomTabsIntent.Builder().apply {
                enableUrlBarHiding()
            }.build().launchUrl(context, GuardianService.HOST_SUPPORT.toUri())
        }
    }
}