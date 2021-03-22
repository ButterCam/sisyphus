package com.bybutter.sisyphus.test

import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.jackson.Yaml
import com.bybutter.sisyphus.protobuf.invoke
import com.bybutter.sisyphus.string.toTitleCase
import org.junit.platform.commons.logging.LoggerFactory
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClasspathResourceSelector
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.FilterBuilder
import java.io.File
import java.io.InputStream

class SisyphusTestEngine : HierarchicalTestEngine<SisyphusTestEngineContext>() {
    override fun getId(): String {
        return "sisyphus-junit"
    }

    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        val classpathResourceSelectors = discoveryRequest.getSelectorsByType(ClasspathResourceSelector::class.java)
        val classpathRootSelectors = discoveryRequest.getSelectorsByType(ClasspathRootSelector::class.java)
        val fileSelectors = discoveryRequest.getSelectorsByType(FileSelector::class.java)
        val descriptor = SisyphusTestCasesDescriptor(uniqueId)

        defaultSelector(uniqueId, descriptor)
        for (selector in classpathResourceSelectors) {
            resolveSelector(uniqueId, descriptor, selector)
        }
        for (selector in classpathRootSelectors) {
            resolveSelector(uniqueId, descriptor, selector)
        }
        for (selector in fileSelectors) {
            resolveSelector(uniqueId, descriptor, selector)
        }

        return descriptor
    }

    override fun createExecutionContext(request: ExecutionRequest): SisyphusTestEngineContext {
        return SisyphusTestEngineContext(request.engineExecutionListener)
    }

    private fun defaultSelector(uniqueId: UniqueId, root: SisyphusTestCasesDescriptor) {
        val resources = Reflections("META-INF.sisyphus", ResourcesScanner())
            .getResources(
                FilterBuilder().include(".*_test.yaml")
                    .include(".*_test.yml")
                    .include(".*_test.json")
            )

        for (resource in resources) {
            resolveSelector(uniqueId, root, DiscoverySelectors.selectClasspathResource(resource))
        }
    }

    private fun resolveSelector(
        uniqueId: UniqueId,
        root: SisyphusTestCasesDescriptor,
        selector: ClasspathResourceSelector
    ) {
        for (resource in SisyphusTestEngine::class.java.classLoader.getResources(selector.classpathResourceName)) {
            createTestCaseDescriptor(uniqueId, resource.openStream(), resource.file)?.let {
                root.addChild(it)
            }
        }
    }

    private fun resolveSelector(
        uniqueId: UniqueId,
        root: SisyphusTestCasesDescriptor,
        selector: ClasspathRootSelector
    ) {
    }

    private fun resolveSelector(uniqueId: UniqueId, root: SisyphusTestCasesDescriptor, selector: FileSelector) {
        if (selector.file.isDirectory) {
            for (file in selector.file.walk()) {
                if (file.name.endsWith("_test.yaml", true) ||
                    file.name.endsWith("_test.yml", true) ||
                    file.name.endsWith("_test.json", true)
                ) {
                    createTestCaseDescriptor(uniqueId, file.inputStream(), file.name)?.let {
                        root.addChild(it)
                    }
                }
            }
        } else {
            createTestCaseDescriptor(uniqueId, selector.file.inputStream(), selector.file.name)?.let {
                root.addChild(it)
            }
        }
    }

    private fun createTestCaseDescriptor(uniqueId: UniqueId, inputStream: InputStream, name: String): TestDescriptor? {
        val case = when (name.substringAfterLast('.')) {
            "yml", "yaml" -> {
                Yaml.deserialize(inputStream, TestCase::class.java)
            }
            "json" -> {
                Json.deserialize(inputStream, TestCase::class.java)
            }
            else -> {
                logger.info {
                    "Unsupported test case file."
                }
                return null
            }
        }
        return SisyphusTestCaseDescriptor(uniqueId, case {
            this.name = this.name.takeIf { it.isNotBlank() } ?: File(name).nameWithoutExtension.toTitleCase()
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SisyphusTestEngine::class.java)
    }
}
