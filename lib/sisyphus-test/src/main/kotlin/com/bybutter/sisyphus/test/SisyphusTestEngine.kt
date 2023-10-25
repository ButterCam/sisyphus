package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.spi.ServiceLoader
import com.bybutter.sisyphus.test.descriptor.SisyphusTestDescriptor
import com.bybutter.sisyphus.test.discovery.SisyphusTestSelectorResolver
import com.bybutter.sisyphus.test.extension.Extension
import com.bybutter.sisyphus.test.extension.SelectorResolver
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import java.util.Optional

class SisyphusTestEngine : HierarchicalTestEngine<SisyphusTestEngineContext>() {
    override fun getId(): String {
        return "sisyphus-junit"
    }

    override fun getGroupId(): Optional<String> {
        return Optional.of("com.bybutter.sisyphus")
    }

    override fun getArtifactId(): Optional<String> {
        return Optional.of("sisyphus-test")
    }

    private val extensions = ServiceLoader.load(Extension::class.java)

    override fun discover(
        discoveryRequest: EngineDiscoveryRequest,
        uniqueId: UniqueId,
    ): TestDescriptor {
        val resolver =
            EngineDiscoveryRequestResolver.builder<SisyphusTestDescriptor>()
                .addSelectorResolver(SisyphusTestSelectorResolver())
                .apply {
                    extensions.filterIsInstance<SelectorResolver>().forEach {
                        addSelectorResolver(it)
                    }
                }
                .build()
        val defaultRequest =
            LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectPackage("META-INF.sisyphus"))
                .build()

        return SisyphusTestDescriptor(uniqueId).apply {
            resolver.resolve(defaultRequest, this)
            resolver.resolve(discoveryRequest, this)
        }
    }

    override fun createExecutionContext(request: ExecutionRequest): SisyphusTestEngineContext {
        return SisyphusTestRootContext(extensions)
    }
}
