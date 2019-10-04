package org.mozilla.firefox.vpn.backend

import com.wireguard.android.configStore.ConfigStore
import com.wireguard.config.Config

class FileConfigStore: ConfigStore {
    override fun create(name: String?, config: Config?): Config {
        return config!!
    }

    override fun delete(name: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enumerate(): MutableSet<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun load(name: String?): Config {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun rename(name: String?, replacement: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun save(name: String?, config: Config?): Config {
        return config!!
    }
}