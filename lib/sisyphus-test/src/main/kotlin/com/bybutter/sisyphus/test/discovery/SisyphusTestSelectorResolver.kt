package com.bybutter.sisyphus.test.discovery

import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.jackson.Yaml
import com.bybutter.sisyphus.protobuf.invoke
import com.bybutter.sisyphus.string.toTitleCase
import com.bybutter.sisyphus.test.descriptor.SisyphusTestCaseDescriptor
import com.bybutter.sisyphus.test.SisyphusTestEngine
import com.bybutter.sisyphus.test.descriptor.SisyphusTestStepDescriptor
import com.bybutter.sisyphus.test.TestCase
import com.bybutter.sisyphus.test.TestStep
import org.junit.platform.commons.logging.LoggerFactory
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.discovery.ClasspathResourceSelector
import org.junit.platform.engine.discovery.DirectorySelector
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.engine.discovery.FileSelector
import org.junit.platform.engine.discovery.PackageSelector
import org.junit.platform.engine.support.discovery.SelectorResolver
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.util.FilterBuilder
import java.io.File
import java.io.InputStream
import java.util.Optional

class SisyphusTestSelectorResolver : SelectorResolver {
    override fun resolve(
        selector: ClasspathResourceSelector,
        context: SelectorResolver.Context
    ): SelectorResolver.Resolution {
        return SelectorResolver.Resolution.matches(
            SisyphusTestEngine::class.java.classLoader.getResources(selector.classpathResourceName).asSequence()
                .mapNotNull {
                    val case = deserializeTestCase(it.file, it.openStream()) ?: return@mapNotNull null
                    context.addToParent { parent ->
                        Optional.of(createTestCaseDescriptor(parent, case, it.file))
                    }.toMatch().orElse(null)
                }.toSet()
        )
    }

    override fun resolve(selector: FileSelector, context: SelectorResolver.Context): SelectorResolver.Resolution {
        val case = deserializeTestCase(selector.file.name, selector.file.inputStream())
            ?: return SelectorResolver.Resolution.unresolved()
        return context.addToParent { parent ->
            Optional.of(createTestCaseDescriptor(parent, case, selector.file.name))
        }.toResolution()
    }

    override fun resolve(selector: DirectorySelector, context: SelectorResolver.Context): SelectorResolver.Resolution {
        return SelectorResolver.Resolution.matches(
            selector.directory.walk().mapNotNull {
                if (!it.name.endsWith("_test.yaml", true) &&
                    !it.name.endsWith("_test.yml", true) &&
                    !it.name.endsWith("_test.json", true)
                ) {
                    return@mapNotNull null
                }
                val case = deserializeTestCase(it.name, it.inputStream()) ?: return@mapNotNull null
                context.addToParent { parent ->
                    Optional.of(createTestCaseDescriptor(parent, case, it.name))
                }.toMatch().orElse(null)
            }.toSet()
        )
    }

    override fun resolve(selector: PackageSelector, context: SelectorResolver.Context): SelectorResolver.Resolution {
        return SelectorResolver.Resolution.selectors(
            Reflections(selector.packageName, ResourcesScanner())
                .getResources(
                    FilterBuilder().include(".*_test.yaml")
                        .include(".*_test.yml")
                        .include(".*_test.json")
                ).map { DiscoverySelectors.selectClasspathResource(it) }.toSet()
        )
    }

    override fun resolve(selector: DiscoverySelector, context: SelectorResolver.Context): SelectorResolver.Resolution {
        return when (selector) {
            is TestStepSelector -> {
                context.addToParent { parent ->
                    Optional.of(createTestStepDescriptor(parent, selector.step))
                }.toResolution()
            }
            else -> super.resolve(selector, context)
        }
    }

    private fun Optional<TestDescriptor>.toMatch(): Optional<SelectorResolver.Match> {
        return map {
            SelectorResolver.Match.exact(it) {
                if (it is SisyphusTestCaseDescriptor) {
                    it.case.steps.map { TestStepSelector(it) }.toSet()
                } else {
                    setOf()
                }
            }
        }
    }

    private fun Optional<TestDescriptor>.toResolution(): SelectorResolver.Resolution {
        return toMatch().map {
            SelectorResolver.Resolution.match(it)
        }.orElse(SelectorResolver.Resolution.unresolved())
    }

    private fun deserializeTestCase(filename: String, inputStream: InputStream): TestCase? {
        try {
            return when (filename.substringAfterLast('.').toLowerCase()) {
                "json" -> Json.deserialize(inputStream, TestCase::class.java)
                "yaml", "yml" -> Yaml.deserialize(inputStream, TestCase::class.java)
                else -> null
            }
        } catch (e: Exception) {
            logger.warn(e) {
                "Fail to read test case file '$filename'"
            }
        } finally {
            inputStream.close()
        }
        return null
    }

    private fun createTestCaseDescriptor(parent: TestDescriptor, case: TestCase, name: String): TestDescriptor {
        val case = case {
            this.name = this.name.takeIf { it.isNotBlank() } ?: File(name).nameWithoutExtension.toTitleCase()
            val steps = this.steps.map {
                it {
                    Preconditions.notBlank(this.id, "Step id of case '${this@case.name}' must not be null")
                    this.name = this.name.takeIf { it.isNotBlank() } ?: this.id
                    this.authority = this.authority.takeIf { it.isNotBlank() } ?: "localhost:9090"
                }
            }
            this.steps.clear()
            this.steps += steps
        }
        return SisyphusTestCaseDescriptor(parent.uniqueId.append(SisyphusTestCaseDescriptor.SEGMENT_TYPE, name), case)
    }

    private fun createTestStepDescriptor(parent: TestDescriptor, step: TestStep): TestDescriptor {
        return SisyphusTestStepDescriptor(
            parent.uniqueId.append(SisyphusTestStepDescriptor.SEGMENT_TYPE, step.id),
            step
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SisyphusTestSelectorResolver::class.java)
    }
}