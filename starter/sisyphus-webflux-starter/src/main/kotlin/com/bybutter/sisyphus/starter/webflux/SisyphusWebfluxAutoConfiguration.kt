package com.bybutter.sisyphus.starter.webflux

import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.config.EnableWebFlux

@Configuration
@EnableWebFlux
@Import(WebFluxAutoConfiguration.WebFluxConfig::class, CorsConfigurationSourceRegistrar::class)
@ComponentScan(basePackageClasses = [SisyphusWebfluxAutoConfiguration::class])
class SisyphusWebfluxAutoConfiguration
