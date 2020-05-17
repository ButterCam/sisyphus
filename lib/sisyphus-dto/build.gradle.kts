lib

plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":lib:sisyphus-jackson"))
    implementation(project(":lib:sisyphus-common"))

    implementation(Dependencies.Kotlin.reflect)
}
