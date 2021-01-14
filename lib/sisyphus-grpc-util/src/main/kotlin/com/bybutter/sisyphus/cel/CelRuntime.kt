package com.bybutter.sisyphus.cel

import com.bybutter.sisyphus.cel.grammar.CelParser
import com.bybutter.sisyphus.protobuf.CustomProtoType
import com.bybutter.sisyphus.protobuf.CustomProtoTypeSupport
import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.ProtoTypes
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod

open class CelRuntime(val macro: CelMacro = CelMacro(), val std: CelStandardLibrary = CelStandardLibrary()) {
    private val memberFunctions = mutableMapOf<String, MutableList<KFunction<*>>>()

    private val macroFunctions = mutableMapOf<String, MutableList<KFunction<*>>>()

    init {
        for (memberFunction in std.javaClass.kotlin.memberFunctions) {
            if (memberFunction.javaMethod?.canAccess(std) != true) continue
            memberFunctions.getOrPut(memberFunction.name) { mutableListOf() } += memberFunction
        }

        for (memberFunction in std.javaClass.kotlin.memberExtensionFunctions) {
            if (memberFunction.javaMethod?.canAccess(std) != true) continue
            memberFunctions.getOrPut(memberFunction.name) { mutableListOf() } += memberFunction
        }

        for (macroFunction in macro.javaClass.kotlin.memberFunctions) {
            if (macroFunction.javaMethod?.canAccess(macro) != true) continue
            macroFunctions.getOrPut(macroFunction.name) { mutableListOf() } += macroFunction
        }

        for (macroFunction in macro.javaClass.kotlin.memberExtensionFunctions) {
            if (macroFunction.javaMethod?.canAccess(macro) != true) continue
            macroFunctions.getOrPut(macroFunction.name) { mutableListOf() } += macroFunction
        }
    }

    private fun KFunction<*>.macroCompatibleWith(th: Any?, arguments: List<CelParser.ExprContext>): Boolean {
        val thType = this.extensionReceiverParameter?.type?.classifier as? KClass<*>
        if (th == null && thType == null) return macroCompatibleWith(arguments)
        if (th == null || thType == null) return false
        if (!thType.isInstance(th)) return false
        return macroCompatibleWith(arguments)
    }

    private fun KFunction<*>.macroCompatibleWith(arguments: List<CelParser.ExprContext>): Boolean {
        var usedExpr = 0
        var hasVar = false
        val parameters = valueParameters

        loop@ for ((index, parameter) in parameters.withIndex()) {
            val valueKClass = parameter.type.classifier as KClass<*>
            when (valueKClass) {
                CelContext::class -> {
                    if (parameter == parameters.first()) continue@loop
                    return false
                }
                CelParser.ExprContext::class -> {
                    usedExpr++
                    if (usedExpr > arguments.size) return false
                }
                Array<CelParser.ExprContext>::class -> {
                    if (parameter.isVararg) {
                        hasVar = true
                        continue@loop
                    }
                    return false
                }
                else -> return false
            }
        }

        if (usedExpr == arguments.size) return true
        if (hasVar && usedExpr < arguments.size) return true
        return false
    }

    private fun KFunction<*>.compatibleWith(th: Any?, arguments: List<Any?>): Boolean {
        val thType = this.extensionReceiverParameter?.type?.classifier as? KClass<*>
        if (th == null && thType == null) return compatibleWith(listOfNotNull(this.extensionReceiverParameter) + this.valueParameters, arguments)
        if (th == null || thType == null) return false
        if (!thType.isInstance(th)) return false
        return compatibleWith(valueParameters, arguments)
    }

    private fun KFunction<*>.compatibleWith(arguments: List<Any?>): Boolean {
        return compatibleWith(listOfNotNull(this.extensionReceiverParameter) + this.valueParameters, arguments)
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

    fun getGlobalField(key: String, global: Map<String, Any?>): Any? {
        return when (key) {
            "int" -> "int"
            "uint" -> "uint"
            "double" -> "double"
            "bool" -> "bool"
            "string" -> "string"
            "bytes" -> "bytes"
            "list" -> "list"
            "map" -> "map"
            "null_type" -> "null_type"
            else -> {
                if (key.startsWith(".")) {
                    ProtoTypes.getSupportByProtoName(key)?.let { return ".${it.fullName}" }
                }
                return global[key] ?: throw NoSuchFieldException("No such field named '$key' in CEL global space.")
            }
        }
    }

    fun invoke(th: Any?, function: String, vararg arguments: Any?): Any? {
        return invoke(th, function, arguments.toList())
    }

    fun invoke(th: Any?, function: String, arguments: List<Any?>): Any? {
        return if (th == null) {
            val func = memberFunctions[function]?.firstOrNull {
                it.compatibleWith(arguments)
            } ?: throw NoSuchMethodException(
                "Can't find method '$function(${arguments.joinToString(", ") { it?.javaClass?.canonicalName ?: "null" }})' in CEL standard library."
            )
            func.call(std, *arguments.toTypedArray())
        } else {
            val func = memberFunctions[function]?.firstOrNull {
                it.compatibleWith(th, arguments)
            } ?: throw NoSuchMethodException(
                "Can't find method '${th.javaClass.canonicalName}.$function(${arguments.joinToString(", ") { it?.javaClass?.canonicalName ?: "null" }})' in CEL standard library."
            )
            func.call(std, th, *arguments.toTypedArray())
        }
    }

    @OptIn(InternalProtoApi::class)
    fun createMessage(type: String, initializer: Map<String, Any?>): Any {
        val messageSupport = ProtoTypes.ensureSupportByProtoName(type)
        return messageSupport.newMutable().apply {
            for ((key, value) in initializer) {
                value ?: continue
                val property = getProperty(key)
                    ?: throw IllegalStateException("Message type '$type' has not field '$key'.")
                this[key] = if (property.returnType.isSubtypeOf(CustomProtoType::class.starProjectedType)) {
                    val support = (property.returnType.classifier as KClass<*>).companionObjectInstance as CustomProtoTypeSupport<CustomProtoType<Any?>, Any?>
                    support.wrapRaw(value)
                } else {
                    value
                }
            }
        }
    }

    fun findMarcoFunction(th: Any?, function: String?, arguments: List<CelParser.ExprContext>): KFunction<*>? {
        return if (th == null) {
            macroFunctions[function]?.firstOrNull {
                it.macroCompatibleWith(arguments)
            }
        } else {
            macroFunctions[function]?.firstOrNull {
                it.macroCompatibleWith(th, arguments)
            }
        }
    }

    inline fun invokeMarco(context: CelContext, th: Any?, function: String?, arguments: List<CelParser.ExprContext>, block: (Any?) -> Unit) {
        val function = findMarcoFunction(th, function, arguments) ?: return
        val result = if (th == null) {
            function.call(macro, context, *arguments.toTypedArray())
        } else {
            function.call(macro, th, context, *arguments.toTypedArray())
        }
        block(result)
    }

    companion object {
        internal val idRegex = """^[_a-zA-Z][_a-zA-Z0-9]*$""".toRegex()

        internal val memberRegex = """^\.?[_a-zA-Z][_a-zA-Z0-9]*(?:\.[_a-zA-Z][_a-zA-Z0-9]*)*$""".toRegex()
    }
}
