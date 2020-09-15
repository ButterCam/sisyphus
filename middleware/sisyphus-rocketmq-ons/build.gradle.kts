middleware

plugins {
    `java-library`
}

description = "Middleware for using RocketMQ in Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-dto"))
    implementation(project(":lib:sisyphus-jackson"))

    api(Dependencies.Aliyun.RocketMQ.RocketMQ)
}
