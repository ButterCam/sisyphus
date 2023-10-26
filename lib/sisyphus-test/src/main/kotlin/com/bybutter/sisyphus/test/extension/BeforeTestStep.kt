package com.bybutter.sisyphus.test.extension

import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.descriptor.SisyphusTestStepDescriptor

interface BeforeTestStep : Extension {
    fun beforeTestStep(
        context: SisyphusTestEngineContext,
        descriptor: SisyphusTestStepDescriptor,
    )
}
