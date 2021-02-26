package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.protobuf.MessageSupport
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
import com.bybutter.sisyphus.protobuf.primitives.FileDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FloatValue
import com.bybutter.sisyphus.protobuf.primitives.Int32Value
import com.bybutter.sisyphus.protobuf.primitives.Int64Value
import com.bybutter.sisyphus.protobuf.primitives.ListValue
import com.bybutter.sisyphus.protobuf.primitives.StringValue
import com.bybutter.sisyphus.protobuf.primitives.Struct
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.UInt32Value
import com.bybutter.sisyphus.protobuf.primitives.UInt64Value
import com.bybutter.sisyphus.protobuf.primitives.Value
import com.bybutter.sisyphus.protobuf.primitives.invoke
import com.bybutter.sisyphus.protobuf.primitives.now
import com.bybutter.sisyphus.protobuf.primitives.string
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerRouterFunction
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.ComposedSchema
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
    fun fetchSchemaModel(path: String): SchemaModel? {
        val messageSupport = ProtoTypes.findSupport(path) as? MessageSupport<*, *> ?: return null
        val fileSupport = messageSupport.file()
        val subTypeNames = mutableSetOf<String>()
        val schema = ObjectSchema().apply {
            val messagePath = listOf(
                FileDescriptorProto.MESSAGE_TYPE_FIELD_NUMBER,
                fileSupport.descriptor.messageType.indexOf(messageSupport.descriptor)
            )
            for (field in messageSupport.descriptor.field) {
                // Whether the field is decorated by 'repeated'.
                val repeated = field.label == FieldDescriptorProto.Label.REPEATED
                val fieldSchema = if (field.type == FieldDescriptorProto.Type.MESSAGE) {
                    when (field.typeName) {
                        FieldMask.name -> StringSchema().example("*")
                        Timestamp.name -> StringSchema().example(Timestamp.now().string())
                        Duration.name -> StringSchema().example(Duration.invoke(100).string())
                        Struct.name -> ObjectSchema()
                        Value.name -> ComposedSchema()
                            .addOneOfItem(ObjectSchema())
                            .addOneOfItem(BooleanSchema())
                            .addOneOfItem(NumberSchema())
                            .addOneOfItem(StringSchema())
                            .addOneOfItem(ArraySchema())
                        ListValue.name -> ArraySchema()
                        DoubleValue.name -> NumberSchema()
                        FloatValue.name -> NumberSchema()
                        Int64Value.name -> NumberSchema()
                        UInt64Value.name -> NumberSchema()
                        Int32Value.name -> NumberSchema()
                        UInt32Value.name -> NumberSchema()
                        BoolValue.name -> BooleanSchema()
                        StringValue.name -> StringSchema()
                        BytesValue.name -> StringSchema()
                        ListValue.name -> ArraySchema()
                        FieldMask.name -> StringSchema()
                        else -> {
                            if (ProtoTypes.findSupport(field.typeName) == null) {
                                ObjectSchema()
                            } else {
                                subTypeNames.add(field.typeName)
                                ObjectSchema().`$ref`(
                                    SwaggerRouterFunction.COMPONENTS_SCHEMAS_PREFIX + field.typeName.trim(
                                        '.'
                                    )
                                )
                            }
                        }
                    }
                } else {
                    fetchSchema(field.type, field.typeName)
                }
                this.addProperties(
                    field.jsonName,
                    (if (repeated) ArraySchema().items(fieldSchema) else fieldSchema).apply {
                        SwaggerDescription.fetchDescription(
                            messagePath + listOf(
                                FileDescriptorProto.PACKAGE_FIELD_NUMBER,
                                messageSupport.descriptor.field.indexOf(field)
                            ), fileSupport.descriptor
                        )?.let { description ->
                            this.description = description
                        }
                    })
            }
            name = messageSupport.name
            description = SwaggerDescription.fetchDescription(messagePath, fileSupport.descriptor)
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
                StringSchema()._enum(ProtoTypes.findEnumSupport(typeName).descriptor.value.map { it.name })
            }
            else -> StringSchema()
        }
    }
}

data class SchemaModel(
    var schema: Schema<*>,
    var subTypeNames: Set<String>
)
