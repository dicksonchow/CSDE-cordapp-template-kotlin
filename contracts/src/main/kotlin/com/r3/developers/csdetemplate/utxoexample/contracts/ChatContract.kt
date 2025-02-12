package com.r3.developers.csdetemplate.utxoexample.contracts

import com.r3.developers.csdetemplate.utxoexample.states.ChatState
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class ChatContract: Contract {

    // Command Class used to indicate that the transaction should start a new chat.
    class Create: Command
    // Command Class used to indicate that the transaction should append a new ChatState to an existing chat.
    class Update: Command

    // verify() function is used to apply contract rules to the transaction.
    override fun verify(transaction: UtxoLedgerTransaction) {

        // Ensures that there is only one command in the transaction
        val command = transaction.commands.singleOrNull() ?: throw CordaRuntimeException("Require a single command ")

        // Applies a universal constraint (applies to all transactions irrespective of command)
        "The output state should have two and only two participants" using {
            val output = transaction.outputContractStates.single() as ChatState
            output.participants.size== 2
        }
        // Switches case based on the command
        when(command) {
            // Rules applied only to transactions with the Create Command.
            is Create -> {
                "When command is Create there should be no input states" using (transaction.inputContractStates.isEmpty())
                "When command is Create there should be one and only one output state" using (transaction.outputContractStates.size == 1)
            }
            // Rules applied only to transactions with the Update Command.
            is Update -> {
                "When command is Update there should be one and only one input states" using (transaction.inputContractStates.size == 1)
                "When command is Update there should be one and only one output state" using (transaction.outputContractStates.size == 1)
            }
        }
    }

    // Helper function to allow writing constraints in the Corda 4 '"text" using (boolean)' style
    private infix fun String.using(expr: Boolean) {
        if (!expr) throw IllegalArgumentException("Failed requirement: $this")
    }

    // Helper function to allow writing constraints in '"text" using {lambda}' style where the last expression
    // in the lambda is a boolean.
    private infix fun String.using(expr: () -> Boolean) {
        if (!expr.invoke()) throw IllegalArgumentException("Failed requirement: $this")
    }
}