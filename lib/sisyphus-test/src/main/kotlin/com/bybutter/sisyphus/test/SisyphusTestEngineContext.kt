package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.dsl.cel.CelEngine
import com.bybutter.sisyphus.test.extension.Extension
import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext

interface SisyphusTestEngineContext : EngineExecutionContext {
    fun cel(): CelEngine

    fun channel(authority: String): Channel

    fun results(): Map<String, TestResult>

    fun extensions(): List<Extension>
}

inline fun <reified T : Extension> SisyphusTestEngineContext.extensions(block: T.() -> Unit) {
    extensions().forEach {
        if (it !is T) return@forEach
        it.block()
    }
}

class SisyphusTestRootContext(extensions: List<Extension>) : SisyphusTestEngineContext {
    private val celEngine: CelEngine = CelEngine()
    private val channels: MutableMap<String, Channel> = mutableMapOf()
    private val extensions: MutableList<Extension> = extensions.toMutableList()

    override fun cel(): CelEngine {
        return celEngine
    }

    override fun channel(authority: String): Channel {
        return channels.getOrPut(authority) {
            ManagedChannelBuilder.forTarget(authority).usePlaintext().build()
        }
    }

    override fun results(): Map<String, TestResult> {
        return mapOf()
    }

    override fun extensions(): List<Extension> {
        return extensions
    }

    fun forCase(): SisyphusTestCaseContext {
        return SisyphusTestCaseContext(this)
    }
}

class SisyphusTestCaseContext(val parent: SisyphusTestRootContext) : SisyphusTestEngineContext {
    private val results: MutableMap<String, TestResult> = mutableMapOf()

    override fun cel(): CelEngine {
        return parent.cel()
    }

    override fun channel(authority: String): Channel {
        return parent.channel(authority)
    }

    override fun results(): Map<String, TestResult> {
        return results
    }

    override fun extensions(): List<Extension> {
        return parent.extensions()
    }

    fun record(result: TestResult) {
        val id = result.step?.id?.takeIf { it.isNotEmpty() } ?: return
        results[id] = result
    }

    fun forStep(callContext: CallContext): SisyphusTestStepContext {
        return SisyphusTestStepContext(this, callContext)
    }
}

class SisyphusTestStepContext(
    val parent: SisyphusTestCaseContext,
    val callContext: CallContext,
) : SisyphusTestEngineContext {
    override fun cel(): CelEngine {
        return parent.cel()
    }

    override fun channel(authority: String): Channel {
        return parent.channel(authority)
    }

    override fun results(): Map<String, TestResult> {
        return parent.results()
    }

    override fun extensions(): List<Extension> {
        return parent.extensions()
    }

    fun record(result: TestResult) {
        return parent.record(result)
    }
}
