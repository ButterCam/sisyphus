package com.bybutter.sisyphus.middleware.redis.util

import io.netty.util.HashedWheelTimer
import io.netty.util.Timer
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit

object DistributedLock {
    fun <T> lock(leaseTime: Long, action: () -> T): T? {
        return try {
            if () {
                action()
            } else {
                null
            }

        } finally {
            unlock()
        }
    }

    fun renewExpiration(){
        val timer = HashedWheelTimer()
        timer.newTimeout({},30,TimeUnit.MILLISECONDS)
    }

    fun <T> tryLock(waitTime: Long, leaseTime: Long, action: () -> T) {

    }

    private fun unlock() {
        TODO("Not yet implemented")
    }
}

fun main() {
    DistributedLock.lock(30) {

    }
}

