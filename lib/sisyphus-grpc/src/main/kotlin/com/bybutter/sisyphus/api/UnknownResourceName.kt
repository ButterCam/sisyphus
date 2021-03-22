package com.bybutter.sisyphus.api

import com.google.api.pathtemplate.PathTemplate

abstract class UnknownResourceName<T : ResourceName>(private val value: String) : ResourceName {
    override fun template(): PathTemplate {
        return unknownTemplate
    }

    override fun singular(): String {
        return support().singular
    }

    override fun plural(): String {
        return support().plural
    }

    override fun toMap(): Map<String, String> {
        return mapOf()
    }

    override fun get(key: String): String? {
        return null
    }

    override fun value(): String {
        return value
    }

    override fun toString(): String {
        return value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        private val unknownTemplate = PathTemplate.create("**")
    }
}
