package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.dto.DtoMeta
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase

internal class ModelSerializer : BeanSerializerBase {
    constructor(source: BeanSerializerBase) : super(source)

    constructor(source: ModelSerializer, objectIdWriter: ObjectIdWriter) : super(source, objectIdWriter)

    constructor(source: ModelSerializer, toIgnore: MutableSet<String>) : super(source, toIgnore)

    constructor(source: ModelSerializer, objectIdWriter: ObjectIdWriter, filterId: Any?) : super(
        source,
        objectIdWriter,
        filterId
    )

    constructor(
        source: ModelSerializer,
        properties: Array<out BeanPropertyWriter>?,
        filteredProperties: Array<out BeanPropertyWriter>?
    ) : super(
        source,
        properties,
        filteredProperties
    )

    constructor(source: ModelSerializer, toIgnore: MutableSet<String>?, toInclude: MutableSet<String>?) : super(
        source,
        toIgnore,
        toInclude
    )

    override fun withObjectIdWriter(objectIdWriter: ObjectIdWriter): BeanSerializerBase {
        return ModelSerializer(this, objectIdWriter)
    }

    override fun withIgnorals(toIgnore: MutableSet<String>): BeanSerializerBase {
        return ModelSerializer(this, toIgnore)
    }

    override fun withByNameInclusion(
        toIgnore: MutableSet<String>?,
        toInclude: MutableSet<String>?
    ): BeanSerializerBase {
        return ModelSerializer(this, toIgnore, toInclude)
    }

    override fun asArraySerializer(): BeanSerializerBase {
        throw UnsupportedOperationException("Unsupported array serializer for DtoModel.")
    }

    override fun withProperties(
        properties: Array<out BeanPropertyWriter>?,
        filteredProperties: Array<out BeanPropertyWriter>?
    ): BeanSerializerBase {
        return ModelSerializer(this, properties, filteredProperties)
    }

    override fun withFilterId(filterId: Any?): BeanSerializerBase {
        return ModelSerializer(this, _objectIdWriter, filterId)
    }

    override fun serialize(bean: Any, gen: JsonGenerator, provider: SerializerProvider) {
        gen.currentValue = bean

        gen.writeStartObject()
        serializeFields(bean, gen, provider)
        gen.writeEndObject()
    }

    override fun serializeFields(bean: Any, gen: JsonGenerator, provider: SerializerProvider?) {
        serializeTypeInfo(bean, gen, provider)
        super.serializeFields(bean, gen, provider)
    }

    override fun serializeFieldsFiltered(bean: Any, gen: JsonGenerator, provider: SerializerProvider?) {
        serializeTypeInfo(bean, gen, provider)
        super.serializeFieldsFiltered(bean, gen, provider)
    }

    private fun serializeTypeInfo(bean: Any, gen: JsonGenerator, provider: SerializerProvider?) {
        gen.currentValue = bean as DtoMeta
        val context = getParentContext(gen.outputContext)

        val outputType = (context.currentValue as? DtoMeta)?.`$outputType` ?: false

        if (outputType) {
            gen.writeStringField("\$type", bean.`$type`.typeName)
        }
    }

    private fun getParentContext(context: JsonStreamContext): JsonStreamContext {
        return context.parent?.let {
            val root = getParentContext(it)
            if (root.currentValue !is DtoMeta) {
                it
            } else {
                root
            }
        } ?: context
    }
}
