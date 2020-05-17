inline val org.gradle.plugin.use.PluginDependenciesSpec.protobuf: org.gradle.plugin.use.PluginDependencySpec
    get() = id("com.bybutter.sisyphus.protobuf")

inline val org.gradle.plugin.use.PluginDependenciesSpec.sisyphus: org.gradle.plugin.use.PluginDependencySpec
    get() = id("com.bybutter.sisyphus.project")

inline val org.gradle.plugin.use.PluginDependenciesSpec.publish: org.gradle.plugin.use.PluginDependencySpec
    get() {
        id("nebula.maven-base-publish")
        return id("nebula.source-jar")
    }
