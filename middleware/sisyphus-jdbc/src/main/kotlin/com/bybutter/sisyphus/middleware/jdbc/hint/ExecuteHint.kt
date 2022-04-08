package com.bybutter.sisyphus.middleware.jdbc.hint

interface ExecuteHint {
    fun wrapSql(sql: String): String

    fun key(): String {
        return this.javaClass.canonicalName
    }
}
