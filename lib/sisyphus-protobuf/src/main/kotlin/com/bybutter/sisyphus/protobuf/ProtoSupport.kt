package com.bybutter.sisyphus.protobuf

interface ProtoSupport<T> {
    val name: String
    val parent: ProtoSupport<*>
    val descriptor: T

    fun file(): FileSupport {
        var support: ProtoSupport<*> = this
        while (support !is FileSupport) {
            support = support.parent
        }
        return support
    }

    fun children(): Array<ProtoSupport<*>> {
        return arrayOf()
    }
}
