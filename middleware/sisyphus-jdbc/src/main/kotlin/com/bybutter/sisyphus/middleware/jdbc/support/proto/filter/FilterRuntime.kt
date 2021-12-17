package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.reflect.LambdaFunction
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

open class FilterRuntime(private val std: FilterStandardLibrary = FilterStandardLibrary()) {
    private val memberFunctions = mutableMapOf<String, MutableList<KFunction<*>>>()

    init {
        for (memberFunction in std.javaClass.kotlin.memberFunctions) {
            if (memberFunction.javaMethod?.canAccess(std) != true) continue
            memberFunctions.getOrPut(memberFunction.name) { mutableListOf() } += memberFunction
        }
    }

    fun <R> register(function: String, block: Function<R>) {
        memberFunctions.getOrPut(function) { mutableListOf() } += LambdaFunction(block)
    }

    fun invoke(function: String, arguments: List<Any?>): Any? {
        return invokeOrDefault(function, arguments) {
            throw NoSuchMethodException(
                "Can't find function '$function(${arguments.joinToString(", ") { it?.javaClass?.canonicalName ?: "null" }})' in CEL standard library."
            )
        }
    }

    fun invokeOrDefault(function: String, arguments: List<Any?>, block: () -> Any?): Any? {
        val func = memberFunctions[function]?.firstOrNull {
            it.compatibleWith(arguments)
        } ?: return block()
        return try {
            if (func.instanceParameter == null) {
                func.call(*arguments.toTypedArray())
            } else {
                func.call(std, *arguments.toTypedArray())
            }
        } catch (ex: InvocationTargetException) {
            throw ex.cause ?: ex
        } catch (e: Exception) {
            throw e
        }
    }

    private fun KFunction<*>.compatibleWith(arguments: List<Any?>): Boolean {
        return compatibleWith(
            listOfNotNull(this.extensionReceiverParameter) + this.valueParameters,
            arguments
        )
    }

    private fun KFunction<*>.compatibleWith(parameters: List<KParameter>, arguments: List<Any?>): Boolean {
        if (parameters.size != arguments.size) return false
        for ((index, parameter) in parameters.withIndex()) {
            val type = arguments[index]?.javaClass
            if (type == null && parameter.type.isMarkedNullable) continue
            if (type == null) return false
            if (!(parameter.type.classifier as KClass<*>).isInstance(arguments[index])) return false
        }

        return true
    }
}

