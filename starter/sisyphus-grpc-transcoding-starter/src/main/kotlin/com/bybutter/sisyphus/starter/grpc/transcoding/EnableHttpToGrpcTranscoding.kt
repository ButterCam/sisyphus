package com.bybutter.sisyphus.starter.grpc.transcoding

import com.bybutter.sisyphus.starter.grpc.transcoding.swagger.authentication.DefaultSwaggerConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

/**
 * Enable HTTP/json to gRPC/protobuf transcoding in current gRPC server.
 * @property services Array<String> the name of services which need to enable CORS in transcoding.
 * Empty list for all supported services.
 */
@ComponentScan(basePackageClasses = [EnableHttpToGrpcTranscoding::class])
@Import(DefaultSwaggerConfig::class, GrpcTranscodingConfig::class)
annotation class EnableHttpToGrpcTranscoding(val services: Array<String> = [])
