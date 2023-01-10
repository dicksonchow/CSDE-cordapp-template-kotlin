package com.r3.developers.csdetemplate.utxo.laura

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionContract
import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionState
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.InitiatingFlow
import net.corda.v5.application.flows.RPCRequestData
import net.corda.v5.application.flows.RPCStartableFlow
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.util.contextLogger
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.common.Party
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.time.Instant

@InitiatingFlow("request-token-definition-protocol")
class RequestTokenDefinitionFlow : RPCStartableFlow {

    private companion object {
        val log = contextLogger()
    }

    @CordaInject
    lateinit var flowMessaging: FlowMessaging

    @CordaInject
    lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var jsonService: JsonMarshallingService

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @Suspendable
    override fun call(requestBody: RPCRequestData): String {

        log.info("RequestTokenDefinitionFlow started.")

        val request = requestBody.getRequestBodyAs(jsonService, RequestTokenDefinitionRequestMessage::class.java)

        val ourIdentity = memberLookup.myInfo()

        val tokenDefinitionAuthority = memberLookup.lookup(request.tokenDefinitionAuthority)
            ?: throw IllegalArgumentException("Unknown TokenDefinition Authority: ${request.tokenDefinitionAuthority}.")

        val notary = notaryLookup.notaryServices.first()

        val notaryKey = memberLookup.lookup().single {
            //it.memberProvidedContext["corda.notary.service.name"] == notary.name.toString() // TODO : Comment and use the one below when running tests
            it.name == notary.name
        }.ledgerKeys.first()

        val flowSessions = listOf(tokenDefinitionAuthority)
            .map { flowMessaging.initiateFlow(it.name) }
            .toSet()

        val tokenDefinitionState = TokenDefinitionState(
            request.name,
            request.fractionDigits,
            ourIdentity.ledgerKeys.first(),
            tokenDefinitionAuthority.ledgerKeys.first()
        )

        val transaction = utxoLedgerService
            .getTransactionBuilder()
            .setNotary(Party(notary.name, notaryKey))
            .setTimeWindowUntil(Instant.now().plusSeconds(360))
            .addOutputState(tokenDefinitionState)
            .addCommand(TokenDefinitionContract.Request)
            .addSignatories(listOf(ourIdentity.ledgerKeys.first()))

        val fullySignedTransaction = transaction.toSignedTransaction(ourIdentity.ledgerKeys.first())

        utxoLedgerService.finalize(fullySignedTransaction, flowSessions.toList())

        return fullySignedTransaction.id.toString()
    }
}
