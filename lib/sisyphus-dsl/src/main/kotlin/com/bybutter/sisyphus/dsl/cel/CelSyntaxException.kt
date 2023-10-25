package com.bybutter.sisyphus.dsl.cel

class CelSyntaxException(
    val line: Int,
    val charPositionInLine: Int,
    val antlrErrorMessage: String?,
    cause: Exception?,
) : RuntimeException("Wrong CEL syntax in line $line:$charPositionInLine\n\t$antlrErrorMessage", cause)
