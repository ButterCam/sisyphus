package com.bybutter.sisyphus.middleware.jdbc.transaction

import javax.sql.DataSource

open class DelegatingDataSource(protected val delegate: DataSource) : DataSource by delegate {
    override fun isWrapperFor(iface: Class<*>): Boolean {
        return iface.isInstance(this) || delegate.isWrapperFor(iface)
    }

    override fun <T : Any> unwrap(iface: Class<T>): T {
        return if (iface.isInstance(this)) {
            this as T
        } else delegate.unwrap(iface)
    }
}
