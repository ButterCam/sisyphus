plugins {
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = "Sisyphus customized gRPC runtime for RxJava2(client only)"

dependencies {
    api(projects.lib.sisyphusGrpc)
    api(libs.grpc.stub)
    api(libs.grpc.rxjava)
    api(libs.rxjava)

    proto(libs.google.commonProtos)
}

protobuf {
    plugins {
        separatedRxJava()
    }

    packageMapping(
        "google.api" to "com.bybutter.sisyphus.api",
        "google.cloud.audit" to "com.bybutter.sisyphus.cloud.audit",
        "google.geo.type" to "com.bybutter.sisyphus.geo.type",
        "google.logging.type" to "com.bybutter.sisyphus.logging.type",
        "google.longrunning" to "com.bybutter.sisyphus.longrunning",
        "google.rpc" to "com.bybutter.sisyphus.rpc",
        "google.rpc.context" to "com.bybutter.sisyphus.rpc.context",
        "google.type" to "com.bybutter.sisyphus.type"
    )
}
