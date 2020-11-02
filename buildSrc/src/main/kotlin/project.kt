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

val Project.next: Project
    get() {
        pluginManager.apply(JavaLibraryPlugin::class.java)
        pluginManager.apply(IdeaPlugin::class.java)
        pluginManager.apply(VersionsPlugin::class.java)
        pluginManager.apply(SisyphusProjectPlugin::class.java)

        kotlin.managedDependencies

        tasks.withType(JavaCompile::class.java) {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }

        dependencies {
            add("testImplementation", Dependencies.junit)
        }

        tasks.withType<Test> {
            useJUnitPlatform()
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
        group = "com.bybutter.sisyphus.proto"

        managedDependencies

        tasks.withType(JavaCompile::class.java) {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }

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