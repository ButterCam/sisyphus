package com.bybutter.sisyphus.starter.grpc.test

import com.bybutter.sisyphus.middleware.grpc.RpcServiceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest

@SpringBootApplication
@SpringBootTest
class RegisterTest {
    @Test
    fun `test client proxy by service autowiring`(@Autowired client: RpcService.Client) {
        runBlocking {
            client.sayHello(
                Request {
                    value = 1
                }
            )

            client.sayHelloStream(
                Request {
                    value = 2
                }
            ).collect {
                println("receive: ${it.value}")
            }

            val result2 = client.sayHelloStream2(
                flow {
                    emit(
                        Request {
                            value = 3
                        }
                    )
                    emit(
                        Request {
                            value = 4
                        }
                    )
                }
            )

            client.sayHelloStream3(
                flow {
                    emit(
                        Request {
                            value = 5
                        }
                    )
                    emit(
                        Request {
                            value = 6
                        }
                    )
                }
            ).collect {
                println("receive: ${it.value}")
            }
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

    override fun sayHelloStream(input: Request): Flow<Response> = flow {
        emit(
            Response {
                value = "test1"
            }
        )
        emit(
            Response {
                value = "test2"
            }
        )
    }

    override suspend fun sayHelloStream2(input: Flow<Request>): Response {
        input.collect {
            println("receive: ${it.value}")
        }

        return Response {
            value = "test"
        }
    }

    override fun sayHelloStream3(input: Flow<Request>): Flow<Response> = flow {
        input.collect {
            emit(
                Response {
                    value = "receive: ${it.value}"
                }
            )
        }
    }
}
