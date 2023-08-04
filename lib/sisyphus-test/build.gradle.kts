import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = " Test framework for testing gRPC apis in Sisyphus Framework"

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.majorVersion
    targetCompatibility = JavaVersion.VERSION_17.majorVersion
}

dependencies {
    api(libs.junit.jupiter.engine)

    implementation(projects.lib.sisyphusDsl)
    implementation(projects.lib.sisyphusGrpc)
    implementation(projects.lib.sisyphusJacksonProtobuf)
    implementation(libs.junit.launcher)
    implementation(libs.grpc.stub)
    implementation(libs.reflections)

    testImplementation(projects.starter.sisyphusGrpcServerStarter)
    testImplementation(projects.starter.sisyphusSpringBootTestStarter)
}
