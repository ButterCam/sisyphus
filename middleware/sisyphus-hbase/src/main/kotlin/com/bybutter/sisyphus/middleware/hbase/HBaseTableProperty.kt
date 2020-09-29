package com.bybutter.sisyphus.middleware.hbase

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class HBaseTableProperty(
    val qualifier: Class<*>,
    val urls: List<String>,
    val publicUrls: List<String>,
    val template: Class<*>
)

data class HBaseTableProperties(
    @NestedConfigurationProperty
    val hbase: Map<String, HBaseTableProperty>
)
