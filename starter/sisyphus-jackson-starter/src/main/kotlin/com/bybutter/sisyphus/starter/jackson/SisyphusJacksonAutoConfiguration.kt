package com.bybutter.sisyphus.starter.jackson

import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.http.codec.CodecsAutoConfiguration
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@AutoConfigureBefore(JacksonAutoConfiguration::class, CodecsAutoConfiguration::class)
@Import(JacksonAutoRegister::class)
@ComponentScan(basePackageClasses = [SisyphusJacksonAutoConfiguration::class])
class SisyphusJacksonAutoConfiguration
