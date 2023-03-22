plugins {
    `sisyphus-middleware`
}

description = "Middleware for using HBase in Sisyphus Project"

dependencies {
    api(projects.lib.sisyphusDto)
    api(projects.lib.sisyphusJackson)
    api(libs.spring.boot)
    api(libs.hbase) {
        exclude("org.slf4j", "slf4j-log4j12")
    }
    implementation(projects.lib.sisyphusCommon)
}
