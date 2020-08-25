package com.bybutter.sisyphus.middleware.jdbc.utils

import com.bybutter.sisyphus.api.paging.OffsetPaging
import com.bybutter.sisyphus.api.paging.invoke
import com.bybutter.sisyphus.api.paging.nextPage
import org.jooq.Record
import org.jooq.SelectConditionStep

data class Page<T>(var data: List<T>, var nextToken: String?, var total: Int? = null)

inline fun <reified T : Record> SelectConditionStep<T>.withPaging(pageToken: String, pageSize: Int, needTotal: Boolean = true): Page<T> {
    val offset = OffsetPaging(pageToken)?.offset
    val size = if (pageSize in 1..30) {
        pageSize
    } else {
        30
    }
    val count = if (needTotal) this.count() else null
    val data = this.offset(offset).limit(size).fetchInto(T::class.java)
    val next = OffsetPaging(pageToken).nextPage(data.size, size)
    return Page(data, next, count)
}
