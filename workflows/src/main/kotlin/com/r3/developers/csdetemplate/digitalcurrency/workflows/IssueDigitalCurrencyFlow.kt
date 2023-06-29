package com.r3.developers.csdetemplate.digitalcurrency.workflows

import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable

//define a basic object for JSON API response

//annotate flow with a protocol name
class IssueDigitalCurrencyFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {

        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            //get API request params from request body

            //get MemberInfo for currency participants (issuer node + holder)

            //create proposal for currency state to be on ledger

            //specify notary

            //specify parties who need to sign off on the currency issuance

            //create a transaction builder to issue the currency
            /*
                notary
                time window
                digital currency output state
                issue command
                signatories
             */

            //sign transaction

            //initiate session with holder

            //finalize transaction

            //return finalized transaction id
            return "implement me"
        }
        catch (e: Exception) {
            logger.warn("Failed to process issue digital currency for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-issue-digital-currency-protocol")
class FinalizeIssueDigitalCurrencyResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                //enforce responder flow validation
//                val state = ledgerTransaction.getOutputStates(DigitalCurrency::class.java).singleOrNull() ?:
//                throw CordaRuntimeException("Failed verification - transaction did not have exactly one output DigitalCurrency.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished issue digital currency responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Issue DigitalCurrency responder flow failed with exception", e)
            throw e
        }
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "issue-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.IssueDigitalCurrencyFlow",
    "requestBody": {
        "quantity":100,
        "holder":"CN=Bank of Alice, OU=Test Dept, O=R3, L=NYC, C=US",
    }
}
 */