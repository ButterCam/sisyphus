import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val Project.kotlin: Project
    get() {
        apply {
            plugin("kotlin")
            plugin("kotlin-spring")
            plugin("org.jlleitschuh.gradle.ktlint")
        }

        dependencies {
            add("api", Dependencies.Kotlin.stdlib)
            add("api", Dependencies.Kotlin.reflect)
            add("api", Dependencies.Kotlin.Coroutines.core)
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
        }

        return this
    }