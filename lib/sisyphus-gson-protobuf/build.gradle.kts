lib

plugins {
    `java-library`
}

description = "Gson support for Sisyphus protobuf runtime customized message"

dependencies {
    api("com.google.code.gson:gson:2.9.0")
    api(project(":lib:sisyphus-protobuf"))
}
