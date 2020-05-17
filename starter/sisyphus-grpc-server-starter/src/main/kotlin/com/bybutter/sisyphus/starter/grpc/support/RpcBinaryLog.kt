package com.bybutter.sisyphus.starter.grpc.support

import io.grpc.BinaryLog
import io.grpc.Channel
import io.grpc.ServerMethodDefinition
import org.slf4j.LoggerFactory

class RpcBinaryLog : BinaryLog() {
    companion object {
        private val logger = LoggerFactory.getLogger(RpcBinaryLog::class.java)
    }

    override fun <ReqT : Any, RespT : Any> wrapMethodDefinition(oMethodDef: ServerMethodDefinition<ReqT, RespT>): ServerMethodDefinition<*, *> {
        return oMethodDef
    }

    override fun wrapChannel(channel: Channel): Channel {
        return channel
    }

    override fun close() {
    }
}
