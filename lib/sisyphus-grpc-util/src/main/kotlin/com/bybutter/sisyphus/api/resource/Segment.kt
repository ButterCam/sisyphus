package com.bybutter.sisyphus.api.resource

/**
 * Specifies a path segment.
 */
internal data class Segment(
    /**
 * The path segment kind.
 */
val kind: SegmentKind,
    /**
 * The value for the segment. For literals, custom verbs, and wildcards, this reflects the value
 * as it appears in the template. For bindings, this represents the variable of the binding.
 */
val value: String
) {

    /**
     * Returns true of this segment is one of the wildcards,
     */
    val isAnyWildcard: Boolean
        get() {
            return kind == SegmentKind.WILDCARD || kind == SegmentKind.PATH_WILDCARD
        }

    fun separator(): String {
        return when (kind) {
            SegmentKind.CUSTOM_VERB -> ":"
            SegmentKind.END_BINDING -> ""
            else -> "/"
        }
    }

    companion object {
        val EMPTY = Segment(SegmentKind.LITERAL, "")

        /**
         * A constant for the WILDCARD segment.
         */
        val WILDCARD = Segment(SegmentKind.WILDCARD, "*")

        /**
         * A constant for the PATH_WILDCARD segment.
         */
        val PATH_WILDCARD = Segment(SegmentKind.PATH_WILDCARD, "**")

        /**
         * A constant for the END_BINDING segment.
         */
        val END_BINDING = Segment(SegmentKind.END_BINDING, "")
    }
}
