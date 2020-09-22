package com.bybutter.middleware.distributed.lock.autoconfigure.annotation

import java.lang.annotation.Documented
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Documented
@Inherited
annotation class SisyphusDistributedLock(
    /**
     * A field of the first parameter on the method
     */
    val rKeyParam: String = "",
    /**
     * A field of the first parameter on the method
     */
    val rValueParam: String = "",
    val leaseTime: Long = 5000L,
    val leaseRenewTime: Long = 5000L,
    val threshold: Long = 2000L,
    val leaseRenewalNumber: Int = 3,
    val enableWatchDog: Boolean = false
)
