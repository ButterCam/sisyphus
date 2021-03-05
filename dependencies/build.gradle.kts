plugins {
    `java-platform`
    id("nebula.maven-publish")
    sisyphus
}

group = "com.bybutter.sisyphus"
description = "Dependencies of Sisyphus Project (Bill of Materials)"

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(project(":sisyphus-bom")))
    api(platform("org.springframework.boot:spring-boot-dependencies:2.4.3"))
    api(platform("io.micrometer:micrometer-bom:1.6.4"))
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.4.31"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.4.2"))
    api(platform("org.apache.maven:maven:3.6.3"))
    api(platform("io.grpc:grpc-bom:1.36.0"))
    api(platform("com.google.protobuf:protobuf-bom:3.15.4"))
    api(platform("org.apache.rocketmq:rocketmq-all:4.8.0"))

    constraints {
        api("com.squareup:kotlinpoet:1.7.2")
        api("com.aliyun.hbase:alihbase-client:2.8.4")
        api("org.reflections:reflections:0.9.12")
        api("com.github.os72:protoc-jar:3.11.4")
        api("io.kubernetes:client-java:11.0.1")
        api("io.netty:netty-tcnative-boringssl-static:2.0.36.Final")
        api("org.apache.maven.wagon:wagon-http:3.4.3")
        api("org.junit.jupiter:junit-jupiter:5.7.1")
        api("org.reflections:reflections:0.9.12")
        api("com.squareup.okhttp3:okhttp:4.9.1")
        api("com.squareup.retrofit2:retrofit:2.9.0")
        api("io.github.resilience4j:resilience4j-retrofit:1.7.0")
        api("org.antlr:antlr4:4.9.1")
        api("io.swagger.core.v3:swagger-core:2.1.7")
        api("org.jooq:jooq:3.14.8")
        api("com.google.api.grpc:proto-google-common-protos:2.1.0")
        api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        api("org.jetbrains.kotlin:kotlin-allopen:1.4.31")
        api("org.springframework.boot:spring-boot-gradle-plugin:2.4.3")
        api("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
        api("com.github.ben-manes:gradle-versions-plugin:0.28.0")
        api("com.netflix.nebula:nebula-publishing-plugin:17.3.2")
        api("com.netflix.nebula:gradle-contacts-plugin:5.1.0")
        api("com.netflix.nebula:gradle-info-plugin:9.3.0")
        api("org.gradle.kotlin:plugins:1.3.6")
        api("com.gradle.publish:plugin-publish-plugin:0.12.0")
        api("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")
        api("com.palantir.gradle.docker:gradle-docker:0.25.0")
        api("io.grpc:grpc-kotlin-stub:1.0.0")
        api("com.google.api:api-compiler:0.0.8")
        api("com.alibaba.csp:sentinel-grpc-adapter:1.8.1")
        api("com.alibaba.csp:sentinel-transport-simple-http:1.8.1")
        api("com.alibaba.csp:sentinel-parameter-flow-control:1.8.1")
        api("com.alibaba.csp:sentinel-datasource-redis:1.8.1")
        api("com.google.protobuf:protobuf-javalite:3.15.5")
        api("org.mongodb:mongodb-driver-reactivestreams:4.2.2")
        api("com.android.tools.build:gradle:4.1.2")
        api("com.salesforce.servicelibs:rxgrpc-stub:1.0.1")
        api("com.google.api:api-common:1.10.1")
    }
}