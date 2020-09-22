package com.bybutter.middleware.distributed.lock.autoconfigure.annotation.support

import com.bybutter.middleware.distributed.lock.RedisDistributedLock
import com.bybutter.middleware.distributed.lock.RedisLockProperty
import com.bybutter.middleware.distributed.lock.autoconfigure.annotation.SisyphusDistributedLock
import com.bybutter.sisyphus.jackson.Json
import com.bybutter.sisyphus.jackson.toJson
import io.lettuce.core.api.StatefulRedisConnection
import java.util.UUID
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
class SisyphusLockSupport {

    @Autowired
    private lateinit var beanFactory: ConfigurableListableBeanFactory

    @Autowired(required = false)
    private lateinit var redisLockProperty: RedisLockProperty

    @Pointcut("@annotation(sisyphusDistributedLock)")
    fun sisyphusLockPointCut(sisyphusDistributedLock: SisyphusDistributedLock) {
    }

    @Around("sisyphusLockPointCut(sisyphusDistributedLock)")
    fun around(joinPoint: ProceedingJoinPoint, sisyphusDistributedLock: SisyphusDistributedLock): Any? {
        val args = joinPoint.args
        val key: String
        val value: String
        if (args == null || args.isEmpty() || sisyphusDistributedLock.rKeyParam == "") {
            key = joinPoint.signature.name
            value = UUID.randomUUID().toString() + System.currentTimeMillis()
        } else {
            key = Json.deserialize(args[0].toJson()).get(sisyphusDistributedLock.rKeyParam).textValue()
            value = Json.deserialize(args[0].toJson()).get(sisyphusDistributedLock.rValueParam).textValue()
        }
        var statefulRedisConnection: StatefulRedisConnection<String, String>? = null
        val beanNamesForType = beanFactory.getBeanNamesForType(StatefulRedisConnection::class.java)
        loop@ for (it in beanNamesForType) {
            val abstractBeanDefinition = beanFactory.getBeanDefinition(it) as AbstractBeanDefinition
            for (qualifier in abstractBeanDefinition.qualifiers) {
                if (qualifier.typeName == redisLockProperty.redisQualifier.typeName) {
                    statefulRedisConnection = beanFactory.getBean(it) as StatefulRedisConnection<String, String>
                    break@loop
                }
            }
        }
        if (statefulRedisConnection == null) {
            throw NullPointerException("stringRedisTemplate is not be null.")
        }
        val redisDistributedLock = RedisDistributedLock(
                statefulRedisConnection,
                key,
                value,
                sisyphusDistributedLock.leaseTime,
                sisyphusDistributedLock.enableWatchDog,
                sisyphusDistributedLock.threshold,
                sisyphusDistributedLock.leaseRenewTime,
                sisyphusDistributedLock.leaseRenewalNumber
                )
        if (redisDistributedLock.tryLock(10, TimeUnit.MILLISECONDS)) {
            try {
                return joinPoint.proceed()
            } finally {
                redisDistributedLock.unLock()
            }
        } else {
            throw RuntimeException("locked failed.")
        }
    }
}
