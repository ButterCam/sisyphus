import org.gradle.kotlin.dsl.idea
import org.gradle.kotlin.dsl.`java-platform`
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    idea
    `java-platform`
    `maven-publish`
    id("com.bybutter.sisyphus.project")
}

group = "com.bybutter.sisyphus"
