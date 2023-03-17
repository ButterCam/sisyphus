import com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val Project.java: Project
    get() {
        pluginManager.apply(JavaLibraryPlugin::class.java)
        pluginManager.apply(SisyphusProjectPlugin::class.java)

        tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }
        return this
    }

val Project.kotlin: Project
    get() {
        apply {
            plugin("kotlin")
            plugin("kotlin-spring")
            plugin("org.jlleitschuh.gradle.ktlint")
        }

        dependencies {
            add("api", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
            add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }

        return this
    }

val Project.next: Project
    get() {
        pluginManager.apply(IdeaPlugin::class.java)

        java.kotlin

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        return this
    }

val Project.middleware: Project
    get() {
        next

        group = "com.bybutter.sisyphus.middleware"
        return this
    }

val Project.lib: Project
    get() {
        next

        group = "com.bybutter.sisyphus"
        return this
    }

val Project.starter: Project
    get() {
        next

        group = "com.bybutter.sisyphus.starter"
        return this
    }

val Project.tools: Project
    get() {
        next

        group = "com.bybutter.sisyphus.tools"
        return this
    }

val Project.plugin: Project
    get() {
        apply {
            plugin("com.gradle.plugin-publish")
        }

        next

        group = "com.bybutter.sisyphus.tools"
        return this
    }