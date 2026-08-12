package com.fsck.k9.view

import com.fsck.k9.mail.Address
import net.thunderbird.core.common.net.HostNameUtils

/**
 * Used to parse name & email address pairs entered by the user.
 *
 * TODO: Build a custom implementation that can deal with typical inputs from users who are not familiar with the
 *  RFC 5322 address-list syntax. See (ignored) tests in `UserInputEmailAddressParserTest`.
 */
internal class UserInputEmailAddressParser {

    @Throws(InvalidUnicodeDomainException::class)
    fun parse(input: String): List<Address> {
        return Address.parseUnencoded(input)
            .mapNotNull { address ->
                when {
                    address.isIncomplete() -> null
                    // A Unicode string that isn't a valid IDN (e.g. ≠.net)
                    // earns a specific complaint instead of being silently dropped.
                    address.hasInvalidUnicodeDomain() -> throw InvalidUnicodeDomainException(address.address)
                    address.isInvalidDomainPart() -> null
                    else -> Address.parse(address.toEncodedString()).firstOrNull()
                }
            }
    }

    private fun Address.isIncomplete() = hostname.isNullOrBlank()

    private fun Address.isInvalidDomainPart() = HostNameUtils.isLegalHostNameOrIP(hostname) == null

    private fun Address.hasInvalidUnicodeDomain() = hasNonAsciiDomain() && isInvalidDomainPart()

    private fun Address.hasNonAsciiDomain() = hostname?.any { it.code >= ASCII_LIMIT } == true

    private companion object {
        const val ASCII_LIMIT = 128
    }
}

internal class InvalidUnicodeDomainException(message: String) : Exception(message)
