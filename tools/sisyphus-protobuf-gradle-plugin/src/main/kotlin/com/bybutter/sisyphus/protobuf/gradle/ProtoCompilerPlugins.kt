package com.bybutter.sisyphus.protobuf.gradle

import com.bybutter.sisyphus.protobuf.compiler.CodeGenerator
import com.bybutter.sisyphus.protobuf.compiler.GroupedGenerator
import com.bybutter.sisyphus.protobuf.compiler.SortableGenerator
import com.bybutter.sisyphus.protobuf.compiler.core.state.GeneratingState
import com.bybutter.sisyphus.reflect.instance
import kotlin.reflect.KClass

data class ProtoCompilerPlugins(
    val buildInPlugins: MutableSet<BuildInPlugin> = mutableSetOf(),
    val plugins: MutableList<CodeGenerator<*>> = mutableListOf()
) {
    fun spi(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.GENERATORS_FROM_SPI
        return this
    }

    fun liteDescriptor(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.LITE_DESCRIPTOR_GENERATOR
        return this
    }

    fun inlineDescriptor(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.INLINE_DESCRIPTOR_GENERATOR
        return this
    }

    fun basic(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.BASIC_GENERATOR
        return this
    }

    fun coroutine(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.COROUTINE_SERVICE_GENERATOR
        return this
    }

    fun separatedCoroutine(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.SEPARATED_COROUTINE_SERVICE_GENERATOR
        return this
    }

    fun rxJava(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.RXJAVA_SERVICE_GENERATOR
        return this
    }

    fun separatedRxJava(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.SEPARATED_RXJAVA_SERVICE_GENERATOR
        return this
    }

    fun resourceName(): ProtoCompilerPlugins {
        buildInPlugins += BuildInPlugin.RESOURCE_NAME_GENERATOR
        return this
    }

    inline fun <reified T : GeneratingState<*, *>> inline(noinline block: (T) -> Unit) {
        plugin(object : CodeGenerator<T> {
            override fun generate(state: T): Boolean {
                block(state)
                return true
            }
        })
    }

    inline fun <reified T : GeneratingState<*, *>> inline(order: Int, noinline block: (T) -> Unit) {
        plugin(object : SortableGenerator<T> {
            override val order: Int get() = order

            override fun generate(state: T): Boolean {
                block(state)
                return true
            }
        })
    }

    inline fun <reified T : GeneratingState<*, *>> inline(group: String, noinline block: (T) -> Unit) {
        plugin(object : GroupedGenerator<T> {
            override val group: String
                get() = group

            override fun generate(state: T): Boolean {
                block(state)
                return true
            }
        })
    }

    inline fun <reified T : GeneratingState<*, *>> inline(group: String, order: Int, noinline block: (T) -> Unit) {
        plugin(object : GroupedGenerator<T>, SortableGenerator<T> {
            override val group: String
                get() = group

            override val order: Int get() = order

            override fun generate(state: T): Boolean {
                block(state)
                return true
            }
        })
    }

    fun plugin(plugin: BuildInPlugin) {
        buildInPlugins += plugin
    }

    fun plugin(className: String) {
        plugin(Class.forName(className) as Class<CodeGenerator<*>>)
    }

    fun plugin(clazz: KClass<out CodeGenerator<*>>) {
        plugin(clazz.java)
    }

    fun plugin(clazz: Class<out CodeGenerator<*>>) {
        plugin(clazz.instance())
    }

    fun plugin(generator: CodeGenerator<*>) {
        plugins += generator
    }

    companion object {
        val default = ProtoCompilerPlugins().basic().coroutine().resourceName()
    }
}
