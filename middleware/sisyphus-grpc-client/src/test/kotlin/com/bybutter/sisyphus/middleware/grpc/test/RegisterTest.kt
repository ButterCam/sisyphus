package com.bybutter.sisyphus.middleware.grpc.test

import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import com.bybutter.sisyphus.middleware.grpc.SisyphusGrpcClientAutoConfiguration
import com.bybutter.sisyphus.middleware.grpc.test.Request
import com.bybutter.sisyphus.middleware.grpc.test.Response
import com.bybutter.sisyphus.middleware.grpc.test.RpcService
import com.bybutter.sisyphus.rpc.GrpcContextCoroutineContextElement
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [SisyphusGrpcClientAutoConfiguration::class, ServiceImpl::class])
class RegisterTest {
    @Test
    fun `test client proxy by service autowiring`(@Autowired client: RpcService.Client) {
        runBlocking {
            client.sayHello(Request {
                value = 1
            })

            val result = client.sayHelloStream(Request {
                value = 2
            })
            for (out in result) {
                println("receive: ${out.value}")
            }

            val hello2 = client.sayHelloStream2()
            hello2.send(Request {
                value = 3
            })
            hello2.send(Request {
                value = 4
            })
            hello2.close()
            val result2 = hello2.await()

            val hello3 = client.sayHelloStream3()
            hello3.send(Request {
                value = 5
            })
            hello3.send(Request {
                value = 6
            })
            hello3.close()
            for (out in hello3) {
                println("receive: ${out.value}")
            }
            result2
        }
    }
}

@RpcServiceImpl
class ServiceImpl : RpcService() {
    override suspend fun sayHello(input: Request): Response {
        return Response {
            value = "test"
        }
    }

    override fun sayHelloStream(input: Request): ReceiveChannel<Response> = GlobalScope.produce<Response>(GrpcContextCoroutineContextElement()) {
        send(Response {
            value = "test1"
        })
        send(Response {
            value = "test2"
        })
    }

    override suspend fun sayHelloStream2(input: ReceiveChannel<Request>): Response {
        for (request in input) {
            println("receive: ${request.value}")
        }

        return Response {
            value = "test"
        }
    }

    override fun sayHelloStream3(input: ReceiveChannel<Request>): ReceiveChannel<Response> = GlobalScope.produce<Response>(GrpcContextCoroutineContextElement()) {
        for (request in input) {
            send(Response {
                value = "receive: ${request.value}"
            })
        }
    }
}
