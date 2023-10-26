package com.bybutter.sisyphus.dto.enums

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class StringEnumDeserializer : JsonDeserializer<StringEnum>, ContextualDeserializer {
    private var targetClass: JavaType? = null

    constructor()
    constructor(targetClass: JavaType) {
        if (!targetClass.rawClass.isEnum || !StringEnum::class.java.isAssignableFrom(targetClass.rawClass)) {
            throw UnsupportedOperationException("Only support deserialize for 'BaseStringEnum'.")
        }
        this.targetClass = targetClass
    }

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext,
    ): StringEnum? {
        val clazz = targetClass ?: throw RuntimeException("Target class is null.")
        return StringEnum.valueOf(p.valueAsString, clazz)
    }

    override fun createContextual(
        ctxt: DeserializationContext,
        property: BeanProperty?,
    ): JsonDeserializer<*> {
        return StringEnumDeserializer(ctxt.contextualType)
    }
}
