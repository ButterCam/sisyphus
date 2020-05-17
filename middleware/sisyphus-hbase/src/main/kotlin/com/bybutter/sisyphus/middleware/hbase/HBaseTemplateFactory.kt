package com.bybutter.sisyphus.middleware.hbase

interface HBaseTemplateFactory {
    fun createTemplate(property: HBaseTableProperty): HTableTemplate<*, *>
}
