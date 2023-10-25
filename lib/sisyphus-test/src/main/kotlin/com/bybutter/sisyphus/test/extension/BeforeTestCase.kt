package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestCaseDescriptor

interface BeforeTestCase : Extension {
    fun beforeTestCase(
        context: SisyphusTestEngineContext,
        descriptor: SisyphusTestCaseDescriptor,
    )
}
