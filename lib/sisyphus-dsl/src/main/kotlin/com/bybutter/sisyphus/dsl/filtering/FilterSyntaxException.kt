package com.bybutter.sisyphus.dsl.filtering

class FilterSyntaxException(
    val line: Int,
    val charPositionInLine: Int,
    val antlrErrorMessage: String?,
    cause: Exception?,
) : RuntimeException("Wrong Filter syntax in line $line:$charPositionInLine\n\t$antlrErrorMessage", cause)
