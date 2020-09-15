package com.bybutter.sisyphus.middleware.grpc

import com.bybutter.sisyphus.middleware.grpc.autoconfigure.ClientRegistrar
import com.bybutter.sisyphus.rpc.CallOptionsInterceptor
import com.bybutter.sisyphus.rpc.SisyphusStub
import com.bybutter.sisyphus.spi.Ordered
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.AbstractStub
import kotlin.reflect.full.companionObject
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.core.env.Environment

interface ClientRepository : Ordered {
    fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory, environment: Environment): List<AbstractBeanDefinition>

    fun getStubFromService(service: Class<*>): Class<*> {
        return service.kotlin.companionObject?.java?.classes?.firstOrNull {
            it.simpleName == "Stub"
        } ?: throw IllegalStateException("Grpc service must have stub class in companion.")
    }

    fun getClientFromService(service: Class<*>): Class<*> {
        return service.declaredClasses.firstOrNull { it.simpleName == "Client" }
                ?: throw IllegalStateException("Grpc service must have nested class named 'Client'.")
    }

    fun interceptStub(stub: AbstractStub<*>, builderInterceptors: Iterable<ClientBuilderInterceptor>, interceptors: Iterable<ClientInterceptor>): AbstractStub<*> {
        var result = stub

        for (interceptor in builderInterceptors) {
            result = interceptor.intercept(result)
        }

        return result.withInterceptors(*interceptors.toList().toTypedArray())
    }

    fun createGrpcChannel(target: String, beanFactory: ConfigurableListableBeanFactory): Channel {
        return ManagedChannelBuilder.forTarget(target).usePlaintext().userAgent("Generated by Sisyphus").build().apply {
            val channelLifecycleManager = beanFactory.getBean(ClientRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_CHANNEL_LIFECYCLE) as ManagedChannelLifecycle
            channelLifecycleManager.registerManagedChannel(this)
        }
    }

    fun createGrpcClient(target: Class<*>, channel: Channel, optionsInterceptors: Iterable<CallOptionsInterceptor>, callOptions: CallOptions): AbstractStub<*> {
        if (SisyphusStub::class.java.isAssignableFrom(target)) {
            return target.getDeclaredConstructor(Channel::class.java, Iterable::class.java, CallOptions::class.java)
                    .newInstance(channel, optionsInterceptors, callOptions) as AbstractStub<*>
        } else {
            return target.getDeclaredConstructor(Channel::class.java, CallOptions::class.java)
                    .newInstance(channel, callOptions) as AbstractStub<*>
        }
    }
}
