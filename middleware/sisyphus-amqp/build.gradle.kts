plugins {
    `sisyphus-middleware`
}

description = "Middleware for using AMQP in Sisyphus Project"

dependencies {
    implementation(projects.lib.sisyphusDto)
    implementation(projects.lib.sisyphusJackson)
    implementation(projects.middleware.sisyphusSpring)

    api(libs.spring.boot)
    api(libs.spring.framework.amqp)
}
