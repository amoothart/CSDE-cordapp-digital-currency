package com.r3.developers.csdetemplate.digitalcurrency.workflows

import com.r3.developers.csdetemplate.digitalcurrency.helpers.findInfo
import com.r3.developers.csdetemplate.digitalcurrency.states.DigitalCurrency
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory

data class DigitalCurrencyStateResults(val quantity: Int, val holder: MemberX500Name)

class ListDigitalCurrencyFlow : ClientStartableFlow {
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
        log.info("ListDigitalCurrenciesFlow.call() called")
        val queryingMember = memberLookup.myInfo()

//        val states = ledgerService.findUnconsumedStatesByType(DigitalCurrency::class.java).filter { digitalCurrency ->
//            digitalCurrency.state.contractState.holder == queryingMember.ledgerKeys.first()
//        }
//
//        val results = states.map {
//            DigitalCurrencyStateResults(
//                it.state.contractState.quantity,
//                memberLookup.findInfo(it.state.contractState.holder).name) }
//
//        return jsonMarshallingService.format(results)
        return "implement me"
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.csdetemplate.digitalcurrency.workflows.ListDigitalCurrenciesFlow",
    "requestBody": {}
}
*/