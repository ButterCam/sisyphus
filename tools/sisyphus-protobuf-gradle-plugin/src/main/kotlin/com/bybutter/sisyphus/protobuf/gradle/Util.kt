package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.string.toCamelCase

fun protoConfigurationName(sourceSetName: String): String {
    return if (sourceSetName == "main") {
        "proto"
    } else {
        "$sourceSetName proto".toCamelCase()
    }
}

fun protoApiConfigurationName(sourceSetName: String): String {
    return "${protoConfigurationName(sourceSetName)} api".toCamelCase()
}

fun implementationConfigurationName(sourceSetName: String): String {
    return if (sourceSetName == "main") {
        "implementation"
    } else {
        "$sourceSetName implementation".toCamelCase()
    }
}

fun extractProtoTaskName(sourceSetName: String): String {
    return "extract $sourceSetName proto".toCamelCase()
}

fun generateProtoTaskName(sourceSetName: String): String {
    return "generate $sourceSetName proto".toCamelCase()
}

fun compileKotlinTaskName(sourceSetName: String): String {
    return if (sourceSetName == "main") {
        "compileKotlin"
    } else {
        "compile $sourceSetName Kotlin".toCamelCase()
    }
}
