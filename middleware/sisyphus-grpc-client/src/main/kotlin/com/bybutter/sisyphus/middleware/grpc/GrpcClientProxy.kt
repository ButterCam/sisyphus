package com.bybutter.sisyphus.middleware.grpc

import com.bybutter.sisyphus.rpc.GrpcContextCoroutineContextElement
import com.bybutter.sisyphus.rpc.ManyToManyCall
import com.bybutter.sisyphus.rpc.ManyToOneCall
import com.bybutter.sisyphus.rpc.RpcMethod
import com.bybutter.sisyphus.rpc.asStreamObserver
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.core.annotation.AnnotationUtils

fun <T> Any.clientProxy(target: Class<*>): T {
    return Proxy.newProxyInstance(target.classLoader, arrayOf(target), GrpcClientProxy(target, this)) as T
}

class GrpcClientProxy(private val target: Class<*>, private val service: Any) : InvocationHandler {
    private val methodMapping: Map<Method, MethodMapping>

    init {
        val serviceMethods = service.javaClass.methods.mapNotNull {
            val info = AnnotationUtils.findAnnotation(it, RpcMethod::class.java) ?: return@mapNotNull null
            info to it
        }.associate { it.first.name to it.second }

        methodMapping = target.methods.mapNotNull {
            val info = AnnotationUtils.findAnnotation(it, RpcMethod::class.java) ?: return@mapNotNull null
            val serviceMethod = serviceMethods[info.name] ?: return@mapNotNull null
            MethodMapping(it, serviceMethod, info)
        }.associateBy { it.clientMethod }
    }

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
        val args = args ?: arrayOf()
        val mapping = methodMapping[method] ?: return method.invoke(this, *args)

        return when {
            mapping.methodInfo.input.streaming && mapping.methodInfo.output.streaming -> {
                invokeBiStreaming(mapping)
            }
            mapping.methodInfo.input.streaming -> {
                invokeClientStreaming(mapping)
            }
            mapping.methodInfo.output.streaming -> {
                invokeDirectly(mapping, args)
            }
            else -> {
                invokeDirectly(mapping, args)
            }
        }
    }

    private fun invokeDirectly(mapping: MethodMapping, args: Array<out Any>): Any {
        return mapping.serviceMethod.invoke(service, *args)
    }

    private fun invokeClientStreaming(mapping: MethodMapping): ManyToOneCall<*, *> {
        val defer = CompletableDeferred<Any?>()
        val input = Channel<Any>(Channel.UNLIMITED)

        GlobalScope.launch(GrpcContextCoroutineContextElement()) {
            val result = mapping.serviceMethod.kotlinFunction?.callSuspend(service, input)
            defer.complete(result)
        }

        return ManyToOneCall(input.asStreamObserver(), defer)
    }

    private fun invokeBiStreaming(mapping: MethodMapping): ManyToManyCall<*, *> {
        val input = Channel<Any>(Channel.UNLIMITED)
        val output = mapping.serviceMethod.invoke(service, input) as Channel<*>

        return ManyToManyCall(input.asStreamObserver(), output)
    }
}

private data class MethodMapping(val clientMethod: Method, val serviceMethod: Method, val methodInfo: RpcMethod)
