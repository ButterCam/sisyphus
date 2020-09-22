package com.bybutter.middleware.distributed.lock.autoconfigure.annotation

import java.lang.annotation.Documented
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
@Inherited
annotation class SisyphusDistributedLock(
    /**
     * 方法上第一个参数的某一个字段
     */
    val rKeyParam: String = "",
    /**
     * 方法上第一个参数的某一个字段
     */
    val rValueParam: String = "",
    val leaseTime: Long = 5000L,
    val leaseRenewTime: Long = 5000L,
    val threshold: Long = 2000L,
    val leaseRenewalNumber: Int = 3,
    val enableWatchDog: Boolean = false
)
