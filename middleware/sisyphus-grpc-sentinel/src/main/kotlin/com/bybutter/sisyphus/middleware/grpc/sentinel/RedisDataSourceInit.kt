package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler
import com.alibaba.csp.sentinel.datasource.Converter
import com.alibaba.csp.sentinel.datasource.ReadableDataSource
import com.alibaba.csp.sentinel.datasource.WritableDataSource
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
import com.bybutter.sisyphus.jackson.parseJson
import io.lettuce.core.RedisClient

class RedisDataSourceInit(private val redisClient: RedisClient, private val sentinelProperties: SentinelProperties) : InitFunc {

    override fun init() {
        val rule = "${sentinelProperties.projectName}:sentinel:rules"
        val flowRule = "$rule:flow-rule"
        val degradeRule = "$rule:degrade-rule"
        val systemRule = "$rule:system-rule"
        val authorityRule = "$rule:authority-rule"
        val hotParamFlowRule = "$rule:param-flow-rule"

        // 流控规则
        val parserFlowRule = Converter { source: String -> source.parseJson<List<FlowRule>>()}

        val redisDataSource: ReadableDataSource<String, List<FlowRule>> = SisyphusRedisDataSource(redisClient, flowRule, flowRule, parserFlowRule)
        FlowRuleManager.register2Property(redisDataSource.property)

        val flowRuleWDS: WritableDataSource<List<FlowRule>> = SisyphusRedisDataSource(redisClient, flowRule, parserFlowRule)
        WritableDataSourceRegistry.registerFlowDataSource(flowRuleWDS)

        // 降级规则
        val parserDegradeRule =  Converter { source: String -> source.parseJson<List<DegradeRule>>()}
        val degradeRuleRDS: ReadableDataSource<String, List<DegradeRule>> = SisyphusRedisDataSource(redisClient, degradeRule, degradeRule, parserDegradeRule)
        DegradeRuleManager.register2Property(degradeRuleRDS.property)
        val degradeRuleWDS: WritableDataSource<List<DegradeRule>> = SisyphusRedisDataSource(redisClient, degradeRule, parserDegradeRule)
        WritableDataSourceRegistry.registerDegradeDataSource(degradeRuleWDS)

        // 系统规则
        val parserSystemRule = Converter { source: String -> source.parseJson<List<SystemRule>>()}
        val systemRuleRDS: ReadableDataSource<String, List<SystemRule>> = SisyphusRedisDataSource(redisClient, systemRule, systemRule, parserSystemRule)
        SystemRuleManager.register2Property(systemRuleRDS.property)
        val systemRuleWDS: WritableDataSource<List<SystemRule>> = SisyphusRedisDataSource(redisClient, systemRule, parserSystemRule)
        WritableDataSourceRegistry.registerSystemDataSource(systemRuleWDS)

        // 授权规则
        val parserAuthorityRule = Converter { source: String -> source.parseJson<List<AuthorityRule>>()}
        val authorityRuleRDS: ReadableDataSource<String, List<AuthorityRule>> = SisyphusRedisDataSource(redisClient, authorityRule, authorityRule, parserAuthorityRule)
        AuthorityRuleManager.register2Property(authorityRuleRDS.property)
        val authorityRuleWDS: WritableDataSource<List<AuthorityRule>> = SisyphusRedisDataSource(redisClient, authorityRule, parserAuthorityRule)
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityRuleWDS)

        // 热点参数规则
        val parserParamFlowRule = Converter { source: String -> source.parseJson<List<ParamFlowRule>>()}
        val hotParamFlowRuleRDS: ReadableDataSource<String, List<ParamFlowRule>> = SisyphusRedisDataSource(redisClient, hotParamFlowRule, hotParamFlowRule, parserParamFlowRule)
        ParamFlowRuleManager.register2Property(hotParamFlowRuleRDS.property)
        val paramFlowRuleWDS: WritableDataSource<List<ParamFlowRule>> = SisyphusRedisDataSource(redisClient, hotParamFlowRule, parserParamFlowRule)
        ModifyParamFlowRulesCommandHandler.setWritableDataSource(paramFlowRuleWDS)
    }
}
