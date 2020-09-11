import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

object Dependencies {
    object Kotlin {
        private const val group = "org.jetbrains.kotlin"
        const val stdlib = "$group:kotlin-stdlib-jdk8"
        const val reflect = "$group:kotlin-reflect"
        const val poet = "com.squareup:kotlinpoet"
        const val plugin = "$group:kotlin-gradle-plugin"

        object Coroutines {
            private const val group = "org.jetbrains.kotlinx"
            const val core = "$group:kotlinx-coroutines-core"
            const val reactor = "$group:kotlinx-coroutines-reactor"
            const val guava = "$group:kotlinx-coroutines-guava"
            const val jdk = "$group:kotlinx-coroutines-jdk8"
        }
    }

    object Jackson {
        private const val group = "com.fasterxml.jackson.core"
        const val databind = "$group:jackson-databind"
        const val core = "$group:jackson-core"
        const val annotations = "$group:jackson-annotations"

        object Module {
            private const val group = "com.fasterxml.jackson.module"
            const val kotlin = "$group:jackson-module-kotlin"
        }

        object Dataformat {
            private const val group = "com.fasterxml.jackson.dataformat"
            const val xml = "$group:jackson-dataformat-xml"
            const val yaml = "$group:jackson-dataformat-yaml"
            const val properties = "$group:jackson-dataformat-properties"
            const val cbor = "$group:jackson-dataformat-cbor"
            const val smile = "$group:jackson-dataformat-smile"
        }
    }
    object Aliyun {
        object RocketMQ {
            private const val group = "com.aliyun.openservices"
            const val RocketMQ = "$group:ons-client"
        }
    }

    object Alibaba {
        object Sentinel {
            private const val group = "com.alibaba.csp"
            const val GrpcAdapter = "$group:sentinel-grpc-adapter"
            const val SentinelTransport = "$group:sentinel-transport-simple-http"
            const val SentinelFlowControl = "$group:sentinel-parameter-flow-control"
            const val SentinelDatasource = "$group:sentinel-datasource-redis"
        }
    }

    object Spring {
        object Framework {
            private const val group = "org.springframework"

            const val webflux = "$group:spring-webflux"

            const val tx = "$group:spring-tx"

            const val amqp = "$group.amqp:spring-rabbit"

        }

        object Boot {
            private const val group = "org.springframework.boot"

            const val boot = "$group:spring-boot-starter"

            const val webflux = "$group:spring-boot-starter-webflux"

            const val jooq = "$group:spring-boot-starter-jooq"

            const val jdbc = "$group:spring-boot-starter-jdbc"

            const val test = "$group:spring-boot-starter-test"

            const val redis = "$group:spring-boot-starter-data-redis"

            const val amqp = "$group:spring-boot-starter-amqp"

            const val cp = "$group:spring-boot-configuration-processor"

            const val jackson = "$group:spring-boot-starter-json"

            const val actuator = "$group:spring-boot-starter-actuator"
        }
    }

    object Proto {
        private const val group = "com.google.protobuf"

        const val base = "$group:protobuf-java"
        const val apiCompiler = "com.google.api:api-compiler:0.0.8"

        const val runtimeProto = "$group:protobuf-java:3.12.2"
        const val grpcProto = "com.google.api.grpc:proto-google-common-protos:1.18.0"
    }

    object Grpc {
        private const val group = "io.grpc"

        const val api = "$group:grpc-api"

        const val core = "$group:grpc-core"

        const val stub = "$group:grpc-stub"

        const val netty = "$group:grpc-netty"

        const val proto = "$group:grpc-protobuf"

        const val kotlin = "$group:grpc-kotlin-stub"
    }

    object Maven {
        private const val group = "org.apache.maven"

        const val resolver = "$group:maven-resolver-provider"

        const val resolverConnector = "$group.resolver:maven-resolver-connector-basic"

        const val resolverWagon = "$group.resolver:maven-resolver-transport-wagon"

        const val wagonFile = "$group.wagon:wagon-file"

        const val wagonHttp = "$group.wagon:wagon-http"
    }

    const val elastic5 = "org.elasticsearch.client:transport"

    const val mysql = "mysql:mysql-connector-java"

    const val postgresql = "org.postgresql:postgresql"

    const val junit = "org.junit.jupiter:junit-jupiter"

    const val hbase = "com.aliyun.hbase:alihbase-client"

    const val reflections = "org.reflections:reflections"

    const val jooq = "org.jooq:jooq"

    const val hikari = "com.zaxxer:HikariCP"

    const val h2 = "com.h2database:h2"

    const val protoc = "com.github.os72:protoc-jar"

    const val nettyTcnative = "io.netty:netty-tcnative-boringssl-static"

    const val kubeJavaClient = "io.kubernetes:client-java"

    const val retrofit = "com.squareup.retrofit2:retrofit"

    const val okhttp = "com.squareup.okhttp3:okhttp"

    const val resilience4j = "io.github.resilience4j:resilience4j-retrofit"

    const val lettuce = "io.lettuce:lettuce-core"

    const val antlr4 = "org.antlr:antlr4:4.8"

    const val swagger = "io.swagger.core.v3:swagger-core"
}

val Project.managedDependencies: Project
    get() {
        dependencies {
            add("implementation", platform(project(":sisyphus-dependencies")))
        }

        return this
    }