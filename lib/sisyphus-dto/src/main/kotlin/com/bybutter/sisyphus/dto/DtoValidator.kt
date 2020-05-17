package com.bybutter.sisyphus.dto

interface DtoValidator<T : DtoModel> {
    fun verify(value: T, params: Array<out String>): Exception?
}
