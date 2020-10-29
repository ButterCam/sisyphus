package com.bybutter.sisyphus.protobuf.jackson

import com.bybutter.sisyphus.protobuf.Message
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier

class ProtoModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        context.addBeanSerializerModifier(object : BeanSerializerModifier() {
            override fun modifySerializer(
                config: SerializationConfig?,
                beanDesc: BeanDescription,
                serializer: JsonSerializer<*>
            ): JsonSerializer<*> {
                if (Message::class.java.isAssignableFrom(beanDesc.beanClass)) {
                    return ProtoSerializer<Message<*, *>>(beanDesc.type)
                }
                return super.modifySerializer(config, beanDesc, serializer)
            }
        })

        context.addBeanDeserializerModifier(object : BeanDeserializerModifier() {
            override fun modifyDeserializer(
                config: DeserializationConfig,
                beanDesc: BeanDescription,
                deserializer: JsonDeserializer<*>
            ): JsonDeserializer<*> {
                if (Message::class.java.isAssignableFrom(beanDesc.beanClass)) {
                    return ProtoDeserializer<Message<*, *>>(beanDesc.type)
                }
                return super.modifyDeserializer(config, beanDesc, deserializer)
            }
        })
    }
}
