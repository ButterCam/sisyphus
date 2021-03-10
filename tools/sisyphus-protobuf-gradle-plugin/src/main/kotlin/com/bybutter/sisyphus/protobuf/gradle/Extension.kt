package com.bybutter.sisyphus.protobuf.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

val Project.android: BaseExtension
    get() = project.extensions.getByType(BaseExtension::class.java)

val Project.isAndroid: Boolean
    get() = project.extensions.findByName("android") != null

val Project.sourceSets: SourceSetContainer
    get() {
        return project.extensions.getByType(SourceSetContainer::class.java)
    }

val BaseExtension.variants: DomainObjectSet<out BaseVariant>
    get() {
        return when (this) {
            is AppExtension -> applicationVariants
            is LibraryExtension -> libraryVariants
            else -> throw GradleException("Unsupported android BaseExtension type")
        }
    }
