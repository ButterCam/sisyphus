package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestStepDescriptor

interface AfterTestStep : Extension {
    fun afterTestStep(context: SisyphusTestEngineContext, descriptor: SisyphusTestStepDescriptor)
}
