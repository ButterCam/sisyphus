package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.compiler.booster.ProtobufBoosterContext
import com.bybutter.sisyphus.protobuf.compiler.core.state.FileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.core.state.advance
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.KModifier

class ProtobufCompiler(
    files: DescriptorProtos.FileDescriptorSet,
    packageShading: Map<String, String> = mapOf(),
    val generators: CodeGenerators = CodeGenerators()
) {
    private var context = ProtobufBoosterContext()

    fun boosterContext(): ProtobufBoosterContext {
        return context
    }

    private val descriptorSet = FileDescriptorSet(files, packageShading).apply {
        resolve()
    }

    fun generate(files: Collection<String>): ProtoCompileResults {
        context = ProtobufBoosterContext()
        val results = files.map { generate(it) }
        val boostFunc = context.builder.build()
        val booster = if (boostFunc.body.isNotEmpty()) {
            kFile("com.bybutter.sisyphus.protobuf.booster", "Booster") {
                addType(
                    kObject(context.name) {
                        implements(RuntimeTypes.PROTOBUF_BOOSTER)

                        if (context.order != 0) {
                            property("order", Int::class) {
                                this += KModifier.OVERRIDE
                                getter {
                                    addStatement("return ${context.order}")
                                }
                            }
                        }

                        addFunction(boostFunc)
                    }
                )
            }
        } else {
            null
        }
        return ProtoCompileResults(booster, results)
    }

    private fun generate(file: String): ProtoCompileResult {
        val fileDescriptor = descriptorSet.files.firstOrNull { it.descriptor.name == file }
            ?: throw IllegalArgumentException("Proto file '$file' not imported.")
        val result = mutableListOf<GeneratedFile>()
        FileGeneratingState(this, fileDescriptor, result).advance()
        return ProtoCompileResult(fileDescriptor, result)
    }
}
