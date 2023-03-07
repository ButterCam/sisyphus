lib

plugins {
    `java-library`
}

description = "Jackson support for Sisyphus protobuf runtime customized message"

dependencies {
    api(projects.lib.sisyphusJackson)
    api(projects.lib.sisyphusProtobuf)
}
