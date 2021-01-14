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

import java.util.Stack

/**
 * Exception thrown if there is a validation problem with a path template, http config, or related
 * framework methods. Comes as an illegal argument exception subclass. Allows to globally set a
 * thread-local validation context description which each exception inherits.
 */
class ValidationException(format: String, vararg args: Any) : IllegalArgumentException(message(contextLocal.get(), format, *args)) {
    interface Supplier<T> {
        fun get(): T
    }

    companion object {
        private val contextLocal = ThreadLocal<Stack<Supplier<String>>>()

        /**
         * Sets the validation context description. Each thread has its own description, so this is thread
         * safe.
         */
        fun pushCurrentThreadValidationContext(supplier: Supplier<String>) {
            var stack: Stack<Supplier<String>>? = contextLocal.get()
            if (stack == null) {
                stack = Stack()
                contextLocal.set(stack)
            }
            stack.push(supplier)
        }

        fun pushCurrentThreadValidationContext(context: String) {
            pushCurrentThreadValidationContext(
                    object : Supplier<String> {
                        override fun get(): String {
                            return context
                        }
                    })
        }

        /**
         * Clears the validation context.
         */
        fun popCurrentThreadValidationContext() {
            val stack = contextLocal.get()
            stack?.pop()
        }

        private fun message(context: Stack<Supplier<String>>?, format: String, vararg args: Any): String {
            if (context == null || context.isEmpty()) {
                return String.format(format, *args)
            }
            val result = StringBuilder()
            for (supplier in context) {
                result.append(supplier.get() + ": ")
            }
            return result.toString() + String.format(format, *args)
        }
    }
}
