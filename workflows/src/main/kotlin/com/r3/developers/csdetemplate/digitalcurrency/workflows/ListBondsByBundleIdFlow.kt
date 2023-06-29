package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.helpers.findInfo
import com.r3.developers.csdetemplate.digitalcurrency.states.BundleOfBonds
import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import org.slf4j.LoggerFactory
import java.util.*

data class ListBondsByBundleId(val bundleId: UUID)

class ListBondsByBundleIdFlow : AbstractFlow(), ClientStartableFlow {
    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        log.info("ListBondsByBundleIdFlow.call() called")
        val flowArgs = requestBody.getRequestBodyAs(json, ListBondsByBundleId::class.java)

        val queryingMember = memberLookup.myInfo()

        val bundle = ledgerService.findUnconsumedStatesByType(BundleOfBonds::class.java).filter { bundle ->
            bundle.state.contractState.bundleId == flowArgs.bundleId
        }

        if( bundle.isEmpty()) throw CordaRuntimeException("No bundle found for id: ${flowArgs.bundleId}")

        val bonds = ledgerService.findUnconsumedStatesByType(Bond::class.java).filter { bond ->
            bond.state.contractState.creditor == queryingMember.ledgerKeys.first() &&
                    bundle.first().state.contractState.bondIds.contains(bond.state.contractState.bondId)
        }

        val results = bonds.map {
            BondsStateResults(
                it.state.contractState.bondId,
                memberLookup.findInfo(it.state.contractState.creditor).name,
                it.state.contractState.interestRate,
                it.state.contractState.fixedInterestRate,
                it.state.contractState.loanToValue,
                it.state.contractState.creditQualityRating,
                it.state.contractState.bundled) }

        return json.format(results)
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-bonds-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.ListBondsByBundleIdFlow",
    "requestBody": {}
}
*/