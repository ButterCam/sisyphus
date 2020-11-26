package com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.utils

import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.primitives.BoolValue
import com.bybutter.sisyphus.protobuf.primitives.BytesValue
import com.bybutter.sisyphus.protobuf.primitives.DescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.DoubleValue
import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.EnumDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import com.bybutter.sisyphus.protobuf.primitives.FieldMask
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
import com.bybutter.sisyphus.protobuf.string
import com.bybutter.sisyphus.starter.grpc.transcoding.support.swagger.SwaggerRouterFunction
import com.google.protobuf.DescriptorProtos
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
                    val typeName = field.typeName.trim('.')
                    when (typeName) {
                        FieldMask.fullName -> StringSchema().example("*")
                        Timestamp.fullName -> StringSchema().example(Timestamp.now().string())
                        Duration.fullName -> StringSchema().example(Duration.invoke(100).string())
                        Struct.fullName -> ObjectSchema()
                        Value.fullName -> ComposedSchema()
                                .addOneOfItem(ObjectSchema())
                                .addOneOfItem(BooleanSchema())
                                .addOneOfItem(NumberSchema())
                                .addOneOfItem(StringSchema())
                                .addOneOfItem(ArraySchema())
                        ListValue.fullName -> ArraySchema()
                        DoubleValue.fullName -> NumberSchema()
                        FloatValue.fullName -> NumberSchema()
                        Int64Value.fullName -> NumberSchema()
                        UInt64Value.fullName -> NumberSchema()
                        Int32Value.fullName -> NumberSchema()
                        UInt32Value.fullName -> NumberSchema()
                        BoolValue.fullName -> BooleanSchema()
                        StringValue.fullName -> StringSchema()
                        BytesValue.fullName -> StringSchema()
                        ListValue.fullName -> ArraySchema()
                        FieldMask.fullName -> StringSchema()
                        else -> {
                            subTypeNames.add(typeName)
                            ObjectSchema().`$ref`(SwaggerRouterFunction.COMPONENTS_SCHEMAS_PREFIX + field.typeName.trim('.'))
                        }
                    }
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
                StringSchema()._enum(enumValueDescriptions.map { it.name })
            }
            else -> StringSchema()
        }
    }
}

data class SchemaModel(
    var schema: Schema<*>,
    var subTypeNames: Set<String>
)
