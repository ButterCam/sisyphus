package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestCaseDescriptor

interface AfterTestCase : Extension {
    fun afterTestCase(context: SisyphusTestEngineContext, descriptor: SisyphusTestCaseDescriptor)
}
