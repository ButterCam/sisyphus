lib

plugins {
    `java-library`
}

description = "Gson support for Sisyphus protobuf runtime customized message"

dependencies {
    api(projects.lib.sisyphusProtobuf)
    api(libs.gson)
}
