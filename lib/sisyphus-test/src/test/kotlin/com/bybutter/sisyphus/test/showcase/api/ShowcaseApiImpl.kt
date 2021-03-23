package com.bybutter.sisyphus.test.showcase.api

import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.test.showcase.EchoRequest
import com.bybutter.sisyphus.test.showcase.EchoResponse
import com.bybutter.sisyphus.test.showcase.ShowcaseApi

@RpcServiceImpl
class ShowcaseApiImpl : ShowcaseApi() {
    override suspend fun echo(input: EchoRequest): EchoResponse {
        return EchoResponse {
            message = "echo: ${input.message}"
        }
    }
}
