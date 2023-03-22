plugins {
    `sisyphus-starter`
}

description = "Starter for configure spring boot environment in Sisyphus Test Framework"

dependencies {
    api(libs.spring.boot)
    api(projects.lib.sisyphusTest)
}
