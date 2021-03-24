package com.bybutter.sisyphus.starter.grpc

import io.grpc.Server
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.Shutdown
import org.springframework.context.SmartLifecycle
import kotlin.concurrent.thread

class ServerLifecycle(private val server: Server, private val shutdown: Shutdown) : SmartLifecycle {
    private var running = false

    override fun getPhase(): Int {
        return super.getPhase() - 10
    }

    override fun isRunning(): Boolean {
        if (server.isTerminated) return false
        if (server.isShutdown) return false
        return running
    }

    override fun start() {
        server.start()
        running = true
        logger.info("Running gRPC server via netty on port: ${server.port}")
    }

    override fun stop() {
        server.shutdownNow()
    }

    override fun stop(callback: Runnable) {
        when (shutdown) {
            Shutdown.GRACEFUL -> {
                logger.info("Commencing graceful shutdown for gRPC server. Waiting for active requests to complete")

                server.shutdown()
                thread(name = "grpc-shutdown") {
                    server.awaitTermination()
                    logger.info("Graceful shutdown complete for gRPC server.")
                    callback.run()
                }
            }
            else -> {
                stop()
                callback.run()
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServerLifecycle::class.java)
    }
}
