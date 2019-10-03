package org.mozilla.guardian.main.vpn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.model.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import com.wireguard.crypto.KeyPair
import kotlinx.android.synthetic.main.fragment_vpn.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.guardian.MainApplication
import org.mozilla.guardian.R
import java.net.InetAddress

class VpnFragment : Fragment() {

    private lateinit var vpnViewModel: VpnViewModel

    private var pendingTunnel: Tunnel? = null
    private lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = prepareConfig()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vpnViewModel = ViewModelProviders.of(this).get(VpnViewModel::class.java)
        return inflater.inflate(R.layout.fragment_vpn, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tunnelManager = (activity?.application as MainApplication).tunnelManager
        val tunnel = tunnelManager.create(config, "aaa")

        vpn_switch.isChecked = tunnel.state == Tunnel.State.UP

        vpn_switch.setOnCheckedChangeListener { _, isChecked ->
            GlobalScope.launch(Dispatchers.IO) {
                if (isChecked) {
                    val intent = GoBackend.VpnService.prepare(context)
                    if (intent != null) {
                        withContext(Dispatchers.Main){
                            vpn_switch.isChecked = false
                            startActivityForResult(intent, 0)
                        }
                    } else {
                        tunnel.state = Tunnel.State.TOGGLE
                    }
                } else {
                    tunnel.state = Tunnel.State.TOGGLE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            vpn_switch.isChecked = true
        } else {
            Toast.makeText(context, "Permission denied by user", Toast.LENGTH_LONG).show()
        }
    }

    private fun prepareConfig(): Config {
        val inetInterface = Interface.Builder().apply {
            setKeyPair(KeyPair(Key.fromBase64("+PRvHmXSZ11CkUqJX2NvvYuCATY8+6H2ctP0GXwebWs=")))
            addAddress(InetNetwork.parse("10.64.114.119/32"))
            addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
        }.build()

        return Config.Builder().apply {
            setInterface(inetInterface)

            val peers = ArrayList<Peer>(1)
            peers.add(Peer.Builder().apply {
                setPublicKey(Key.fromBase64("Wy2FhqDJcZU03O/D9IUG/U5BL0PLbF06nvsfgIwrmGk="))
                parseEndpoint("185.232.22.58:32768")
                setPersistentKeepalive(60)
                parseAllowedIPs("0.0.0.0/0")
            }.build())
            peers.add(Peer.Builder().apply {
                setPublicKey(Key.fromBase64("Rzh64qPcg8W8klJq0H4EZdVCH7iaPuQ9falc99GTgRA="))
                parseEndpoint("103.231.88.2:32768")
                setPersistentKeepalive(60)
                parseAllowedIPs("0.0.0.0/0")
            }.build())

            addPeers(peers)
        }.build()
    }
}