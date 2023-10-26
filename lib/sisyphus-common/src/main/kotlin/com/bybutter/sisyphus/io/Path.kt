package com.bybutter.sisyphus.io

import java.io.File

/**
 * Convert path to unix style path, it will replace all '\' to '/'.
 */
fun String.toUnixPath(): String {
    return this.replace('\\', '/')
}

/**
 * Convert path to windows style path, it will replace all '/' to '\'.
 */
fun String.toWindowsPath(): String {
    return this.replace('/', '\\')
}

/**
 * Convert path to current os style path, it will replace all '/' and '\' to [File.separatorChar].
 */
fun String.toPlatformPath(): String {
    return buildString {
        for (ch in this@toPlatformPath) {
            append(
                when (ch) {
                    '\\', '/' -> File.separatorChar
                    else -> ch
                },
            )
        }
    }
}

fun String.replaceExtensionName(
    old: String,
    new: String,
): String {
    val extension = ".$old"
    if (!this.endsWith(extension)) return this
    return this.substring(0, this.length - old.length - 1) + ".$new"
}
