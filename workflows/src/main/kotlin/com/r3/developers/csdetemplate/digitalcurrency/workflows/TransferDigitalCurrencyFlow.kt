package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import com.r3.developers.csdetemplate.digitalcurrency.helpers.CoinSelection
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import java.time.Duration
import java.time.Instant

data class TransferDigitalCurrency(val quantity: Int, val toHolder: String)

@InitiatingFlow(protocol = "finalize-transfer-digital-currency-protocol")
class TransferDigitalCurrencyFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val flowArgs = requestBody.getRequestBodyAs(json, TransferDigitalCurrency::class.java)

            val fromHolder = memberLookup.myInfo()

            if (flowArgs.toHolder == fromHolder.name.toString()) {
                throw CordaRuntimeException("Cannot transfer money to self.")
            }

            val toHolder = memberLookup.lookup(MemberX500Name.parse(flowArgs.toHolder)) ?:
                throw CordaRuntimeException("MemberLookup can't find toHolder specified in flow arguments.")

//            val availableTokens = ledgerService.findUnconsumedStatesByType(DigitalCurrency::class.java)
//
//            val coinSelection = CoinSelection()
//            val (currencyToSpend, spentCurrency) = coinSelection.selectTokensForTransfer(flowArgs.quantity,
//                                                            fromHolder.ledgerKeys.first(),
//                                                            toHolder.ledgerKeys.first(),
//                                                            availableTokens)
//
//            val notary = notaryLookup.notaryServices.single()
//
//            val txBuilder = ledgerService.createTransactionBuilder()
//                .setNotary(notary.name)
//                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
//                .addInputStates(currencyToSpend.map { it.ref })
//                .addOutputStates(spentCurrency)
//                .addCommand(DigitalCurrencyContract.Transfer())
//                .addSignatories(fromHolder.ledgerKeys.first(), toHolder.ledgerKeys.first()) // issuer does not sign
//
//            val signedTransaction = txBuilder.toSignedTransaction()
//
//            val session = flowMessaging.initiateFlow(toHolder.name)
//
//            val finalizedSignedTransaction = ledgerService.finalize(
//                signedTransaction,
//                listOf(session)
//            )
//            return finalizedSignedTransaction.transaction.id.toString().also {
//                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
//            }
            return "implement me"
        }
        catch (e: Exception) {
            logger.warn("Failed to process transfer digital currency for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-transfer-digital-currency-protocol")
class FinalizeTransferDigitalCurrencyResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
//                val state = ledgerTransaction.getOutputStates(DigitalCurrency::class.java).first() ?:
//                    throw CordaRuntimeException("Failed verification - transaction did not have at least one output DigitalCurrency.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished transfer digital currency responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Transfer DigitalCurrency responder flow failed with exception", e)
            throw e
        }
    }
}

/*
{
    "clientRequestId": "transfer-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.TransferDigitalCurrencyFlow",
    "requestBody": {
        "quantity":30,
        "toHolder":"CN=Bank of Bob, OU=Test Dept, O=R3, L=NYC, C=US"
    }
}
 */