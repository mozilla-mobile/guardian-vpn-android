package org.mozilla.firefox.vpn.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import coil.api.load
import coil.transform.CircleCropTransformation
import kotlinx.android.synthetic.main.fragment_settings.*
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.util.launchUrl
import org.mozilla.firefox.vpn.util.viewModel

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

    private val userRepository by lazy {
        activity!!.guardianComponent.userRepo
    }

    private val component by lazy {
        SettingsComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_my_devices.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_devices)
        }
        btn_manage_account.setOnClickListener {
            launchUrl(GuardianService.HOST_FXA)
        }
        btn_get_help.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_help)
        }
        btn_about.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_about)
        }
        btn_sign_out.setOnClickListener {
            settingsViewModel.signOut()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        userRepository.getUserInfo()?.let { userInfo ->
            val userName = userInfo.user.displayName
            profile_name?.text = if (userName.isNotEmpty()) {
                userName
            } else {
                getString(R.string.settings_default_user_name)
            }
            profile_email?.text = userInfo.user.email
            profile_image?.load(userInfo.user.avatar) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        }
    }
}