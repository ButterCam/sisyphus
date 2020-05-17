package com.bybutter.sisyphus.middleware.hbase.annotation

@Target(AnnotationTarget.CLASS)
annotation class HTable(val value: String = "", val byteValue: ByteArray = [])
