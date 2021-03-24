package com.bybutter.sisyphus.string

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    constructor(major: Int, minor: Int) : this(major, minor, 0)

    private val version = versionOf(major, minor, patch)

    private fun versionOf(major: Int, minor: Int, patch: Int): Int {
        return major.shl(16) + minor.shl(8) + patch
    }

    /**
     * Returns the string representation of this version
     */
    override fun toString(): String = "$major.$minor.$patch"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherVersion = (other as? Version) ?: return false
        return this.version == otherVersion.version
    }

    override fun hashCode(): Int = version

    override fun compareTo(other: Version): Int = version - other.version

    /**
     * Returns `true` if this version is not less than the version specified
     * with the provided [major] and [minor] components.
     */
    fun isAtLeast(major: Int, minor: Int): Boolean =
        this.major > major || (
            this.major == major &&
                this.minor >= minor
            )

    /**
     * Returns `true` if this version is not less than the version specified
     * with the provided [major], [minor] and [patch] components.
     */
    fun isAtLeast(major: Int, minor: Int, patch: Int): Boolean =
        this.major > major || (
            this.major == major &&
                (
                    this.minor > minor || this.minor == minor &&
                        this.patch >= patch
                    )
            )

    companion object {
        fun parse(version: String): Version {
            val parts = version.split('.').map { it.toInt() }

            return Version(
                parts.getOrElse(0) { 0 },
                parts.getOrElse(1) { 0 },
                parts.getOrElse(2) { 0 }
            )
        }
    }
}
