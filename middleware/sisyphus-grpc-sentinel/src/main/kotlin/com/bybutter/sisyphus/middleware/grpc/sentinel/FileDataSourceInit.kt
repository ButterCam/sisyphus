package com.bybutter.sisyphus.middleware.grpc.sentinel

import com.alibaba.csp.sentinel.command.handler.ModifyParamFlowRulesCommandHandler
import com.alibaba.csp.sentinel.datasource.Converter
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource
import com.alibaba.csp.sentinel.datasource.FileWritableDataSource
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
import com.alibaba.fastjson.JSON
import com.bybutter.sisyphus.jackson.parseJson
import java.io.File
import java.io.IOException

class FileDataSourceInit(private val sentinelProperties: SentinelProperties) : InitFunc {

    override fun init() {
        val ruleDir = "~${File.separator}${sentinelProperties.projectName}${File.separator}sentinel${File.separator}rules"
        val flowRulePath = "$ruleDir${File.separator}flow-rule.json"
        val degradeRulePath = "$ruleDir${File.separator}degrade-rule.json"
        val systemRulePath = "$ruleDir${File.separator}system-rule.json"
        val authorityRulePath = "$ruleDir${File.separator}authority-rule.json"
        val hotParamFlowRulePath = "$ruleDir${File.separator}param-flow-rule.json"
        mkdirIfNotExits(ruleDir)
        createFileIfNotExits(flowRulePath)
        createFileIfNotExits(degradeRulePath)
        createFileIfNotExits(systemRulePath)
        createFileIfNotExits(authorityRulePath)
        createFileIfNotExits(hotParamFlowRulePath)
        // flowRule
        val flowRuleRDS: ReadableDataSource<String, List<FlowRule>> = FileRefreshableDataSource(
                flowRulePath,
                flowRuleListParser
        )
        FlowRuleManager.register2Property(flowRuleRDS.property)
        val flowRuleWDS: WritableDataSource<List<FlowRule>> = FileWritableDataSource(
                flowRulePath,
                ::encodeJson
        )
        WritableDataSourceRegistry.registerFlowDataSource(flowRuleWDS)

        // degradeRule
        val degradeRuleRDS: ReadableDataSource<String, List<DegradeRule>> = FileRefreshableDataSource(
                degradeRulePath,
                degradeRuleListParser
        )
        DegradeRuleManager.register2Property(degradeRuleRDS.property)
        val degradeRuleWDS: WritableDataSource<List<DegradeRule>> = FileWritableDataSource(
                degradeRulePath,
                ::encodeJson
        )
        WritableDataSourceRegistry.registerDegradeDataSource(degradeRuleWDS)

        // systemRule
        val systemRuleRDS: ReadableDataSource<String, List<SystemRule>> = FileRefreshableDataSource(
                systemRulePath,
                systemRuleListParser
        )
        SystemRuleManager.register2Property(systemRuleRDS.property)
        val systemRuleWDS: WritableDataSource<List<SystemRule>> = FileWritableDataSource(
                systemRulePath,
                ::encodeJson
        )
        WritableDataSourceRegistry.registerSystemDataSource(systemRuleWDS)

        // authorityRule
        val authorityRuleRDS: ReadableDataSource<String, List<AuthorityRule>> = FileRefreshableDataSource(
                flowRulePath,
                authorityRuleListParser
        )
        AuthorityRuleManager.register2Property(authorityRuleRDS.property)
        val authorityRuleWDS: WritableDataSource<List<AuthorityRule>> = FileWritableDataSource(
                authorityRulePath,
                ::encodeJson
        )
        WritableDataSourceRegistry.registerAuthorityDataSource(authorityRuleWDS)

        // hotParamFlowRule
        val hotParamFlowRuleRDS: ReadableDataSource<String, List<ParamFlowRule>> = FileRefreshableDataSource(
                hotParamFlowRulePath,
                hotParamFlowRuleListParser
        )
        ParamFlowRuleManager.register2Property(hotParamFlowRuleRDS.property)
        val paramFlowRuleWDS: WritableDataSource<List<ParamFlowRule>> = FileWritableDataSource(
                hotParamFlowRulePath,
                ::encodeJson
        )
        ModifyParamFlowRulesCommandHandler.setWritableDataSource(paramFlowRuleWDS)
    }

    private val flowRuleListParser: Converter<String, List<FlowRule>> = Converter { source: String -> source.parseJson<List<FlowRule>>() }

    private val degradeRuleListParser: Converter<String, List<DegradeRule>> = Converter { source: String -> source.parseJson<List<DegradeRule>>() }

    private val systemRuleListParser: Converter<String, List<SystemRule>> = Converter { source: String -> source.parseJson<List<SystemRule>>() }

    private val authorityRuleListParser: Converter<String, List<AuthorityRule>> = Converter { source: String -> source.parseJson<List<AuthorityRule>>() }

    private val hotParamFlowRuleListParser: Converter<String, List<ParamFlowRule>> = Converter { source: String -> source.parseJson<List<ParamFlowRule>>() }

    private fun mkdirIfNotExits(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    @Throws(IOException::class)
    private fun createFileIfNotExits(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    private fun <T> encodeJson(t: T): String? {
        return JSON.toJSONString(t)
    }
}
