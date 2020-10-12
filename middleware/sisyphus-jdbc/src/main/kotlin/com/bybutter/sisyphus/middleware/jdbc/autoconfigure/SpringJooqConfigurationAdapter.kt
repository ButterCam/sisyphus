package com.bybutter.sisyphus.middleware.jdbc.autoconfigure

import com.bybutter.sisyphus.middleware.jdbc.JooqConfigInterceptor
import kotlin.streams.toList
import org.jooq.Configuration
import org.jooq.ExecuteListenerProvider
import org.jooq.ExecutorProvider
import org.jooq.RecordListenerProvider
import org.jooq.RecordMapperProvider
import org.jooq.RecordUnmapperProvider
import org.jooq.TransactionListenerProvider
import org.jooq.TransactionProvider
import org.jooq.VisitListenerProvider
import org.jooq.conf.Settings
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SpringJooqConfigurationAdapter : JooqConfigInterceptor {
    @Autowired
    protected lateinit var transactionProvider: ObjectProvider<TransactionProvider>

    @Autowired
    protected lateinit var recordMapperProvider: ObjectProvider<RecordMapperProvider>

    @Autowired
    protected lateinit var recordUnmapperProvider: ObjectProvider<RecordUnmapperProvider>

    @Autowired
    protected lateinit var settings: ObjectProvider<Settings>

    @Autowired
    protected lateinit var recordListenerProviders: ObjectProvider<RecordListenerProvider>

    @Autowired
    protected lateinit var executeListenerProviders: ObjectProvider<ExecuteListenerProvider>

    @Autowired
    protected lateinit var visitListenerProviders: ObjectProvider<VisitListenerProvider>

    @Autowired
    protected lateinit var transactionListenerProviders: ObjectProvider<TransactionListenerProvider>

    @Autowired
    protected lateinit var executorProvider: ObjectProvider<ExecutorProvider>

    override val name: String? = null

    override val qualifier: Class<*>? = null

    override fun intercept(configuration: Configuration): Configuration {
        transactionProvider.ifAvailable { configuration.set(it) }
        recordMapperProvider.ifAvailable { configuration.set(it) }
        recordUnmapperProvider.ifAvailable { configuration.set(it) }
        settings.ifAvailable { configuration.set(it) }
        executorProvider.ifAvailable { configuration.set(it) }

        configuration.set(*recordListenerProviders.orderedStream().toList().toTypedArray())
        configuration.set(*executeListenerProviders.orderedStream().toList().toTypedArray())
        configuration.set(*visitListenerProviders.orderedStream().toList().toTypedArray())
        configuration.set(*transactionListenerProviders.orderedStream().toList().toTypedArray())

        return configuration
    }
}
