package com.bybutter.sisyphus.test.descriptor

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.SisyphusTestRootContext
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.extension.AfterTestCase
import com.bybutter.sisyphus.test.extension.BeforeTestCase
import com.bybutter.sisyphus.test.extensions
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node
import org.opentest4j.AssertionFailedError

class SisyphusTestCaseDescriptor(id: UniqueId, val case: TestCase) :
    EngineDescriptor(id, case.name),
    Node<SisyphusTestEngineContext> {

    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        return if (case.steps.isEmpty() && case.asserts.isEmpty()) Node.SkipResult.skip("No test steps found.") else Node.SkipResult.doNotSkip()
    }

    override fun prepare(context: SisyphusTestEngineContext): SisyphusTestEngineContext {
        return (context as SisyphusTestRootContext).forCase()
    }

    override fun before(context: SisyphusTestEngineContext): SisyphusTestEngineContext {
        context.extensions<BeforeTestCase> { beforeTestCase(context, this@SisyphusTestCaseDescriptor) }
        return context
    }

    override fun around(context: SisyphusTestEngineContext, invocation: Node.Invocation<SisyphusTestEngineContext>) {
        invocation.invoke(context)
        val engine = context.cel().fork(context.results())
        for (assert in case.asserts) {
            val result = engine.eval(assert)
            if (result != true) {
                throw AssertionFailedError(
                    "Assertion '$assert' failed in test '$displayName'.",
                    true,
                    result
                )
            }
        }
    }

    override fun after(context: SisyphusTestEngineContext) {
        context.extensions<AfterTestCase> { afterTestCase(context, this@SisyphusTestCaseDescriptor) }
    }

    override fun getType(): TestDescriptor.Type {
        return TestDescriptor.Type.CONTAINER_AND_TEST
    }

    companion object {
        const val SEGMENT_TYPE = "cases"
    }
}
