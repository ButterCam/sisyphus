middleware

plugins {
    `java-library`
}

description = "Middleware for using HBase in Sisyphus Project"

dependencies {
    api(project(":lib:sisyphus-dto"))
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.hbase)
    implementation(project(":lib:sisyphus-common"))
}
