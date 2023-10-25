package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.dto.DtoModel
import com.bybutter.sisyphus.reflect.JvmType
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import java.lang.reflect.Type

class DtoModule : SimpleModule() {
    override fun setupModule(context: SetupContext) {
        context.appendAnnotationIntrospector(DtoAnnotationIntrospector)

        context.addBeanSerializerModifier(
            object : BeanSerializerModifier() {
                override fun modifySerializer(
                    config: SerializationConfig?,
                    beanDesc: BeanDescription,
                    serializer: JsonSerializer<*>,
                ): JsonSerializer<*> {
                    if (DtoModel::class.java.isAssignableFrom(beanDesc.beanClass)) {
                        return ModelSerializer(serializer as BeanSerializerBase)
                    }
                    if (Type::class.java.isAssignableFrom(beanDesc.beanClass)) {
                        return TypeSerializer()
                    }
                    return super.modifySerializer(config, beanDesc, serializer)
                }
            },
        )

        context.addBeanDeserializerModifier(
            object : BeanDeserializerModifier() {
                override fun modifyDeserializer(
                    config: DeserializationConfig,
                    beanDesc: BeanDescription,
                    deserializer: JsonDeserializer<*>,
                ): JsonDeserializer<*> {
                    if (DtoModel::class.java.isAssignableFrom(beanDesc.beanClass)) {
                        return ModelDeserializer<DtoModel>(beanDesc.type)
                    }
                    if (Type::class.java == beanDesc.beanClass || JvmType::class.java.isAssignableFrom(beanDesc.beanClass)) {
                        return TypeDeserializer()
                    }
                    return super.modifyDeserializer(config, beanDesc, deserializer)
                }
            },
        )
    }
}
