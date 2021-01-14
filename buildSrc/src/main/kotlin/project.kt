import com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin
import com.github.benmanes.gradle.versions.VersionsPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin

val Project.java: Project
    get() {
        pluginManager.apply(JavaLibraryPlugin::class.java)
        pluginManager.apply(SisyphusProjectPlugin::class.java)

        managedDependencies

        tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }
        return this
    }

val Project.next: Project
    get() {
        pluginManager.apply(IdeaPlugin::class.java)
        pluginManager.apply(VersionsPlugin::class.java)

        java.kotlin.managedDependencies

        dependencies {
            add("testImplementation", Dependencies.junit)
        }

        tasks.withType<Test> {
            useJUnitPlatform()
        }

        tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }
        return this
    }

val Project.middleware: Project
    get() {
        next

        group = "com.bybutter.sisyphus.middleware"

        dependencies {
            add("api", Dependencies.Spring.Boot.boot)
            add("testImplementation", Dependencies.Spring.Boot.test)
        }
        return this
    }

val Project.lib: Project
    get() {
        next

        group = "com.bybutter.sisyphus"
        return this
    }

val Project.proto: Project
    get() {
        java

        group = "com.bybutter.sisyphus.proto"

        pluginManager.apply(JavaLibraryPlugin::class.java)
        pluginManager.apply(SisyphusProjectPlugin::class.java)

        return this
    }

val Project.starter: Project
    get() {
        next

        group = "com.bybutter.sisyphus.starter"

        dependencies {
            add("api", Dependencies.Spring.Boot.boot)
            add("testImplementation", Dependencies.Spring.Boot.test)
        }
        return this
    }

val Project.tools: Project
    get() {
        next

        group = "com.bybutter.sisyphus.tools"
        return this
    }