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
    api(platform("com.google.protobuf:protobuf-bom:3.21.5"))
    api(platform("io.grpc:grpc-bom:1.48.1"))
    api(platform("io.micrometer:micrometer-bom:1.9.3"))
    api(platform("org.apache.maven:maven:3.8.6"))
    api(platform("org.apache.rocketmq:rocketmq-all:4.7.1"))
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.7.22"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    api(platform("org.junit:junit-bom:5.9.0"))
    api(platform("org.springframework.boot:spring-boot-dependencies:2.7.2"))
    api(platform("com.fasterxml.jackson:jackson-bom:2.13.3"))

    constraints {
        api("com.aliyun.hbase:alihbase-client:2.8.7")
        api("com.android.tools.build:gradle:4.1.2")
        api("com.github.ben-manes:gradle-versions-plugin:0.28.0")
        api("com.google.api.grpc:proto-google-common-protos:2.9.2")
        api("com.google.api:api-common:2.2.1")
        api("com.google.api:api-compiler:0.0.8")
        api("com.gradle.publish:plugin-publish-plugin:0.21.0")
        api("com.netflix.nebula:gradle-contacts-plugin:6.0.0")
        api("com.netflix.nebula:gradle-info-plugin:11.3.3")
        api("com.netflix.nebula:nebula-publishing-plugin:18.4.0")
        api("com.bmuschko:gradle-docker-plugin:8.1.0")
        api("com.salesforce.servicelibs:rxgrpc-stub:1.2.3")
        api("com.squareup.okhttp3:okhttp:4.10.0")
        api("com.squareup.retrofit2:retrofit:2.9.0")
        api("com.squareup:kotlinpoet:1.11.0")
        api("io.github.resilience4j:resilience4j-retrofit:1.7.1")
        api("io.github.resilience4j:resilience4j-circuitbreaker:1.7.1")
        api("io.grpc:grpc-kotlin-stub:1.3.0")
        api("io.kubernetes:client-java:15.0.1")
        api("io.swagger.core.v3:swagger-core:2.2.1")
        api("org.antlr:antlr4:4.10.1")
        api("org.antlr:antlr4-runtime:4.10.1")
        api("org.apache.maven.wagon:wagon-http:3.5.1")
        api("org.eclipse.jgit:org.eclipse.jgit:6.2.0.202206071550-r")
        api("org.gradle.kotlin:plugins:1.3.6")
        api("org.jetbrains.kotlin:kotlin-allopen:1.7.10")
        api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        api("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
        api("org.jooq:jooq:3.17.2")
        api("org.mongodb:mongodb-driver-reactivestreams:4.6.1")
        api("org.reflections:reflections:0.10.2")
        api("org.springframework.boot:spring-boot-gradle-plugin:2.7.2")
    }
}