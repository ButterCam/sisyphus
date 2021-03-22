package com.bybutter.sisyphus.test

import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node

class SisyphusTestCasesDescriptor(id: UniqueId) : EngineDescriptor(id, "Sisyphus Test"), Node<SisyphusTestEngineContext> {
    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        return if(children.isEmpty()) Node.SkipResult.skip("No test cases found.") else Node.SkipResult.doNotSkip()
    }
}

