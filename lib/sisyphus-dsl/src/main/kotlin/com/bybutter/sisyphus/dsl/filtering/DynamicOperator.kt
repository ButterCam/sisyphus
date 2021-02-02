package com.bybutter.sisyphus.dsl.filtering

import com.bybutter.sisyphus.protobuf.primitives.Duration
import com.bybutter.sisyphus.protobuf.primitives.Timestamp
import com.bybutter.sisyphus.protobuf.primitives.compareTo
import com.bybutter.sisyphus.protobuf.primitives.tryParse
import com.bybutter.sisyphus.string.PathMatcher

object DynamicOperator : Comparator<String> {
    override fun compare(o1: String?, o2: String?): Int {
        val left = if (o1 == "null") null else o1
        val right = if (o2 == "null") null else o2

        val leftDouble = left?.toDoubleOrNull()
        val rightDouble = right?.toDoubleOrNull()

        if (leftDouble != null && rightDouble != null) return leftDouble.compareTo(rightDouble)

        val leftBoolean = when (left) {
            "true" -> true
            "false" -> false
            null -> false
            else -> null
        }
        val rightBoolean = when (right) {
            "true" -> true
            "false" -> false
            null -> false
            else -> null
        }
        if (leftBoolean != null && rightBoolean != null) return leftBoolean.compareTo(rightBoolean)

        val leftDuration = left?.let { Duration.tryParse(it) }
        val rightDuration = right?.let { Duration.tryParse(it) }
        if (leftDuration != null && rightDuration != null) return leftDuration.compareTo(rightDuration)

        val leftTimestamp = left?.let { Timestamp.tryParse(it) }
        val rightTimestamp = right?.let { Timestamp.tryParse(it) }
        if (leftTimestamp != null && rightTimestamp != null) return leftTimestamp.compareTo(rightTimestamp)

        if (left == null && right == null) return 0
        if (left == null && right != null) return -1
        if (left != null && right == null) return 1

        return left!!.compareTo(right!!)
    }

    fun equals(o1: String?, o2: String?): Boolean {
        val left = if (o1 == "null") null else o1
        val right = if (o2 == "null") null else o2

        if (left == null && right == null) return true
        if (left == null || right == null) return false

        val leftDouble = left.toDoubleOrNull()
        val rightDouble = right.toDoubleOrNull()
        if (leftDouble != null && rightDouble != null) return leftDouble == rightDouble

        val leftBoolean = when (left) {
            "true" -> true
            "false" -> false
            null -> false
            else -> null
        }
        val rightBoolean = when (right) {
            "true" -> true
            "false" -> false
            null -> false
            else -> null
        }
        if (leftBoolean != null && rightBoolean != null) return leftBoolean == rightBoolean

        val leftDuration = Duration.tryParse(left)
        val rightDuration = Duration.tryParse(right)
        if (leftDuration != null && rightDuration != null) return leftDuration.compareTo(rightDuration) == 0

        val leftTimestamp = Timestamp.tryParse(left)
        val rightTimestamp = Timestamp.tryParse(right)
        if (leftTimestamp != null && rightTimestamp != null) return leftTimestamp.compareTo(rightTimestamp) == 0

        return PathMatcher.match(right, left)
    }
}
