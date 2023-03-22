import org.gradle.api.Plugin
import org.gradle.api.Project

class GradlePluginProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("com.gradle.plugin-publish")
        target.plugins.apply("org.gradle.java-gradle-plugin")
        target.plugins.apply(ToolProjectPlugin::class.java)
    }
}