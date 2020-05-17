package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.reflect.SimpleType
import com.bybutter.sisyphus.reflect.toType
import com.fasterxml.jackson.databind.JavaType

// TODO: improve performance
val JavaType.jvm: SimpleType
    get() {
        return this.toCanonical().toType() as SimpleType
    }
