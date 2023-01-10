package com.r3.dc.platform.contracts.tokendefinition

import net.corda.v5.base.annotations.CordaSerializable

@CordaSerializable
enum class TokenDefinitionStatus {
    REQUESTED,
    APPROVED,
    REJECTED;

    fun canAdvanceFrom(status: TokenDefinitionStatus): Boolean = when (status) {
        REQUESTED -> this in setOf(APPROVED, REJECTED)
        else -> false
    }
}