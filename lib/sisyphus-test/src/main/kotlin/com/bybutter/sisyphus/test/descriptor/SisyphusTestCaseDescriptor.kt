package com.bybutter.sisyphus.test.descriptor

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.TestResult
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node

class SisyphusTestCaseDescriptor(id: UniqueId, val case: TestCase) : EngineDescriptor(id, case.name),
    Node<SisyphusTestEngineContext> {

    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        return if (case.steps.isEmpty() && case.asserts.isEmpty()) Node.SkipResult.skip("No test steps found.") else Node.SkipResult.doNotSkip()
    }

    override fun after(context: SisyphusTestEngineContext) {

    }

    override fun getType(): TestDescriptor.Type {
        return TestDescriptor.Type.CONTAINER_AND_TEST
    }

    companion object {
        const val SEGMENT_TYPE = "cases"
    }
}