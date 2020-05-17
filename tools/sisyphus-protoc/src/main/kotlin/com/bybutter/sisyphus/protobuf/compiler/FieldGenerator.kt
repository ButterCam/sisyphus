package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.ResourceReference
import com.bybutter.sisyphus.api.resource.ResourceName
import com.bybutter.sisyphus.api.resourceReference
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoReader
import com.bybutter.sisyphus.protobuf.ProtoWriter
import com.bybutter.sisyphus.protobuf.Size
import com.bybutter.sisyphus.protobuf.WellKnownTypes
import com.bybutter.sisyphus.protobuf.primitives.FieldOptions
import com.bybutter.sisyphus.string.toPascalCase
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock

abstract class BaseFieldGenerator protected constructor(override val parent: ProtobufElement, val descriptor: DescriptorProtos.FieldDescriptorProto) : ProtobufElement() {
    private val kotlinPascalName by lazy {
        kotlinName.toPascalCase()
    }
    val hasFunName by lazy {
        "has$kotlinPascalName"
    }
    val clearFunName by lazy {
        "clear$kotlinPascalName"
    }

    var path: List<Int> = listOf()
        protected set

    val isPacked: Boolean
        get() {
            if (descriptor.options?.hasPacked() == true) {
                return descriptor.options?.packed == true
            }
            return descriptor.isRepeated && ensureParent<FileGenerator>().isProto3 && descriptor.canPack
        }

    val reference: ResourceReference? = FieldOptions.parse(descriptor.options.toByteArray()).resourceReference

    override val documentation: String by lazy {
        val location = ensureParent<FileGenerator>().descriptor.sourceCodeInfo.locationList.firstOrNull {
            it.pathList.contentEquals(path)
        } ?: return@lazy ""

        listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    val resourceNameInfo: String? by lazy {
        reference?.type ?: run {
            val parent = parent
            if (parent is MessageGenerator) {
                if (parent.resource == null) {
                    return@run null
                }

                if (parent.resource.nameField == descriptor.name) {
                    return@run parent.resource.type
                }

                if (parent.resource.nameField == "" && descriptor.name == "name") {
                    return@run parent.resource.type
                }
                null
            } else {
                null
            }
        }
    }

    override fun init() {
        super.init()
        val parent = parent

        path = when (parent) {
            is MessageGenerator -> {
                parent.path + listOf(DescriptorProtos.DescriptorProto.FIELD_FIELD_NUMBER, parent.descriptor.fieldList.indexOf(descriptor))
            }
            else -> throw IllegalStateException("Field must be a child of message.")
        }
    }

    open val kotlinType: TypeName by lazy {
        if (resourceNameInfo != null) {
            return@lazy context().resourceNames[resourceNameInfo!!]?.kotlinType ?: ResourceName::class.asClassName()
        }

        when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> Boolean::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> ByteArray::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> Double::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> Float::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32 -> UInt::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64 -> ULong::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> Int::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> Long::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32 -> Int::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64 -> Long::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32 -> Int::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64 -> Long::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> String::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> UInt::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> ULong::class.asClassName()
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            else -> {
                if (descriptor.typeName == WellKnownTypes.ANY_TYPENAME) {
                    Message::class.asClassName().parameterizedBy(TypeVariableName("*"), TypeVariableName("*"))
                } else {
                    ClassName.bestGuess(typeElement.fullKotlinName)
                }
            }
        }
    }

    val typeElement: ProtobufElement by lazy {
        getElementByProtoName(descriptor.typeName)
            ?: throw IllegalStateException("Proto type '${descriptor.typeName}' not found.")
    }

    open val nullable: Boolean
        get() {
            if (descriptor.isRequired) {
                return false
            }

            if (descriptor.type != DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                return false
            }

            return true
        }

    val isMap get() = descriptor.isRepeated && typeElement is MapEntryGenerator

    open val valueType: TypeName by lazy {
        val typeElement = typeElement
        when {
            descriptor.isRepeated && typeElement is MapEntryGenerator -> MAP.parameterizedBy(typeElement.keyField.kotlinType, typeElement.valueField.kotlinType)
            descriptor.isRepeated -> LIST.parameterizedBy(kotlinType)
            customType -> kotlinType.copy(true)
            else -> kotlinType.copy(nullable)
        }
    }

    open val mutableValueType: TypeName by lazy {
        val typeElement = typeElement
        when {
            descriptor.isRepeated && typeElement is MapEntryGenerator -> MUTABLE_MAP.parameterizedBy(typeElement.keyField.kotlinType, typeElement.valueField.kotlinType)
            descriptor.isRepeated -> MUTABLE_LIST.parameterizedBy(kotlinType)
            customType -> kotlinType.copy(true)
            else -> kotlinType.copy(nullable)
        }
    }

    // private val options: FieldOptions? by lazy {
    //    descriptor.options.toByteArray().parseProto<FieldOptions>()
    // }

    fun asMemberName(): MemberName {
        return MemberName(parent.fullKotlinName, kotlinName)
    }

    fun generateAnnotations(): List<AnnotationSpec> {
        val annotations = AnnotationCollection()
        generateAnnotations(annotations)
        return annotations.toList()
    }

    protected val customType: Boolean by lazy {
        resourceNameInfo != null
    }

    protected open fun generateAnnotations(annotations: AnnotationCollection) {
        if (descriptor.options?.deprecated == true) {
            annotations.addAnnotation(
                AnnotationSpec.builder(Deprecated::class).addMember(
                    "message = %S",
                    "$kotlinName has been marked as deprecated"
                ).build()
            )
        }
    }

    protected fun getTypeName(): String {
        return when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> "Double"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> "Float"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> "Int64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> "UInt64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> "Int32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64 -> "Fixed64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32 -> "Fixed32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> "Bool"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> "String"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> {
                if (descriptor.typeName == WellKnownTypes.ANY_TYPENAME) {
                    "Any"
                } else {
                    "Message"
                }
            }
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> "Bytes"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> "UInt32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> "Enum"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32 -> "SFixed32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64 -> "SFixed64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32 -> "SInt32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64 -> "SInt64"
            null -> throw UnsupportedOperationException("Unknown field type.")
        }
    }

    protected open fun defaultValue(): String {
        if (descriptor.isRepeated) {
            return if (typeElement is MapEntryGenerator) {
                "mutableMapOf()"
            } else {
                "mutableListOf()"
            }
        }

        if (resourceNameInfo != null) {
            return "null"
        }

        return when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> "0.0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> "0.0f"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> "0L"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> "0UL"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> "0"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> "false"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> "\"\""
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> "byteArrayOf()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32,
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> "0U"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> "$valueType()"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> "null"
        }
    }
}

open class FieldGenerator constructor(override val parent: MessageGenerator, descriptor: DescriptorProtos.FieldDescriptorProto) : BaseFieldGenerator(parent, descriptor) {
    override val kotlinName: String = descriptor.jsonName
    override val protoName: String = descriptor.name

    open fun applyToMessage(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(kotlinName, valueType)
                .addKdoc(escapeDoc(documentation))
                .addAnnotations(generateAnnotations())
                .build()
        ).apply {
            if (!descriptor.isRequired) {
                addFunction(
                    FunSpec.builder(hasFunName)
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(Boolean::class.java)
                        .build()
                )
            }
        }
    }

    open fun applyToMutable(builder: TypeSpec.Builder) {
        builder.addProperty(
            PropertySpec.builder(kotlinName, mutableValueType)
                .apply {
                    if (!descriptor.isRepeated) {
                        mutable()
                    }
                }
                .addModifiers(KModifier.OVERRIDE)
                .addAnnotations(generateAnnotations())
                .build()
        )

        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(valueType.copy(true))
                        .build()
                )
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.ABSTRACT)
                        .returns(valueType)
                        .build()
                )
            }
        }
    }

    open fun applyToImpl(builder: TypeSpec.Builder) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                builder.addProperty(
                    PropertySpec.builder("_$hasFunName", Boolean::class, KModifier.PRIVATE)
                        .mutable()
                        .initializer("false")
                        .build()
                )
                builder.addProperty(
                    PropertySpec.builder(kotlinName, valueType, KModifier.OVERRIDE)
                        .mutable()
                        .initializer(defaultValue())
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return if(_$hasFunName) field else ${defaultValue()}")
                                .build()
                        )
                        .setter(
                            FunSpec.setterBuilder()
                                .addParameter("value", valueType)
                                .addStatement("field = value")
                                .apply {
                                    if (valueType.isNullable) {
                                        addStatement("if(value != null) _$hasFunName = true")
                                    } else {
                                        addStatement("_$hasFunName = true")
                                    }
                                }
                                .addStatement("invalidCache()")
                                .build()
                        )
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(hasFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Boolean::class)
                        .addStatement("return _$hasFunName")
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(valueType.copy(true))
                        .addStatement("if(!_$hasFunName) return null")
                        .beginControlFlow("return %N.apply", kotlinName)
                        .addStatement("this@%T.%N = ${defaultValue()}", parent.implType, kotlinName)
                        .addStatement("_$hasFunName = false")
                        .addStatement("invalidCache()")
                        .endControlFlow()
                        .build()
                )
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                builder.addProperty(
                    PropertySpec.builder("_$kotlinName", valueType.copy(true), KModifier.PRIVATE)
                        .mutable()
                        .initializer("null")
                        .build()
                )
                builder.addProperty(
                    PropertySpec.builder(kotlinName, valueType, KModifier.OVERRIDE)
                        .mutable()
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement("return %N!!", "_$kotlinName")
                                .build()
                        )
                        .setter(
                            FunSpec.setterBuilder()
                                .addParameter("value", valueType)
                                .addStatement("%N = value", "_$kotlinName")
                                .addStatement("invalidCache()")
                                .build()
                        )
                        .build()
                )
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.addProperty(
                    PropertySpec.builder(kotlinName, mutableValueType, KModifier.OVERRIDE)
                        .initializer(defaultValue())
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(hasFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(Boolean::class)
                        .addStatement("return %N.isNotEmpty()", kotlinName)
                        .build()
                )
                builder.addFunction(
                    FunSpec.builder(clearFunName)
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(valueType)
                        .apply {
                            if (isMap) {
                                beginControlFlow("return %N.toMap().apply", kotlinName)
                            } else {
                                beginControlFlow("return %N.toList().apply", kotlinName)
                            }
                        }
                        .addStatement("this@%T.%N.clear()", parent.implType, kotlinName)
                        .addStatement("invalidCache()")
                        .endControlFlow()
                        .build()
                )
            }
            else -> throw UnsupportedOperationException("Unknown field label.")
        }
    }

    open fun applyToSupport(builder: TypeSpec.Builder) {
    }

    open fun applyToClearFun(builder: FunSpec.Builder) {
        if (!descriptor.isRequired) {
            builder.addStatement("%N()", clearFunName)
        }
    }

    open fun applyToMergeWithFun(builder: FunSpec.Builder, other: String) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                builder.beginControlFlow("if(%N.$hasFunName())", other)
                builder.addStatement("this.%N·=·%N.%N", kotlinName, other, kotlinName)
                builder.endControlFlow()
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                if (descriptor.type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE) {
                    if (descriptor.typeName == WellKnownTypes.ANY_TYPENAME) {
                        builder.addStatement("this.%N·=·%N.%N", kotlinName, other, kotlinName)
                    } else {
                        builder.addStatement("this.%N·=·this.%N·+·%N.%N", kotlinName, kotlinName, other, kotlinName)
                    }
                } else {
                    builder.addStatement("this.%N·=·%N.%N", kotlinName, other, kotlinName)
                }
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.addStatement("this.%N·+=·%N.%N", kotlinName, other, kotlinName)
            }
            else -> throw UnsupportedOperationException("Unknown field label.")
        }
    }

    open fun applyToClearByNameFun(builder: FunSpec.Builder) {
        if (!descriptor.isRequired) {
            if (descriptor.jsonName != descriptor.name) {
                builder.addStatement("%S, %S -> this.$clearFunName()", protoName, descriptor.jsonName)
            } else {
                builder.addStatement("%S -> this.$clearFunName()", protoName)
            }
        } else {
            if (descriptor.jsonName != descriptor.name) {
                builder.addStatement("%S, %S -> throw %T(%S)", protoName, descriptor.jsonName, IllegalStateException::class, "Can't clear required field")
            } else {
                builder.addStatement("%S -> throw %T(%S)", protoName, IllegalStateException::class, "Can't clear required field")
            }
        }
    }

    open fun applyToClearByNumberFun(builder: FunSpec.Builder) {
        if (!descriptor.isRequired) {
            builder.addStatement("%L -> this.$clearFunName()", descriptor.number)
        } else {
            builder.addStatement("%L -> throw %T(%S)", descriptor.number, IllegalStateException::class, "Can't clear required field")
        }
    }

    open fun applyToGetByNameFun(builder: FunSpec.Builder) {
        if (descriptor.jsonName != descriptor.name) {
            builder.addStatement("%S, %S -> this.%N·as·T", protoName, descriptor.jsonName, kotlinName)
        } else {
            builder.addStatement("%S -> this.%N·as·T", protoName, kotlinName)
        }
    }

    open fun applyToGetByNumberFun(builder: FunSpec.Builder) {
        builder.addStatement("%L -> this.%N·as·T", descriptor.number, kotlinName)
    }

    open fun applyToGetPropertyByNameFun(builder: FunSpec.Builder) {
        if (descriptor.jsonName != descriptor.name) {
            builder.addStatement("%S, %S -> %T::%N", protoName, descriptor.jsonName, parent.kotlinType, kotlinName)
        } else {
            builder.addStatement("%S -> %T::%N", protoName, parent.kotlinType, kotlinName)
        }
    }

    open fun applyToGetPropertyByNumberFun(builder: FunSpec.Builder) {
        builder.addStatement("%L -> %T::%N", descriptor.number, parent.kotlinType, kotlinName)
    }

    open fun applyToSetByNameFun(builder: FunSpec.Builder) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                if (descriptor.jsonName != descriptor.name) {
                    builder.beginControlFlow("%S, %S -> ", protoName, descriptor.jsonName)
                } else {
                    builder.beginControlFlow("%S -> ", protoName)
                }
                builder.addStatement("this.%N.clear()", kotlinName)
                builder.addStatement("this.%N += value as %T", kotlinName, valueType)
                builder.endControlFlow()
            }
            else -> {
                if (descriptor.jsonName != descriptor.name) {
                    builder.addStatement("%S, %S -> this.%N·=·value·as·%T", protoName, descriptor.jsonName, kotlinName, valueType)
                } else {
                    builder.addStatement("%S -> this.%N·=·value·as·%T", protoName, kotlinName, valueType)
                }
            }
        }
    }

    open fun applyToSetByNumberFun(builder: FunSpec.Builder) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.beginControlFlow("%L -> ", descriptor.number)
                builder.addStatement("this.%N.clear()", kotlinName)
                builder.addStatement("this.%N += value as %T", kotlinName, valueType)
                builder.endControlFlow()
            }
            else -> {
                builder.addStatement("%L -> this.%N·=·value·as·%T", descriptor.number, kotlinName, valueType)
            }
        }
    }

    open fun applyToHasByNameFun(builder: FunSpec.Builder) {
        if (!descriptor.isRequired) {
            if (descriptor.jsonName != descriptor.name) {
                builder.addStatement("%S, %S -> this.$hasFunName()", protoName, descriptor.jsonName)
            } else {
                builder.addStatement("%S -> this.$hasFunName()", protoName)
            }
        } else {
            if (descriptor.jsonName != descriptor.name) {
                builder.addStatement("%S, %S -> true", protoName, descriptor.jsonName)
            } else {
                builder.addStatement("%S -> true", protoName)
            }
        }
    }

    open fun applyToHasByNumberFun(builder: FunSpec.Builder) {
        if (!descriptor.isRequired) {
            builder.addStatement("%L -> this.$hasFunName()", descriptor.number)
        } else {
            builder.addStatement("%L -> true", descriptor.number)
        }
    }

    open fun applyToEqualsFun(builder: FunSpec.Builder, other: String) {
        when (descriptor.label) {
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL -> {
                builder.beginControlFlow("if($hasFunName()·!=·%N.$hasFunName())", other)
                builder.addStatement("return false")
                builder.endControlFlow()
                builder.beginControlFlow("if($hasFunName()·&&·this.%N·!=·other.%N)", kotlinName, kotlinName)
                builder.addStatement("return false")
                builder.endControlFlow()
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED -> {
                builder.beginControlFlow("if(this.%N != %N.%N)", kotlinName, other, kotlinName)
                builder.addStatement("return false")
                builder.endControlFlow()
            }
            DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED -> {
                builder.beginControlFlow("if(!this.%N.%M(%N.%N))", kotlinName, List<*>::contentEquals.asMemberName(), other, kotlinName)
                builder.addStatement("return false")
                builder.endControlFlow()
            }
            else -> throw UnsupportedOperationException("Unknown field label.")
        }
    }

    open fun applyToComputeSizeFun(builder: FunSpec.Builder) {
        val element = getElementByProtoName(descriptor.typeName)
        if (descriptor.isOptional) {
            builder.beginControlFlow("if($hasFunName())")
        }

        when {
            element is MapEntryGenerator -> {
                builder.addStatement("result·+=·%T.sizeOf(${descriptor.number},·this.%N)", element.kotlinType, kotlinName)
            }
            customType && isPacked -> {
                builder.addStatement("result·+=·%T.of${getTypeName()}(${descriptor.number},·this.%N.map·{ it.raw() })", Size::class, kotlinName)
            }
            isPacked -> {
                builder.addStatement("result·+=·%T.of${getTypeName()}(${descriptor.number},·this.%N)", Size::class, kotlinName)
            }
            customType && descriptor.isRepeated -> {
                builder.addStatement("result·+=·this.%N.sumBy{ %T.of${getTypeName()}(${descriptor.number},·it.raw()) }", kotlinName, Size::class)
            }
            descriptor.isRepeated -> {
                builder.addStatement("result·+=·this.%N.sumBy{ %T.of${getTypeName()}(${descriptor.number},·it) }", kotlinName, Size::class)
            }
            customType -> {
                builder.addStatement("result·+=·%T.of${getTypeName()}(${descriptor.number},·this.%N?.raw())", Size::class, kotlinName)
            }
            else -> {
                builder.addStatement("result·+=·%T.of${getTypeName()}(${descriptor.number},·this.%N)", Size::class, kotlinName)
            }
        }
        if (descriptor.isOptional) {
            builder.endControlFlow()
        }
    }

    open fun applyToWriteFieldsFun(builder: FunSpec.Builder) {
        val element = getElementByProtoName(descriptor.typeName)
        if (descriptor.isOptional) {
            builder.beginControlFlow("if($hasFunName())")
        }
        when {
            element is MapEntryGenerator -> {
                builder.addStatement("%T.writeMap(output,·${descriptor.number},·this.%N)", element.kotlinType, kotlinName)
            }
            customType && isPacked -> {
                builder.addStatement("%T.write${getTypeName()}(output,·${descriptor.number},·this.%N.map·{ it.raw() })", ProtoWriter::class, kotlinName)
            }
            isPacked -> {
                builder.addStatement("%T.write${getTypeName()}(output,·${descriptor.number},·this.%N)", ProtoWriter::class, kotlinName)
            }
            customType && descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach{ %T.write${getTypeName()}(output,·${descriptor.number},·it.raw()) }", kotlinName, ProtoWriter::class)
            }
            descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach{ %T.write${getTypeName()}(output,·${descriptor.number},·it) }", kotlinName, ProtoWriter::class)
            }
            customType -> {
                builder.addStatement("%T.write${getTypeName()}(output,·${descriptor.number},·this.%N?.raw())", ProtoWriter::class, kotlinName)
            }
            else -> {
                builder.addStatement("%T.write${getTypeName()}(output,·${descriptor.number},·this.%N)", ProtoWriter::class, kotlinName)
            }
        }
        if (descriptor.isOptional) {
            builder.endControlFlow()
        }
    }

    open fun applyToReadFieldFun(builder: FunSpec.Builder) {
        val element = getElementByProtoName(descriptor.typeName)
        var readBlock = buildCodeBlock {
            when {
                element is MapEntryGenerator -> {
                    add("%T.readPair(input, input.readInt32())", element.kotlinType)
                }
                getTypeName() == "Message" -> {
                    add("%T.readMessage(input,·%T::class.java,·field,·wire,·input.readInt32())",
                        ProtoReader::class, kotlinType)
                }
                getTypeName() == "Any" -> {
                    add("%T.readAny(input,·field,·wire,·input.readInt32())",
                        ProtoReader::class)
                }
                descriptor.isRepeated && getTypeName() == "Enum" -> {
                    add("%T.readEnumList(input,·%T::class.java,·field,·wire)",
                        ProtoReader::class, kotlinType)
                }
                descriptor.canPack && descriptor.isRepeated -> {
                    add("%T.read${getTypeName()}List(input,·field,·wire)", ProtoReader::class)
                }
                getTypeName() == "Enum" -> {
                    add("%T.readEnum(input,·%T::class.java,·field,·wire)",
                        ProtoReader::class, kotlinType)
                }
                else -> {
                    add("%T.read${getTypeName()}(input,·field,·wire)",
                        ProtoReader::class)
                }
            }
        }

        if (customType) {
            readBlock = when {
                descriptor.isRepeated && getTypeName() == "Enum" -> buildCodeBlock {
                    add("%L.map·{ %T.wrapRaw(it) }", readBlock, kotlinType)
                }
                descriptor.canPack && descriptor.isRepeated -> buildCodeBlock {
                    add("%L.map·{ %T.wrapRaw(it) }", readBlock, kotlinType)
                }
                else -> buildCodeBlock {
                    add("%T.wrapRaw(%L)", kotlinType, readBlock)
                }
            }
        }

        readBlock = when {
            descriptor.isRepeated -> buildCodeBlock {
                add("this.%N·+=·%L", kotlinName, readBlock)
            }
            descriptor.canPack -> buildCodeBlock {
                add("%L?.let·{ this.%N·=·it }", readBlock, kotlinName)
            }
            else -> buildCodeBlock {
                add("this.%N·=·%L", kotlinName, readBlock)
            }
        }

        builder.addStatement("%L -> %L", descriptor.number, readBlock)
    }

    open fun applyToComputeHashCode(builder: FunSpec.Builder) {
        when {
            isMap -> {
                builder.beginControlFlow("for((key, value) in this.%N)", kotlinName)
                builder.addStatement("result·=·result·*37·+·${descriptor.number}")
                builder.addStatement("result·=·result·*31·+·key.hashCode()")
                builder.addStatement("result·=·result·*31·+·value.hashCode()")
                builder.endControlFlow()
            }
            descriptor.isRepeated -> {
                builder.beginControlFlow("for(item in this.%N)", kotlinName)
                builder.addStatement("result·=·result·*37·+·${descriptor.number}")
                if (customType) {
                    builder.addStatement("result·=·result·*31·+·(item.raw().hashCode() ?: 0)")
                } else {
                    builder.addStatement("result·=·result·*31·+·item.hashCode()")
                }
                builder.endControlFlow()
            }
            descriptor.isRequired -> {
                builder.addStatement("result·=·result·*37·+·${descriptor.number}")
                if (customType) {
                    builder.addStatement("result·=·result·*31·+·(this.%N?.raw()?.hashCode() ?: 0)", kotlinName)
                } else {
                    builder.addStatement("result·=·result·*31·+·this.%N.hashCode()", kotlinName)
                }
            }
            else -> {
                builder.beginControlFlow("if($hasFunName())")
                builder.addStatement("result·=·result·*37·+·${descriptor.number}")
                if (customType) {
                    builder.addStatement("result·=·result·*31·+·(this.%N?.raw()?.hashCode() ?: 0)", kotlinName)
                } else {
                    builder.addStatement("result·=·result·*31·+·this.%N.hashCode()", kotlinName)
                }
                builder.endControlFlow()
            }
        }
    }
}
