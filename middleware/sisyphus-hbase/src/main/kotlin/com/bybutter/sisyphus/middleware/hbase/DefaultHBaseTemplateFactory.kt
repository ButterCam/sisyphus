package com.bybutter.sisyphus.middleware.hbase

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.HConstants
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(value = [HBaseTemplateFactory::class])
class DefaultHBaseTemplateFactory : HBaseTemplateFactory {
    private val connections: MutableMap<String, Connection> = hashMapOf()

    override fun createTemplate(property: HBaseTableProperty): HTableTemplate<*, *> {
        val connection = createConnection(property.urls, property)
        return createTemplate(property.template, connection)
    }

    protected fun createConnection(urls: List<String>, property: HBaseTableProperty): Connection {
        return connections.getOrPut(urls.sorted().joinToString()) {
            val config = HBaseConfiguration.create().apply {
                this[HConstants.ZOOKEEPER_QUORUM] = urls.joinToString(",")
            }
            ConnectionFactory.createConnection(config)
        }
    }

    protected fun createTemplate(clazz: Class<*>, connection: Connection): HTableTemplate<*, *> {
        val template = clazz.constructors.first {
            it.canAccess(null) && it.parameters.isEmpty()
        }.newInstance() as HTableTemplate<*, *>
        template.connection = connection
        return template
    }
}
