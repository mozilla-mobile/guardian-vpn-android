/*
 * Copyright © 2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend

import android.os.ParcelFileDescriptor
import com.wireguard.config.Config

public class Tunnel {

    public val name: String

    var tunFd: ParcelFileDescriptor? = null
    var tunnelHandle: Int? = null

    val state: State
        get() {
            return if (tunnelHandle == null) {
                State.Down
            } else {
                State.Up
            }
        }


    val config: Config

    constructor(name: String, config: Config) {
        this.name = name
        this.config = config
    }

    sealed class State {
        object Up: State()
        object Down: State()
    }

}