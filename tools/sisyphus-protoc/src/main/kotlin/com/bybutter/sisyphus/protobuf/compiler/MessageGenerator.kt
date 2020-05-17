package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.api.resource
import com.bybutter.sisyphus.collection.contentEquals
import com.bybutter.sisyphus.protobuf.AbstractMutableMessage
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoSupport
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.MessageOptions
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.DescriptorProtos
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import kotlin.reflect.KProperty

val INTERNAL_PROTO_API = ClassName("com.bybutter.sisyphus.protobuf", "InternalProtoApi")
val USE_INTERNAL_PROTO_API =
    AnnotationSpec.builder(ClassName("kotlin", "OptIn"))
        .addMember("%T::class", INTERNAL_PROTO_API).build()

open class MessageGenerator(override val parent: ProtobufElement, val descriptor: DescriptorProtos.DescriptorProto) : ProtobufElement() {
    override val kotlinName: String = descriptor.name
    override val protoName: String = descriptor.name

    val mutableName by lazy { "Mutable$kotlinName" }
    val implName by lazy { "${kotlinName}Impl" }
    val supportName by lazy { "${kotlinName}Support" }

    val fullMutableName: String by lazy {
        val parent = parent
        when (parent) {
            is MessageGenerator -> {
                "${parent.fullMutableName}.$mutableName"
            }
            is FileGenerator -> {
                "${parent.internalKotlinName}.$mutableName"
            }
            else -> throw IllegalStateException("Message must be a child of message or file.")
        }
    }

    val fullImplName: String by lazy {
        val parent = parent
        when (parent) {
            is MessageGenerator -> {
                "${parent.fullImplName}.$implName"
            }
            is FileGenerator -> {
                "${parent.internalKotlinName}.$implName"
            }
            else -> throw IllegalStateException("Message must be a child of message or file.")
        }
    }

    val fullSupportName: String by lazy {
        val parent = parent
        when (parent) {
            is MessageGenerator -> {
                "${parent.fullSupportName}.$supportName"
            }
            is FileGenerator -> {
                "${parent.internalKotlinName}.$supportName"
            }
            else -> throw IllegalStateException("Message must be a child of message or file.")
        }
    }

    val kotlinType by lazy {
        ClassName.bestGuess(fullKotlinName)
    }
    val mutableType by lazy {
        ClassName.bestGuess(fullMutableName)
    }
    val implType by lazy {
        ClassName.bestGuess(fullImplName)
    }
    val supportType by lazy {
        ClassName.bestGuess(fullSupportName)
    }

    var path: List<Int> = listOf()
        private set

    val resource = MessageOptions.parse(descriptor.options.toByteArray()).resource

    override val documentation: String by lazy {
        val location = ensureParent<FileGenerator>().descriptor.sourceCodeInfo.locationList.firstOrNull {
            it.pathList.contentEquals(path)
        } ?: return@lazy ""

        listOf(location.leadingComments, location.trailingComments).filter { it.isNotBlank() }.joinToString("\n\n")
    }

    override fun init() {
        super.init()
        val parent = parent

        path = when (parent) {
            is FileGenerator -> {
                listOf(DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, parent.descriptor.messageTypeList.indexOf(descriptor))
            }
            is MessageGenerator -> {
                parent.path + listOf(DescriptorProtos.DescriptorProto.NESTED_TYPE_FIELD_NUMBER, parent.descriptor.nestedTypeList.indexOf(descriptor))
            }
            else -> throw IllegalStateException("Message must be a child of message or file.")
        }

        val oneOfs = descriptor.oneofDeclList.map { OneOfGenerator(this, it) }

        addElements(oneOfs)

        for (field in descriptor.fieldList) {
            if (field.hasOneofIndex()) {
                val fieldGenerator = OneOfFieldGenerator(this, oneOfs[field.oneofIndex], field)
                oneOfs[field.oneofIndex].addElement(fieldGenerator)
                addElement(fieldGenerator)
            } else {
                addElement(FieldGenerator(this, field))
            }
        }

        for (type in descriptor.nestedTypeList) {
            if (type.isMapType) {
                addElement(MapEntryGenerator(this, type))
            } else {
                addElement(NestedMessageGenerator(this, type))
            }
        }

        for (type in descriptor.enumTypeList) {
            addElement(EnumGenerator(this, type))
        }

        if (resource != null) {
            addElements(ResourceNameParentGenerator(this, resource))
        }
    }

    override fun prepareGenerating() {
        for ((extendee, fields) in descriptor.extensionList.groupBy { it.extendee }) {
            addElement(ExtensionsGenerator(this, getElementByProtoName(extendee) as MessageGenerator, fields))
        }
        super.prepareGenerating()
    }

    fun field(name: String): FieldGenerator {
        return this.children.first { it is FieldGenerator && it.protoName == name } as FieldGenerator
    }

    open fun generate(): TypeSpec {
        return TypeSpec.interfaceBuilder(kotlinName)
            .addSuperinterface(Message::class.asClassName().parameterizedBy(kotlinType, mutableType))
            .addKdoc(escapeDoc(documentation))
            .apply {
                for (child in children) {
                    when (child) {
                        is OneOfGenerator -> {
                            child.applyToMessage(this)
                        }
                        is FieldGenerator -> {
                            child.applyToMessage(this)
                        }
                        is EnumGenerator -> {
                            addType(child.generate())
                        }
                        is ExtensionsGenerator -> {
                            child.applyToType(this)
                            addType(child.generate())
                        }
                        is MessageGenerator -> {
                            addType(child.generate())
                        }
                        is ResourceNameParentGenerator -> {
                            addType(child.generate())
                            for (typeSpec in child.generateImpl()) {
                                addType(typeSpec)
                            }
                        }
                    }
                }
            }
            .addType(generateCompanion())
            .build()
    }

    open fun generateMutable(): TypeSpec {
        return TypeSpec.interfaceBuilder(mutableName)
            .addSuperinterface(MutableMessage::class.asClassName().parameterizedBy(kotlinType, mutableType))
            .addSuperinterface(kotlinType)
            .apply {
                for (child in children) {
                    when (child) {
                        is OneOfGenerator -> {
                            child.applyToMutable(this)
                        }
                        is FieldGenerator -> {
                            child.applyToMutable(this)
                        }
                        is MessageGenerator -> {
                            addType(child.generateMutable())
                        }
                    }
                }
            }
            .build()
    }

    open fun generateImpl(): TypeSpec {
        return TypeSpec.classBuilder(implName)
            .superclass(AbstractMutableMessage::class.asClassName().parameterizedBy(kotlinType, mutableType))
            .addSuperclassConstructorParameter("%T", kotlinType)
            .addSuperinterface(mutableType)
            .addModifiers(KModifier.INTERNAL)
            .apply {
                for (child in children) {
                    when (child) {
                        is OneOfGenerator -> {
                            child.applyToImpl(this)
                        }
                        is FieldGenerator -> {
                            child.applyToImpl(this)
                        }
                        is MessageGenerator -> {
                            addType(child.generateImpl())
                        }
                    }
                }
            }
            .addFunction(
                FunSpec.builder("mergeWith")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("other", kotlinType.copy(true))
                    .addStatement("other ?: return")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToMergeWithFun(this, "other")
                                }
                            }
                        }
                    }
                    .addStatement("this.unknownFields() += other.unknownFields()")
                    .build()
            )
            .addFunction(
                FunSpec.builder("unionOf")
                    .addAnnotation(USE_INTERNAL_PROTO_API)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("other", kotlinType.copy(true))
                    .returns(kotlinType)
                    .beginControlFlow("return cloneMutable().apply")
                    .addStatement("other ?: return@apply")
                    .addStatement("this.mergeWith(other)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("clone")
                    .addAnnotation(USE_INTERNAL_PROTO_API)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(kotlinType)
                    .addStatement("return cloneMutable()", kotlinType)
                    .build()
            )
            .addFunction(
                FunSpec.builder("cloneMutable")
                    .addAnnotation(INTERNAL_PROTO_API)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(mutableType)
                    .addCode(
                        buildCodeBlock {
                            beginControlFlow("return %T().apply", implType)
                            addStatement("this.mergeWith(this@%T)", implType)
                            endControlFlow()
                        }
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("clear")
                    .addModifiers(KModifier.OVERRIDE)
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToClearFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("clearAllFieldInExtensions()")
                    .addStatement("unknownFields().clear()")
                    .addStatement("invalidCache()")
                    .build()
            )
            .addFunction(
                FunSpec.builder("clearFieldInCurrent")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldName", String::class)
                    .returns(ANY.copy(true))
                    .beginControlFlow("return when(fieldName)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToClearByNameFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> clearFieldInExtensions(fieldName)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("clear")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldNumber", Int::class)
                    .returns(ANY.copy(true))
                    .beginControlFlow("return when(fieldNumber)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToClearByNumberFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> clearFieldInExtensions(fieldNumber)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("getFieldInCurrent")
                    .addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldName", String::class)
                    .returns(TypeVariableName("T"))
                    .beginControlFlow("return when(fieldName)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToGetByNameFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> getFieldInExtensions(fieldName)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("get")
                    .addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                    .addModifiers(KModifier.OPERATOR, KModifier.OVERRIDE)
                    .addParameter("fieldNumber", Int::class)
                    .returns(TypeVariableName("T"))
                    .beginControlFlow("return when(fieldNumber)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToGetByNumberFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> getFieldInExtensions(fieldNumber)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("getProperty")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldName", String::class)
                    .returns(KProperty::class.asClassName().parameterizedBy(TypeVariableName("*")).copy(true))
                    .beginControlFlow("return when(fieldName)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToGetPropertyByNameFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> getPropertyInExtensions(fieldName)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("getProperty")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldNumber", Int::class)
                    .returns(KProperty::class.asClassName().parameterizedBy(TypeVariableName("*")).copy(true))
                    .beginControlFlow("return when(fieldNumber)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToGetPropertyByNumberFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> getPropertyInExtensions(fieldNumber)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("setFieldInCurrent")
                    .addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldName", String::class)
                    .addParameter("value", TypeVariableName("T"))
                    .beginControlFlow("return when(fieldName)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToSetByNameFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> setFieldInExtensions(fieldName, value)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("set")
                    .addTypeVariable(TypeVariableName("T", ANY.copy(true)))
                    .addModifiers(KModifier.OPERATOR, KModifier.OVERRIDE)
                    .addParameter("fieldNumber", Int::class)
                    .addParameter("value", TypeVariableName("T"))
                    .beginControlFlow("return when(fieldNumber)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToSetByNumberFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> setFieldInExtensions(fieldNumber, value)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("hasFieldInCurrent")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldName", String::class)
                    .returns(Boolean::class)
                    .beginControlFlow("return when(fieldName)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToHasByNameFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> hasFieldInExtensions(fieldName)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("has")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("fieldNumber", Int::class)
                    .returns(Boolean::class)
                    .beginControlFlow("return when(fieldNumber)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToHasByNumberFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> hasFieldInExtensions(fieldNumber)")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("equals")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("other", kotlinType)
                    .returns(Boolean::class)
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToEqualsFun(this, "other")
                                }
                            }
                        }
                    }
                    .addStatement("return true")
                    .build()
            )
            .addFunction(
                FunSpec.builder("computeSize")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(Int::class)
                    .addStatement("var result = 0")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToComputeSizeFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("return result")
                    .build()
            )
            .addFunction(
                FunSpec.builder("computeHashCode")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(Int::class)
                    .addStatement("var result = this.javaClass.hashCode()")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToComputeHashCode(this)
                                }
                            }
                        }
                    }
                    .addStatement("return result")
                    .build()
            )
            .addFunction(
                FunSpec.builder("writeFields")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("output", CodedOutputStream::class)
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToWriteFieldsFun(this)
                                }
                            }
                        }
                    }
                    .build()
            )
            .addFunction(
                FunSpec.builder("readField")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("input", CodedInputStream::class)
                    .addParameter("field", Int::class)
                    .addParameter("wire", Int::class)
                    .returns(Boolean::class)
                    .beginControlFlow("when(field)")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    child.applyToReadFieldFun(this)
                                }
                            }
                        }
                    }
                    .addStatement("else -> return false")
                    .endControlFlow()
                    .addStatement("return true")
                    .build()
            )
            .build()
    }

    open fun generateSupport(): TypeSpec {
        return TypeSpec.classBuilder(supportName)
            .addModifiers(KModifier.ABSTRACT)
            .superclass(ProtoSupport::class.asClassName().parameterizedBy(kotlinType, mutableType))
            .addSuperclassConstructorParameter("%S", fullProtoName)
            .primaryConstructor(FunSpec.constructorBuilder().addModifiers(KModifier.INTERNAL).build())
            .apply {
                for (child in children) {
                    when (child) {
                        is FieldGenerator -> {
                            child.applyToSupport(this)
                        }
                        is MessageGenerator -> {
                            addType(child.generateSupport())
                        }
                        is ResourceNameParentGenerator -> {
                            addType(child.generateSupport())
                        }
                    }
                }
            }
            .addProperty(
                PropertySpec.builder("descriptor", DescriptorProto::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .delegate(
                        buildCodeBlock {
                            beginControlFlow("%M", MemberName("kotlin", "lazy"))
                            val parent = parent
                            when (parent) {
                                is FileGenerator -> {
                                    addStatement("%T.descriptor.messageType.first{ it.name == %S }", parent.fileMetaTypeName, protoName)
                                }
                                is MessageGenerator -> {
                                    addStatement("%T.descriptor.nestedType.first{ it.name == %S }", parent.kotlinType, protoName)
                                }
                                else -> throw IllegalStateException("Message must be a child of message or file.")
                            }
                            endControlFlow()
                        }
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("newMutable")
                    .addAnnotation(INTERNAL_PROTO_API)
                    .returns(mutableType)
                    .addModifiers(KModifier.OVERRIDE)
                    .addStatement("return %T()", implType)
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addAnnotation(USE_INTERNAL_PROTO_API)
                    .addModifiers(KModifier.OPERATOR)
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    if (child.descriptor.isRequired) {
                                        addParameter(child.kotlinName, child.valueType)
                                    }
                                }
                            }
                        }
                    }
                    .returns(kotlinType)
                    .apply {
                        if (this.parameters.isEmpty()) {
                            addStatement("return newMutable()")
                        } else {
                            beginControlFlow("return newMutable().apply")
                            for (child in children) {
                                when (child) {
                                    is FieldGenerator -> {
                                        if (child.descriptor.isRequired) {
                                            addStatement("this.%N = %N", child.kotlinName, child.kotlinName)
                                        }
                                    }
                                }
                            }
                            endControlFlow()
                        }
                    }
                    .build()
            )
            .addFunction(
                FunSpec.builder("invoke")
                    .addAnnotation(USE_INTERNAL_PROTO_API)
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    if (child.descriptor.isRequired) {
                                        addParameter(child.kotlinName, child.valueType)
                                    }
                                }
                            }
                        }
                    }
                    .addModifiers(KModifier.INLINE, KModifier.OPERATOR)
                    .addParameter("block", LambdaTypeName.get(mutableType, listOf(), UNIT))
                    .returns(kotlinType)
                    .beginControlFlow("return newMutable().apply")
                    .apply {
                        for (child in children) {
                            when (child) {
                                is FieldGenerator -> {
                                    if (child.descriptor.isRequired) {
                                        addStatement("this.%N = %N", child.kotlinName, child.kotlinName)
                                    }
                                }
                            }
                        }
                        addStatement("block()")
                    }
                    .endControlFlow()
                    .build()
            )
            .build()
    }

    open fun generateCompanion(): TypeSpec {
        return TypeSpec.companionObjectBuilder()
            .superclass(supportType)
            .build()
    }
}
