package com.bybutter.sisyphus.middleware.kafka.serialization

abstract class ProtobufContextInitializer {
    protected var useJson: Boolean = false

    protected fun init(configs: MutableMap<String, *>, isKey: Boolean) {
    }

    companion object {
    }
}
