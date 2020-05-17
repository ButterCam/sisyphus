package com.bybutter.sisyphus.dto

@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.ANNOTATION_CLASS)
annotation class PropertyHooks(vararg val hooks: PropertyHook)
