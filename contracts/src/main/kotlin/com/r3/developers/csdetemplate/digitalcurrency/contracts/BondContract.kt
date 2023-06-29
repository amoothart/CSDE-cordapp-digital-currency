package com.r3.developers.csdetemplate.digitalcurrency.contracts

import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class BondContract: Contract {

    class Issue: Command
    class Sell: Command
    class Bundle: Command

    override fun verify(transaction: UtxoLedgerTransaction) {
        val command = transaction.commands.firstOrNull { it is Issue || it is Sell || it is Bundle }
            ?: throw CordaRuntimeException("Requires a single Bond command")

        when(command) {
            is Issue -> {
                "When command is Issue there should be no input states." using (transaction.inputContractStates.isEmpty())
                "When command is Issue there should be one and only one output state." using (transaction.outputContractStates.size == 1)

                "The output state should have only 1 participant." using {
                    val output = transaction.outputContractStates.first() as Bond
                    output.participants.size==1
                }
            }
            is Sell -> {
                "When command is Sell there should be at least two input states." using (transaction.inputContractStates.size >= 2)
                "When command is Sell there should be at least two output states." using (transaction.outputContractStates.size >= 2)

                val sentBond = transaction.inputContractStates.filterIsInstance<Bond>().first()
                val receivedBond = transaction.outputContractStates.filterIsInstance<Bond>().first()
                "When command is Sell the new owner should be different than the current owner." using (
                        sentBond.creditor != receivedBond.creditor)

                "When command is Sell there must be exactly one participants." using (
                        transaction.outputContractStates.all { it.participants.size == 1 })
            }
            is Bundle -> {
                val targetBonds = transaction.inputContractStates.filterIsInstance<Bond>()
                "At least one target bond has already been bundled." using targetBonds.all { bond -> !bond.bundled }
            }
            else -> {
                throw CordaRuntimeException("Command ${command} not allowed.")
            }
        }
    }

    // Helper function to allow writing constraints in the Corda 4 '"text" using (boolean)' style
    private infix fun String.using(expr: Boolean) {
        if (!expr) throw CordaRuntimeException("Failed requirement: $this")
    }

    // Helper function to allow writing constraints in '"text" using {lambda}' style where the last expression
    // in the lambda is a boolean.
    private infix fun String.using(expr: () -> Boolean) {
        if (!expr.invoke()) throw CordaRuntimeException("Failed requirement: $this")
    }
}