package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.MapEntry
import com.bybutter.sisyphus.protobuf.MapEntrySupport
import com.bybutter.sisyphus.protobuf.MutableMapEntry
import com.bybutter.sisyphus.protobuf.Size
import com.bybutter.sisyphus.reflect.Reflect
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName

class MapEntryGenerator(parent: MessageGenerator, descriptor: DescriptorProtos.DescriptorProto) : NestedMessageGenerator(parent, descriptor) {
    val keyField: MapEntryFieldGenerator get() = children[0] as MapEntryFieldGenerator
    val valueField: MapEntryFieldGenerator get() = children[1] as MapEntryFieldGenerator

    override fun init() {
        for (field in descriptor.fieldList) {
            addElement(MapEntryFieldGenerator(this, field))
        }
    }

    override fun generate(): TypeSpec {
        return super.generate().toBuilder()
            .apply {
                superinterfaces.clear()
            }
            .addSuperinterface(MapEntry::class.asClassName().parameterizedBy(keyField.kotlinType, valueField.kotlinType, kotlinType, mutableType))
            .build()
    }

    override fun generateMutable(): TypeSpec {
        return super.generateMutable().toBuilder()
            .apply {
                superinterfaces.clear()
            }
            .addSuperinterface(kotlinType)
            .addSuperinterface(MutableMapEntry::class.asClassName().parameterizedBy(keyField.kotlinType, valueField.kotlinType, kotlinType, mutableType))
            .build()
    }

    override fun generateSupport(): TypeSpec {
        return super.generateSupport().toBuilder()
            .apply {
                Reflect.setPrivateField(this, "superclass", MapEntrySupport::class.asTypeName().parameterizedBy(keyField.kotlinType, valueField.kotlinType, kotlinType, mutableType))
            }
            .addFunction(
                FunSpec.builder("sizeOfPair")
                    .addParameter("entry", Map.Entry::class.asTypeName().parameterizedBy(keyField.kotlinType, valueField.kotlinType))
                    .returns(Int::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement("var result = 0")
                    .apply {
                        keyField.applyToSizeOfPairFun(this)
                        valueField.applyToSizeOfPairFun(this)
                    }
                    .addStatement("return result", Size::class)
                    .build()
            )
            .addFunction(
                FunSpec.builder("writePair")
                    .addParameter("output", CodedOutputStream::class)
                    .addParameter("entry", Map.Entry::class.asTypeName().parameterizedBy(keyField.kotlinType, valueField.kotlinType))
                    .addModifiers(KModifier.OVERRIDE)
                    .apply {
                        keyField.applyToWritePairFun(this)
                        valueField.applyToWritePairFun(this)
                    }
                    .build()
            )
            .addFunction(
                FunSpec.builder("readPair")
                    .addParameter("input", CodedInputStream::class)
                    .addParameter("size", Int::class)
                    .returns(Pair::class.asTypeName().parameterizedBy(keyField.kotlinType, valueField.kotlinType))
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement("val entry = %T()", implType)
                    .addStatement("entry.readFrom(input, size)")
                    .addStatement("return entry.key to entry.value")
                    .build()
            )
            .build()
    }
}
