package com.bybutter.sisyphus.middleware.redis.cache.interceptor.common

import com.bybutter.sisyphus.middleware.redis.cache.interceptor.EvaluationContextInterceptor
import com.bybutter.sisyphus.middleware.redis.cache.toCRC32
import java.lang.reflect.Method
import org.aopalliance.intercept.MethodInvocation
import org.slf4j.LoggerFactory
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

object KeyGenerator {
    private val log = LoggerFactory.getLogger(this.javaClass)
    fun generateNormalKey(invocation: MethodInvocation, key: String, method: Method, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): Any? {
        val arguments = invocation.arguments
        return if (key.isNotEmpty()) {
            if (key.contains("#")) {
                try {
                    spELParser(arguments, key, method, evaluationContextInterceptorList) ?: key
                } catch (e: Exception) {
                    log.error("Normal key spEL parse error.")
                    null
                }
            } else {
                key
            }
        } else {
            normalParamsHandle(arguments)
        }
    }

    fun generateGlobalKey(invocation: MethodInvocation, key: String, method: Method, remCount: Int, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): String? {
        val key = generateNormalKey(invocation, key, method, evaluationContextInterceptorList)?.toString() ?: return null
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

    fun generateBatchKey(invocation: MethodInvocation, key: String, method: Method, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): List<*>? {
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
                    spELParser(arguments, key, method, evaluationContextInterceptorList) as List<*>
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

    private fun spELParser(arguments: Array<Any>, spELString: String, method: Method, evaluationContextInterceptorList: List<EvaluationContextInterceptor>): Any? {
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
        evaluationContextInterceptorList.forEach {
            it.intercept(context)
        }
        return expression.getValue(context)
    }
}
