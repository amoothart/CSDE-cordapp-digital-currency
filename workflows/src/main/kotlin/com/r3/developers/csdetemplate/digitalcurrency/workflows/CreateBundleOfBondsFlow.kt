package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.contracts.BundleOfBondsContract
import com.r3.developers.csdetemplate.digitalcurrency.contracts.BondContract
import com.r3.developers.csdetemplate.digitalcurrency.states.BundleOfBonds
import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import java.time.Duration
import java.time.Instant
import java.util.*

data class CreateBundleOfBonds(val bondIds: List<UUID>)

@InitiatingFlow(protocol = "finalize-create-bundle-protocol")
class CreateBundleOfBondsFlow: AbstractFlow(), ClientStartableFlow {

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        logger.info("${this::class.java.enclosingClass}.call() called")
        try {
            val flowArgs = requestBody.getRequestBodyAs(json, CreateBundleOfBonds::class.java)

            val myInfo = memberLookup.myInfo()

            val bundle = BundleOfBonds(
                bundleId = UUID.randomUUID(),
                myInfo.ledgerKeys.first(),
                flowArgs.bondIds,
                participants = listOf(myInfo.ledgerKeys.first()))

            val targetBonds = ledgerService.findUnconsumedStatesByType(Bond::class.java).filter { bond ->
                flowArgs.bondIds.contains(bond.state.contractState.bondId)
            }

            if(targetBonds.isEmpty()) throw CordaRuntimeException("Found no bonds to bundle.")

            val bundledBonds = targetBonds.map { bond ->
                bond.state.contractState.bundled()
            }

            val notary = notaryLookup.notaryServices.single()

            val txBuilder = ledgerService.createTransactionBuilder()
                .setNotary(notary.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addInputStates(targetBonds.map { it.ref })
                .addOutputStates(bundledBonds)
                .addCommand(BondContract.Bundle())
                .addOutputState(bundle)
                .addCommand(BundleOfBondsContract.Create())
                .addSignatories(myInfo.ledgerKeys.first())

            val selfSignedTransaction = txBuilder.toSignedTransaction()

            val session = flowMessaging.initiateFlow(myInfo.name)

            val finalizedSignedTransaction = ledgerService.finalize(
                selfSignedTransaction, listOf(session))

            return finalizedSignedTransaction.transaction.id.toString().also {
                logger.info("Successful ${selfSignedTransaction.commands.first()} with response: $it")
            }
        }
        catch (e: Exception) {
            logger.warn("Failed to process bundle bond for request body '$requestBody' with exception: '${e.message}'")
            throw e
        }
    }
}

@InitiatedBy(protocol = "finalize-create-bundle-protocol")
class FinalizeCreateBundleOfBondsResponderFlow: AbstractFlow(), ResponderFlow {

    @Suspendable
    override fun call(session: FlowSession) {
        logger.info("${this::class.java.enclosingClass}.call() called")

        try {
            val finalizedSignedTransaction = ledgerService.receiveFinality(session) { ledgerTransaction ->
                val state = ledgerTransaction.getOutputStates(BundleOfBonds::class.java).singleOrNull() ?:
                throw CordaRuntimeException("Failed verification - transaction did not have exactly one output BundleOfBonds.")

                logger.info("Verified the transaction- ${ledgerTransaction.id}")
            }
            logger.info("Finished create bundle of bonds responder flow - ${finalizedSignedTransaction.transaction.id}")
        }
        catch (e: Exception) {
            logger.warn("Create Bundle of Bonds responder flow failed with exception", e)
            throw e
        }
    }
}