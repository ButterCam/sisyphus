package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler
import com.alibaba.csp.sentinel.datasource.Converter
import com.alibaba.csp.sentinel.datasource.ReadableDataSource
import com.alibaba.csp.sentinel.datasource.WritableDataSource
import com.alibaba.csp.sentinel.datasource.redis.RedisDataSource
import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig
import com.alibaba.csp.sentinel.init.InitFunc
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager
import com.alibaba.csp.sentinel.slots.system.SystemRule
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager
import com.alibaba.csp.sentinel.transport.util.WritableDataSourceRegistry
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import javax.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RedisDataSourceInit : InitFunc {

    @Autowired
    private lateinit var sentinelProperties: SentinelProperties

    @PostConstruct
    override fun init() {
        val rule = "${sentinelProperties.projectName}:sentinel:rules"
        val flowRule = "$rule:flow-rule"
        val degradeRule = "$rule:degrade-rule"
        val systemRule = "$rule:system-rule"
        val authorityRule = "$rule:authority-rule"
        val hotParamFlowRule = "$rule:param-flow-rule"

        // 流控规则
        val parserFlowRule = Converter { source: String -> JSON.parseObject(source, object : TypeReference<List<FlowRule>>() {}) }
        val config: RedisConnectionConfig = RedisConnectionConfig.builder()
                .withHost(sentinelProperties.redisHost)
                .withPort(sentinelProperties.redisPort)
                .build()
        val redisDataSource: ReadableDataSource<String, List<FlowRule>> = RedisDataSource(config, flowRule, flowRule, parserFlowRule)
        FlowRuleManager.register2Property(redisDataSource.property)

        val flowRuleWDS: WritableDataSource<List<FlowRule>> = RedisWritableDataSource(sentinelProperties.redisHost, sentinelProperties.redisPort, flowRule)
        WritableDataSourceRegistry.registerFlowDataSource(flowRuleWDS)

        // 降级规则
        val parserDegradeRule = Converter { source: String -> JSON.parseObject(source, object : TypeReference<List<DegradeRule>>() {}) }
        val degradeRuleRDS: ReadableDataSource<String, List<DegradeRule>> = RedisDataSource(config, degradeRule, degradeRule, parserDegradeRule)
        DegradeRuleManager.register2Property(degradeRuleRDS.property)
        val degradeRuleWDS: WritableDataSource<List<DegradeRule>> = RedisWritableDataSource(sentinelProperties.redisHost, sentinelProperties.redisPort, degradeRule)
        WritableDataSourceRegistry.registerDegradeDataSource(degradeRuleWDS)

        // 系统规则
        val parserSystemRule = Converter { source: String -> JSON.parseObject(source, object : TypeReference<List<SystemRule>>() {}) }
        val systemRuleRDS: ReadableDataSource<String, List<SystemRule>> = RedisDataSource(config, systemRule, systemRule, parserSystemRule)
        SystemRuleManager.register2Property(systemRuleRDS.property)
        val systemRuleWDS: WritableDataSource<List<SystemRule>> = RedisWritableDataSource(sentinelProperties.redisHost, sentinelProperties.redisPort, systemRule)
        WritableDataSourceRegistry.registerSystemDataSource(systemRuleWDS)

        // 授权规则
        val parserAuthorityRule = Converter { source: String -> JSON.parseObject(source, object : TypeReference<List<AuthorityRule>>() {}) }
        val authorityRuleRDS: ReadableDataSource<String, List<AuthorityRule>> = RedisDataSource(config, authorityRule, authorityRule, parserAuthorityRule)
        AuthorityRuleManager.register2Property(authorityRuleRDS.property)
        val authorityRuleWDS: WritableDataSource<List<AuthorityRule>> = RedisWritableDataSource(sentinelProperties.redisHost, sentinelProperties.redisPort, authorityRule)
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityRuleWDS)

        // 热点参数规则
        val parserParamFlowRule = Converter { source: String -> JSON.parseObject(source, object : TypeReference<List<ParamFlowRule>>() {}) }
        val hotParamFlowRuleRDS: ReadableDataSource<String, List<ParamFlowRule>> = RedisDataSource(config, hotParamFlowRule, hotParamFlowRule, parserParamFlowRule)
        ParamFlowRuleManager.register2Property(hotParamFlowRuleRDS.property)
        val paramFlowRuleWDS: WritableDataSource<List<ParamFlowRule>> = RedisWritableDataSource(sentinelProperties.redisHost, sentinelProperties.redisPort, hotParamFlowRule)
        ModifyParamFlowRulesCommandHandler.setWritableDataSource(paramFlowRuleWDS)
    }
}
