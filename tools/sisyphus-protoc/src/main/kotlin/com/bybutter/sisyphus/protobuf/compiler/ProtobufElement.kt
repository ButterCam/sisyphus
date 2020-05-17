package com.bybutter.sisyphus.protobuf.compiler

import com.bybutter.sisyphus.reflect.uncheckedCast
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

abstract class ProtobufElement {
    abstract val parent: ProtobufElement

    abstract val kotlinName: String

    abstract val protoName: String

    open val documentation: String = ""

    open val fullKotlinName: String by lazy {
        val parents = mutableListOf<ProtobufElement>()
        var parent = this.parent
        while (parent.parent != parent) {
            parents.add(parent)
            parent = parent.parent
        }

        parents.reverse()
        parents.add(this)
        parents.joinToString(".") { it.kotlinName }
    }

    open val fullProtoName: String by lazy {
        val parents = mutableListOf<ProtobufElement>()
        var parent = this.parent
        while (parent.parent != parent) {
            parents.add(parent)
            parent = parent.parent
        }

        parents.reverse()
        parents.add(this)
        parents.joinToString(".") { it.protoName }
    }

    val children: List<ProtobufElement> get() = internalChildren

    private val internalChildren = mutableListOf<ProtobufElement>()

    inline fun <reified T> findParent(): T? {
        return findParent(T::class.java)
    }

    fun <T> findParent(clazz: Class<T>): T? {
        var parent = this
        while (parent != parent.parent) {
            parent = parent.parent
            if (clazz.isInstance(parent)) {
                return parent.uncheckedCast()
            }
        }

        return null
    }

    inline fun <reified T> ensureParent(): T {
        val t = findParent<T>()
        if (t != null) {
            return t
        }
        throw IllegalStateException("Can't find parent(${T::class}) of '${this.javaClass.kotlin}'.")
    }

    open fun init() {
    }

    open fun prepareGenerating() {
        for (child in children) {
            child.prepareGenerating()
        }
    }

    fun addElements(vararg elements: ProtobufElement) {
        addElements(elements.toList())
    }

    fun addElements(elements: Collection<ProtobufElement>) {
        for (element in elements) {
            addElement(element)
        }
    }

    fun addElement(element: ProtobufElement) {
        element.init()
        internalChildren.add(element)
    }

    protected fun clear() {
        internalChildren.clear()
    }

    fun context(): ProtobufGenerateContext {
        return ensureParent()
    }

    fun getElementByProtoName(rawName: String): ProtobufElement? {
        val namePart = rawName.split(".")
        return (context() as ProtobufElement).getElementByProtoName(namePart)
    }

    fun getTypeNameByProtoName(rawName: String): TypeName {
        return ClassName.bestGuess(getElementByProtoName(rawName)!!.fullKotlinName)
    }

    private fun getElementByProtoName(rawNamePart: List<String>, index: Int = 0): ProtobufElement? {
        val namePart = protoName.split(".")

        if (index + namePart.size > rawNamePart.size) {
            return null
        }

        val sub = rawNamePart.subList(index, index + namePart.size)
        for ((i, s) in sub.withIndex()) {
            if (namePart[i] != s) {
                return null
            }
        }

        if (index + namePart.size == rawNamePart.size) {
            return this
        }

        for (child in this.children) {
            val result = child.getElementByProtoName(rawNamePart, index + namePart.size)
            if (result != null) {
                return result
            }
        }

        return null
    }
}
