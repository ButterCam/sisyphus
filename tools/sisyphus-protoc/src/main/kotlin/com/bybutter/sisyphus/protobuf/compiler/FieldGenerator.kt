package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.ResourceReference
import com.bybutter.sisyphus.api.resource.ResourceName
import com.bybutter.sisyphus.api.resourceReference
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.WellKnownTypes
import com.bybutter.sisyphus.protobuf.coded.WireType
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

    protected fun getProtoType(): String {
        return when (descriptor.type) {
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE -> "double"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT -> "float"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64 -> "int64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64 -> "uint64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32 -> "int32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64 -> "fixed64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32 -> "fixed32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL -> "bool"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING -> "string"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP -> throw UnsupportedOperationException("Group is not supported by butter proto.")
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_MESSAGE -> {
                if (descriptor.typeName == WellKnownTypes.ANY_TYPENAME) {
                    "any"
                } else {
                    "message"
                }
            }
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES -> "bytes"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32 -> "uint32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM -> "enum"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32 -> "sfixed32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64 -> "sfixed64"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32 -> "sint32"
            DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64 -> "sint64"
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

    open fun applyToWriteFieldsFun(builder: FunSpec.Builder) {
        val element = getElementByProtoName(descriptor.typeName)
        if (descriptor.isOptional) {
            builder.beginControlFlow("if($hasFunName())")
        }
        when {
            customType && descriptor.isScalar && isPacked -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.LENGTH_DELIMITED)}).beginLd().apply{ this@${parent.implName}.%N.forEach { ${getProtoType()}(it.raw()) } }.endLd()", kotlinName)
            }
            descriptor.isScalar && isPacked -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.LENGTH_DELIMITED)}).beginLd().apply{ this@${parent.implName}.%N.forEach { ${getProtoType()}(it) } }.endLd()", kotlinName)
            }
            customType && descriptor.isScalar && descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach { writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(it.raw()) }", kotlinName)
            }
            descriptor.isScalar && descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach { writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(it) }", kotlinName)
            }
            customType && descriptor.isScalar -> {
                builder.addStatement("this.%N?.let { writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(it.raw()) }", kotlinName)
            }
            descriptor.isScalar -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(this.%N)", kotlinName)
            }
            element is MapEntryGenerator -> {
                builder.addStatement("this.%N.forEach { k, v -> writer.tag(${WireType.tagOf(descriptor.number, WireType.LENGTH_DELIMITED)}).beginLd().tag(${WireType.tagOf(element.keyField.descriptor.number, WireType.valueOf(element.keyField.descriptor.wireType))}).${element.keyField.getProtoType()}(k).tag(${WireType.tagOf(element.valueField.descriptor.number, WireType.valueOf(element.valueField.descriptor.wireType))}).${element.valueField.getProtoType()}(v).endLd() }", kotlinName)
            }
            element is MessageGenerator && descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach { writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(it) }", kotlinName)
            }
            element is MessageGenerator -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).${getProtoType()}(this.%N)", kotlinName)
            }
            element is EnumGenerator && isPacked -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.LENGTH_DELIMITED)}).beginLd().apply{ this@${parent.implName}.%N.forEach { int32(it.number) } }.endLd()", kotlinName)
            }
            element is EnumGenerator && descriptor.isRepeated -> {
                builder.addStatement("this.%N.forEach { writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).int32(it.number) }", kotlinName)
            }
            element is EnumGenerator -> {
                builder.addStatement("writer.tag(${WireType.tagOf(descriptor.number, WireType.getWireType(descriptor.wireType))}).int32(this.%N.number)", kotlinName)
            }
        }
        if (descriptor.isOptional) {
            builder.endControlFlow()
        }
    }

    open fun applyToReadFieldFun(builder: FunSpec.Builder) {
        val element = getElementByProtoName(descriptor.typeName)
        builder.addStatement("${descriptor.number} -> %L", buildCodeBlock {
            when {
                customType && descriptor.isScalar && descriptor.wireType == WireType.VARINT.ordinal && descriptor.isRepeated -> {
                    add("reader.packed(wire) { this.%N += %T.wrapRaw(it.${getProtoType()}()) }", kotlinName, kotlinType)
                }
                descriptor.isScalar && descriptor.wireType == WireType.VARINT.ordinal && descriptor.isRepeated -> {
                    add("reader.packed(wire) { this.%N += it.${getProtoType()}() }", kotlinName)
                }
                customType && descriptor.wireType == WireType.VARINT.ordinal && descriptor.isScalar -> {
                    add("reader.packed(wire) { this.%N = %T.wrapRaw(it.${getProtoType()}()) }", kotlinName, kotlinType)
                }
                descriptor.isScalar && descriptor.wireType == WireType.VARINT.ordinal -> {
                    add("reader.packed(wire) { this.%N = it.${getProtoType()}() }", kotlinName)
                }
                customType && descriptor.isScalar && descriptor.isRepeated -> {
                    add("this.%N += %T.wrapRaw(reader.${getProtoType()}())", kotlinName, kotlinType)
                }
                descriptor.isScalar && descriptor.isRepeated -> {
                    add("this.%N += reader.${getProtoType()}()", kotlinName)
                }
                customType && descriptor.isScalar -> {
                    add("this.%N = %T.wrapRaw(reader.${getProtoType()}())", kotlinName, kotlinType)
                }
                descriptor.isScalar -> {
                    add("this.%N = reader.${getProtoType()}()", kotlinName)
                }
                element is MapEntryGenerator -> {
                    when {
                        element.valueField.descriptor.isScalar -> add("reader.mapEntry({ it.${element.keyField.getProtoType()}() }, { it.${element.valueField.getProtoType()}() }) { k, v -> this.%N[k] = v }", kotlinName)
                        element.valueField.descriptor.typeName == WellKnownTypes.ANY_TYPENAME -> add("reader.mapEntry({ it.${element.keyField.getProtoType()}() }, { it.any() }) { k, v -> this.%N[k] = v }", kotlinName)
                        element.valueField.typeElement is MessageGenerator -> add("reader.mapEntry({ it.${element.keyField.getProtoType()}() }, { %T.parse(reader, reader.int32()) }) { k, v -> this.%N[k] = v }", element.valueField.kotlinType, kotlinName)
                        element.valueField.typeElement is EnumGenerator -> add("reader.mapEntry({ it.${element.keyField.getProtoType()}() }, { %T(reader.int32()) }) { k, v -> this.%N[k] = v }", element.valueField.kotlinType, kotlinName)
                    }
                }
                descriptor.typeName == WellKnownTypes.ANY_TYPENAME && descriptor.isRepeated -> {
                    add("this.%N += reader.any()", kotlinName)
                }
                descriptor.typeName == WellKnownTypes.ANY_TYPENAME -> {
                    add("this.%N = reader.any()", kotlinName)
                }
                element is MessageGenerator && descriptor.isRepeated -> {
                    add("this.%N += %T.newMutable().apply { readFrom(reader) }", kotlinName, kotlinType)
                }
                element is MessageGenerator -> {
                    add("this.%N = %T.newMutable().apply { readFrom(reader) }", kotlinName, kotlinType)
                }
                element is EnumGenerator && descriptor.isRepeated -> {
                    add("reader.packed(wire) { this.%N += %T.fromNumber(it.int32()) ?: %T.values().first() }", kotlinName, kotlinType, kotlinType)
                }
                element is EnumGenerator -> {
                    add("this.%N = %T(reader.int32())", kotlinName, kotlinType)
                }
            }
        })
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
