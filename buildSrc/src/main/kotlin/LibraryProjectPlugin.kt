import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class LibraryProjectPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply(KotlinProjectPlugin::class.java)

        target.group = "com.bybutter.sisyphus"

        target.tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
        }

        target.tasks.withType<JavaCompile> {
            sourceCompatibility = JavaVersion.VERSION_1_8.majorVersion
            targetCompatibility = JavaVersion.VERSION_1_8.majorVersion
        }
    }
}