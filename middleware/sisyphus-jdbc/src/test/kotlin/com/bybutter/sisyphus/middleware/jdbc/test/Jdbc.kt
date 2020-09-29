package com.bybutter.sisyphus.middleware.jdbc.test

import org.springframework.beans.factory.annotation.Qualifier

object Jdbc {
    @Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
    @Qualifier
    annotation class Test
}
