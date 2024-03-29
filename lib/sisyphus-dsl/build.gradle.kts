plugins {
    antlr
    `sisyphus-library`
    `sisyphus-protobuf`
}

description = "Utils and toolkit for building gRPC service easier"

dependencies {
    api(libs.antlr4.runtime)
    api(projects.lib.sisyphusGrpc)
    implementation(projects.lib.sisyphusCommon)
    implementation(libs.kotlin.reflect)

    antlr(libs.antlr4)

    testImplementation(libs.junit.jupiter)
}

configurations {
    api.get().setExtendsFrom(api.get().extendsFrom.filter { it.name != "antlr" })
}

tasks.kotlinSourcesJar.get().dependsOn(tasks.generateGrammarSource)
tasks.sourcesJar.get().dependsOn(tasks.generateGrammarSource)
tasks.compileKotlin.get().dependsOn(tasks.generateGrammarSource)
tasks.compileJava.get().dependsOn(tasks.generateGrammarSource)
tasks.formatKotlinMain.get().dependsOn(tasks.generateGrammarSource)
tasks.lintKotlinMain.get().dependsOn(tasks.generateGrammarSource)

tasks.compileTestKotlin.get().dependsOn(tasks.generateTestGrammarSource)
tasks.compileTestJava.get().dependsOn(tasks.generateTestGrammarSource)
tasks.formatKotlinTest.get().dependsOn(tasks.generateTestGrammarSource)
tasks.lintKotlinTest.get().dependsOn(tasks.generateTestGrammarSource)