/*
 * Copyright 2016, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.bybutter.sisyphus.api.resource

import java.util.Objects

/**
 * Class for representing and working with resource names.
 *
 *
 *
 * A resource name is represented by [PathTemplate], an assignment to variables in the
 * template, and an optional endpoint. The `ResourceName` class implements the map interface
 * (unmodifiable) to work with the variable assignments, and has methods to reproduce the string
 * representation of the name, to construct new names, and to dereference names into resources.
 *
 *
 *
 * As a resource name essentially represents a match of a path template against a string, it can be
 * also used for other purposes than naming resources. However, not all provided methods may make
 * sense in all applications.
 *
 *
 *
 * Usage examples:
 *
 *
 * PathTemplate template = PathTemplate.create("shelves/&#42;&#47;books/&#42;");
 * TemplatedResourceName resourceName = TemplatedResourceName.create(template, "shelves/s1/books/b1");
 * assert resourceName.get("$1").equals("b1");
 * assert resourceName.parentName().toString().equals("shelves/s1/books");
 *
 */
class TemplatedResourceName private constructor(
    private val template: PathTemplate,
    private val data: Map<String, String>,
    private val endpoint: String?
) : Map<String, String> by data {

    private val stringRepr: String by lazy {
        template.instantiate(data)
    }

    /**
     * Represents a resource name resolver which can be registered with this class.
     */
    interface Resolver {
        /**
         * Resolves the resource name into a resource by calling the underlying API.
         */
        fun <T> resolve(resourceType: Class<T>, name: TemplatedResourceName, version: String?): T
    }

    override fun toString(): String {
        return stringRepr
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is TemplatedResourceName) {
            return false
        }
        return (template == obj.template &&
            endpoint == obj.endpoint &&
            values == obj.values)
    }

    override fun hashCode(): Int {
        return Objects.hash(template, endpoint, values)
    }

    /**
     * Gets the template associated with this resource name.
     */
    fun template(): PathTemplate {
        return template
    }

    /**
     * Checks whether the resource name has an endpoint.
     */
    fun hasEndpoint(): Boolean {
        return endpoint != null
    }

    /**
     * Returns the endpoint of this resource name, or null if none is defined.
     */
    fun endpoint(): String? {
        return endpoint
    }

    /**
     * Returns a resource name with specified endpoint.
     */
    fun withEndpoint(endpoint: String?): TemplatedResourceName {
        endpoint ?: throw NullPointerException("'endpoint' must be not null")
        return TemplatedResourceName(template, data, endpoint)
    }

    /**
     * Returns the parent resource name. For example, if the name is `shelves/s1/books/b1`, the
     * parent is `shelves/s1/books`.
     */
    fun parentName(): TemplatedResourceName {
        val parentTemplate = template.parentTemplate()
        return TemplatedResourceName(parentTemplate, data, endpoint)
    }

    /**
     * Returns true of the resource name starts with the parent resource name, i.e. is a child of the
     * parent.
     */
    fun startsWith(parentName: TemplatedResourceName): Boolean {
        // TODO: more efficient implementation.
        return toString().startsWith(parentName.toString())
    }

    /**
     * Attempts to resolve a resource name into a resource, by calling the associated API. The
     * resource name must have an endpoint. An optional version can be specified to determine in which
     * version of the API to call.
     */
    fun <T> resolve(resourceType: Class<T>, version: String?): T {
        if (hasEndpoint()) {
            throw IllegalStateException("Resource name must have an endpoint.")
        }
        return resourceNameResolver.resolve<T>(resourceType, this, version)
    }

    companion object {

        // The registered resource name resolver.
        // TODO(wrwg): its a bit spooky to have this static global. Think of ways to
        //  configure this from the outside instead if programmatically (e.g. java properties).
        @Volatile
        private var resourceNameResolver: Resolver = object : Resolver {
            override fun <T> resolve(resourceType: Class<T>, name: TemplatedResourceName, version: String?): T {
                throw IllegalStateException("No resource name resolver is registered in ResourceName class.")
            }
        }

        /**
         * Sets the resource name resolver which is used by the [.resolve] method. By
         * default, no resolver is registered.
         */
        fun registerResourceNameResolver(resolver: Resolver) {
            resourceNameResolver = resolver
        }

        /**
         * Creates a new resource name based on given template and path. The path must match the template,
         * otherwise null is returned.
         *
         * @throws ValidationException if the path does not match the template.
         */
        fun create(template: PathTemplate, path: String): TemplatedResourceName {
            val values = template.match(path)
                ?: throw ValidationException("path '%s' does not match template '%s'", path, template)
            return TemplatedResourceName(template, values, null)
        }

        /**
         * Creates a new resource name from a template and a value assignment for variables.
         *
         * @throws ValidationException if not all variables in the template are bound.
         */
        fun create(template: PathTemplate, values: Map<String, String>): TemplatedResourceName {
            if (!values.keys.containsAll(template.vars())) {
                val unbound = template.vars().toMutableSet()
                unbound.removeAll(values.keys)
                throw ValidationException("unbound variables: %s", unbound)
            }
            return TemplatedResourceName(template, values, null)
        }
    }
}
