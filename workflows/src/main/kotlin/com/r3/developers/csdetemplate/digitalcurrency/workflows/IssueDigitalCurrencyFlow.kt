package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import java.security.PublicKey
import java.time.Duration
import java.time.Instant

data class IssueDigitalCurrency(val quantity: Int, val holder: String)

@InitiatingFlow(protocol = "finalize-issue-digital-currency-protocol")
class IssueDigitalCurrencyFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {

        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val flowArgs = requestBody.getRequestBodyAs(json, IssueDigitalCurrency::class.java)

            val myInfo = memberLookup.myInfo()
            val holder = memberLookup.lookup(MemberX500Name.parse(flowArgs.holder)) ?:
                throw CordaRuntimeException("MemberLookup can't find holder specified in flow arguments.")

            val digitalCurrency = DigitalCurrency(flowArgs.quantity,
                holder.ledgerKeys.first(),
                participants = listOf(myInfo.ledgerKeys.first(), holder.ledgerKeys.first()))

            val notary = notaryLookup.notaryServices.single()

            val signatories = mutableListOf<PublicKey>(myInfo.ledgerKeys.first())
            signatories.union(digitalCurrency.participants)

            val txBuilder = ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(digitalCurrency)
                .addCommand(DigitalCurrencyContract.Issue())
                .addSignatories(signatories)

            val signedTransaction = txBuilder.toSignedTransaction()

            val session = flowMessaging.initiateFlow(holder.name)

            val finalizedSignedTransaction = ledgerService.finalize(
                signedTransaction,
                listOf(session)
            )
            return finalizedSignedTransaction.transaction.id.toString().also {
                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
            }
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
                val state = ledgerTransaction.getOutputStates(DigitalCurrency::class.java).singleOrNull() ?:
                throw CordaRuntimeException("Failed verification - transaction did not have exactly one output DigitalCurrency.")

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