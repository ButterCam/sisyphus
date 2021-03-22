package com.bybutter.sisyphus.test

import org.junit.jupiter.params.provider.ArgumentsSource

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(JsonFileArgumentsProvider::class)
annotation class JsonFileSource(vararg val resources: String)
