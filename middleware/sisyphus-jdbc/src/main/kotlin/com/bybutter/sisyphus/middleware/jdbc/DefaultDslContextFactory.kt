package com.bybutter.sisyphus.middleware.jdbc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
@ConditionalOnMissingBean(value = [DslContextFactory::class])
class DefaultDslContextFactory(@Autowired private val configInterceptors: List<JooqConfigInterceptor>) : AbstractDslContextFactory(configInterceptors)
