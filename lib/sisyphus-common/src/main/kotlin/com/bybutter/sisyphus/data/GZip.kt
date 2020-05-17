package com.bybutter.sisyphus.data

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Compress data with gZip algorithm.
 */
fun ByteArray.gzip(): ByteArray {
    ByteArrayOutputStream().use {
        GZIPOutputStream(it).use { gzip ->
            gzip.write(this)
        }
        it.flush()
        return it.toByteArray()
    }
}

/**
 * Decompress data with gZip algorithm.
 */
fun ByteArray.ungzip(): ByteArray {
    ByteArrayInputStream(this).use {
        GZIPInputStream(it).use { gzip ->
            return gzip.readBytes()
        }
    }
}
