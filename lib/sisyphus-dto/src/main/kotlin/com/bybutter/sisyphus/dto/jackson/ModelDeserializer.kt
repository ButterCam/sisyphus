package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.dto.DtoModel
import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.jackson.javaType
import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.toType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer

internal class ModelDeserializer<T : DtoModel>(val targetClass: JavaType) : JsonDeserializer<T>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
        val node = p.readValueAsTree<TreeNode>()
        val targetType = selectType(node, ctxt)
        val beanDescription = ctxt.config.introspect(targetType)

        return DtoModel(targetType.toCanonical().toType() as SimpleType) {
            deserializeInto(p.codec, node, beanDescription, this, ctxt)
        }
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext, intoValue: T): T {
        val node = Json.deserialize(p)
        val targetType = selectType(node, ctxt)

        if (targetType.rawClass != intoValue.javaClass) {
            return ctxt.readValue(p, targetType)
        }

        val beanDescription = ctxt.config.introspect(targetType)
        return deserializeInto(p.codec, node, beanDescription, intoValue, ctxt)
    }

    private fun deserializeInto(
        codec: ObjectCodec,
        node: TreeNode,
        beanDescription: BeanDescription,
        intoValue: T,
        ctxt: DeserializationContext
    ): T {
        val properties = beanDescription.findProperties().associateBy { it.name }

        for (fieldName in node.fieldNames()) {
            val property = properties[fieldName] ?: continue

            try {
                val parser = node.get(fieldName).traverse(codec)
                parser.nextToken()
                property.setter.callOnWith(intoValue, ctxt.readValue<Any>(parser, property.getter.type))
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return intoValue
    }

    private fun selectType(node: TreeNode, ctxt: DeserializationContext): JavaType {
        val targetType = try {
            ctxt.readValue<JavaType>(node.get("\$type").traverse(), JavaType::class.java.javaType) ?: targetClass
        } catch (e: Exception) {
            targetClass
        }

        if (targetClass.isTypeOrSuperTypeOf(targetType.rawClass)) {
            return targetType
        }
        return targetClass
    }
}
