package com.r3.developers.csdetemplate.digitalcurrency.states

import com.r3.developers.csdetemplate.digitalcurrency.contracts.BundleOfBondsContract
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.*

@BelongsToContract(BundleOfBondsContract::class)
data class BundleOfBonds(
    val bundleId: UUID,
    val originator: PublicKey,
    val bondIds: List<UUID>,
    private val participants: List<PublicKey>) : ContractState {
    override fun getParticipants(): List<PublicKey> {
        return listOf(originator)
    }
}