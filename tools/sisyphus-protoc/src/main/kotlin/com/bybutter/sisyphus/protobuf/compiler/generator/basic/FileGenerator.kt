package com.bybutter.sisyphus.protobuf.compiler.generator.basic

import com.bybutter.sisyphus.io.replaceExtensionName
import com.bybutter.sisyphus.protobuf.compiler.RuntimeTypes
import com.bybutter.sisyphus.protobuf.compiler.extends
import com.bybutter.sisyphus.protobuf.compiler.function
import com.bybutter.sisyphus.protobuf.compiler.generating.advance
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ApiFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.EnumSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ExtensionFieldGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ExtensionFieldSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ExtensionRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FileOptionGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.FileOptionSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ImplementationFileGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageImplementationGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MessageSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.MutableMessageGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceRegisterGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.basic.ServiceSupportGeneratingState
import com.bybutter.sisyphus.protobuf.compiler.generating.fileMetadataName
import com.bybutter.sisyphus.protobuf.compiler.generating.internalPackageName
import com.bybutter.sisyphus.protobuf.compiler.generating.kotlinFileName
import com.bybutter.sisyphus.protobuf.compiler.generating.packageName
import com.bybutter.sisyphus.protobuf.compiler.generator.UniqueGenerator
import com.bybutter.sisyphus.protobuf.compiler.kFile
import com.bybutter.sisyphus.protobuf.compiler.kObject
import com.bybutter.sisyphus.protobuf.compiler.plusAssign
import com.bybutter.sisyphus.protobuf.compiler.property
import com.squareup.kotlinpoet.KModifier

open class ApiFileGenerator : UniqueGenerator<ApiFileGeneratingState> {
    override fun generate(state: ApiFileGeneratingState): Boolean {
        state.target += kFile(state.packageName(), state.kotlinFileName()) {
            for (descriptorProto in state.descriptor.messageTypeList) {
                MessageGeneratingState(state, descriptorProto, this).advance()
            }

            for (enum in state.descriptor.enumTypeList) {
                EnumGeneratingState(state, enum, this).advance()
            }

            for (extension in state.descriptor.extensionList) {
                ExtensionFieldGeneratingState(state, extension, this).advance()
            }

            for (service in state.descriptor.serviceList) {
                ServiceGeneratingState(state, service, this).advance()
            }

            FileOptionGeneratingState(state, state.descriptor, this).advance()
        }

        return true
    }
}

open class ImplementationFileGenerator : UniqueGenerator<ImplementationFileGeneratingState> {
    override fun generate(state: ImplementationFileGeneratingState): Boolean {
        state.target += kFile(state.internalPackageName(), state.kotlinFileName()) {
            this.addType(kObject(state.fileMetadataName()) {
                this extends RuntimeTypes.FILE_SUPPORT

                property("descriptor", RuntimeTypes.FILE_DESCRIPTOR_PROTO) {
                    this += KModifier.OVERRIDE
                    initializer("readDescriptor(%S)", state.descriptor.name.replaceExtensionName("proto", "pb"))
                }

                function("register") {
                    this += KModifier.OVERRIDE

                    for (message in state.descriptor.messageTypeList) {
                        MessageRegisterGeneratingState(state, message, this).advance()
                    }

                    for (enum in state.descriptor.enumTypeList) {
                        EnumRegisterGeneratingState(state, enum, this).advance()
                    }

                    for (extension in state.descriptor.extensionList) {
                        ExtensionRegisterGeneratingState(state, extension, this).advance()
                    }

                    for (service in state.descriptor.serviceList) {
                        ServiceRegisterGeneratingState(state, service, this).advance()
                    }
                }
            })

            for (message in state.descriptor.messageTypeList) {
                MutableMessageGeneratingState(state, message, this).advance()
                MessageImplementationGeneratingState(state, message, this).advance()
                MessageSupportGeneratingState(state, message, this).advance()
            }

            for (enum in state.descriptor.enumTypeList) {
                EnumSupportGeneratingState(state, enum, this).advance()
            }

            for (extension in state.descriptor.extensionList) {
                ExtensionFieldSupportGeneratingState(state, extension, this).advance()
            }

            for (service in state.descriptor.serviceList) {
                ServiceSupportGeneratingState(state, service, this).advance()
            }

            FileOptionSupportGeneratingState(state, state.descriptor, this).advance()
        }

        return true
    }
}