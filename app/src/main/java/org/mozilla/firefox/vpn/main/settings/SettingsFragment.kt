package org.mozilla.firefox.vpn.main.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import coil.api.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.firefox.vpn.BuildConfig
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.FragmentSettingsBinding
import org.mozilla.firefox.vpn.guardianComponent
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.service.GuardianService
import org.mozilla.firefox.vpn.ui.UiDemoActivity
import org.mozilla.firefox.vpn.util.launchUrl
import org.mozilla.firefox.vpn.util.viewLifecycle
import org.mozilla.firefox.vpn.util.viewModel

class SettingsFragment : Fragment() {

    private val component by lazy {
        SettingsComponentImpl(activity!!.guardianComponent)
    }

    private val viewModel by viewModel { component.viewModel }

    private var binding: FragmentSettingsBinding by viewLifecycle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.myDevicesBtn.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_devices)
        }
        binding.manageAccountBtn.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_FXA)
            }
        }
        binding.getHelpBtn.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_help)
        }
        binding.aboutBtn.setOnClickListener {
            findNavController().navigate(R.id.action_settings_main_to_about)
        }
        binding.feedbackBtn.setOnClickListener {
            viewLifecycleOwner.lifecycle.coroutineScope.launch(Dispatchers.Main) {
                launchUrl(GuardianService.HOST_FEEDBACK)
            }
        }
        binding.signOutBtn.setOnClickListener {
            viewModel.signOut()
        }

        binding.uiDemoBtn.visibility = if (BuildConfig.DEBUG) { View.VISIBLE } else { View.GONE }
        binding.uiDemoBtn.setOnClickListener {
            startActivity(Intent(context, UiDemoActivity::class.java))
        }

        viewModel.userInfo.observe(viewLifecycleOwner, Observer { userInfo ->
            val userName = userInfo.user.displayName
            binding.profileName.text = if (userName.isNotEmpty()) {
                userName
            } else {
                getString(R.string.settings_default_user_name)
            }
            binding.profileEmail.text = userInfo.user.email
            binding.profileImage.load(userInfo.user.avatar) {
                crossfade(true)
                transformations(CircleCropTransformation())
            }
        })

        viewModel.showDeviceLimitReached.observe(viewLifecycleOwner, Observer {
            binding.deviceWarning.visibility = if (it) { View.VISIBLE } else { View.INVISIBLE }
        })

        viewModel.gotoMainPage.observe(viewLifecycleOwner, Observer {
            startActivity(OnboardingActivity.getStartIntent(view.context))
            activity?.finish()
        })
    }
}
