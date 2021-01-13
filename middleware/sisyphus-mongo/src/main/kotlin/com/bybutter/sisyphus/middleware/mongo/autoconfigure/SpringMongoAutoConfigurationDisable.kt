package com.bybutter.sisyphus.middleware.mongo.autoconfigure

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata

class SpringMongoAutoConfigurationDisable : AutoConfigurationImportFilter {
    override fun match(
        autoConfigurationClasses: Array<out String?>,
        autoConfigurationMetadata: AutoConfigurationMetadata
    ): BooleanArray {
        return autoConfigurationClasses.map {
            it == null || !it.startsWith("org.springframework.boot.autoconfigure.mongo.")
        }.toBooleanArray()
    }
}
