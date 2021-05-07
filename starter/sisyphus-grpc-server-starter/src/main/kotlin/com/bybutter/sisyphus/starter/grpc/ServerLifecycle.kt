package com.bybutter.sisyphus.starter.grpc

import io.grpc.Server
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.Shutdown
import org.springframework.context.SmartLifecycle
import kotlin.concurrent.thread

class ServerLifecycle(private val shutdown: Shutdown, private vararg val servers: Server) : SmartLifecycle {
    private var running = false

    override fun getPhase(): Int {
        return super.getPhase() - 10
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun start() {
        servers.forEach {
            it.start()
        }
        running = true
        logger.info("Running gRPC server via netty on port: ${servers.firstOrNull()?.port}")
    }

    override fun stop() {
        servers.forEach {
            it.shutdownNow()
        }
    }

    override fun stop(callback: Runnable) {
        when (shutdown) {
            Shutdown.GRACEFUL -> {
                logger.info("Commencing graceful shutdown for gRPC server. Waiting for active requests to complete")
                servers.forEach {
                    it.shutdown()
                }
                thread(name = "grpc-shutdown") {
                    servers.forEach {
                        it.awaitTermination()
                    }
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
