package net.thunderbird.core.common.net

import java.net.IDN

internal actual fun hostNameToAscii(hostName: String): String? {
    return try {
        IDN.toASCII(hostName, IDN.USE_STD3_ASCII_RULES)
    } catch (ignored: IllegalArgumentException) {
        // Not a valid IDN; that's a "no" for validation, nothing more to report.
        null
    }
}
