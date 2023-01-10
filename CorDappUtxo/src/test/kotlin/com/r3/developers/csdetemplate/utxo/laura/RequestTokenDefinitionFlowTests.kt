package com.r3.developers.csdetemplate.utxo.laura

import net.corda.simulator.RequestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RequestTokenDefinitionFlowTests : FlowTests() {

    @Test
    fun `RequestTokenDefinitionFlow returns the correct message`() {
        val requestTokenDefinitionRequestData =
            RequestData.create(
                "r1",
                RequestTokenDefinitionFlow::class.java,
                RequestTokenDefinitionRequestMessage("GBP", 2, BOB_X500)
            )

        val requestTokenDefinitionFlowResponse = aliceVN.callFlow(requestTokenDefinitionRequestData)
        assertEquals("SHA-256:00000000000000000000000000000000", requestTokenDefinitionFlowResponse)

//        val queryTokenDefinitionRequestData = RequestData.create("r2", QueryTokenDefinitionFlow::class.java, "")
//        val queryTokenDefinitionFlowResponse = aliceVN.callFlow(queryTokenDefinitionRequestData)
//
//        assert(queryTokenDefinitionFlowResponse == "[]")
    }
}
