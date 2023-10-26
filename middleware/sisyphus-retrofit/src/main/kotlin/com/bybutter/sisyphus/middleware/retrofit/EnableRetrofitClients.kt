package com.bybutter.sisyphus.middleware.retrofit

import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * Enable retrofit in the current service.
 * When the service is started, the classes with [RetrofitClient] annotation under the specified path will be scanned and injected into the current spring container.
 * @param basePackageNames Array<String> Packages that need to be scanned in addition to the default package.
 * @param basePackageClasses Array<KClass<*>> Class as anchor, scan classes with retrofit annotations through the class's package.
 * @param clientClassNames Array<String> Add the specified client to spring container by class name. Please note that,These classes must used [RetrofitClient] annotation.
 * @param clientClasses Array<KClass<*>> Add the specified client to spring container by class. Please note that,These classes must used [RetrofitClient] annotation.
 * */
@Import(RetrofitClientsRegistrar::class)
annotation class EnableRetrofitClients(
    val basePackageNames: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = [],
    val clientClassNames: Array<String> = [],
    val clientClasses: Array<KClass<*>> = [],
)
