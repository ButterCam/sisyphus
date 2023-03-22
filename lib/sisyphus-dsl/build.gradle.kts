plugins {
    antlr
    `sisyphus-library`
}

description = "Utils and toolkit for building gRPC service easier"

dependencies {
    api(libs.antlr4.runtime)
    api(projects.lib.sisyphusGrpc)
    implementation(projects.lib.sisyphusCommon)

    antlr(libs.antlr4)

    testImplementation(libs.junit.jupiter)
}

configurations {
    api.get().setExtendsFrom(api.get().extendsFrom.filter { it.name != "antlr" })
}
