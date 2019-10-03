package org.mozilla.guardian.main.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import coil.api.load
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.guardian.R
import org.mozilla.guardian.user.data.Result
import org.mozilla.guardian.user.data.UserRepository
import org.mozilla.guardian.user.domain.GetLoginInfoUseCase

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var getLoginInfo: GetLoginInfoUseCase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        return root
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
                        profile_image?.load(userInfo.value.avatar)
                    }
                }
            }
        }
    }
}