plugins {
    libs.plugins.sisyphus.project
    libs.plugins.nebula.maven
    `java-platform`
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