plugins {
    `sisyphus-middleware`
}

description = "Middleware for using RocketMQ in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.rocketmq)

    implementation(libs.rocketmq.acl)
    implementation(libs.kotlin.coroutines)
    implementation(projects.lib.sisyphusCommon)

    compileOnly(libs.spring.boot.actuator)
}
