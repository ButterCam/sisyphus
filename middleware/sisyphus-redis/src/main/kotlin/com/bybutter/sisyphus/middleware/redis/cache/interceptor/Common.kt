package com.bybutter.sisyphus.middleware.redis.cache.interceptor

import com.bybutter.sisyphus.middleware.redis.cache.toCRC32
import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.ByteArrayCodec
import java.lang.reflect.Method
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.util.ByteUtils
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

object KeyGenerator {

    private val log = LoggerFactory.getLogger(this.javaClass)
    fun generateNormalKey(
        invocation: MethodInvocation,
        key: String,
        method: Method,
        evaluationContextInterceptorList: List<EvaluationContextInterceptor>
    ): Any? {
        val arguments = invocation.arguments
        return if (key.isNotEmpty()) {
            if (key.contains("#")) {
                try {
                    spELParser(arguments, key, invocation.method)?.toString()
                            ?: key
                } catch (e: Exception) {
                    log.error("Normal key spEL parse error.")
                    null
                }
            } else {
                key
            }
        } else {
            normalParamsHandle(arguments)?.toString()
        }
    }

    fun generateGlobalKey(
        invocation: MethodInvocation,
        key: String,
        method: Method,
        remCount: Int,
        evaluationContextInterceptorList: List<EvaluationContextInterceptor>
    ): String? {
        val key =
            generateNormalKey(invocation, key, method, evaluationContextInterceptorList)?.toString() ?: return null
        return key.toCRC32().rem(remCount).toString()
    }

    private fun normalParamsHandle(arguments: Array<Any>): Any? {
        if (arguments.isEmpty()) {
            log.error("Arguments is empty.")
            return null
        }
        if (arguments.size == 1) {
            val param: Any = arguments[0]
            if (param.javaClass.isPrimitive) {
                return param
            }
        }
        return arguments.contentDeepHashCode()
    }

    fun generateBatchKey(
        invocation: MethodInvocation,
        key: String,
        method: Method,
        evaluationContextInterceptorList: List<EvaluationContextInterceptor>
    ): List<*>? {
        val arguments = invocation.arguments
        return when {
            // 方法参数空的
            arguments.isEmpty() -> {
                log.error("Arguments is empty.")
                null
            }
            // key存在且是spEL表达式
            (key.isNotEmpty() && key.contains("#")) -> {
                try {
                    spELParser(arguments, key, invocation.method) as List<*>
                } catch (e: Exception) {
                    log.error("Batch key spEL parse error.")
                    null
                }
            }
            // 有key，但不是spEL表达式（不是一个List）或者无key情况
            else -> batchParamsHandle(arguments)
        }
    }

    private fun batchParamsHandle(arguments: Array<Any>): List<*>? {
        return if (!Collection::class.java.isAssignableFrom(arguments[0].javaClass)) {
            log.error("The first argument is not collection.")
            null
        } else {
            arguments[0] as List<*>
        }
    }

    private fun spELParser(
        arguments: Array<Any>,
        spELString: String,
        method: Method,
        evaluationContextInterceptorList: List<EvaluationContextInterceptor>
    ): Any? {
        // SpEL解析器
        val parser = SpelExpressionParser()
        // 用于获取方法参数定义名字
        val nameDiscoverer = DefaultParameterNameDiscoverer()
        val paramNames = nameDiscoverer.getParameterNames(method)
        val expression: Expression = parser.parseExpression(spELString)
        val context: EvaluationContext = StandardEvaluationContext()
        for (i in arguments.indices) {
            context.setVariable(paramNames[i], arguments[i])
        }
        return expression.getValue(context)
    }

    private fun byteArrayKey(key: String?): ByteArray? {
        return key?.let { ByteUtils.getBytes(keySerializationPair.write(it)) }
    }
}

object RedisConnection {

    fun getCurrent(applicationContext: ApplicationContext, qualifier: String): StatefulRedisConnection<ByteArray, ByteArray>? {
        val clients = applicationContext.getBeansOfType(RedisClient::class.java)
        if (clients.size == 1 || qualifier.isEmpty()) return clients.values.firstOrNull()?.connect(ByteArrayCodec())
        for ((key, client) in clients) {
            val clientBeanDefinition = (applicationContext as GenericApplicationContext).getBeanDefinition(key) as AbstractBeanDefinition
            if (clientBeanDefinition.hasQualifier(qualifier)) {
                return client.connect(ByteArrayCodec())
            }
        }
        return null
    }
}

object MultiRedisSerializer {
    fun deserializeCacheValue(value: ByteArray?, redisSerializer: RedisSerializer<Any>?): Any? {
        value ?: return null
        val valueSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer
                ?: RedisSerializer.json())
        return valueSerializationPair.read(ByteBuffer.wrap(value))
    }

    fun serializeCacheValue(value: Any?, redisSerializer: RedisSerializer<Any>?): ByteArray? {
        value ?: return null
        val valueSerializationPair = RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer
                ?: RedisSerializer.json())
        return ByteUtils.getBytes(valueSerializationPair.write(value))
    }
}
