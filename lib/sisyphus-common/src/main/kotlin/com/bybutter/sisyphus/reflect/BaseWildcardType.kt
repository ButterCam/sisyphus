package com.bybutter.sisyphus.reflect

import com.bybutter.sisyphus.collection.contentEquals
import java.lang.reflect.WildcardType

abstract class BaseWildcardType(protected val target: List<SimpleType>) : JvmType(), WildcardType {

    override fun equals(other: Any?): Boolean {
        return this.javaClass == other?.javaClass && target.contentEquals((other as? BaseWildcardType)?.target)
    }

    override fun hashCode(): Int {
        var base = this.javaClass.hashCode()
        for (parameter in target) {
            val hash = parameter.hashCode()
            base = (base xor hash) + hash
        }
        return base
    }
}
