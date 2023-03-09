middleware

plugins {
    `java-library`
}

description = "Middleware for using AMQP in Sisyphus Project"

dependencies {
    implementation(projects.lib.sisyphusDto)
    implementation(projects.lib.sisyphusJackson)

    api(libs.spring.boot)
    api(libs.spring.framework.amqp)
}
