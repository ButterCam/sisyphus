package com.bybutter.sisyphus.test.descriptor

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.TestResult
import com.bybutter.sisyphus.test.TestStep
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ClientInterceptors
import io.grpc.ForwardingClientCall
import io.grpc.ForwardingClientCallListener
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServiceDescriptor
import io.grpc.Status
import io.grpc.stub.ClientCalls
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node
import kotlin.reflect.full.memberProperties

class SisyphusTestStepDescriptor(id: UniqueId, val step: TestStep) :
    EngineDescriptor(id, step.name.takeIf { it.isNotBlank() } ?: step.id) , Node<SisyphusTestEngineContext> {

    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        val engine = context.celEngine.fork(context.result)

        for (precondition in step.precondition) {
            if(engine.eval(precondition) == false) {
                return Node.SkipResult.skip("Due to precondition '$precondition' failed, skip step '$displayName' in case '${parent.get().displayName}'")
            }
        }

        return Node.SkipResult.doNotSkip()
    }

    @OptIn(InternalProtoApi::class)
    override fun execute(
        context: SisyphusTestEngineContext,
        dynamicTestExecutor: Node.DynamicTestExecutor
    ): SisyphusTestEngineContext {
        val channel = context.channel(step.authority)
        val serviceName = step.method.substringBefore('/')
        val methodName = step.method.substringAfter('/')
        val service = ProtoTypes.findServiceSupport(".$serviceName")
        val descriptor = service.javaClass.kotlin.memberProperties.first {
            it.name == "serviceDescriptor"
        }.get(service) as ServiceDescriptor
        val method = descriptor.methods.first { it.bareMethodName == methodName } as MethodDescriptor<Message<*, *>, Message<*, *>>

        val prepareEngine = context.celEngine.fork(context.result)
        val case = (parent.get() as SisyphusTestCaseDescriptor).case
        val headers = Metadata()
        for ((k, v) in case.metadata) {
            headers.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
        }
        for ((k, v) in step.metadata) {
            headers.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
        }
        for (cel in step.metadataScript) {
            val map = prepareEngine.eval(cel) as? Map<*, *> ?: continue
            for ((k, v) in map) {
                if(k is String && v is String) {
                    headers.put(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER), v)
                }
            }
        }
        val input = step.input?.cloneMutable()?.apply {
            for (cel in step.inputScript) {
                val part = prepareEngine.eval(cel) as? Message<*, *> ?: continue
                if(part.javaClass == this.javaClass) {
                    copyFrom(part)
                }
            }
        }

        val callContext = CallContext(headers, input as Message<*, *>)
        val output = ClientCalls.blockingUnaryCall(
            ClientInterceptors.interceptForward(channel, CallContextInterceptor(callContext))
            , method, CallOptions.DEFAULT, input)
        callContext.output = output

        context.result(step, TestResult {
            this.id = step.id
            this.name = step.name
            this.method = step.method
            callContext.status?.code?.value()?.let { this.status = it }
            callContext.status?.description?.let { this.message = it }
            this.input = callContext.input
            this.output = callContext.output
            this.headers += callContext.headers.keys().map {
                it to callContext.headers[Metadata.Key.of(it, Metadata.ASCII_STRING_MARSHALLER)]!!
            }
            this.trailers += callContext.trailers!!.keys().map {
                it to callContext.headers[Metadata.Key.of(it, Metadata.ASCII_STRING_MARSHALLER)]!!
            }
        })

        return context
    }

    override fun getType(): TestDescriptor.Type {
        return TestDescriptor.Type.TEST
    }

    override fun getExecutionMode(): Node.ExecutionMode {
        return Node.ExecutionMode.SAME_THREAD
    }

    companion object {
        const val SEGMENT_TYPE = "steps"
    }
}

class CallContextInterceptor(private val context: CallContext) : ClientInterceptor {
    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return CallWithHeader(next.newCall(method, callOptions), context)
    }

    class CallWithHeader<T1, T2>(delegate: ClientCall<T1, T2>, private val context: CallContext): ForwardingClientCall.SimpleForwardingClientCall<T1, T2>(delegate) {
        override fun start(responseListener: Listener<T2>, headers: Metadata) {
            headers.merge(context.headers)
            super.start(StatusListener(responseListener, context), headers)
        }
    }

    class StatusListener<T>(delegate: ClientCall.Listener<T>, private val context: CallContext): ForwardingClientCallListener.SimpleForwardingClientCallListener<T>(delegate) {
        override fun onClose(status: Status, trailers: Metadata) {
            context.status = status
            context.trailers = trailers
            super.onClose(status, trailers)
        }
    }
}

data class CallContext(
    val headers: Metadata,
    val input: Message<*, *>,
    var status: Status? = null,
    var trailers: Metadata? = null,
    var output: Message<*, *>? = null
)