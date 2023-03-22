plugins {
    `sisyphus-starter`
}

description = "Starter for fastly creating OpenAPI v3 document for sisyphus backend service"

dependencies {
    implementation(projects.starter.sisyphusGrpcTranscodingStarter)
    api(libs.swagger)
    api(projects.starter.sisyphusWebfluxStarter)
}
