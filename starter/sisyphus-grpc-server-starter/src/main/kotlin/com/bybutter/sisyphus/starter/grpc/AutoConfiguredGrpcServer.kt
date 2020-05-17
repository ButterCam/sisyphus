package com.bybutter.sisyphus.starter.grpc

import org.springframework.beans.factory.annotation.Qualifier

@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Qualifier(ServiceRegistrar.QUALIFIER_AUTO_CONFIGURED_GRPC_SERVER)
annotation class AutoConfiguredGrpcServer
