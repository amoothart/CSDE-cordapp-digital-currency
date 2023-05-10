package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import com.r3.developers.csdetemplate.digitalcurrency.contracts.MortgageContract
import com.r3.developers.csdetemplate.digitalcurrency.helpers.CoinSelection
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import com.r3.developers.csdetemplate.digitalcurrency.states.Mortgage
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import java.time.Duration
import java.time.Instant

// TODO: query by state id
data class SellMortgage(val address: String, val price: Int, val toOwner: String)

@InitiatingFlow(protocol = "finalize-sell-mortgage-protocol")
class SellMortgageFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val flowArgs = requestBody.getRequestBodyAs(json, SellMortgage::class.java)

            val fromOwner = memberLookup.myInfo()

            if (flowArgs.toOwner == fromOwner.name.toString()) {
                throw CordaRuntimeException("Cannot sell mortgage to self.")
            }

            val toHolder = memberLookup.lookup(MemberX500Name.parse(flowArgs.toOwner)) ?:
                throw CordaRuntimeException("MemberLookup can't find toHolder specified in flow arguments.")

            //TODO: needs query which returns StateAndRef
//            val existingMortgage = ledgerService.query("find mortgage by address", Mortgage::class.java)
//                                        .setParameter("address", flowArgs.address)
//                                        .execute()
//                                        .results.first()
            val existingMortgages = ledgerService.findUnconsumedStatesByType(Mortgage::class.java)
            val soldMortgage = existingMortgages.singleOrNull { it.state.contractState.address == flowArgs.address }
                ?: throw CordaRuntimeException("No mortgage found for address ${flowArgs.address}")

            val purchasedMortgage = soldMortgage.state.contractState.newOwner(toHolder.name)

            val notary = notaryLookup.notaryServices.single()

            val txBuilder = ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addInputStates(soldMortgage.ref)
                .addOutputStates(purchasedMortgage)
                .addCommand(MortgageContract.Sell())
                .addSignatories(fromOwner.ledgerKeys.first(), toHolder.ledgerKeys.first())

            val session = flowMessaging.initiateFlow(toHolder.name
            ) { flowContextProperties: FlowContextProperties ->
                flowContextProperties.put("price", flowArgs.price.toString())
                flowContextProperties.put("toOwner", flowArgs.toOwner)
            }

            logger.info("Seller sending TxBuilder to ${session.counterparty}")
            val updatedTxBuilder = ledgerService.sendAndReceiveTransactionBuilder(txBuilder, session)
            logger.info("Seller received TxBuilder from ${session.counterparty}")

            val signedTransaction = updatedTxBuilder.toSignedTransaction()

            logger.info("Seller finalizing Tx}")
            val finalizedSignedTransaction = ledgerService.finalize(signedTransaction, listOf(session))
            logger.info("Seller transaction finalized}")

            return finalizedSignedTransaction.transaction.id.toString().also {
                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
            }
        }
        catch (e: Exception) {
            logger.warn("Failed to process sell mortgage for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-sell-mortgage-protocol")
class FinalizeSellMortgageResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            logger.info("Buyer waiting to receive TxBuilder}")
            val proposedTxBuilder = ledgerService.receiveTransactionBuilder(session)
            logger.info("Buyer received TxBuilder}")
            val price = flowEngine.flowContextProperties.get("price") ?: throw CordaRuntimeException("Price not provided to buyer")
            val newOwner = flowEngine.flowContextProperties.get("toOwner") ?: throw CordaRuntimeException("New owner not provided to buyer")
            val toOwner = memberLookup.lookup(MemberX500Name.parse(newOwner)) ?:
                throw CordaRuntimeException("Buyer can't find toOwner specified in flow session.")

            val availableTokens = ledgerService.findUnconsumedStatesByType(DigitalCurrency::class.java)
            val coinSelection = CoinSelection()
            val (currencyToSpend, spentCurrency) = coinSelection.selectTokensForTransfer(price.toInt(), session.counterparty, availableTokens)

            proposedTxBuilder.addInputStates(currencyToSpend.map { it.ref })
            proposedTxBuilder.addOutputStates(spentCurrency)
            proposedTxBuilder.addCommand(DigitalCurrencyContract.Transfer())

            //send updated transaction back to seller
            logger.info("Buyer sending updated TxBuilder}")
            ledgerService.sendUpdatedTransactionBuilder(proposedTxBuilder, session)

            //wait for second session to sign and record transaction
            logger.info("Buyer waiting for finalization}")
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                val state = ledgerTransaction.getOutputStates(DigitalCurrency::class.java).first() ?:
                    throw CordaRuntimeException("Failed verification - transaction did not have at least one output DigitalCurrency.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished sell mortgage responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Sell Mortgage responder flow failed with exception", e)
            throw e
        }
    }
}

/*
{
    "clientRequestId": "sell-mortgage-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.SellMortgageFlow",
    "requestBody": {
        "address":"1234 Main St.",
        "price":100,
        "toOwner":"O=Bank of Bob, L=NYC, C=US"
    }
}
 */