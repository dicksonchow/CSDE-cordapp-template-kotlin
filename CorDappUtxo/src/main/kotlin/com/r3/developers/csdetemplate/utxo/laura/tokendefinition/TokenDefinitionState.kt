package com.r3.dc.platform.contracts.tokendefinition

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionStatus.*
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*

@BelongsToContract(TokenDefinitionContract::class)
data class TokenDefinitionState(
    val name: String,
    val fractionDigits: Int,
    val tokenDefiningEntity: PublicKey,
    val tokenDefinitionAuthority: PublicKey,
    val status: TokenDefinitionStatus = REQUESTED,
    val id: UUID = UUID.randomUUID()
) : ContractState {
    override val participants: List<PublicKey> get() = listOf(tokenDefiningEntity, tokenDefinitionAuthority).distinct()

    fun approve() = advance(APPROVED)

    fun reject() = advance(REJECTED)

    private fun advance(status: TokenDefinitionStatus): TokenDefinitionState {
        check(status.canAdvanceFrom(this.status)) { "Cannot advance to $status from ${this.status}." }
        return copy(status = status)
    }
}