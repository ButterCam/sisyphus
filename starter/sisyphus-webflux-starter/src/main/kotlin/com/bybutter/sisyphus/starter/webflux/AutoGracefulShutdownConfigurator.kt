package com.bybutter.sisyphus.starter.webflux

import java.util.Properties
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.web.server.Shutdown
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertiesPropertySource

class AutoGracefulShutdownConfigurator : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (environment.containsProperty("server.shutdown")) return

        environment.propertySources.addLast(PropertiesPropertySource("AutoGracefulShutdownConfiguration", Properties().apply {
            this["server.shutdown"] = Shutdown.GRACEFUL.name
        }))
    }
}
