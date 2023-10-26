package com.bybutter.sisyphus.test.descriptor

import com.bybutter.sisyphus.protobuf.InternalProtoApi
import com.bybutter.sisyphus.protobuf.Message
import com.bybutter.sisyphus.protobuf.MutableMessage
import com.bybutter.sisyphus.protobuf.ProtoTypes
import com.bybutter.sisyphus.protobuf.findServiceSupport
import com.bybutter.sisyphus.protobuf.primitives.toTime
import com.bybutter.sisyphus.reflect.uncheckedCast
import com.bybutter.sisyphus.test.CallContext
import com.bybutter.sisyphus.test.SisyphusTestCaseContext
import com.bybutter.sisyphus.test.SisyphusTestEngineContext
import com.bybutter.sisyphus.test.SisyphusTestStepContext
import com.bybutter.sisyphus.test.TestStep
import com.bybutter.sisyphus.test.extension.AfterTestStep
import com.bybutter.sisyphus.test.extension.BeforeTestStep
import com.bybutter.sisyphus.test.extensions
import com.bybutter.sisyphus.test.mergeFrom
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
import io.grpc.StatusException
import io.grpc.stub.ClientCalls
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.hierarchical.Node
import org.opentest4j.AssertionFailedError
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.memberProperties

class SisyphusTestStepDescriptor(id: UniqueId, private val step: TestStep) :
    EngineDescriptor(id, step.name), Node<SisyphusTestEngineContext> {
    override fun shouldBeSkipped(context: SisyphusTestEngineContext): Node.SkipResult {
        val engine = context.cel().fork(context.results())
        for (precondition in step.precondition) {
            if (engine.eval(precondition) == false) {
                return Node.SkipResult.skip(
                    "Due to precondition '$precondition' failed, skip step '$displayName' in case '${parent.get().displayName}'",
                )
            }
        }
        return Node.SkipResult.doNotSkip()
    }

    @OptIn(InternalProtoApi::class)
    override fun prepare(context: SisyphusTestEngineContext): SisyphusTestEngineContext {
        val engine = context.cel().fork(context.results())
        val parent = parent.get() as SisyphusTestCaseDescriptor

        val serviceName = step.method.substringBefore('/')
        val methodName = step.method.substringAfter('/')
        val serviceSupport = ProtoTypes.findServiceSupport(".$serviceName")
        val descriptor =
            serviceSupport.javaClass.kotlin.memberProperties.first {
                it.name == "serviceDescriptor"
            }.get(serviceSupport) as ServiceDescriptor
        val methodDescriptor =
            descriptor.methods.firstOrNull { it.bareMethodName == methodName }
                ?: throw IllegalStateException("Method '${step.method}' not found in test'${parent.displayName}.$displayName'.")
        val method =
            serviceSupport.descriptor.method.firstOrNull { it.name == methodName }
                ?: throw IllegalStateException("Method '${step.method}' not found in test'${parent.displayName}.$displayName'.")

        val metadata = parent.case.metadata.toMutableMap()
        metadata += step.metadata
        for (script in step.metadataScript) {
            val result =
                engine.eval(script) as? Map<*, *>
                    ?: throw IllegalStateException(
                        "Metadata cel script '$script' must return a map value in test '${
                            parent.displayName
                        }.$displayName'.",
                    )
            metadata += result.map { it.key.toString() to it.value.toString() }
        }

        var input: MutableMessage<*, *>? = step.input?.cloneMutable()
        if (input != null && method.inputType != input.type()) {
            throw IllegalStateException(
                "Method '${step.method}' need '${method.inputType}' as input, but '${
                    input.type()
                }' provided in test '${parent.displayName}.$displayName'.",
            )
        }
        for (script in step.inputScript) {
            val result =
                engine.eval(script) as? Message<*, *>
                    ?: throw IllegalStateException(
                        "Input cel script '$script' must return a message in test '${
                            parent.displayName
                        }.$displayName'.",
                    )
            if (result.type() != method.inputType) {
                throw IllegalStateException(
                    "Method '${step.method}' need '${method.inputType}' as input, but '${
                        result.type()
                    }' provided in input script of test '${parent.displayName}.$displayName'.",
                )
            }
            if (input != null) {
                input.copyFrom(result)
            } else {
                input = result.cloneMutable()
            }
        }

        val options =
            step.timeout?.let {
                CallOptions.DEFAULT.withDeadlineAfter(it.toTime(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS)
            } ?: CallOptions.DEFAULT

        return (context as SisyphusTestCaseContext).forStep(
            CallContext(
                parent.case,
                step,
                options,
                methodDescriptor.uncheckedCast(),
                Metadata().mergeFrom(metadata),
                input as Message<*, *>,
            ),
        )
    }

    override fun before(context: SisyphusTestEngineContext): SisyphusTestEngineContext {
        context.extensions<BeforeTestStep> { beforeTestStep(context, this@SisyphusTestStepDescriptor) }
        return context
    }

    @OptIn(InternalProtoApi::class)
    override fun execute(
        context: SisyphusTestEngineContext,
        dynamicTestExecutor: Node.DynamicTestExecutor,
    ): SisyphusTestEngineContext {
        val stepContext = context as SisyphusTestStepContext
        val engine = context.cel().fork(context.results())

        var retryCount = step.retryCount
        while (retryCount >= 0) {
            retryCount--
            try {
                executeInternal(stepContext)
            } catch (e: Throwable) {
                if (retryCount < 0) throw e
                val retryEngine = engine.fork(context.callContext.toCelContext())
                for (condition in step.retryCondition) {
                    val result = retryEngine.eval(condition)
                    if (result != true) {
                        throw e
                    }
                }
                continue
            }
        }
        context.record(context.callContext.toTestResult())
        return context
    }

    override fun after(context: SisyphusTestEngineContext) {
        context.extensions<AfterTestStep> { afterTestStep(context, this@SisyphusTestStepDescriptor) }
    }

    private fun executeInternal(context: SisyphusTestStepContext) {
        val channel = context.channel(step.authority)
        val engine = context.cel().fork(context.results())
        try {
            ClientCalls.blockingUnaryCall(
                ClientInterceptors.interceptForward(channel, CallContextInterceptor(context.callContext)),
                context.callContext.method,
                context.callContext.options,
                context.callContext.input,
            )
        } catch (e: StatusException) {
            if (!step.insensitive) {
                throw e
            }
        }

        val assertEngine = engine.fork(context.callContext.toCelContext())
        for (assert in step.asserts) {
            val result = assertEngine.eval(assert)
            if (result != true) {
                throw AssertionFailedError(
                    "Assertion '$assert' failed in test '${parent.get().displayName}.$displayName'.",
                    true,
                    result,
                )
            }
        }
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
        next: Channel,
    ): ClientCall<ReqT, RespT> {
        return CallWithHeader(next.newCall(method, callOptions), context)
    }

    class CallWithHeader<T1, T2>(delegate: ClientCall<T1, T2>, private val context: CallContext) :
        ForwardingClientCall.SimpleForwardingClientCall<T1, T2>(delegate) {
        override fun start(
            responseListener: Listener<T2>,
            headers: Metadata,
        ) {
            headers.merge(context.headers)
            super.start(StatusListener(responseListener, context), headers)
        }
    }

    class StatusListener<T>(delegate: ClientCall.Listener<T>, private val context: CallContext) :
        ForwardingClientCallListener.SimpleForwardingClientCallListener<T>(delegate) {
        override fun onClose(
            status: Status,
            trailers: Metadata,
        ) {
            context.status = status
            context.trailers = trailers
            super.onClose(status, trailers)
        }

        override fun onMessage(message: T) {
            context.output = message as Message<*, *>
            super.onMessage(message)
        }
    }
}
