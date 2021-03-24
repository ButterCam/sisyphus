package com.bybutter.sisyphus.middleware.grpc

import io.grpc.ManagedChannel
import org.springframework.context.SmartLifecycle
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ManagedChannelLifecycle : SmartLifecycle {
    private val channelList: MutableSet<ManagedChannel> = mutableSetOf()

    private var running = false

    fun registerManagedChannel(channel: ManagedChannel) {
        channelList += channel
    }

    override fun isRunning(): Boolean = running

    override fun start() {
        running = true
    }

    override fun stop() {
        running = false
        for (channel in channelList) {
            channel.shutdownNow()
        }
        channelList.clear()
    }

    override fun stop(callback: Runnable) {
        running = false
        for (channel in channelList) {
            channel.shutdown()
        }
        thread(name = "grpc-channel-shutdown") {
            for (channel in channelList) {
                channel.awaitTermination(30, TimeUnit.SECONDS)
            }
            channelList.clear()
            callback.run()
        }
    }
}
