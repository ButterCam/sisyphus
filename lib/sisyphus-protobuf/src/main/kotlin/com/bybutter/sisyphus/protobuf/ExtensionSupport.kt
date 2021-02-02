package com.bybutter.sisyphus.protobuf

import com.bybutter.sisyphus.protobuf.coded.Reader
import com.bybutter.sisyphus.protobuf.coded.Writer
import com.bybutter.sisyphus.protobuf.primitives.FieldDescriptorProto
import kotlin.reflect.KProperty

abstract class ExtensionSupport<T> : ProtoSupport<FieldDescriptorProto> {
    abstract val extendee: MessageSupport<*, *>

    abstract fun write(writer: Writer, value: T)

    abstract fun read(reader: Reader, number: Int, wire: Int, extension: MessageExtension<T>?): MessageExtension<T>

    abstract fun getProperty(): KProperty<*>

    abstract fun default(): T?

    open fun wrap(value: T): MessageExtension<T> {
        return MessageExtensionImpl(value, this)
    }
}
