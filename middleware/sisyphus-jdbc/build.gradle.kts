middleware

plugins {
    `java-library`
}

description = "Middleware for using JDBC in Sisyphus Project"

dependencies {
    api(Dependencies.jooq)
    implementation(Dependencies.hikari)

    runtimeOnly(Dependencies.mysql)
    runtimeOnly(Dependencies.postgresql)

    compileOnly(project(":lib:sisyphus-dsl"))

    testImplementation(Dependencies.h2)
    testImplementation(project(":lib:sisyphus-dsl"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
