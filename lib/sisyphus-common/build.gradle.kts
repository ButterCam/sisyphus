plugins {
    `sisyphus-library`
}

description = "Common lib of Sisyphus Project"

dependencies {
    implementation(libs.kotlin.reflect)
    compileOnly(libs.kotlin.coroutines)
}
