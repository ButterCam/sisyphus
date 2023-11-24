package com.bybutter.sisyphus.reflect

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.valueParameters

fun KFunction<*>.resolveArguments(args: List<Any?>): Map<KParameter, Any?>? {
    val requiredParameters = mutableListOf<KParameter>()
    val optionalParameters = mutableListOf<KParameter>()
    val result = mutableMapOf<KParameter, Any?>()
    var index = 0

    extensionReceiverParameter?.let {
        requiredParameters += it
    }

    valueParameters.forEach {
        if (it.isOptional || it.isVararg) {
            optionalParameters += it
        } else {
            requiredParameters += optionalParameters
            optionalParameters.clear()
            requiredParameters += it
        }
    }

    for (it in requiredParameters) {
        if (index >= args.size) return null

        if (!it.accept(args[index])) {
            return null
        }

        result[it] = args[index]
        index++
    }

    for (it in optionalParameters) {
        if (index >= args.size) break

        if (it.isVararg) {
            val vararg = mutableListOf<Any?>()
            while (index < args.size) {
                if (!it.accept(args[index])) {
                    continue
                }
                vararg += args[index]
                index++
            }
            val varargCollection = vararg as java.util.Collection<Any?>
            result[it] =
                varargCollection.toArray(
                    java.lang.reflect.Array.newInstance(
                        (it.type.arguments.first().type!!.classifier as KClass<*>).java,
                        0,
                    ) as Array<*>,
                )
        } else {
            if (!it.accept(args[index])) {
                break
            }
            result[it] = args[index]
            index++
        }
    }

    return result
}

private fun KParameter.accept(value: Any?): Boolean =
    if (isVararg) {
        type.arguments.first().type?.accept(value)!!
    } else {
        type.accept(value)
    }

private fun KType.accept(value: Any?): Boolean {
    if (value == null) {
        return this.isMarkedNullable
    }
    return (this.classifier as KClass<*>).isInstance(value)
}
