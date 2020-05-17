middleware

plugins {
    `java-library`
}

dependencies {
    api(Dependencies.jooq)
    implementation(Dependencies.hikari)

    runtimeOnly(Dependencies.mysql)
    runtimeOnly(Dependencies.postgresql)

    compileOnly(project(":lib:sisyphus-grpc"))

    testImplementation(Dependencies.h2)
}
