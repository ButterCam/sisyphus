package com.bybutter.sisyphus.uri

import java.net.URI

class JdbcURIBuilder internal constructor(uri: URI) {
    private val scheme = mutableListOf<String>()
    val schemePart: List<String> = scheme
    val builder: URIBuilder

    init {
        if (uri.scheme != "jdbc") {
            throw IllegalArgumentException("Wrong JDBC URI '$uri'.")
        }
        var resolvedUri = uri
        while (resolvedUri.scheme != null) {
            scheme += resolvedUri.scheme
            resolvedUri = URI.create(resolvedUri.schemeSpecificPart)
        }
        builder = resolvedUri.toBuilder()
    }

    var userInfo: String?
        get() = builder.userInfo
        set(value) {
            builder.setUserInfo(value ?: return)
        }
    var host: String?
        get() = builder.host
        set(value) {
            builder.setHost(value ?: return)
        }
    var port: Int
        get() = builder.port
        set(value) {
            builder.setPort(value)
        }
    var path: String?
        get() = builder.path
        set(value) {
            builder.setPath(value ?: return)
        }
    val query get() = builder.query

    fun appendQueryPart(key: String, vararg value: String): JdbcURIBuilder {
        builder.appendQueryPart(key, *value)
        return this
    }

    fun appendQueryPart(key: String, value: Iterable<String>): JdbcURIBuilder {
        builder.appendQueryPart(key, value)
        return this
    }

    fun clearQuery(): JdbcURIBuilder {
        builder.clearQuery()
        return this
    }

    fun setQueryPart(query: String): JdbcURIBuilder {
        builder.setQueryPart(query)
        return this
    }

    fun getQuery(key: String): List<String> {
        return builder.getQuery(key)
    }

    fun build(): URI {
        val ssp = buildString {
            for (scheme in schemePart.subList(1, schemePart.size)) {
                append(scheme)
                append(":")
            }
            append("//")
            append(builder)
        }
        return URI(scheme.first(), ssp, null)
    }

    override fun toString(): String {
        return buildString {
            for (scheme in schemePart) {
                append(scheme)
                append(":")
            }
            append("//")
            append(builder)
        }
    }
}

fun URI.toJdbcBuilder(): JdbcURIBuilder {
    return JdbcURIBuilder(this)
}
