package com.r3.dc.platform.contracts.tokendefinition

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionStatus.*
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class TokenDefinitionContract : Contract {
    override fun verify(transaction: UtxoLedgerTransaction) {
        val command = transaction.getCommands(TokenDefinitionCommand::class.java).singleOrNull()
            ?: throw IllegalArgumentException("Expected a single command of type: ${TokenDefinitionCommand::class.java}.")

        command.verify(transaction)
    }

    private interface TokenDefinitionCommand : Command {
        fun verify(transaction: UtxoLedgerTransaction)
    }

    object Request : TokenDefinitionCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On token definition request, zero input states must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On token definition request, only one output state must be created."

        internal const val CONTRACT_RULE_STATUS =
            "On token definition request, the status must be REQUESTED."

        internal const val CONTRACT_RULE_SIGNATORIES =
            "On token definition request, the Token Defining Entity must sign the transaction."

        override fun verify(transaction: UtxoLedgerTransaction) {

            val inputs = transaction.getInputStates(TokenDefinitionState::class.java)
            val outputs = transaction.getOutputStates(TokenDefinitionState::class.java)

            require(inputs.isEmpty()) { CONTRACT_RULE_INPUTS }
            require(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val output = outputs.single()

            require(output.status == REQUESTED) { CONTRACT_RULE_STATUS }
            require(transaction.signatories.contains(output.tokenDefiningEntity)) { CONTRACT_RULE_SIGNATORIES }
        }
    }

    object Approve : TokenDefinitionCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On token definition approval, only one input state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On token definition approval, only one output state must be created."

        internal const val CONTRACT_RULE_STATUS =
            "On token definition approval, the status must be APPROVED."

        internal const val CONTRACT_RULE_SIGNATORIES =
            "On token definition approval, the Token Definition Authority must sign the transaction."

        override fun verify(transaction: UtxoLedgerTransaction) {

            val inputs = transaction.getInputStates(TokenDefinitionState::class.java)
            val outputs = transaction.getOutputStates(TokenDefinitionState::class.java)

            require(inputs.size == 1) { CONTRACT_RULE_INPUTS }
            require(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val output = outputs.single()

            require(output.status == APPROVED) { CONTRACT_RULE_STATUS }
            require(transaction.signatories.contains(output.tokenDefinitionAuthority)) { CONTRACT_RULE_SIGNATORIES }
        }
    }

    object Reject : TokenDefinitionCommand {

        internal const val CONTRACT_RULE_INPUTS =
            "On token definition rejection, only one input state must be consumed."

        internal const val CONTRACT_RULE_OUTPUTS =
            "On token definition rejection, only one output state must be created."

        internal const val CONTRACT_RULE_STATUS =
            "On token definition rejection, the status must be REJECTED."

        internal const val CONTRACT_RULE_SIGNATORIES =
            "On token definition rejection, the Token Definition Authority must sign the transaction."

        override fun verify(transaction: UtxoLedgerTransaction) {

            val inputs = transaction.getInputStates(TokenDefinitionState::class.java)
            val outputs = transaction.getOutputStates(TokenDefinitionState::class.java)

            require(inputs.size == 1) { CONTRACT_RULE_INPUTS }
            require(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val output = outputs.single()

            require(output.status == REJECTED) { CONTRACT_RULE_STATUS }
            require(transaction.signatories.contains(output.tokenDefinitionAuthority)) { CONTRACT_RULE_SIGNATORIES }
        }
    }
}
