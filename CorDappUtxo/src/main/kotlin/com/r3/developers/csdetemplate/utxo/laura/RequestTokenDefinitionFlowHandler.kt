package com.r3.developers.csdetemplate.utxo.laura

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionState
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.InitiatedBy
import net.corda.v5.application.flows.ResponderFlow
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.util.contextLogger
import net.corda.v5.ledger.utxo.UtxoLedgerService

@InitiatedBy("request-token-definition-protocol")
class RequestTokenDefinitionFlowHandler : ResponderFlow {

    private companion object {
        val log = contextLogger()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(session: FlowSession) {

        log.info("RequestTokenDefinitionFlowHandler started.")
        utxoLedgerService.receiveFinality(session) { ledgerTransaction ->

            log.info("Received transaction: ${ledgerTransaction.id}")

            val state = ledgerTransaction.outputContractStates.first() as TokenDefinitionState
            if (state.name == "USD") {
                log.info("Failed to verify the transaction - $ledgerTransaction")
                throw IllegalStateException("Failed verification")
            }
            log.info("Verified the transaction- ${ledgerTransaction.id}")
        }
    }
}
