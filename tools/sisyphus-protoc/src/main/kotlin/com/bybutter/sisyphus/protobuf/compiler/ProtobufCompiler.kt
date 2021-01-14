package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ImplementationFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.compiler
import com.bybutter.sisyphus.protobuf.compiler.generating.packageName
import com.bybutter.sisyphus.protobuf.compiler.generator.CodeGenerators
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import java.util.Stack
import java.util.logging.Logger

class ProtobufCompiler(val generators: CodeGenerators = CodeGenerators()) {
    private val fileDescriptors = mutableMapOf<String, DescriptorProtos.FileDescriptorProto>()

    private val logger = Logger.getLogger("Sisyphus Protobuf Compiler")

    private val packageShading = mutableMapOf<String, String>()

    fun withProtos(fileSet: DescriptorProtos.FileDescriptorSet): ProtobufCompiler {
        for (fileDescriptorProto in fileSet.fileList) {
            if (fileDescriptors.containsKey(fileDescriptorProto.name)) {
                logger.warning("Duplicate '${fileDescriptorProto.name}' proto file imported.")
            }
            fileDescriptors[fileDescriptorProto.name] = fileDescriptorProto
        }
        return this
    }

    fun shading(srcPackage: String, destPackage: String): ProtobufCompiler {
        packageShading[srcPackage] = destPackage
        return this
    }

    fun shading(map: Map<String, String>): ProtobufCompiler {
        packageShading += map
        return this
    }

    fun generate(file: String): ProtoCompileResult {
        val descriptor = fileDescriptors[file] ?: throw IllegalArgumentException("Proto file '$file' not imported.")
        val result = mutableListOf<FileSpec>()

        ApiFileGeneratingState(this, descriptor, result).advance()
        ImplementationFileGeneratingState(this, descriptor, result).advance()

        return ProtoCompileResult(descriptor, result)
    }

    fun shade(name: String): String {
        val shadedPackage = packageShading.keys.maxBy {
            if (name.startsWith(it)) it.length else -1
        } ?: return name

        if (name.startsWith(shadedPackage)) {
            return packageShading[shadedPackage] + name.substringAfter(shadedPackage)
        }

        return name
    }

    private fun findDescriptor(findStack: Stack<Any>, name: String): Boolean {
        val name = name.trim('.')
        if (name.isEmpty()) return true

        when (val descriptor = findStack.lastOrNull()) {
            null -> {
                for (value in fileDescriptors.values) {
                    if (!name.startsWith(value.`package`)) continue
                    findStack.push(value)
                    if (findDescriptor(findStack, name.substringAfter(value.`package`))) {
                        return true
                    }
                    findStack.pop()
                }
            }
            is DescriptorProtos.FileDescriptorProto -> {
                val part = name.substringBefore('.')
                for (descriptorProto in descriptor.messageTypeList) {
                    if (descriptorProto.name != part) continue
                    findStack.push(descriptorProto)
                    if (findDescriptor(findStack, name.substringAfter('.', ""))) {
                        return true
                    }
                    findStack.pop()
                }
                for (descriptorProto in descriptor.enumTypeList) {
                    if (descriptorProto.name != part) continue
                    findStack.push(descriptorProto)
                    if (findDescriptor(findStack, name.substringAfter('.', ""))) {
                        return true
                    }
                    findStack.pop()
                }
            }
            is DescriptorProtos.DescriptorProto -> {
                val part = name.substringBefore('.')
                for (descriptorProto in descriptor.nestedTypeList) {
                    if (descriptorProto.name != part) continue
                    findStack.push(descriptorProto)
                    if (findDescriptor(findStack, name.substringAfter('.', ""))) {
                        return true
                    }
                    findStack.pop()
                }
                for (descriptorProto in descriptor.enumTypeList) {
                    if (descriptorProto.name != part) continue
                    findStack.push(descriptorProto)
                    if (findDescriptor(findStack, name.substringAfter('.', ""))) {
                        return true
                    }
                    findStack.pop()
                }
            }
        }

        return false
    }

    fun protoDescriptor(name: String): DescriptorProtos.DescriptorProto {
        return Stack<Any>().apply {
            findDescriptor(this, name)
        }.pop() as DescriptorProtos.DescriptorProto
    }

    fun enumDescriptor(name: String): DescriptorProtos.EnumDescriptorProto {
        return Stack<Any>().apply {
            findDescriptor(this, name)
        }.pop() as DescriptorProtos.EnumDescriptorProto
    }

    fun packageName(file: DescriptorProtos.FileDescriptorProto): String {
        return if (file.options.hasJavaPackage()) {
            shade(file.options.javaPackage)
        } else {
            shade(file.`package`)
        }
    }

    fun internalPackageName(file: DescriptorProtos.FileDescriptorProto): String {
        return "${packageName(file)}.internal".trim('.')
    }

    fun protoClassName(name: String): ClassName {
        val stack = Stack<Any>()
        findDescriptor(stack, name)
        return ClassName.bestGuess(stack.joinToString(".") {
            when (it) {
                is DescriptorProtos.FileDescriptorProto -> packageName(it)
                is DescriptorProtos.DescriptorProto -> it.name
                is DescriptorProtos.EnumDescriptorProto -> it.name
                else -> TODO()
            }
        }.trim('.'))
    }

    fun protoMutableClassName(name: String): ClassName {
        val stack = Stack<Any>()
        findDescriptor(stack, name)
        return ClassName.bestGuess(stack.joinToString(".") {
            when (it) {
                is DescriptorProtos.FileDescriptorProto -> internalPackageName(it)
                is DescriptorProtos.DescriptorProto -> "Mutable${it.name}"
                is DescriptorProtos.EnumDescriptorProto -> it.name
                else -> TODO()
            }
        }.trim('.'))
    }
}
