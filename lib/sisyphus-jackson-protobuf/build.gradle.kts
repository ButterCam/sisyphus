lib

plugins {
    `java-library`
    protobuf
}

description = "Jackson support for Sisyphus protobuf runtime customized message"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-protobuf"))
}
