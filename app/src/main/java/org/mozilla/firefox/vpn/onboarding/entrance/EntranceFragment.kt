package org.mozilla.firefox.vpn.onboarding.entrance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import org.mozilla.firefox.vpn.R
import org.mozilla.firefox.vpn.databinding.FragmentEntranceBinding
import org.mozilla.firefox.vpn.onboarding.OnboardingActivity
import org.mozilla.firefox.vpn.util.viewLifecycle

class EntranceFragment : Fragment() {

    private var binding: FragmentEntranceBinding by viewLifecycle()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentEntranceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.authBtn.setOnClickListener {
            val activity = activity as? OnboardingActivity ?: return@setOnClickListener
            activity.startLoginFlow()
        }
        binding.introBtn.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_intro))
    }
}
