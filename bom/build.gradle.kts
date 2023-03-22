plugins {
    `sisyphus-bom`
}

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