plugins {
    `sisyphus-starter`
}

description = "Starter for build application with String Webflux in Sisyphus Framework"

dependencies {
    api(projects.starter.sisyphusJacksonStarter)
    api(libs.spring.boot.webflux)
}
