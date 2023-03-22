plugins {
    `sisyphus-library`
}

description = "Jackson utils for Sisyphus"

dependencies {
    api(projects.lib.sisyphusCommon)
    api(libs.jackson.kotlin)
    api(libs.jackson.yaml)

    implementation(libs.kotlin.reflect)

    compileOnly(libs.jackson.cbor)
    compileOnly(libs.jackson.smile)
    compileOnly(libs.jackson.properties)
}
