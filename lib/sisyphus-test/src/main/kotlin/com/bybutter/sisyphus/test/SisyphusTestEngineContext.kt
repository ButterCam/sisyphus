package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.dsl.cel.CelEngine
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext

class SisyphusTestEngineContext : EngineExecutionContext {
    private val _channels: MutableMap<String, Channel> = mutableMapOf()

    private val _results: MutableMap<String, TestResult> = mutableMapOf()

    val celEngine = CelEngine()

    val channels: Map<String, Channel> get() = _channels

    val result: Map<String, TestResult> get() = _results

    fun channel(authority: String): Channel {
        return _channels.getOrPut(authority) {
            ManagedChannelBuilder.forTarget(authority).build()
        }
    }

    fun result(step: TestStep, result: TestResult) {
        _results[step.name] = result
    }
}