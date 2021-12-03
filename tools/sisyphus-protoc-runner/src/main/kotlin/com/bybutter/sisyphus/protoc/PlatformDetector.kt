package com.bybutter.sisyphus.protoc

import java.util.Locale

object PlatformDetector {
    fun detect(): Platform {
        val properties = System.getProperties()
        val osName = properties.getProperty("os.name").normalizeOs()
        val osArch = properties.getProperty("os.arch").normalizeArch()
        val osVersion = properties.getProperty("os.version")
        return Platform(osName, osArch, osVersion, "$osName-$osArch")
    }

    private fun String.normalizeOs(): String {
        val osName = this.normalize()
        if (osName.startsWith("aix")) {
            return "aix"
        }
        if (osName.startsWith("hpux")) {
            return "hpux"
        }
        if (osName.startsWith("os400")) {
            if (osName.length <= 5 || !Character.isDigit(osName.get(5))) {
                return "os400"
            }
        }
        if (osName.startsWith("linux")) {
            return "linux"
        }
        if (osName.startsWith("macosx") || osName.startsWith("osx")) {
            return "osx"
        }
        if (osName.startsWith("freebsd")) {
            return "freebsd"
        }
        if (osName.startsWith("openbsd")) {
            return "openbsd"
        }
        if (osName.startsWith("netbsd")) {
            return "netbsd"
        }
        if (osName.startsWith("solaris") || osName.startsWith("sunos")) {
            return "sunos"
        }
        if (osName.startsWith("windows")) {
            return "windows"
        }
        return "unknown"
    }

    private fun String.normalizeArch(): String {
        val osArch = this.normalize()
        if (osArch.matches("^(x8664|amd64|ia32e|em64t|x64)$".toRegex())) {
            return "x86_64"
        }
        if (osArch.matches("^(x8632|x86|i[3-6]86|ia32|x32)$".toRegex())) {
            return "x86_32"
        }
        if (osArch.matches("^(ia64|itanium64)$".toRegex())) {
            return "itanium_64"
        }
        if (osArch.matches("^(sparc|sparc32)$".toRegex())) {
            return "sparc_32"
        }
        if (osArch.matches("^(sparcv9|sparc64)$".toRegex())) {
            return "sparc_64"
        }
        if (osArch.matches("^(arm|arm32)$".toRegex())) {
            return "arm_32"
        }
        if ("aarch64" == osArch) {
            return "aarch_64"
        }
        if (osArch.matches("^(ppc|ppc32)$".toRegex())) {
            return "ppc_32"
        }
        if ("ppc64" == osArch) {
            return "ppc_64"
        }
        if ("ppc64le" == osArch) {
            return "ppcle_64"
        }
        if ("s390" == osArch) {
            return "s390_32"
        }
        if ("s390x" == osArch) {
            return "s390_64"
        }
        return "unknown"
    }

    private fun String.normalize(): String {
        return this.lowercase(Locale.US).replace("[^a-z0-9]+".toRegex(), "")
    }
}

data class Platform(val osName: String, val osArch: String, val osVersion: String, val osClassifier: String) {
    override fun toString(): String {
        return "$osClassifier/$osVersion"
    }
}
