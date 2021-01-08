middleware

plugins {
    `java-library`
}

description = "Middleware for using RocketMQ in Sisyphus Project"

dependencies {
    implementation(project(":lib:sisyphus-common"))

    api(project(":middleware:sisyphus-jdbc"))
    implementation(Dependencies.hikari)
    api(Dependencies.Seata.common)
    api(Dependencies.Seata.config)
    api(Dependencies.Seata.core)
    api(Dependencies.Seata.rm)
    api(Dependencies.Seata.tm)
    api(Dependencies.Seata.grpc)
    api(Dependencies.Seata.rmDatasource)
    api(Dependencies.Seata.seataSpring)
    api(Dependencies.Seata.serializerProtobuf)
    api(Dependencies.Seata.serializerSeata)
    api(Dependencies.Seata.sqlparserCore)
    api(Dependencies.Seata.sqlparserDruid)
}
