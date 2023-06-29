package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import com.r3.developers.csdetemplate.digitalcurrency.contracts.BondContract
import com.r3.developers.csdetemplate.digitalcurrency.helpers.CoinSelection
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.collections.List

data class SellBond (val bondIds: List<UUID>, val price: Int, val buyer: String)

@InitiatingFlow(protocol = "finalize-sell-bond-protocol")
class SellBondFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val flowArgs = requestBody.getRequestBodyAs(json, SellBond::class.java)

            val fromOwner = memberLookup.myInfo()

            if (flowArgs.buyer == fromOwner.name.toString()) {
                throw CordaRuntimeException("Cannot sell bond to self.")
            }

            val buyer = memberLookup.lookup(MemberX500Name.parse(flowArgs.buyer)) ?:
                throw CordaRuntimeException("MemberLookup can't find toHolder specified in flow arguments.")

            // Queries the VNode's vault for unconsumed states to be sold
            // Filter query results to the bond id which was provided in the API request

            // Map existing bond to a proposed new state with a new owner "buyer"

            val notary = notaryLookup.notaryServices.single()

            // Complete transaction builder with the following information
            // bond input state
            // bond output state
            // sell command
            // signatories
            val txBuilder = ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))

            val session = flowMessaging.initiateFlow(buyer.name
            ) { flowContextProperties: FlowContextProperties ->
                // Establish a session to the buyer's virtual node and trade information: price
            }

            logger.info("Seller sending TxBuilder to ${session.counterparty}")
            //send and receive partial transaction builder to buyer
            logger.info("Seller received TxBuilder from ${session.counterparty}")

            //sign (and check) the completed transaction
//            val signedTransaction = updatedTxBuilder.toSignedTransaction()

            logger.info("Seller finalizing Tx}")
            // finalize the completed transaction
//            val finalizedSignedTransaction = ledgerService.finalize(signedTransaction, listOf(session))
            logger.info("Seller transaction finalized}")

//            return finalizedSignedTransaction.transaction.id.toString().also {
//                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
//            }
            return "implement me"
        }
        catch (e: Exception) {
            logger.warn("Failed to process sell bond for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-sell-bond-protocol")
class FinalizeSellBondResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            logger.info("Buyer waiting to receive TxBuilder}")
            // receive proposed transaction from seller
            logger.info("Buyer received TxBuilder}")
            // get price from counterparty flow session
            // get buyer from our identity
            // get seller from session counterparty

            // query buyer's ledger for unspent cash
            // select tokens to spend using Coin Selection helper
            val coinSelection = CoinSelection()
//            val (currencyToSpend, spentCurrency) = coinSelection.selectTokensForTransfer(price.toInt(),
//                                                            buyer.ledgerKeys.first(),
//                                                            seller.ledgerKeys.first(),
//                                                            availableTokens)

            // add buyer's currency to transaction builder as input
            // add updated currency with seller as owner as output
            // add command for transferring digital currency

            //send updated transaction back to seller
            logger.info("Buyer sending updated TxBuilder}")

            //wait for second session to sign and record transaction
            logger.info("Buyer waiting for finalization}")
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                // restore buyer's checks on the final transaction proposal
//                val state = ledgerTransaction.getOutputStates(DigitalCurrency::class.java).first() ?:
//                    throw CordaRuntimeException("Failed verification - transaction did not have at least one output DigitalCurrency.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished sell bond responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Sell Bond responder flow failed with exception", e)
            throw e
        }
    }
}

/*
{
    "clientRequestId": "sell-bond-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.SellBondFlow",
    "requestBody": {
        "bondIds":["1234"],
        "price":100,
        "buyer":"O=Bank of Bob, L=NYC, C=US"
    }
}
 */