package com.bybutter.sisyphus.test

internal fun TestStep.extractContextTo(map: MutableMap<String, Any?>) {
    map["id"] = id
    map["name"] = name
    map["authority"] = authority
    map["method"] = method
    map["optional"] = optional
    map["input"] = input
    map["metadata"] = metadata
}