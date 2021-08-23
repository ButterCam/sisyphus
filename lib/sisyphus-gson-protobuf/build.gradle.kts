lib

plugins {
    `java-library`
}

description = "Gson support for Sisyphus protobuf runtime customized message"

dependencies {
    api("com.google.code.gson:gson:2.8.8")
    api(project(":lib:sisyphus-protobuf"))
}
