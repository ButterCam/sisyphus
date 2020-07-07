middleware

plugins {
    `java-library`
}

description = "Middleware for grpc service discovery of kubernetes in Sisyphus Project"

dependencies {
    api(project(":middleware:sisyphus-grpc-client"))
    api(project(":lib:sisyphus-jackson"))
    implementation(Dependencies.kubeJavaClient)
}
