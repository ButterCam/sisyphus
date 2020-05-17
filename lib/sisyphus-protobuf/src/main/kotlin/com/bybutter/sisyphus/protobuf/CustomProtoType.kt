package com.bybutter.sisyphus.protobuf

/**
 * Interface of custom protobuf type, it can be used for map protobuf types to custom types.
 *
 * For example: Protobuf compiler use it to mapping string fields which has resource name option to ResourceName.
 */
interface CustomProtoType<T> {
    /**
     * Get the raw protobuf type of current custom proto type.
     */
    fun raw(): T
}

interface CustomProtoTypeSupport<T : CustomProtoType<TRaw>, TRaw> {
    val rawType: Class<TRaw>

    /**
     * Wrap raw protobuf type to custom proto type.
     */
    fun wrapRaw(value: TRaw): T
}
