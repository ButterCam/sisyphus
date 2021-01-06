package com.bybutter.sisyphus.middleware.jdbc.utils

import com.bybutter.sisyphus.api.paging.OffsetPaging
import com.bybutter.sisyphus.api.paging.invoke
import com.bybutter.sisyphus.api.paging.nextPage
import org.jooq.Record
import org.jooq.SelectLimitStep

data class Page<T>(var data: List<T>, var nextToken: String?, var total: Int? = null)

fun <T : Record> SelectLimitStep<T>.withPaging(pageToken: String, pageSize: Int, needTotal: Boolean = true): Page<T> {
    return withTypePaging(this.recordType, pageToken, pageSize, needTotal) as Page<T>
}

inline fun <T : Record, reified R> SelectLimitStep<T>.withTypePaging(pageToken: String, pageSize: Int, needTotal: Boolean = true): Page<R> {
    return withTypePaging(R::class.java, pageToken, pageSize, needTotal) as Page<R>
}

fun <T : Record> SelectLimitStep<T>.withTypePaging(target: Class<*>, pageToken: String, pageSize: Int, needTotal: Boolean = true): Page<*> {
    val offset = OffsetPaging(pageToken)?.offset
    val size = if (pageSize in 1..30) {
        pageSize
    } else {
        30
    }
    val count = if (needTotal) this.count() else null
    val data = this.offset(offset).limit(size).fetchInto(target)
    val next = OffsetPaging(pageToken).nextPage(data.size, size)
    return Page(data, next, count)
}
