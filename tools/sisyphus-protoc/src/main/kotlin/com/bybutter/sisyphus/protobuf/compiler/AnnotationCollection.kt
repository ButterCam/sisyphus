package com.bybutter.sisyphus.protobuf.compiler

import com.squareup.kotlinpoet.AnnotationSpec

class AnnotationCollection(private val annotations: MutableMap<String, AnnotationSpec> = mutableMapOf()) : Collection<AnnotationSpec> by annotations.values {
    fun addAnnotation(annotation: AnnotationSpec) {
        annotations[annotation.className.toString()] = annotation
    }
}
