package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.helpers.findInfo
import com.r3.developers.csdetemplate.digitalcurrency.states.BundleOfBonds
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.util.*

data class BundleOfBondsStateResults(val bundleId: UUID,
                                     val originator: MemberX500Name,
                                     val bonds: List<UUID>)

class ListBundleOfBondsFlow : ClientStartableFlow {
    private companion object {
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @CordaInject
    lateinit var jsonMarshallingService: JsonMarshallingService

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    lateinit var ledgerService: UtxoLedgerService

    @CordaInject
    lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        log.info("ListBundleOfBondsFlow.call() called")
        val queryingMember = memberLookup.myInfo()

        val states = ledgerService.findUnconsumedStatesByType(BundleOfBonds::class.java).filter { bundle ->
            bundle.state.contractState.originator == queryingMember.ledgerKeys.first()
        }

        val results = states.map {
            BundleOfBondsStateResults(
                it.state.contractState.bundleId,
                memberLookup.findInfo(it.state.contractState.originator).name,
                it.state.contractState.bondIds) }

        return jsonMarshallingService.format(results)
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-bonds-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.ListBundleOfBondsFlow",
    "requestBody": {}
}
*/