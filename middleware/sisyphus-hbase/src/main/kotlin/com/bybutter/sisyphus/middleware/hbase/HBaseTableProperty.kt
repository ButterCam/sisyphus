package com.bybutter.sisyphus.middleware.hbase

import org.springframework.boot.context.properties.NestedConfigurationProperty

data class HBaseTableProperty(
    val qualifier: Class<*>,
    val urls: List<String>,
    val template: Class<*>,
    val extensions: Map<String, Any> = mapOf()
)

data class HBaseTableProperties(
    @NestedConfigurationProperty
    val hbase: Map<String, HBaseTableProperty>
)
