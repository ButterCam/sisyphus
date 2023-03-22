plugins {
    `sisyphus-library`
}

description = "Common lib of Sisyphus Project"

dependencies {
    compileOnly(libs.spring.boot)
    compileOnly(libs.kotlin.coroutines)
}
