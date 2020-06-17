package com.bybutter.sisyphus.middleware.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientInterceptor
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.AbstractStub
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.AbstractBeanDefinition

interface ClientRepository {

    fun listClientBeanDefinition(beanFactory: ConfigurableListableBeanFactory): List<AbstractBeanDefinition>

    fun processStub(stub: AbstractStub<*>, beanFactory: ConfigurableListableBeanFactory): AbstractStub<*> {
        var result = stub

        val builderInterceptors = beanFactory.getBeansOfType(ClientBuilderInterceptor::class.java)
        for ((_, builderInterceptor) in builderInterceptors) {
            result = builderInterceptor.intercept(result)
        }

        val interceptors = beanFactory.getBeansOfType(ClientInterceptor::class.java)
        return result.withInterceptors(*interceptors.values.toTypedArray())
    }

    fun createGrpcChannel(property: GrpcChannelProperty): Channel {
        return ManagedChannelBuilder.forTarget(property.target).usePlaintext().userAgent("Generated by Sisyphus").build()
    }

    fun createGrpcChannel(name: String, port: Int): Channel {
        return ManagedChannelBuilder.forAddress(name, port).usePlaintext().userAgent("Generated by Sisyphus").build()
    }

    fun createGrpcClient(target: Class<*>, channel: Channel, property: GrpcChannelProperty): AbstractStub<*> {
        return target.getDeclaredConstructor(Channel::class.java, CallOptions::class.java)
                .newInstance(channel, property.options) as AbstractStub<*>
    }

    fun createGrpcClient(target: Class<*>, channel: Channel): AbstractStub<*> {
        return target.getDeclaredConstructor(Channel::class.java, CallOptions::class.java)
                .newInstance(channel, CallOptions.DEFAULT) as AbstractStub<*>
    }
}