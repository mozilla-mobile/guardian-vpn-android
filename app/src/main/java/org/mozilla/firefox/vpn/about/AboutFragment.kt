package org.mozilla.firefox.vpn.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_about.*
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.service.GuardianService

class AboutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        version_text.text = BuildConfig.VERSION_NAME
        btn_terms.setOnClickListener {
            CustomTabsIntent.Builder().apply {
                enableUrlBarHiding()
            }.build().launchUrl(context, GuardianService.HOST_TERMS.toUri())
        }
        btn_policy.setOnClickListener {
            CustomTabsIntent.Builder().apply {
                enableUrlBarHiding()
            }.build().launchUrl(context, GuardianService.HOST_PRIVACY.toUri())
        }
    }
}