package com.bybutter.sisyphus.middleware.grpc

import io.grpc.ManagedChannel
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import org.springframework.context.SmartLifecycle

class ManagedChannelLifecycle(private val channel: ManagedChannel) : SmartLifecycle {
    override fun isRunning(): Boolean = true

    override fun start() {
    }

    override fun stop() {
        channel.shutdownNow()
    }

    override fun stop(callback: Runnable) {
        channel.shutdown()
        thread(name = "grpc-channel-shutdown-${channel.authority()}") {
            channel.awaitTermination(30, TimeUnit.SECONDS)
            callback.run()
        }
    }
}
