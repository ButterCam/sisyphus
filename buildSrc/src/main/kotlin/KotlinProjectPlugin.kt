import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.allopen.gradle.SpringGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.KtlintPlugin

class KotlinProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(JavaProjectPlugin::class.java)
        target.plugins.apply(KtlintPlugin::class.java)
        target.plugins.apply("kotlin")
        target.plugins.apply("kotlin-spring")

        target.dependencies {
            add("api", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        }

        target.tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "17"
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

