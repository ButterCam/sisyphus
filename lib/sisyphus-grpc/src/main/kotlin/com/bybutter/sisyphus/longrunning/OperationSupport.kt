package com.bybutter.sisyphus.longrunning

import com.bybutter.sisyphus.api.HttpRule
import com.bybutter.sisyphus.api.ResourceNameSupport
import com.bybutter.sisyphus.protobuf.primitives.Empty
import com.google.api.pathtemplate.PathTemplate

interface OperationSupport {
    val host: String?

    val pathPrefix: String?

    val operationName: ResourceNameSupport<*>

    suspend fun listOperations(input: ListOperationsRequest): ListOperationsResponse

    suspend fun getOperation(input: GetOperationRequest): Operation

    suspend fun deleteOperation(input: DeleteOperationRequest): Empty

    suspend fun cancelOperation(input: CancelOperationRequest): Empty

    suspend fun waitOperation(input: WaitOperationRequest): Operation
}

private fun PathTemplate.listPath(): String? {
    val noVars = this.withoutVars().toString()
    if (!noVars.endsWith("/operations/*")) {
        return null
    }
    return "/{name=${noVars.removeSuffix("/operations/*")}}/operations"
}

private fun PathTemplate.operationPath(): String? {
    return "/{name=${withoutVars()}}"
}

fun OperationSupport.listOperationsRules(): List<HttpRule> {
    return this@listOperationsRules.operationName.patterns.mapNotNull {
        val basePath = it.listPath() ?: return@mapNotNull null
        HttpRule {
            get =
                buildString {
                    this@listOperationsRules.pathPrefix?.let {
                        append(it)
                    }
                    append(basePath)
                }
        }
    }
}

fun OperationSupport.getOperationRules(): List<HttpRule> {
    return this@getOperationRules.operationName.patterns.mapNotNull {
        HttpRule {
            get =
                buildString {
                    this@getOperationRules.pathPrefix?.let {
                        append(it)
                    }
                    append(it.operationPath())
                }
        }
    }
}

fun OperationSupport.deleteOperationRules(): List<HttpRule> {
    return this@deleteOperationRules.operationName.patterns.mapNotNull {
        HttpRule {
            delete =
                buildString {
                    this@deleteOperationRules.pathPrefix?.let {
                        append(it)
                    }
                    append(it.operationPath())
                }
        }
    }
}

fun OperationSupport.cancelOperationRules(): List<HttpRule> {
    return this@cancelOperationRules.operationName.patterns.mapNotNull {
        HttpRule {
            post =
                buildString {
                    this@cancelOperationRules.pathPrefix?.let {
                        append(it)
                    }
                    append(it.operationPath())
                    append(":cancel")
                }
            body = "*"
        }
    }
}
