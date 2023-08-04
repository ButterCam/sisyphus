import com.bybutter.sisyphus.project.gradle.SisyphusProjectPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.ide.idea.IdeaPlugin

class JavaProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(IdeaPlugin::class.java)
        target.plugins.apply(JavaLibraryPlugin::class.java)
        target.plugins.apply(SisyphusProjectPlugin::class.java)

        target.tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_17.majorVersion
            targetCompatibility = JavaVersion.VERSION_17.majorVersion
        }

        target.dependencies {
            add("api", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        }

        target.tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
}
