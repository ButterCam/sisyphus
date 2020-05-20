lib

plugins {
    `java-library`
}

description = "Jackson utils for Sisyphus"

dependencies {
    api(project(":lib:sisyphus-common"))
    api(Dependencies.Jackson.Module.kotlin)
    api(Dependencies.Jackson.Dataformat.yaml)
    compileOnly(Dependencies.Jackson.Dataformat.cbor)
    compileOnly(Dependencies.Jackson.Dataformat.smile)
    compileOnly(Dependencies.Jackson.Dataformat.properties)

    implementation(Dependencies.Kotlin.reflect)
}
