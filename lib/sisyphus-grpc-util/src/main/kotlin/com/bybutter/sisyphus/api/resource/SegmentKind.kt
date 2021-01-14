package com.bybutter.sisyphus.api.resource

/**
 * Specifies a path segment kind.
 */
internal enum class SegmentKind {
    /** A literal path segment.  */
    LITERAL,

    /** A custom verb. Can only appear at the end of path.  */
    CUSTOM_VERB,

    /** A simple wildcard ('*').  */
    WILDCARD,

    /** A path wildcard ('**').  */
    PATH_WILDCARD,

    /** A field binding start.  */
    BINDING,

    /** A field binding end.  */
    END_BINDING
}
