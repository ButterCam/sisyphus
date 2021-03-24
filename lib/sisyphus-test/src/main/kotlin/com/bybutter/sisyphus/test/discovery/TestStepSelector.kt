package com.bybutter.sisyphus.test.discovery

import com.bybutter.sisyphus.test.TestStep
import org.junit.platform.engine.DiscoverySelector

data class TestStepSelector(val id: String, val step: TestStep) : DiscoverySelector
