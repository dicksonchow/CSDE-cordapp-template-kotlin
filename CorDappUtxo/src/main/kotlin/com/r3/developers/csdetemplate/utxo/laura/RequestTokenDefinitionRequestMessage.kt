package com.r3.developers.csdetemplate.utxo.laura

import net.corda.v5.base.types.MemberX500Name

data class RequestTokenDefinitionRequestMessage(
    val name: String,
    val fractionDigits: Int,
    val tokenDefinitionAuthority: MemberX500Name
)
