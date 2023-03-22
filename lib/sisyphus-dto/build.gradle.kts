plugins {
    `sisyphus-library`
}

description = "Easy to create struct in Sisyphus"

dependencies {
    compileOnly(projects.lib.sisyphusJackson)

    implementation(projects.lib.sisyphusCommon)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.junit.jupiter)
}
