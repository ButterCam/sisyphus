middleware

plugins {
    `java-library`
}

description = "Middleware for using JDBC in Sisyphus Project"

dependencies {
    api(libs.spring.boot)
    api(libs.jooq)
    api(libs.kotlin.coroutines)
    api(projects.lib.sisyphusDsl)
    implementation(libs.hikari)

    runtimeOnly(libs.mysql.connector)
    runtimeOnly(libs.postgresql.connector)

    testImplementation(projects.lib.sisyphusDsl)
    testImplementation(libs.h2)
    testImplementation(libs.spring.boot.test)
}
