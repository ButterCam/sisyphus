package com.bybutter.sisyphus.dto

import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.getTypeArgument
import com.bybutter.sisyphus.reflect.jvm
import java.util.HashMap

open class DtoDsl<T : DtoModel> {
    val type: SimpleType by lazy {
        this.javaClass.getTypeArgument(DtoDsl::class.java, 0).jvm as SimpleType
    }

    /**
     * Create DTO instance with init body.
     */
    inline operator fun invoke(block: T.() -> Unit): T {
        return DtoModel(type, block)
    }

    /**
     * Create DTO instance.
     */
    operator fun invoke(): T {
        return DtoModel(type)
    }

    /**
     * Create DTO instance with shallow copy.
     */
    operator fun invoke(model: DtoModel): T {
        return DtoModel(type) {
            val map = HashMap((model as DtoMeta).`$modelMap`)
            (this as DtoMeta).`$modelMap` = map
        }
    }

    /**
     * Create DTO instance with shallow copy and init body.
     */
    inline operator fun invoke(
        model: DtoModel,
        block: T.() -> Unit,
    ): T {
        return DtoModel(type) {
            val map = HashMap((model as DtoMeta).`$modelMap`)
            (this as DtoMeta).`$modelMap` = map
            block(this)
        }
    }
}
