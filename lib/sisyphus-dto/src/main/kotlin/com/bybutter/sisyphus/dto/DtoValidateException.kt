package com.bybutter.sisyphus.dto

class DtoValidateException : Exception {
    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super("Dto validate fail", cause)

    constructor(message: String) : super(message)
}
