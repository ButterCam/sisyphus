middleware

plugins {
    `java-library`
}

description = "Middleware for grpc service discovery of kubernetes in Sisyphus Project"

dependencies {
    api(project(":middleware:sisyphus-grpc-client"))
    implementation(Dependencies.kubeJavaClient)
}
