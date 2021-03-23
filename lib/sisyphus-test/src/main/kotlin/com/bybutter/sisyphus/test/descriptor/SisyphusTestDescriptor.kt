package com.bybutter.sisyphus.test.descriptor

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node

class SisyphusTestDescriptor(id: UniqueId) : EngineDescriptor(id, "Sisyphus Test"), Node<SisyphusTestEngineContext> {
    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        return if(children.isEmpty()) Node.SkipResult.skip("No test cases found.") else Node.SkipResult.doNotSkip()
    }
}
