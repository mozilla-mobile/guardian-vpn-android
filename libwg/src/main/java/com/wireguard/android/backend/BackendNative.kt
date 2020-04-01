/*
 * Copyright © 2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.wireguard.android.backend

public open class BackendNative {

    public external fun wgGetConfig(handle: Int): String

    public external fun wgGetSocketV4(handle: Int): Int

    public external fun wgGetSocketV6(handle: Int): Int

    public external fun wgTurnOff(handle: Int)

    public external fun wgTurnOn(ifName: String, tunFd: Int, settings: String): Int

    public external fun wgVersion(): String

    init {
        System.loadLibrary("wg-go")
    }

}