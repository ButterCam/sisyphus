package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import com.bybutter.sisyphus.reflect.resolveArguments
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KFunction
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

open class FilterRuntime(private val std: FilterStandardLibrary = FilterStandardLibrary()) {
    private val memberFunctions = mutableMapOf<String, MutableList<KFunction<*>>>()

    init {
        for (memberFunction in std.javaClass.kotlin.memberFunctions) {
            if (memberFunction.javaMethod?.canAccess(std) != true) continue
            memberFunctions.getOrPut(memberFunction.name) { mutableListOf() } += memberFunction
        }
    }

    fun invoke(
        function: String,
        arguments: List<Any?>,
    ): Any? {
        return invokeOrDefault(function, arguments) {
            throw NoSuchMethodException(
                "Can't find function '$function(${
                    arguments.joinToString(
                        ", ",
                    ) { it?.javaClass?.canonicalName ?: "null" }
                })' in filter standard library.",
            )
        }
    }

    fun invokeOrDefault(
        function: String,
        arguments: List<Any?>,
        block: () -> Any?,
    ): Any? {
        val (func, args) =
            memberFunctions[function]?.firstNotNullOfOrNull { func ->
                func.resolveArguments(arguments)?.let {
                    func to it
                }
            } ?: return block()

        return try {
            if (func.instanceParameter == null) {
                func.callBy(args)
            } else {
                func.callBy(
                    args.toMutableMap().apply {
                        this[func.instanceParameter!!] = std
                    },
                )
            }
        } catch (ex: InvocationTargetException) {
            throw ex.cause ?: ex
        } catch (e: Exception) {
            throw e
        }
    }
}
