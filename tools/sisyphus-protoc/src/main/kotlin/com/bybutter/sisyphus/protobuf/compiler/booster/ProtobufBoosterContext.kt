package com.bybutter.sisyphus.protobuf.compiler.booster

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import java.util.UUID

data class ProtobufBoosterContext(
    var order: Int = 0,
    var name: String = "Booster_${UUID.randomUUID().toString().replace("-", "")}",
    val builder: FunSpec.Builder = FunSpec.builder("invoke").addModifiers(KModifier.OVERRIDE)
)
