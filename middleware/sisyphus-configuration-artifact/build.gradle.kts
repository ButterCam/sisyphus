middleware

plugins {
    `java-library`
}

description = "Middleware for manage configuration of Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    implementation(libs.maven.resolver)
    implementation(libs.maven.resolver.connector)
    implementation(libs.maven.resolver.wagon)
    implementation(libs.maven.wagon.file)
    implementation(libs.maven.wagon.http)
    implementation(projects.lib.sisyphusCommon)
}
