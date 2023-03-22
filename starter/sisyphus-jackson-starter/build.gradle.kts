plugins {
    `sisyphus-starter`
}

description = "Starter for configuring HttpMessageEncoder with Jackson in Sisyphus Framework"

dependencies {
    api(projects.lib.sisyphusJackson)
    api(libs.spring.boot.json)
    compileOnly(libs.jackson.cbor)
    compileOnly(libs.jackson.smile)
}
