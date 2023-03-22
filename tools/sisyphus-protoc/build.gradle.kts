plugins {
    `sisyphus-tools`
}

description = "Proto compiler for Sisyphus customized Protobuf runtime"

dependencies {
    api(libs.kotlinpoet)

    implementation(projects.lib.sisyphusCommon)
    implementation(projects.tools.sisyphusProtocRunner)
    implementation(libs.protobuf.java)
    implementation(libs.kotlin.coroutines)
    implementation(libs.google.commonProtos)
    implementation(libs.google.apiCommon)
    implementation(libs.rxjava)

    testImplementation(libs.junit.jupiter)
    testImplementation(projects.lib.sisyphusGrpcCoroutine)
    testImplementation(projects.lib.sisyphusGrpcRxjava)
}
