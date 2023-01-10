package com.r3.developers.csdetemplate.utxo.laura

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionState
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.RPCRequestData
import net.corda.v5.application.flows.RPCStartableFlow
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.util.contextLogger
import net.corda.v5.ledger.utxo.UtxoLedgerService

class QueryTokenDefinitionFlow : RPCStartableFlow {

    private companion object {
        val log = contextLogger()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var jsonService: JsonMarshallingService

    @CordaInject
    lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(requestBody: RPCRequestData): String {

        log.info("QueryTokenDefinitionFlow started.")

        val tokenDefinitionStates = utxoLedgerService.findUnconsumedStatesByType(TokenDefinitionState::class.java)
            .map { it.state.contractState }

        val tokenDefinitionResponse = tokenDefinitionStates.map {
            TokenDefinitionResponse(
                it.name,
                it.fractionDigits,
                memberLookup.lookup(it.tokenDefiningEntity)!!.name.toString(),
                memberLookup.lookup(it.tokenDefinitionAuthority)!!.name.toString(),
                it.status,
                it.id
            )
        }
        return jsonService.format(tokenDefinitionResponse)
    }
}
