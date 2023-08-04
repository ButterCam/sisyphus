plugins {
    `sisyphus-middleware`
}

description = "Middleware for grpc service discovery of kubernetes in Sisyphus Project"

dependencies {
    api(projects.middleware.sisyphusGrpcClient)
    api(projects.lib.sisyphusJackson)
    api(libs.spring.boot)
    implementation(libs.kubernetes)
    implementation(projects.middleware.sisyphusSpring)
}
