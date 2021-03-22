package com.bybutter.sisyphus.test.test.api

import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.test.test.EchoRequest
import com.bybutter.sisyphus.test.test.EchoResponse
import com.bybutter.sisyphus.test.test.TestApi

@RpcServiceImpl
class TestApiImpl : TestApi() {
    override suspend fun echo(input: EchoRequest): EchoResponse {
        return EchoResponse {
            message = "echo: ${input.message}"
        }
    }
}
