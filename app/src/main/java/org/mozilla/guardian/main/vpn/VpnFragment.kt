package org.mozilla.guardian.main.vpn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.layout_vpn_offline.*
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        vpnViewModel = ViewModelProviders.of(this).get(VpnViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_vpn, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vpn_switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                GlobalScope.launch(Dispatchers.Main) {

                    val backend = (activity?.application as MainApplication).backend
                    val tunnelManager = (activity?.application as MainApplication).tunnelManager
                    val inetInterface = Interface.Builder().apply {
                        setKeyPair(KeyPair(Key.fromBase64("+PRvHmXSZ11CkUqJX2NvvYuCATY8+6H2ctP0GXwebWs=")))
                        addAddress(InetNetwork.parse("10.64.129.185/32"))
                        addDnsServer(InetAddress.getByAddress(byteArrayOf(1, 1, 1, 1)))
                    }.build()
                    val config = Config.Builder().apply {
                        setInterface(inetInterface)
                        addPeer(Peer.Builder().apply {
                            setPublicKey(Key.fromBase64("Rzh64qPcg8W8klJq0H4EZdVCH7iaPuQ9falc99GTgRA="))
                            parseEndpoint("103.231.88.2:32768")
                            setPersistentKeepalive(60)
                            parseAllowedIPs("0.0.0.0/0")
                        }.build())
                    }.build()

                    withContext(Dispatchers.IO) {
                        val tunnel = tunnelManager.create(config, "aaa")

                        val intent = GoBackend.VpnService.prepare(context)
                        if (intent != null) {
                            pendingTunnel = tunnel
                            startActivityForResult(intent, 0)
                        } else {
                            tunnel.state = Tunnel.State.TOGGLE
                        }

                    }
                }
                //  Connect to VPN
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(pendingTunnel != null) {
            pendingTunnel?.state = Tunnel.State.UP
            pendingTunnel = null
        }
    }
}