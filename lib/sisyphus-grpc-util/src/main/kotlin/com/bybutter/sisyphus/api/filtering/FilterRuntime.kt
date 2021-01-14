package com.bybutter.sisyphus.api.filtering

class FilterRuntime(vararg functions: Pair<String, FilterFunction>) {
    private val functions = mutableMapOf<String, FilterFunction>(*functions)

    fun registerFunction(name: String, function: FilterFunction) {
        functions[name] = function
    }

    fun invoke(function: String, arguments: List<Any?>): Any? {
        val func = functions[function]
            ?: throw NoSuchMethodException("Method '$function()' not registered in filter expression runtime.")
        return func.invoke(arguments)
    }
}
