lib

plugins {
    `java-library`
}

description = "Easy to create struct in Sisyphus"

dependencies {
    compileOnly(project(":lib:sisyphus-jackson"))
    implementation(project(":lib:sisyphus-common"))

    implementation(Dependencies.Kotlin.reflect)

    testImplementation("org.junit.jupiter:junit-jupiter")
}
