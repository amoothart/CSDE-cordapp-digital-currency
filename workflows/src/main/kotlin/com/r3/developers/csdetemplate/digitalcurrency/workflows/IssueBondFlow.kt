package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.BondContract
import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import java.security.PublicKey
import java.time.Duration
import java.time.Instant
import java.util.*

data class IssueBond(val creditor: String,
                     val interestRate: Double,
                     val fixedInterestRate: Boolean,
                     val loanToValue: Double,
                     val creditQualityRating: String)

@InitiatingFlow(protocol = "finalize-issue-bond-protocol")
class IssueBondFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")
        try {
            val flowArgs = requestBody.getRequestBodyAs(json, IssueBond::class.java)

            val myInfo = memberLookup.myInfo()
            val creditor = memberLookup.lookup(MemberX500Name.parse(flowArgs.creditor)) ?:
                throw CordaRuntimeException("MemberLookup can't find owner specified in flow arguments.")

            val bond = Bond(bondId = UUID.randomUUID(),
                creditor.ledgerKeys.first(),
                flowArgs.interestRate,
                flowArgs.fixedInterestRate,
                flowArgs.loanToValue,
                flowArgs.creditQualityRating,
                participants = listOf(myInfo.ledgerKeys.first(), creditor.ledgerKeys.first()))

            val notary = notaryLookup.notaryServices.single()

            val signatories = mutableListOf<PublicKey>(myInfo.ledgerKeys.first())
            signatories.union(bond.participants)

            val txBuilder = ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(bond)
                .addCommand(BondContract.Issue())
                .addSignatories(signatories)

            val signedTransaction = txBuilder.toSignedTransaction()

            val session = flowMessaging.initiateFlow(creditor.name)

            val finalizedSignedTransaction = ledgerService.finalize(
                signedTransaction,
                listOf(session)
            )
            return finalizedSignedTransaction.transaction.id.toString().also {
                logger.info("Successful ${signedTransaction.commands.first()} with response: $it")
            }
        }
        catch (e: Exception) {
            logger.warn("Failed to process issue bond for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-issue-bond-protocol")
class FinalizeIssueBondResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                val state = ledgerTransaction.getOutputStates(Bond::class.java).singleOrNull() ?:
                throw CordaRuntimeException("Failed verification - transaction did not have exactly one output Bond.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished issue bond responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Issue Bond responder flow failed with exception", e)
            throw e
        }
    }
}