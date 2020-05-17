plugins {
    `java-platform`
    id("nebula.maven-publish")
    id("sisyphus.project")
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