package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import com.r3.developers.csdetemplate.digitalcurrency.helpers.CoinSelection
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import java.time.Duration
import java.time.Instant

data class WithdrawDigitalCurrency(val quantity: Int)

@InitiatingFlow(protocol = "finalize-withdraw-digital-currency-protocol")
class WithdrawDigitalCurrencyFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val flowArgs = requestBody.getRequestBodyAs(json, WithdrawDigitalCurrency::class.java)

            if(flowArgs.quantity <= 0) {
                throw CordaRuntimeException("Must withdraw a positive amount of currency.")
            }

            val fromHolder = memberLookup.myInfo()

//            val availableTokens = ledgerService.findUnconsumedStatesByType(DigitalCurrency::class.java)
//
//            val coinSelection = CoinSelection()
//            val (currencyToWithdraw, remainingCurrency) = coinSelection.selectTokensForRedemption(flowArgs.quantity, availableTokens)
//
//            val notary = notaryLookup.notaryServices.single()
//
//            val txBuilder = ledgerService.createTransactionBuilder()
//                .setNotary(notary.name)
//                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
//                .addInputStates(currencyToWithdraw.map { it.ref })
//                .addOutputStates(remainingCurrency)
//                .addCommand(DigitalCurrencyContract.Withdraw())
//                .addSignatories(fromHolder.ledgerKeys.first()) // issuer does not sign
//
//            val signedTransaction = txBuilder.toSignedTransaction()
//
//            val finalizedSignedTransaction = ledgerService.finalize(
//                signedTransaction,
//                listOf()
//            )
//
//            return finalizedSignedTransaction.transaction.id.toString().also {
//                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
//            }
            return "implement me"
        } catch (e: Exception) {
            logger.warn("Failed to process transfer digital currency for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-withdraw-digital-currency-protocol")
class FinalizeWithdrawDigitalCurrencyResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
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
    "clientRequestId": "withdrawal-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.WithdrawDigitalCurrencyFlow",
    "requestBody": {
        "quantity":30
    }
}
 */