package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestDescriptor

interface AfterTest : Extension {
    fun afterTest(context: SisyphusTestEngineContext, descriptor: SisyphusTestDescriptor)
}
