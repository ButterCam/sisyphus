middleware

plugins {
    `java-library`
}

description = "Middleware for using HBase in Sisyphus Project"

dependencies {
    api(project(":lib:sisyphus-dto"))
    api(project(":lib:sisyphus-jackson"))
    api(Dependencies.hbase) {
        exclude("org.slf4j", "slf4j-log4j12")
    }
    implementation(project(":lib:sisyphus-common"))
}
