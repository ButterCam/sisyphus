
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.idea
import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.withType

plugins {
    idea
    `java-library`
    id("com.bybutter.sisyphus.project")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.majorVersion
    targetCompatibility = JavaVersion.VERSION_17.majorVersion
}

tasks.withType<Test> {
    useJUnitPlatform()
}
