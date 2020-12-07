package com.bybutter.sisyphus.uri

import com.bybutter.sisyphus.data.urlDecode
import com.bybutter.sisyphus.data.urlEncode
import java.net.URI

class URIBuilder {
    // Common part
    var scheme: String? = null
        private set
    var fragment: String? = null
        private set

    // Opaque URI
    var schemeSpecificPart: String? = null
        private set

    // Hierarchical URI
    var authority: String? = null
        private set
    var path: String? = null
        private set
    val query get() = buildQuery()
    private val queryMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    // Server-based Authority
    var userInfo: String? = null
        private set
    var host: String? = null
        private set
    var port: Int = -1
        private set

    val isAbsolute get() = scheme != null

    val isOpaque get() = schemeSpecificPart != null

    internal constructor(uri: URI) {
        scheme = uri.scheme
        fragment = uri.fragment

        if (uri.isOpaque) {
            schemeSpecificPart = uri.schemeSpecificPart
        } else {
            authority = uri.authority
            path = uri.path
            queryMap += parseQuery(uri.query)

            uri.userInfo?.let {
                userInfo = it
            }
            uri.host?.let {
                host = it
            }
            uri.port.let {
                port = it
            }
        }
    }

    constructor(scheme: String) {
        this.scheme = scheme
    }

    private fun parseQuery(query: String): MutableMap<String, MutableList<String>> {
        return query.split('&').asSequence().mapNotNull {
            val data = it.split("=", limit = 2)
            if (data.size != 2) return@mapNotNull null
            data[0].urlDecode() to data[1].urlDecode()
        }.groupBy { it.first }.mapValues {
            it.value.map { it.second }.toMutableList()
        }.toMutableMap()
    }

    fun setScheme(scheme: String): URIBuilder {
        this.scheme = scheme
        return this
    }

    fun setFragment(fragment: String): URIBuilder {
        this.fragment = fragment.urlEncode()
        return this
    }

    fun setAuthority(authority: String): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no authority.")
        this.authority = authority
        normalizeAuthority(false)
        return this
    }

    private fun normalizeAuthority(baseUserInfo: Boolean) {
        if (baseUserInfo) {
            val testURI = URI("test", userInfo, host, -1, "/", null, null)
            authority = testURI.authority
        } else {
            val testURI = URI("test", authority, "/", null, null)
            userInfo = testURI.userInfo
            host = testURI.host
            port = testURI.port
        }
    }

    fun setUserInfo(user: String, password: String): URIBuilder {
        return setUserInfo("${user.urlEncode()}:${password.urlEncode()}")
    }

    fun setUserInfo(userInfo: String): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no user info.")
        this.userInfo = userInfo
        normalizeAuthority(true)
        return this
    }

    fun setHost(host: String): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no port.")
        this.host = host
        normalizeAuthority(true)
        return this
    }

    fun setPort(port: Int): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no port.")
        this.port = port
        normalizeAuthority(true)
        return this
    }

    fun setPath(path: String): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no path.")
        this.path = path
        return this
    }

    fun appendQueryPart(key: String, vararg value: String): URIBuilder {
        return appendQueryPart(key, value.asSequence().asIterable())
    }

    fun appendQueryPart(key: String, value: Iterable<String>): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no query.")
        queryMap.getOrPut(key) { mutableListOf() } += value.map { it.urlEncode() }
        return this
    }

    fun clearQuery(): URIBuilder {
        queryMap.clear()
        return this
    }

    fun setQueryPart(key: String): URIBuilder {
        if (isOpaque) throw IllegalStateException("Opaque URI has no query.")
        queryMap.clear()
        queryMap += parseQuery(key)
        return this
    }

    fun getQuery(query: String): List<String> {
        return queryMap.getOrPut(query) { mutableListOf() }
    }

    private fun buildQuery(): String? {
        val result = buildString {
            for ((key, values) in queryMap) {
                for (value in values) {
                    if (this.isNotEmpty()) {
                        append("&")
                    }
                    append(key)
                    append("=")
                    append(value)
                }
            }
        }
        return if (result.isEmpty()) null else result
    }

    fun build(): URI {
        return if (isOpaque) {
            URI(buildString {
                scheme?.let {
                    append(it)
                    append(":")
                }
                schemeSpecificPart?.let {
                    append(it)
                }
                fragment?.let {
                    append("#")
                    append(it)
                }
            })
        } else {
            URI(buildString {
                scheme?.let {
                    append(it)
                    append(":")
                }
                authority?.let {
                    append("//")
                    append(it)
                }
                path?.let {
                    append(it)
                }
                query?.let {
                    append("?")
                    append(it)
                }
                fragment?.let {
                    append("#")
                    append(it)
                }
            })
        }
    }

    override fun toString(): String {
        return build().toString()
    }
}

fun URI.toBuilder(): URIBuilder {
    return URIBuilder(this)
}
