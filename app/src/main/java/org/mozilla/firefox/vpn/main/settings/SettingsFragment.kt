package org.mozilla.firefox.vpn.main.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import coil.api.load
import coil.transform.CircleCropTransformation
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.user.data.GuardianService
import org.mozilla.firefox.vpn.user.data.Result
import org.mozilla.firefox.vpn.user.data.UserRepository
import org.mozilla.firefox.vpn.user.domain.GetLoginInfoUseCase

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var getLoginInfo: GetLoginInfoUseCase

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
            launchUrl(context!!, GuardianService.HOST_FXA)
        }
        btn_get_help.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_help)
        }
        btn_about.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_about)
        }
    }

    private fun launchUrl(context: Context, url: String) {
        CustomTabsIntent.Builder().apply {
            enableUrlBarHiding()
        }.build().launchUrl(context, url.toUri())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userRepository = UserRepository(activity!!.applicationContext)
        getLoginInfo = GetLoginInfoUseCase(userRepository)

        GlobalScope.launch(Dispatchers.IO) {
            val userInfo = userRepository.getUserInfo()
            when (userInfo) {
                is Result.Success -> {
                    withContext(Dispatchers.Main){

                        profile_name?.text = userInfo.value.displayName
                        profile_email?.text = userInfo.value.email
                        profile_image?.load(userInfo.value.avatar) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                        }
                    }
                }
            }
        }
    }
}