package com.bybutter.sisyphus.dto.jackson

import com.bybutter.sisyphus.dto.MetaProperty
import com.fasterxml.jackson.databind.introspect.AnnotatedMember
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector

internal object DtoAnnotationIntrospector : JacksonAnnotationIntrospector() {
    override fun hasIgnoreMarker(m: AnnotatedMember): Boolean {
        if (_findAnnotation(m, MetaProperty::class.java) != null) {
            return true
        }

        return super.hasIgnoreMarker(m)
    }
}
