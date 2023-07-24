plugins {
    `sisyphus-middleware`
}

description = "Middleware for using Kafka in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.kafka)

    implementation(libs.kotlin.coroutines)
    implementation(projects.lib.sisyphusCommon)

    compileOnly(libs.spring.boot.actuator)
}
