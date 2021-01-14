package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.security.base64Decode
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

interface PatcherNode {
    fun asField(field: FieldDescriptorProto, property: KProperty<*>): Any?
}

class ValueNode : PatcherNode {
    private val values = mutableListOf<String>()

    fun add(value: String) {
        values.add(value)
    }

    fun addAll(value: Iterable<String>) {
        values.addAll(value)
    }

    override fun asField(field: FieldDescriptorProto, property: KProperty<*>): Any? {
        var result = when (field.type) {
            FieldDescriptorProto.Type.DOUBLE -> values.map { it.toDouble() }
            FieldDescriptorProto.Type.FLOAT -> values.map { it.toFloat() }
            FieldDescriptorProto.Type.SINT64,
            FieldDescriptorProto.Type.SFIXED64,
            FieldDescriptorProto.Type.INT64 -> values.map { it.toLong() }
            FieldDescriptorProto.Type.FIXED64,
            FieldDescriptorProto.Type.UINT64 -> values.map { it.toULong() }
            FieldDescriptorProto.Type.SFIXED32,
            FieldDescriptorProto.Type.SINT32,
            FieldDescriptorProto.Type.INT32 -> values.map { it.toInt() }
            FieldDescriptorProto.Type.UINT32,
            FieldDescriptorProto.Type.FIXED32 -> values.map { it.toUInt() }
            FieldDescriptorProto.Type.BOOL -> values.map { it.toBoolean() }
            FieldDescriptorProto.Type.STRING -> values
            FieldDescriptorProto.Type.BYTES -> values.map { it.base64Decode() }
            FieldDescriptorProto.Type.ENUM -> values.map {
                (ProtoTypes.findSupport(field.typeName) as EnumSupport<*>).invoke(it)
            }
            FieldDescriptorProto.Type.MESSAGE -> {
                when (field.typeName.substring(1)) {
                    FieldMask.name -> values.map {
                        FieldMask {
                            this.paths += it.split(",").map { it.trim() }
                        }
                    }
                    Timestamp.name -> values.map { Timestamp(it) }
                    Duration.name -> values.map { Duration(it) }
                    else -> throw IllegalStateException()
                }
            }
            else -> throw IllegalStateException()
        }

        val propertyType = if (field.label == FieldDescriptorProto.Label.REPEATED) {
            property.returnType.arguments.first().type?.classifier as? KClass<*>
        } else {
            property.returnType.classifier as? KClass<*>
        }
        if (propertyType != null && propertyType.isSubclassOf(CustomProtoType::class)) {
            result = result.map {
                val support = (propertyType.companionObjectInstance as CustomProtoTypeSupport<*, Any>)
                support.wrapRaw(it)
            }
        }

        return when (field.label) {
            FieldDescriptorProto.Label.OPTIONAL -> result.lastOrNull()
            FieldDescriptorProto.Label.REQUIRED -> result.last()
            FieldDescriptorProto.Label.REPEATED -> result
            else -> throw IllegalStateException()
        }
    }
}

class MessagePatcher : PatcherNode {
    private val nodes = mutableMapOf<String, PatcherNode>()

    fun add(field: String, value: String) {
        add(0, field.split('.'), value)
    }

    fun addList(field: String, value: List<String>) {
        addList(0, field.split('.'), value)
    }

    fun addAll(map: Map<String, String>) {
        for ((field, value) in map) {
            add(0, field.split('.'), value)
        }
    }

    fun addAllList(map: Map<String, List<String>>) {
        for ((field, value) in map) {
            addList(0, field.split('.'), value)
        }
    }

    protected fun add(index: Int, field: List<String>, value: String) {
        if (index == field.size - 1) {
            val valueNode = nodes.getOrPut(field[index]) {
                ValueNode()
            } as? ValueNode ?: throw IllegalStateException()
            valueNode.add(value)
            return
        }

        val patcherNode = nodes.getOrPut(field[index]) {
            MessagePatcher()
        } as? MessagePatcher ?: throw IllegalStateException()

        patcherNode.add(index + 1, field, value)
    }

    protected fun addList(index: Int, field: List<String>, value: List<String>) {
        if (index == field.size - 1) {
            val valueNode = nodes.getOrPut(field[index]) {
                ValueNode()
            } as? ValueNode ?: throw IllegalStateException()
            valueNode.addAll(value)
            return
        }

        val patcherNode = nodes.getOrPut(field[index]) {
            MessagePatcher()
        } as? MessagePatcher ?: throw IllegalStateException()

        patcherNode.addList(index + 1, field, value)
    }

    fun applyTo(message: MutableMessage<*, *>) {
        for ((field, value) in nodes) {
            if (message.getProperty(field) == null) {
                continue
            }

            if (value is MessagePatcher && message.has(field)) {
                value.applyTo(message[field])
            } else {
                message[field] = value.asField(message.fieldDescriptor(field), message.getProperty(field)!!)
            }
        }
    }

    @OptIn(InternalProtoApi::class)
    override fun asField(field: FieldDescriptorProto, property: KProperty<*>): Any? {
        return when (field.type) {
            FieldDescriptorProto.Type.MESSAGE -> {
                asMessage(field.typeName)
            }
            else -> throw IllegalStateException()
        }
    }

    @OptIn(InternalProtoApi::class)
    fun asMessage(type: String): Message<*, *> {
        return (ProtoTypes.findSupport(type) as MessageSupport<*, *>).newMutable().apply {
            applyTo(this)
        }
    }
}
