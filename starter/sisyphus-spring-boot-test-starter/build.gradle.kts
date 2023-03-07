starter

plugins {
    `java-library`
}

description = "Starter for configure spring boot environment in Sisyphus Test Framework"

dependencies {
    api(libs.spring.boot)
    api(projects.lib.sisyphusTest)
}
