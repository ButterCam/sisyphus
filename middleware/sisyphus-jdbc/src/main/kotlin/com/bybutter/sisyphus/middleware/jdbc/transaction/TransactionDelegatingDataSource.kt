package com.bybutter.sisyphus.middleware.jdbc.transaction

import java.sql.Connection
import javax.sql.DataSource

class TransactionDelegatingDataSource(delegate: DataSource) : DelegatingDataSource(delegate) {
    override fun getConnection(): Connection {
        val transactionContext = CoroutineTransactionContext.current()
        return if (transactionContext?.transactionActive == true) {
            transactionContext.getConnection(delegate)
        } else {
            delegate.connection
        }
    }
}
