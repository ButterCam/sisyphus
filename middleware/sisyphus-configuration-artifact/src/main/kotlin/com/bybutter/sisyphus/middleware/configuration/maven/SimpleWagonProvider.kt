package com.bybutter.sisyphus.middleware.configuration.maven

import org.apache.maven.wagon.Wagon
import org.apache.maven.wagon.providers.file.FileWagon
import org.apache.maven.wagon.providers.http.HttpWagon
import org.eclipse.aether.transport.wagon.WagonProvider

class SimpleWagonProvider : WagonProvider {
    override fun release(wagon: Wagon?) {
    }

    override fun lookup(roleHint: String?): Wagon? {
        return when (roleHint) {
            "http", "https" -> HttpWagon()
            "file" -> FileWagon()
            else -> null
        }
    }
}
