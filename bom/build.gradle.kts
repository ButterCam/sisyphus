plugins {
    `java-platform`
    id("nebula.maven-publish")
    sisyphus
}

group = "com.bybutter.sisyphus"

dependencies {
    constraints {
        rootProject.subprojects {
            this.plugins.withId("org.gradle.java-base") {
                api(this@subprojects)
            }
        }
    }
}