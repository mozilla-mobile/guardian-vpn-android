package org.mozilla.firefox.vpn

import org.mozilla.firefox.vpn.servers.data.CityInfo
import org.mozilla.firefox.vpn.servers.data.CountryInfo
import org.mozilla.firefox.vpn.servers.data.ServerInfo
import org.mozilla.firefox.vpn.service.Server

val testServer = Server(
    "host1",
    "ip4",
    1,
    true,
    "public_key",
    listOf(listOf(25)),
    "gateway4",
    "gateway6"
)

val testUSAServerInfo = ServerInfo(
    CountryInfo(
        "USA",
        "us"
    ),
    CityInfo(
        "Los Angeles, CA",
        "lax",
        0.0,
        0.0
    ),
    testServer
)

val testUSAServerInfo2 = ServerInfo(
    CountryInfo(
        "USA",
        "us"
    ),
    CityInfo(
        "New York, NY",
        "nyc",
        0.0,
        0.0
    ),
    testServer
)

val testCanadaServerInfo = ServerInfo(
    CountryInfo(
        "Canada",
        "ca"
    ),
    CityInfo(
        "Vancouver",
        "van",
        0.0,
        0.0
    ),
    testServer
)

val testCanadaServerInfo2 = ServerInfo(
    CountryInfo(
        "Canada",
        "ca"
    ),
    CityInfo(
        "Montreal",
        "mtr",
        0.0,
        0.0
    ),
    testServer
)

val testServerInfos = listOf(
    testUSAServerInfo,
    testUSAServerInfo,
    testUSAServerInfo2,
    testUSAServerInfo2
)

val testServerInfos2 = listOf(
    testCanadaServerInfo,
    testCanadaServerInfo2
)
