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
    api(platform("org.springframework.boot:spring-boot-dependencies:2.3.3.RELEASE"))
    api(platform("io.micrometer:micrometer-bom:1.5.4"))
    api(platform("org.jetbrains.kotlin:kotlin-bom:1.3.72"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.3.8"))
    api(platform("org.apache.maven:maven:3.6.3"))
    api(platform("io.grpc:grpc-bom:1.31.1"))
    api(platform("com.google.protobuf:protobuf-bom:3.13.0"))

    constraints {
        api("com.squareup:kotlinpoet:1.6.0")
        api("org.elasticsearch.client:transport:5.6.3")
        api("com.aliyun.hbase:alihbase-client:2.8.1")
        api("org.reflections:reflections:0.9.12")
        api("com.github.os72:protoc-jar:3.11.4")
        api("io.kubernetes:client-java:9.0.2")
        api("io.netty:netty-tcnative-boringssl-static:2.0.34.Final")
        api("org.apache.maven.wagon:wagon-http:3.4.1")
        api("org.junit.jupiter:junit-jupiter:5.6.2")
        api("com.github.tomakehurst:wiremock-jre8:2.27.2")
        api("commons-logging:commons-logging:1.2")
        api("org.reflections:reflections:0.9.12")
        api("com.squareup.okhttp3:okhttp:4.8.1")
        api("com.squareup.retrofit2:retrofit:2.9.0")
        api("io.github.resilience4j:resilience4j-retrofit:1.5.0")
        api("org.antlr:antlr4:4.8")
        api("io.swagger.core.v3:swagger-core:2.1.4")
        api("org.jooq:jooq:3.13.4")
        api("com.google.api.grpc:proto-google-common-protos:1.18.1")
        api("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
        api("org.jetbrains.kotlin:kotlin-allopen:1.3.72")
        api("org.springframework.boot:spring-boot-gradle-plugin:2.3.3.RELEASE")
        api("org.jlleitschuh.gradle:ktlint-gradle:9.2.1")
        api("com.github.ben-manes:gradle-versions-plugin:0.28.0")
        api("com.netflix.nebula:nebula-publishing-plugin:17.3.2")
        api("com.netflix.nebula:gradle-contacts-plugin:5.1.0")
        api("com.netflix.nebula:gradle-info-plugin:9.1.1")
        api("org.gradle.kotlin:plugins:1.3.6")
        api("com.gradle.publish:plugin-publish-plugin:0.12.0")
        api("org.eclipse.jgit:org.eclipse.jgit:5.8.1.202007141445-r")
        api("com.palantir.gradle.docker:gradle-docker:0.25.0")
        api("io.grpc:grpc-kotlin-stub:0.1.5")
        api("com.google.api:api-compiler:0.0.8")
        api("com.aliyun.openservices:ons-client:1.8.7.1.Final")
        api("com.alibaba.csp:sentinel-grpc-adapter:1.8.0")
        api("com.alibaba.csp:sentinel-transport-simple-http:1.8.0")
        api("com.alibaba.csp:sentinel-parameter-flow-control:1.8.0")
        api("com.alibaba.csp:sentinel-datasource-redis:1.8.0")
    }
}