plugins {
    `sisyphus-middleware`
}

description = "Middleware for using Retrofit in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.kotlin.coroutines.jdk)
    api(libs.retrofit)
    api(libs.okhttp)
    api(libs.reflections)
    api(libs.resilience4j.retrofit)
    api(libs.resilience4j.circuitbreaker)
    api(projects.lib.sisyphusCommon)
    api(projects.lib.sisyphusDto)
    api(projects.lib.sisyphusJackson)
}
