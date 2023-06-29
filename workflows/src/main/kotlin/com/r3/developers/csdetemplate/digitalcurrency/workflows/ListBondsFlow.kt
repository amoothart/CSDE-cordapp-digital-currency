package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.helpers.findInfo
import com.r3.developers.csdetemplate.digitalcurrency.states.Bond
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

data class BondsStateResults(val mortgageId: UUID,
                             val owner: MemberX500Name,
                             val interestRate: Double,
                             val fixedInterestRate: Boolean,
                             val loanToValue: Double,
                             val creditQualityRating: String,
                             val bundled: Boolean)

class ListBondsFlow : ClientStartableFlow {
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
        log.info("ListMortgagesFlow.call() called")
        val queryingMember = memberLookup.myInfo()

        val states = ledgerService.findUnconsumedStatesByType(Bond::class.java).filter { mortgages ->
            mortgages.state.contractState.creditor == queryingMember.ledgerKeys.first()
        }

        val results = states.map {
            BondsStateResults(
                it.state.contractState.bondId,
                memberLookup.findInfo(it.state.contractState.creditor).name,
                it.state.contractState.interestRate,
                it.state.contractState.fixedInterestRate,
                it.state.contractState.loanToValue,
                it.state.contractState.creditQualityRating,
                it.state.contractState.bundled) }

        return jsonMarshallingService.format(results)
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-mortgages-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.ListMortgagesFlow",
    "requestBody": {}
}
*/