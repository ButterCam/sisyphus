plugins {
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = "Sisyphus customized gRPC runtime"

dependencies {
    api(projects.lib.sisyphusProtobuf)
    api(libs.grpc.api)
    api(libs.google.apiCommon)

    proto(libs.google.commonProtos)
}

protobuf {
    plugins {
        basic()
    }

    packageMapping(
        "com.google.api" to "com.bybutter.sisyphus.api",
        "com.google.cloud.audit" to "com.bybutter.sisyphus.cloud.audit",
        "com.google.geo.type" to "com.bybutter.sisyphus.geo.type",
        "com.google.logging.type" to "com.bybutter.sisyphus.logging.type",
        "com.google.longrunning" to "com.bybutter.sisyphus.longrunning",
        "com.google.rpc" to "com.bybutter.sisyphus.rpc",
        "com.google.rpc.context" to "com.bybutter.sisyphus.rpc.context",
        "com.google.type" to "com.bybutter.sisyphus.type"
    )
}
