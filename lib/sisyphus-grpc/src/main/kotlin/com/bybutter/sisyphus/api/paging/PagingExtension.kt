package com.bybutter.sisyphus.api.paging

import com.bybutter.sisyphus.security.base64UrlSafe
import com.bybutter.sisyphus.security.base64UrlSafeDecode

operator fun NameAnchorPaging.Companion.invoke(pagingToken: String): NameAnchorPaging? {
    if (pagingToken.isEmpty()) {
        return null
    }

    return NameAnchorPaging.parse(pagingToken.base64UrlSafeDecode())
}

fun NameAnchorPaging.toToken(): String {
    return this.toProto().base64UrlSafe()
}

fun NameAnchorPaging?.nextPage(name: String?, size: Int, pageSize: Int): String? {
    if (name == null || size == 0 || size < pageSize) {
        return null
    }

    return NameAnchorPaging {
        this.name = name
    }.toToken()
}

operator fun OffsetPaging.Companion.invoke(pagingToken: String): OffsetPaging? {
    if (pagingToken.isEmpty()) {
        return null
    }

    return OffsetPaging.parse(pagingToken.base64UrlSafeDecode())
}

fun OffsetPaging.toToken(): String {
    return this.toProto().base64UrlSafe()
}

fun OffsetPaging?.nextPage(size: Int, pageSize: Int): String? {
    if (size < pageSize) {
        return null
    }

    return OffsetPaging {
        this.offset = this@nextPage?.offset?.plus(size) ?: size
    }.toToken()
}
