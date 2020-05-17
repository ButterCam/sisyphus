package com.bybutter.sisyphus.middleware.jdbc.transaction

interface TransactionContext {
    fun nest(): TransactionContext

    fun rollback()

    fun commit()
}
