package com.bybutter.sisyphus.middleware.jdbc.support.proto.filter

import java.lang.reflect.InvocationTargetException
import kotlin.jvm.functions.FunctionN
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.ExperimentalReflectionOnLambdas
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.reflect

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

@OptIn(ExperimentalReflectionOnLambdas::class)
class LambdaFunction<R>(private val function: Function<R>) : KFunction<R> by function.reflect()!!, FunctionN<R> {
    override val arity: Int
        get() = (function as kotlin.jvm.internal.FunctionBase<R>).arity

    override fun invoke(vararg args: Any?): R {
        return when (args.size) {
            0 -> (function as Function0<R>).invoke()
            1 -> (function as Function1<Any?, R>).invoke(args[0])
            2 -> (function as Function2<Any?, Any?, R>).invoke(args[0], args[1])
            3 -> (function as Function3<Any?, Any?, Any?, R>).invoke(args[0], args[1], args[2])
            4 -> (function as Function4<Any?, Any?, Any?, Any?, R>).invoke(args[0], args[1], args[2], args[3])
            5 -> (function as Function5<Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4]
            )
            6 -> (function as Function6<Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5]
            )
            7 -> (function as Function7<Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6]
            )
            8 -> (function as Function8<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7]
            )
            9 -> (function as Function9<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8]
            )
            10 -> (function as Function10<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9]
            )
            11 -> (function as Function11<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10]
            )
            12 -> (function as Function12<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11]
            )
            13 -> (function as Function13<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12]
            )
            14 -> (function as Function14<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13]
            )
            15 -> (function as Function15<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14]
            )
            16 -> (function as Function16<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15]
            )
            17 -> (function as Function17<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16]
            )
            18 -> (function as Function18<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16],
                args[17]
            )
            19 -> (function as Function19<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16],
                args[17],
                args[18]
            )
            20 -> (function as Function20<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16],
                args[17],
                args[18],
                args[19]
            )
            21 -> (function as Function21<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16],
                args[17],
                args[18],
                args[19],
                args[20]
            )
            22 -> (function as Function22<Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, Any?, R>).invoke(
                args[0],
                args[1],
                args[2],
                args[3],
                args[4],
                args[5],
                args[6],
                args[7],
                args[8],
                args[9],
                args[10],
                args[11],
                args[12],
                args[13],
                args[14],
                args[15],
                args[16],
                args[17],
                args[18],
                args[19],
                args[20],
                args[21]
            )
            else -> (function as FunctionN<R>).invoke(*args)
        }
    }
}