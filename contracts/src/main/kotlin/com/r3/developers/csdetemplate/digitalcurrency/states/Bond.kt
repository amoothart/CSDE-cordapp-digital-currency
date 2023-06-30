package com.r3.developers.csdetemplate.digitalcurrency.states

import com.r3.developers.csdetemplate.digitalcurrency.contracts.BondContract
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*

@BelongsToContract(BondContract::class)
data class Bond(
    val bondId: UUID,
    val creditor: PublicKey,
    val interestRate: Double,
    val fixedInterestRate: Boolean,
    val loanToValue: Double,
    val creditQualityRating: String,
    val bundled: Boolean = false,
    private val participants: List<PublicKey>) : ContractState {
    override fun getParticipants(): List<PublicKey> {
        return listOf(creditor)
    }

    fun newCreditor(newCreditor: PublicKey) =
        copy(creditor = newCreditor)

    fun bundled() =
        copy(bundled = true)
}