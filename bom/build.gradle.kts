plugins {
    `java-platform`
    alias(libs.plugins.nebula.maven)
    alias(libs.plugins.sisyphus.project)
}

group = "com.bybutter.sisyphus"
description = "Sisyphus Project (Bill of Materials)"

dependencies {
    constraints {
        rootProject.subprojects {
            this.plugins.withId("org.gradle.java-base") {
                api(this@subprojects)
            }
        }
    }
}