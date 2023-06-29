package com.r3.developers.csdetemplate.digitalcurrency.states

import com.r3.developers.csdetemplate.digitalcurrency.contracts.DigitalCurrencyContract
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey

//annotate which contract governs the digital currency state
//inherit state from ContractState class
data class DigitalCurrency(
    //add property to represent the quantity of currency
    //add property to represent the owner of currency
    private val participants: List<PublicKey>) {
        //implement get participants as only the holder

        //sendAmount to get an updated count of the currency

        //sendTo to change ownership of the currency

        //sendAmountTo to change ownership and quantity of the currency
}