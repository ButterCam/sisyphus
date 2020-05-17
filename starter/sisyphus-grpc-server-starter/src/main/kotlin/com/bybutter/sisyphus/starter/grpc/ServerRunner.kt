package com.bybutter.sisyphus.starter.grpc

import io.grpc.Server
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ServerRunner : ApplicationRunner {
    companion object {
        private val logger = LoggerFactory.getLogger(ServerRunner::class.java)
    }

    @Autowired
    private lateinit var grpcServer: Server

    override fun run(args: ApplicationArguments?) {
        grpcServer.start().also {
            logger.info("Running grpc server via netty on port ${it.port}")
        }.awaitTermination()
    }
}
