plugins {
    `java-platform`
    id("nebula.maven-publish")
    sisyphus
}

group = "com.bybutter.sisyphus"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(project(":sisyphus-bom")))
    api(platform("org.springframework.boot:spring-boot-dependencies:2.2.7.RELEASE"))
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.3.70"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.3.6"))
    api(platform("org.apache.maven:maven:3.6.3"))
    api(platform("io.grpc:grpc-bom:1.29.0"))
    api(platform("com.google.protobuf:protobuf-bom:3.11.4"))

    constraints {
        api("com.squareup:kotlinpoet:1.5.0")
        api("org.elasticsearch.client:transport:5.6.3")
        api("com.aliyun.hbase:alihbase-client:2.0.3")
        api("org.reflections:reflections:0.9.11")
        api("com.github.os72:protoc-jar:3.11.4")
        api("io.netty:netty-tcnative-boringssl-static:2.0.20.Final")
        api("org.apache.maven.wagon:wagon-http:3.3.4")
        api("org.junit.jupiter:junit-jupiter:5.5.1")
        api("org.reflections:reflections:0.9.11")
        api("com.squareup.okhttp3:okhttp:4.2.2")
        api("com.squareup.retrofit2:retrofit:2.7.1")
        api("io.github.resilience4j:resilience4j-retrofit:1.3.1")
        api("org.antlr:antlr4:4.8")
        api("io.swagger.core.v3:swagger-core:2.1.1")
        api("org.jooq:jooq:3.13.1")
        api("com.google.api.grpc:proto-google-common-protos:1.18.0")
    }
}