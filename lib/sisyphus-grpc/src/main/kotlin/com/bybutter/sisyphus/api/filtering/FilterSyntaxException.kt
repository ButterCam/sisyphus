package com.bybutter.sisyphus.api.filtering

class FilterSyntaxException(
    val line: Int,
    val charPositionInLine: Int,
    val antlrErrorMessage: String?,
    cause: Exception?
) : RuntimeException("Wrong Filter syntax in line $line:$charPositionInLine\n\t$antlrErrorMessage", cause)
