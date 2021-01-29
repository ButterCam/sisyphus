package com.bybutter.sisyphus.starter.grpc.kubernetes

import org.springframework.context.annotation.Import

/**
 * @param discoveryName name of kubernetes service discovery (required)
 * @param override when the value is true, the old label will be overridden, default false
 * @param services Array<String> the name of services which need to enable service discovery in kubernetes.
 * Empty list for all supported services.
 */
@Import(KubernetesLabelAutoRegister::class)
annotation class EnableKubernetesServiceDiscovery(val discoveryName: String, val override: Boolean = false, val services: Array<String> = [])
