package com.r3.developers.csdetemplate.utxo.laura

import com.r3.developers.csdetemplate.utxo.IssueTokenFlow
import com.r3.developers.csdetemplate.utxo.MoveAllTokenFlow
import net.corda.ledger.utxo.flow.impl.persistence.UtxoLedgerPersistenceService
import net.corda.ledger.utxo.flow.impl.transaction.UtxoTransactionBuilderImpl
import net.corda.ledger.utxo.flow.impl.transaction.factory.UtxoSignedTransactionFactory
import net.corda.simulator.SimulatedVirtualNode
import net.corda.simulator.Simulator
import net.corda.simulator.crypto.HsmCategory
import net.corda.simulator.factories.ServiceOverrideBuilder
import net.corda.simulator.factories.SimulatorConfigurationBuilder
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.common.Party
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.corda.v5.membership.NotaryInfo
import org.junit.jupiter.api.BeforeAll
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy

abstract class FlowTests {

    companion object {
        val ALICE_X500 = MemberX500Name.parse("CN=Alice, OU=Test Dept, O=R3, L=London, C=GB")
        val BOB_X500 = MemberX500Name.parse("CN=Bob, OU=Test Dept, O=R3, L=London, C=GB")
        val NOTARY_X500 = MemberX500Name.parse("CN=NotaryRep1, OU=Test Dept, O=R3, L=London, C=GB")
        val NOTARY_SERVICE_PARTY = Party(NOTARY_X500, mock())

        lateinit var simulator: Simulator
        lateinit var aliceVN: SimulatedVirtualNode
        lateinit var bobVN: SimulatedVirtualNode
        lateinit var notaryVN: SimulatedVirtualNode

        val flows = arrayOf(
            RequestTokenDefinitionFlow::class.java,
            RequestTokenDefinitionFlowHandler::class.java,
//            ApproveTokenDefinitionFlow::class.java,
//            RejectTokenDefinitionFlow::class.java,
            QueryTokenDefinitionFlow::class.java
        )

        @JvmStatic
        @BeforeAll
        fun setUp() {
            setUpSimulator()

            aliceVN = simulator.createVirtualNode(ALICE_X500, *flows)
            bobVN = simulator.createVirtualNode(BOB_X500, *flows)
            notaryVN = simulator.createVirtualNode(NOTARY_X500, *flows)

            aliceVN.generateKey("alice-alias", HsmCategory.LEDGER, "CORDA.ECDSA.SECP256R1")
            bobVN.generateKey("bob-alias", HsmCategory.LEDGER, "CORDA.ECDSA.SECP256R1")
            notaryVN.generateKey("notary-alias", HsmCategory.LEDGER, "CORDA.ECDSA.SECP256R1")
        }

        private fun setUpSimulator() {
            val mockFirstNotaryServiceInfo = mock<NotaryInfo> {
                on { pluginClass } doReturn "DUMMY"
                on { name } doReturn NOTARY_X500
                on { publicKey } doReturn NOTARY_SERVICE_PARTY.owningKey
            }

            val mockNotaryLookup = mock<NotaryLookup> {
                on { notaryServices } doReturn listOf(mockFirstNotaryServiceInfo)
            }

            val signedTx = mock<UtxoSignedTransaction>()
            given(signedTx.id).willReturn(SecureHash("SHA-256", ByteArray(16)))
            //or
            doReturn(SecureHash("SHA-256", ByteArray(16))).`when`(signedTx).id

            val mockUtxoTransactionFactory = mock<UtxoSignedTransactionFactory> {
                on { create(wireTransaction = any(), any(), any()) } doThrow IllegalArgumentException("LAURA!!")
                on { create(utxoTransactionBuilder = any(), any(), any()) } doReturn signedTx
            }

            val mockUtxoLedgerPersistenceService = mock<UtxoLedgerPersistenceService> {

            }

            val utxoTransactionBuilder = UtxoTransactionBuilderImpl(
                mockUtxoTransactionFactory,
                mockUtxoLedgerPersistenceService
            )
            println("[mock] utxoTransactionBuilder > ${utxoTransactionBuilder.hashCode()}")

            val mockUtxoLedgerService = spy<UtxoLedgerService> {
                on { getTransactionBuilder() } doReturn utxoTransactionBuilder
            }

            val notaryLookupBuilder = ServiceOverrideBuilder<NotaryLookup> { _, _, _ -> mockNotaryLookup }
            val utxoLedgerServiceBuilder =
                ServiceOverrideBuilder<UtxoLedgerService> { _, _, _ -> mockUtxoLedgerService }

            val simulatorConfiguration = SimulatorConfigurationBuilder.create()
                .withServiceOverride(NotaryLookup::class.java, notaryLookupBuilder)
                .withServiceOverride(UtxoLedgerService::class.java, utxoLedgerServiceBuilder)
                .build()

            simulator = Simulator(simulatorConfiguration)
        }
    }
}
