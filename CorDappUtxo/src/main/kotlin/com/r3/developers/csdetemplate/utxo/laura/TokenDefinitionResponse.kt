package com.r3.developers.csdetemplate.utxo.laura

import com.r3.dc.platform.contracts.tokendefinition.TokenDefinitionStatus
import java.util.*

data class TokenDefinitionResponse(
    val name: String,
    val fractionDigits: Int,
    val tokenDefiningEntity: String,
    val tokenDefinitionAuthority: String,
    val status: TokenDefinitionStatus,
    val id: UUID
)