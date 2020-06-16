package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.EnumValueOptions
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.string
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerRouterFunction
import com.google.protobuf.DescriptorProtos
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.NumberSchema
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema

object SwaggerSchema {
    /**
     *  Fetch swagger schema.
     *  If proto field type is Message,field type name is returned for next generation.
     * */
    fun fetchSchemaModel(path: String): SchemaModel {
        val fileDescriptor = ProtoTypes.getFileContainingSymbol(path)
        val descriptor = ProtoTypes.getDescriptorBySymbol(path) as DescriptorProto
        val fieldDescriptors = descriptor.field
        val subTypeNames = mutableSetOf<String>()
        val schema = ObjectSchema().apply {
            val messagePath = listOf(DescriptorProtos.FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER, fileDescriptor?.messageType?.indexOf(descriptor))
            for (field in fieldDescriptors) {
                // Whether the field is decorated by 'repeated'.
                val repeated = field.label == FieldDescriptorProto.Label.REPEATED
                val fieldSchema = if (field.type == FieldDescriptorProto.Type.MESSAGE) {
                    subTypeNames.add(field.typeName.trim('.'))
                    ObjectSchema().`$ref`(SwaggerRouterFunction.COMPONENTS_SCHEMAS_PREFIX + field.typeName.trim('.'))
                } else {
                    fetchSchema(field.type, field.typeName)
                }
                this.addProperties(field.jsonName, (if (repeated) ArraySchema().items(fieldSchema) else fieldSchema).apply {
                    SwaggerDescription.fetchDescription(messagePath + listOf(DescriptorProtos.FileDescriptorProto.PACKAGE_FIELD_NUMBER, fieldDescriptors.indexOf(field)), fileDescriptor)?.let { description ->
                        this.description = description
                    }
                })
            }
            name = descriptor.name
            description = SwaggerDescription.fetchDescription(messagePath, fileDescriptor)
        }
        return SchemaModel(schema, subTypeNames)
    }

    /**
     *  Get schema other than FieldDescriptorProto.Type.MESSAGE.
     * */
    fun fetchSchema(type: FieldDescriptorProto.Type, typeName: String): Schema<out Any> {
        return when (type) {
            FieldDescriptorProto.Type.INT32 -> IntegerSchema().format("int32")
            FieldDescriptorProto.Type.INT64 -> IntegerSchema().format("int64")
            FieldDescriptorProto.Type.DOUBLE -> NumberSchema().format("double")
            FieldDescriptorProto.Type.FLOAT -> NumberSchema().format("float")
            FieldDescriptorProto.Type.BOOL -> BooleanSchema()
            FieldDescriptorProto.Type.ENUM -> {
                // Use StringSchema if the enum contains string extensions, otherwise use IntegerSchema
                val enumValueDescriptions = (ProtoTypes.getDescriptorBySymbol(typeName) as EnumDescriptorProto).value
                val enumValues = mutableListOf<String>()
                val hasOptions = enumValueDescriptions.lastOrNull()?.hasOptions() ?: false
                enumValueDescriptions.forEach { description ->
                    if (hasOptions) enumValues.add(EnumValueOptions.parse(description.options!!.toProto()).string) else enumValues.add(description.number.toString())
                }
                if (hasOptions) StringSchema()._enum(enumValues) else IntegerSchema().format("int32").apply {
                    for (value in enumValues) {
                        addEnumItem(value.toInt())
                    }
                }
            }
            else -> StringSchema()
        }
    }
}

data class SchemaModel(
    var schema: Schema<*>,
    var subTypeNames: Set<String>
)
