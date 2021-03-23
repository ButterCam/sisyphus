package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestDescriptor

interface BeforeTest : Extension {
    fun beforeTest(context: SisyphusTestEngineContext, descriptor: SisyphusTestDescriptor)
}
