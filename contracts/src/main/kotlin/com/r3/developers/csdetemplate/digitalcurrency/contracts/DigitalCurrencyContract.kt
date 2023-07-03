package com.r3.developers.csdetemplate.digitalcurrency.contracts

import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class DigitalCurrencyContract: Contract {

    override fun verify(transaction: UtxoLedgerTransaction) {
//        val command = transaction.commands.firstOrNull { it is Issue || it is Transfer || it is Withdraw }
//            ?: throw CordaRuntimeException("Requires a single Digital Currency command")
//
//        when(command) {
//            is Issue -> {
//                //add contract logic
//                "When command is Issue there should be no input states." using (true)
//                "When command is Issue there should be one and only one output state." using (true)
//
//                "The output state should have only one participant." using {
//                    val output = transaction.outputContractStates.first() as DigitalCurrency
//                    // exactly 1 participant
//                    true
//                }
//            }
//            is Transfer -> {
//                "When command is Transfer there should be at least one input state." using (transaction.inputContractStates.size >= 1)
//                "When command is Transfer there should be at least one output state." using (transaction.outputContractStates.size >= 1)
//
//                val sentDigitalCurrency = transaction.inputContractStates.filterIsInstance<DigitalCurrency>()
//                val receivedDigitalCurrency = transaction.outputContractStates.filterIsInstance<DigitalCurrency>()
////                val sentAmount = sentDigitalCurrency.sumOf { it.quantity }
////                val receivedAmount = receivedDigitalCurrency.sumOf { it.quantity }
////                "When command is Transfer the sent and received amount should be the same total amount." using (
////                    sentAmount == receivedAmount)
//
////                "When command is Transfer there must be exactly one participant." using (
////                        receivedDigitalCurrency.all { it.participants.size == 1 })
//                // additional checks for sender/receiver being the specific participants
//            }
//            is Withdraw -> {
//                "When command is Withdraw there should be at least one input state." using (transaction.inputContractStates.size >= 1)
//                "When command is Withdraw there should be no more than one output state." using (transaction.outputContractStates.size < 2)
//
//                val sentDigitalCurrency = transaction.inputContractStates.filterIsInstance<DigitalCurrency>()
//                val remainingDigitalCurrency = transaction.outputContractStates.filterIsInstance<DigitalCurrency>()
////                val sentAmount = sentDigitalCurrency.sumOf { it.quantity }
////                val remainingAmount = remainingDigitalCurrency.sumOf { it.quantity }
////                "When command is Withdraw the sent amount should be greater than the remaining amount." using (
////                        sentAmount > remainingAmount)
//
////                "When command is Withdraw there must be exactly one participant." using (
////                        remainingDigitalCurrency.all { it.participants.size == 1 })
//            }
//            else -> {
//                throw CordaRuntimeException("Command ${command} not allowed.")
//            }
//        }
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