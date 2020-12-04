package com.bybutter.sisyphus.middleware.hbase.converter

import com.bybutter.sisyphus.data.ByteArrayHashingWrapper
import com.bybutter.sisyphus.data.hashWrapper
import com.bybutter.sisyphus.dto.DtoModel
import com.bybutter.sisyphus.dto.jackson.jvm
import com.bybutter.sisyphus.jackson.beanDescription
import com.bybutter.sisyphus.middleware.hbase.TableModelConverter
import com.bybutter.sisyphus.middleware.hbase.ValueConverter
import com.bybutter.sisyphus.middleware.hbase.annotation.HColumn
import com.bybutter.sisyphus.middleware.hbase.getDefaultValueConverter
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition
import java.util.NavigableMap
import org.apache.hadoop.hbase.client.Result

class DefaultTableModelConverter<T>(private val type: JavaType) : TableModelConverter<T> where T : DtoModel {
    private val beanDescription = type.beanDescription

    override fun convert(value: Result): T {
        return DtoModel(type.jvm) {
            for (it in beanDescription.findProperties()) {
                readStructValue(this, listOf(), it, value.map)
            }
        }
    }

    override fun convertBack(value: T): Map<ByteArray, Map<ByteArray, ByteArray>> {
        val map = mutableMapOf<ByteArrayHashingWrapper, MutableMap<ByteArrayHashingWrapper, ByteArray>>()

        for (it in beanDescription.findProperties()) {
            writeByteArrayValue(value, listOf(), it, map)
        }

        return map.mapValues { it.value.mapKeys { it.key.target } }.mapKeys { it.key.target }
    }

    private fun readStructValue(
        instance: Any,
        pre: List<ByteArray>,
        property: BeanPropertyDefinition,
        map: NavigableMap<ByteArray, NavigableMap<ByteArray, NavigableMap<Long, ByteArray>>>
    ) {
        val returnType = property.getter.type
        val propertyColumnInfo = property.getter?.getAnnotation(HColumn::class.java)
        val classColumnInfo = returnType.rawClass.annotations.firstOrNull { it is HColumn } as? HColumn

        val name = (propertyColumnInfo?.byteName)?.let { return@let if (it.isEmpty()) null else it }
                ?: (propertyColumnInfo?.name?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
                ?: (classColumnInfo?.byteName)?.let { return@let if (it.isEmpty()) null else it }
                ?: (classColumnInfo?.name?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
                ?: property.name.toByteArray()
        val qualifier = (propertyColumnInfo?.byteQualifier)?.let { return@let if (it.isEmpty()) null else it }
                ?: (propertyColumnInfo?.qualifier?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
        val converterType = (propertyColumnInfo?.converter ?: classColumnInfo?.converter)?.let {
            return@let if (it.java.isInterface) {
                null
            } else {
                it
            }
        }
        val converter: ValueConverter<Any>? = (converterType?.java?.newInstance()
                ?: getDefaultValueConverter<T>(returnType) as? ValueConverter<*>) as? ValueConverter<Any>

        if (qualifier != null && pre.isNotEmpty()) {
            throw IllegalArgumentException("Set qualifier for three level property.")
        }
        if (pre.size > 1) {
            throw IllegalArgumentException("Set qualifier for three level property.")
        }

        if (qualifier != null) {
            converter ?: throw IllegalArgumentException("Can't convert byte array to type '${returnType.typeName}'.")
            property.setter.callOnWith(
                    instance,
                    map[name]?.get(qualifier)?.firstEntry()?.value?.let { converter.convertBack(it) })
            return
        }

        if (pre.isNotEmpty()) {
            converter ?: throw IllegalArgumentException("Can't convert byte array to type '${returnType.typeName}'.")
            property.setter.callOnWith(
                    instance,
                    map[pre[0]]?.get(name)?.firstEntry()?.value?.let { converter.convertBack(it) })
            return
        }

        property.setter.callOnWith(instance, DtoModel(returnType.jvm) {
            for (it in returnType.beanDescription.findProperties()) {
                readStructValue(this, pre + name, it, map)
            }
        })
    }

    private fun writeByteArrayValue(
        instance: Any,
        pre: List<ByteArray>,
        property: BeanPropertyDefinition,
        map: MutableMap<ByteArrayHashingWrapper, MutableMap<ByteArrayHashingWrapper, ByteArray>>
    ) {
        val returnType = property.getter.type
        val propertyColumnInfo = property.getter?.getAnnotation(HColumn::class.java)
        val classColumnInfo = returnType.rawClass.annotations.firstOrNull { it is HColumn } as? HColumn

        val name = (propertyColumnInfo?.byteName)?.let { return@let if (it.isEmpty()) null else it }
                ?: (propertyColumnInfo?.name?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
                ?: (classColumnInfo?.byteName)?.let { return@let if (it.isEmpty()) null else it }
                ?: (classColumnInfo?.name?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
                ?: property.name.toByteArray()
        val qualifier = (propertyColumnInfo?.byteQualifier)?.let { return@let if (it.isEmpty()) null else it }
                ?: (propertyColumnInfo?.qualifier?.toByteArray())?.let { return@let if (it.isEmpty()) null else it }
        val converterType = (propertyColumnInfo?.converter ?: classColumnInfo?.converter)?.let {
            return@let if (it.java.isInterface) {
                null
            } else {
                it
            }
        }
        val converter: ValueConverter<Any>? = (converterType?.java?.newInstance()
                ?: getDefaultValueConverter<T>(returnType) as? ValueConverter<*>) as? ValueConverter<Any>

        if (qualifier != null && pre.isNotEmpty()) {
            throw IllegalArgumentException("Set qualifier for three level property.")
        }
        if (pre.size > 1) {
            throw IllegalArgumentException("Set qualifier for three level property.")
        }

        if (qualifier != null) {
            converter ?: throw IllegalArgumentException("Can't convert byte array to type '${returnType.typeName}'.")
            property.getter.callOn(instance)?.let {
                map.trySet(name.hashWrapper(), qualifier.hashWrapper(), converter.convert(it))
            }
            return
        }

        if (pre.isNotEmpty()) {
            converter ?: throw IllegalArgumentException("Can't convert byte array to type '${returnType.typeName}'.")
            property.getter.callOn(instance)?.let {
                map.trySet(pre[0].hashWrapper(), name.hashWrapper(), converter.convert(it))
            }
            return
        }

        property.getter.callOn(instance)?.apply {
            for (it in returnType.beanDescription.findProperties()) {
                writeByteArrayValue(this, listOf(name), it, map)
            }
        }
    }

    private fun <T1, T2, T3> MutableMap<T1, MutableMap<T2, T3>>.trySet(key1: T1, key2: T2, value: T3?) {
        value ?: return
        if (this.containsKey(key1)) {
            this[key1]?.set(key2, value)
        } else {
            this[key1] = mutableMapOf(key2 to value)
        }
    }
}
