package com.bybutter.middleware.distributed.lock

interface DistributedLock {
    fun tryLock(): Boolean
    fun lock()
    fun unLock()
}
