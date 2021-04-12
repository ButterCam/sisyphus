lib

plugins {
    `java-library`
}

description = "Jackson support for Sisyphus protobuf runtime customized message"

dependencies {
    api(project(":lib:sisyphus-jackson"))
    api(project(":lib:sisyphus-protobuf"))
}
