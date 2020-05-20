middleware

plugins {
    `java-library`
}

description = "Middleware for manage configuration of Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-common"))
    implementation(Dependencies.Maven.resolver)
    implementation(Dependencies.Maven.resolverConnector)
    implementation(Dependencies.Maven.resolverWagon)
    implementation(Dependencies.Maven.wagonFile)
    implementation(Dependencies.Maven.wagonHttp)
}
